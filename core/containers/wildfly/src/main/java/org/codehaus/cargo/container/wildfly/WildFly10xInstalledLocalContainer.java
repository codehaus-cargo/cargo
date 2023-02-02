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

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * WildFly 10.x series container implementation.
 */
public class WildFly10xInstalledLocalContainer extends WildFly9xInstalledLocalContainer
{
    /**
     * WildFly 10.x series unique id.
     */
    public static final String ID = "wildfly10x";

    /**
     * {@inheritDoc}
     * @see WildFly9xInstalledLocalContainer#WildFly9xInstalledLocalContainer(LocalConfiguration)
     */
    public WildFly10xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        swapConfigurationFilesIfNecessary();
        super.doStart(java);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        super.doStop(java);
        swapConfigurationFilesIfNecessary();
    }

    /**
     * Workaround for <a href="https://issues.redhat.com/browse/WFCORE-1373">WFCORE-1373</a>, where
     * WildFly 10.x doesn't register custom domain directory, causing it to write configuration
     * changes directly into the default directory. This is worked around by swapping configuration
     * files between default and custom directories.
     */
    private void swapConfigurationFilesIfNecessary()
    {
        if (getConfiguration() instanceof StandaloneLocalConfiguration)
        {
            String containerName = getName();
            if (containerName.startsWith("WildFly 10.")
                || containerName.startsWith("JBoss EAP 7.0"))
            {
                String configurationXmlFile = "configuration/"
                    + getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml";
                String defaultConfigurationXmlFile = "standalone/" + configurationXmlFile;
                String customConfigurationXml = getFileHandler().append(
                    getConfiguration().getHome(),  configurationXmlFile);
                String defaultConfigurationXml = getFileHandler().append(
                    getHome(), defaultConfigurationXmlFile);
                String backupConfigurationXml = getFileHandler().append(
                    getConfiguration().getHome(), configurationXmlFile + ".backup");

                getFileHandler().copyFile(customConfigurationXml, backupConfigurationXml);
                getFileHandler().copyFile(defaultConfigurationXml, customConfigurationXml);
                getFileHandler().copyFile(backupConfigurationXml, defaultConfigurationXml);
                getFileHandler().delete(backupConfigurationXml);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultName()
    {
        return "WildFly 10.x (JBoss EAP 7.0)";
    }
}
