/* 
 * ========================================================================
 * 
 * Copyright 2004 Vincent Massol.
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
package org.codehaus.cargo.module.webapp.tomcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.cargo.module.JarArchive;
import org.codehaus.cargo.module.JarArchiveIo;
import org.jdom.JDOMException;

/**
 * Class that encapsulates access to a WAR.
 * 
 * @version $Id$
 */
public class TomcatWarArchive
{
    /**
     * The file representing either the WAR file or the expanded WAR directory.
     */
    private String warFile;
    
    /**
     * The parsed deployment descriptor.
     */
    private TomcatContextXml tomcatContextXml;

    /**
     * Constructor.
     * 
     * @param warFile The web application archive
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not
     *         be parsed
     */
    public TomcatWarArchive(String warFile)
        throws IOException, JDOMException
    {
        this.warFile = warFile;
        this.tomcatContextXml = parseTomcatContextXml();
    }

    /**
     * @return the parsed <code>META-INF/context.xml</code> descriptor or null
     *         if none exists 
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not
     *         be parsed
     */
    private TomcatContextXml parseTomcatContextXml()
        throws IOException, JDOMException
    {
        TomcatContextXml context = null;

        InputStream in = null;
        try
        {
            // Are we manipulating a WAR file or an expanded WAR directory?
            if (new File(this.warFile).isDirectory())
            {
                File contextXmlFile = new File(this.warFile, "META-INF/context.xml");
                if (contextXmlFile.exists())
                {
                    in = new FileInputStream(contextXmlFile);
                }
            }
            else
            {
                JarArchive jarArchive = JarArchiveIo.open(new File(this.warFile));
                in = jarArchive.getResource("META-INF/context.xml");
            }


            if (in != null)
            {
                context = TomcatContextXmlIo.parseTomcatConfigXml(in);
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }

        return context;
    }
    
    /**
     * Returns the <code>META-INF/context.xml</code> deployment descriptor of the web application.
     * 
     * @return The parsed deployment descriptor, or <code>null</code> if no such file exists.
     */
    public final TomcatContextXml getTomcatContextXml()
    {
        return this.tomcatContextXml;
    }

}
