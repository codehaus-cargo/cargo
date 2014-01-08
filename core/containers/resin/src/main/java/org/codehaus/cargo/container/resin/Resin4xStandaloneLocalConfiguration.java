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
package org.codehaus.cargo.container.resin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * Resin 4.x standalone
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} implementation.
 * 
 * @version $Id$
 */
public class Resin4xStandaloneLocalConfiguration extends Resin31xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * 
     * @see Resin31xStandaloneLocalConfiguration#Resin31xStandaloneLocalConfiguration(String)
     */
    public Resin4xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     * 
     * @see Resin31xStandaloneLocalConfiguration#prepareConfigurationDirectory(org.codehaus.cargo.container.Container, java.lang.String)
     */
    protected void prepareConfigurationDirectory(Container container, String confDir)
        throws IOException
    {
        String sourceConf = getFileHandler().append(
            ((InstalledLocalContainer) container).getHome(), "conf");

        getFileHandler().copyFile(getFileHandler().append(sourceConf, "app-default.xml"),
            getFileHandler().append(confDir, "app-default.xml"));
        getFileHandler().copyFile(getFileHandler().append(sourceConf, "cluster-default.xml"),
            getFileHandler().append(confDir, "cluster-default.xml"));
        getFileHandler().copyFile(getFileHandler().append(sourceConf, "health.xml"),
            getFileHandler().append(confDir, "health.xml"));
        getFileHandler().copyFile(getFileHandler().append(sourceConf, "resin.xml"),
            getFileHandler().append(confDir, "resin.xml"));
        getFileHandler().copyFile(getFileHandler().append(sourceConf, "resin.properties"),
            getFileHandler().append(confDir, "resin.properties"));

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("<host id=\"\" root-directory=\".\">",
            "<host id=\"\" root-directory=\".\">\n"
            + createExpandedWarTokenValue("document-directory"));
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "resin.xml"),
                replacements, "UTF-8");
        replacements.clear();
        replacements.put("<allow-servlet-el/>",
            "<allow-servlet-el/>\n"
            + "\n"
            + "<authenticator>\n"
                + "<type>com.caucho.server.security.XmlAuthenticator</type>\n"
                + "<init>\n"
                    + getSecurityToken("<user>", "</user>") + "\n"
                    + "<password-digest>none</password-digest>\n"
                + "</init>\n"
            + "</authenticator>");
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "cluster-default.xml"),
                replacements, "UTF-8");
        replacements.clear();
        replacements.put("8080", getPropertyValue(ServletPropertySet.PORT));
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "resin.properties"),
                replacements, "UTF-8");

        addXmlReplacement("conf/resin.xml", "//resin/log-handler[@name='']", "level",
            getResinLoggingLevel(getPropertyValue(GeneralPropertySet.LOGGING)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getResinConfigurationFileName()
    {
        return "resin.xml";
    }
}
