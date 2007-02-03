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
package org.codehaus.cargo.module.webapp.jboss;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.codehaus.cargo.module.DefaultJarArchive;
import org.xml.sax.SAXException;

/**
 * Class that encapsulates access to a WAR.
 * 
 * @version $Id$
 */
public class JBossWarArchive
{
    /**
     * The file representing either the WAR file or the expanded WAR directory.
     */
    private File warFile;

    /**
     * The parsed deployment descriptor.
     */
    private JBossWebXml jbossWebXml;

    /**
     * Constructor.
     * 
     * @param warFile The web application archive
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    public JBossWarArchive(File warFile)
        throws IOException, SAXException, ParserConfigurationException
    {
        this.warFile = warFile;
        this.jbossWebXml = parseJBossWebXml();
    }

    /**
     * @return the parsed <code>WEB-INF/jboss-web.xml</code> descriptor or null
     *         if none exists 
     * @throws IOException If there was a problem reading the  deployment
     *         descriptor in the WAR
     * @throws SAXException If the deployment descriptor of the WAR could not
     *         be parsed
     * @throws ParserConfigurationException If there is an XML parser
     *         configration problem
     */
    private JBossWebXml parseJBossWebXml()
        throws IOException, SAXException, ParserConfigurationException
    {
        JBossWebXml webXml = null;
        
        InputStream in = null;
        try
        {
            // Are we manipulating a WAR file or an expanded WAR directory?
            if (this.warFile.isDirectory())
            {
                File contextXmlFile = new File(this.warFile, "WEB-INF/jboss-web.xml");
                if (contextXmlFile.exists())
                {
                    in = new FileInputStream(contextXmlFile);
                }
            }
            else
            {
                DefaultJarArchive jarArchive = new DefaultJarArchive(this.warFile.getPath());
                in = jarArchive.getResource("WEB-INF/jboss-web.xml");
            }

            if (in != null)
            {
                webXml = JBossWebXmlIo.parseJBossWebXml(in);
            }
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }

        return webXml;
    }
    
    /**
     * Returns the <code>jboss-web.xml</code> deployment descriptor of the web 
     * application.
     * 
     * @return The parsed deployment descriptor
     */
    public final JBossWebXml getJBossWebXml()
    {
        return this.jbossWebXml;
    }

}
