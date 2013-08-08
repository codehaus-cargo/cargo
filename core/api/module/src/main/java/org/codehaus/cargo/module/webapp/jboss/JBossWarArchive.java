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
package org.codehaus.cargo.module.webapp.jboss;

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
     * @throws IOException If there was a problem reading the deployment descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not be parsed
     */
    public JBossWarArchive(File warFile)
        throws IOException, JDOMException
    {
        this.warFile = warFile;
        this.jbossWebXml = parseJBossWebXml();
    }

    /**
     * @return the parsed <code>WEB-INF/jboss-web.xml</code> descriptor or null if none exists
     * @throws IOException If there was a problem reading the deployment descriptor in the WAR
     * @throws JDOMException If the deployment descriptor of the WAR could not be parsed
     */
    private JBossWebXml parseJBossWebXml()
        throws IOException, JDOMException
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
                JarArchive jarArchive = JarArchiveIo.open(this.warFile);
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
     * Returns the <code>jboss-web.xml</code> deployment descriptor of the web application.
     * 
     * @return The parsed deployment descriptor
     */
    public JBossWebXml getJBossWebXml()
    {
        return this.jbossWebXml;
    }

}
