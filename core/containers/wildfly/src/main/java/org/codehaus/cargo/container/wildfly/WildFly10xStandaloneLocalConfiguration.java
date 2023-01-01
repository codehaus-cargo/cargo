/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.jboss.JBossPropertySet;

/**
 * WildFly 10.x standalone local configuration.
 */
public class WildFly10xStandaloneLocalConfiguration extends WildFly9xStandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see WildFly9xStandaloneLocalConfiguration#WildFly9xStandaloneLocalConfiguration(String)
     */
    public WildFly10xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer c) throws Exception
    {
        super.doConfigure(c);

        // WildFly 10.x has an issue with embedded server, it doesn't register custom domain
        // directory, causing it to write configuration changes directly into default directory.
        // This is fixed by swapping configuration files between default and custom directory.
        // For more info see WFCORE-1373 in WildFly JIRA
        InstalledLocalContainer container = (InstalledLocalContainer) c;
        String containerName = container.getName();
        if (containerName.startsWith("WildFly 10.") || containerName.startsWith("JBoss EAP 7.0"))
        {
            String configurationXmlFile = "configuration/"
                    + getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml";
            String defaultConfigurationXmlFile = "standalone/" + configurationXmlFile;
            String customConfigurationXML = getFileHandler().append(getHome(),
                    configurationXmlFile);
            String defaultConfigurationXML = getFileHandler().append(container.getHome(),
                    defaultConfigurationXmlFile);
            String tempFile = getFileHandler().append(getHome(), configurationXmlFile + ".temp");

            getFileHandler().copyFile(customConfigurationXML, tempFile);
            getFileHandler().copyFile(defaultConfigurationXML, customConfigurationXML);
            getFileHandler().copyFile(tempFile, defaultConfigurationXML);
            getFileHandler().delete(tempFile);
        }
    }
}
