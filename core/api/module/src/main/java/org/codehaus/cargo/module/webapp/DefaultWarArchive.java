/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package org.codehaus.cargo.module.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.codehaus.cargo.module.DefaultJarArchive;
import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.JarArchive;
import org.codehaus.cargo.module.webapp.jboss.JBossWebXml;
import org.codehaus.cargo.module.webapp.jboss.JBossWebXmlIo;
import org.codehaus.cargo.module.webapp.orion.OrionWebXml;
import org.codehaus.cargo.module.webapp.orion.OrionWebXmlIo;
import org.codehaus.cargo.module.webapp.resin.ResinWebXml;
import org.codehaus.cargo.module.webapp.resin.ResinWebXmlIo;
import org.codehaus.cargo.module.webapp.weblogic.WeblogicXml;
import org.codehaus.cargo.module.webapp.weblogic.WeblogicXmlIo;
import org.codehaus.cargo.module.webapp.websphere.IbmWebBndXmi;
import org.codehaus.cargo.module.webapp.websphere.IbmWebBndXmiIo;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.jdom.JDOMException;

/**
 * Class that encapsulates access to a WAR.
 * 
 * @version $Id$
 */
public class DefaultWarArchive extends DefaultJarArchive implements WarArchive
{
    /**
     * The parsed deployment descriptor.
     */
    private WebXml webXml;

    /**
     * The filename.
     */
    private String file;

    /**
     * {@inheritDoc}
     * @see DefaultJarArchive#DefaultJarArchive(String)
     */
    public DefaultWarArchive(String file)
    {
        super(file);
        this.file = file;
    }

    /**
     * Constructor.
     * 
     * @param inputStream The input stream for the web application archive
     * @throws java.io.IOException If there was a problem reading the WAR
     */
    public DefaultWarArchive(InputStream inputStream) throws IOException
    {
        super(inputStream);
    }

    /**
     * {@inheritDoc}
     * @throws JDOMException
     * @see WarArchive#getWebXml()
     */
    public WebXml getWebXml() throws IOException, JDOMException
    {
        if (this.webXml == null)
        {
            InputStream in = null;
            try
            {
                in = getResource("WEB-INF/web.xml");
                if (in != null)
                {
                    this.webXml = WebXmlIo.parseWebXml(in, null);
                }
                else
                {
                    // need to create something, as otherwise vendor descriptors
                    // will fail
                    this.webXml = new WebXml();
                }
            }
            catch (Exception ex)
            {
                throw new CargoException("Error parsing the web.xml file in " + file, ex);
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
            }
            addWeblogicDescriptor();
            addOracleDescriptor();
            addWebsphereDescriptor();
            addResinDescriptor();
            addJBossDescriptor();
        }
        return this.webXml;
    }

    /**
     * {@inheritDoc}
     * @throws JDOMException
     * @see WarArchive#store(java.io.File)
     */
    public void store(File warFile) throws IOException, JDOMException
    {
        FileHandler fileHandler = new DefaultFileHandler();
        JarInputStream in = getContentAsStream();
        JarOutputStream out = new JarOutputStream(new FileOutputStream(warFile));

        // Find all deployment descriptors that Cargo is handling for this WAR file.
        List<String> descriptorNames = new ArrayList<String>();
        descriptorNames.add("WEB-INF/" + getWebXml().getFileName());
        for (Descriptor vendorDescriptor : getWebXml().getVendorDescriptors())
        {
            descriptorNames.add("WEB-INF/" + vendorDescriptor.getFileName());
        }

        // Copy all entries from the original WAR file except for deployment descriptors. The
        // reason we do not copy deployment descriptors is because they may have been modified
        // since they were initially read from the original WAR file.
        JarEntry entry;
        while ((entry = in.getNextJarEntry()) != null)
        {
            if (!descriptorNames.contains(entry.getName()))
            {
                out.putNextEntry(entry);
                fileHandler.copy(in, out);
            }
        }
        in.close();

        // Copy the deployment descriptors to the output file. Start by writing the web.xml file
        // and then the vendor descriptors.

        JarEntry webXmlEntry = new JarEntry("WEB-INF/" + getWebXml().getFileName());
        out.putNextEntry(webXmlEntry);
        AbstractDescriptorIo.writeDescriptor(getWebXml(), out, "UTF-8", true);

        for (Descriptor descriptor : getWebXml().getVendorDescriptors())
        {
            JarEntry descriptorEntry = new JarEntry("WEB-INF/" + descriptor.getFileName());
            out.putNextEntry(descriptorEntry);
            AbstractDescriptorIo.writeDescriptor(descriptor, out, "UTF-8", true);
        }

        out.close();
    }

    /**
     * Returns whether a class of the specified name is contained in the web-app archive, either
     * directly in WEB-INF/classes, or in one of the JARs in WEB-INF/lib.
     * 
     * @param className The name of the class to search for
     * 
     * @return Whether the class was found in the archive
     * 
     * @throws java.io.IOException If an I/O error occurred reading the archive
     */
    @Override
    public boolean containsClass(String className) throws IOException
    {
        boolean containsClass = false;

        // Look in WEB-INF/classes first
        String resourceName = "WEB-INF/classes/" + className.replace('.', '/') + ".class";
        if (getResource(resourceName) != null)
        {
            containsClass = true;
        }

        // Next scan the JARs in WEB-INF/lib
        for (String resource : getResources("WEB-INF/lib/"))
        {
            JarArchive jar = new DefaultJarArchive(getResource(resource));
            if (jar.containsClass(className))
            {
                containsClass = true;
            }
        }

        return containsClass;
    }

    /**
     * Associates the webXml with a weblogic.xml if one is present in the war.
     * 
     * @throws IOException If there was a problem reading the deployment descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not be parsed
     */
    private void addWeblogicDescriptor()
        throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = getResource("WEB-INF/weblogic.xml");
            if (in != null)
            {
                WeblogicXml descr = WeblogicXmlIo.parseWeblogicXml(in);
                if (descr != null)
                {
                    this.webXml.addVendorDescriptor(descr);
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }

    /**
     * Associates the webXml with a resin-web if one is present in the war.
     * 
     * @throws IOException If there was a problem reading the deployment descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not be parsed
     */
    private void addResinDescriptor()
        throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = getResource("WEB-INF/resin-web.xml");
            if (in != null)
            {
                ResinWebXml descr = ResinWebXmlIo.parseResinXml(in);
                if (descr != null)
                {
                    this.webXml.addVendorDescriptor(descr);
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }

    /**
     * Associates the webXml with a orion-web.xml if one is present in the war.
     * 
     * @throws IOException If there was a problem reading the deployment descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not be parsed
     */
    private void addOracleDescriptor()
        throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = getResource("WEB-INF/orion-web.xml");
            if (in != null)
            {
                OrionWebXml descr = OrionWebXmlIo.parseOrionXml(in);
                if (descr != null)
                {
                    this.webXml.addVendorDescriptor(descr);
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }

    /**
     * Associates the webXml with a ibm-web-bnd.xmi, if one is present in the war.
     * 
     * @throws IOException If there was a problem reading the deployment descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not be parsed
     */
    private void addWebsphereDescriptor()
        throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = getResource("WEB-INF/ibm-web-bnd.xmi");
            if (in != null)
            {
                IbmWebBndXmi descr = IbmWebBndXmiIo.parseIbmWebBndXmi(in);
                if (descr != null)
                {
                    this.webXml.addVendorDescriptor(descr);
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }

    /**
     * Associates the webXml with a jboss-web.xml, if one is present in the war.
     * 
     * @throws IOException If there was a problem reading the deployment descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not be parsed
     */
    private void addJBossDescriptor()
        throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = getResource("WEB-INF/jboss-web.xml");
            if (in != null)
            {
                JBossWebXml descr = JBossWebXmlIo.parseJBossWebXml(in);
                if (descr != null)
                {
                    this.webXml.addVendorDescriptor(descr);
                }
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }
}
