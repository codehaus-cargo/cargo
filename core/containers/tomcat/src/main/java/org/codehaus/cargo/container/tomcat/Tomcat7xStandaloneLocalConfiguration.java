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
package org.codehaus.cargo.container.tomcat;

import java.util.Map;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * Catalina standalone {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration}
 * implementation.
 * 
 * @version $Id$
 */
public class Tomcat7xStandaloneLocalConfiguration
    extends Tomcat6xStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see Tomcat6xStandaloneLocalConfiguration#Tomcat6xStandaloneLocalConfiguration(String)
     */
    public Tomcat7xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(ServletPropertySet.USERS, "admin::manager");
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Tomcat 7.x Standalone Configuration";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setupConfFiles(String confDir)
    {
        Map<String, String> replacements = getCatalinaPropertertiesReplacements();
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "catalina.properties"),
            replacements, "UTF-8");

        replacements.clear();
        replacements.put("</Host>", this.createTomcatWebappsToken()
            + "\n      </Host>");
        getFileHandler().replaceInFile(getFileHandler().append(confDir, "server.xml"),
            replacements, "UTF-8");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performXmlReplacements(LocalContainer container)
    {
        addXmlReplacement("conf/server.xml",
            "//Server/Service/Engine/Host/Valve["
                + "@className='org.apache.catalina.valves.AccessLogValve']",
                    "prefix", getPropertyValue(GeneralPropertySet.HOSTNAME) + "_access_log.");
        addXmlReplacement("conf/server.xml",
            "//Server/Service/Engine/Host/Valve["
                + "@className='org.apache.catalina.valves.AccessLogValve']",
                    "resolveHosts", "false");

        super.performXmlReplacements(container);
    }
}
