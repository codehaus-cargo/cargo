/*
 * ========================================================================
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
package org.codehaus.cargo.module.ejb;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.cargo.module.DefaultJarArchive;
import org.codehaus.cargo.module.ejb.jboss.JBossXml;
import org.codehaus.cargo.module.ejb.jboss.JBossXmlIo;
import org.codehaus.cargo.module.ejb.orion.OrionEjbJarXml;
import org.codehaus.cargo.module.ejb.orion.OrionEjbJarXmlIo;
import org.codehaus.cargo.module.ejb.weblogic.WeblogicEjbJarXml;
import org.codehaus.cargo.module.ejb.weblogic.WeblogicEjbJarXmlIo;
import org.codehaus.cargo.module.ejb.websphere.IbmEjbJarBndXmi;
import org.codehaus.cargo.module.ejb.websphere.IbmEjbJarBndXmiIo;
import org.jdom.JDOMException;

/**
 * Class that encapsulates access to an EJB JAR.
 * 
 */
public class DefaultEjbArchive extends DefaultJarArchive implements EjbArchive
{
    /**
     * The parsed deployment descriptor.
     */
    private EjbJarXml ejbJarXml;

    /**
     * {@inheritDoc}
     * @see DefaultJarArchive#DefaultJarArchive(String)
     */
    public DefaultEjbArchive(String file)
    {
        super(file);
    }

    /**
     * Constructor.
     * 
     * @param theInputStream The input stream for the enterprise application archive
     * @throws IOException If there was a problem reading the EJB
     */
    public DefaultEjbArchive(InputStream theInputStream) throws IOException
    {
        super(theInputStream);
    }

    /**
     * {@inheritDoc}
     * @see EjbArchive#getEjbJarXml()
     */
    public EjbJarXml getEjbJarXml() throws IOException, JDOMException
    {
        if (this.ejbJarXml == null)
        {
            InputStream in = null;
            try
            {
                in = getResource("META-INF/ejb-jar.xml");
                this.ejbJarXml = EjbJarXmlIo.parseEjbJarXml(in, null);
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
            addJBossDescriptor();
        }
        return this.ejbJarXml;
    }

    /**
     * Associates the ejb-jar.xml with a weblogic-ejb-jar.xml if one is present in the jar.
     * 
     * @throws IOException If there was a problem reading the deployment descriptor in the EJB jar
     * @throws JDOMException If the deployment descriptor of the EJB jar could not be parsed
     */
    private void addWeblogicDescriptor() throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = getResource("META-INF/weblogic-ejb-jar.xml");
            if (in != null)
            {
                WeblogicEjbJarXml descr = WeblogicEjbJarXmlIo.parseWeblogicEjbJarXml(in);
                if (descr != null)
                {
                    this.ejbJarXml.addVendorDescriptor(descr);
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
     * Associates the ejb-jar.xml with orion-ejb-jar.xml if one is present in the war.
     * 
     * @throws IOException If there was a problem reading the deployment descriptor in the JAR
     * @throws JDOMException If the deployment descriptor of the EJB jar could not be parsed
     */
    private void addOracleDescriptor() throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = getResource("META-INF/orion-ejb-jar.xml");
            if (in != null)
            {
                OrionEjbJarXml descr = OrionEjbJarXmlIo.parseOracleEjbJarXml(in);
                if (descr != null)
                {
                    this.ejbJarXml.addVendorDescriptor(descr);
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
     * Associates the ejb-jar.xml with ibm-ejb-jar-bnd.xmi if one is present in the jar.
     * 
     * @throws IOException If there was a problem reading the deployment descriptor in the JAR
     * @throws JDOMException If the deployment descriptor of the EJB jar could not be parsed
     */
    private void addWebsphereDescriptor() throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = getResource("META-INF/ibm-ejb-jar-bnd.xmi");
            if (in != null)
            {
                IbmEjbJarBndXmi descr = IbmEjbJarBndXmiIo.parseIbmEjbJarXmi(in);
                if (descr != null)
                {
                    this.ejbJarXml.addVendorDescriptor(descr);
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
     * Associates the ejb-jar.xml with jboss.xml if one is present in the jar.
     * 
     * @throws IOException If there was a problem reading the deployment descriptor in the JAR
     * @throws JDOMException If there is an issue with JDOM
     */
    private void addJBossDescriptor() throws IOException, JDOMException
    {
        InputStream in = null;
        try
        {
            in = getResource("META-INF/jboss.xml");
            if (in != null)
            {
                JBossXml descr = JBossXmlIo.parseJBossXml(in);
                if (descr != null)
                {
                    this.ejbJarXml.addVendorDescriptor(descr);
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
