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
package org.codehaus.cargo.container.jboss;

import java.io.File;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss71xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.util.CargoException;

/**
 * JBoss 7.1.x standalone local configuration.
 * 
 * @version $Id$
 */
public class JBoss71xStandaloneLocalConfiguration extends JBoss7xStandaloneLocalConfiguration
{

    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss71xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss7xStandaloneLocalConfiguration#JBoss7xStandaloneLocalConfiguration(String)
     */
    public JBoss71xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        removeXmlReplacement(
            "configuration/standalone.xml",
            "//server/management/management-interfaces/native-interface[@interface='management']",
            "port");
        addXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='management-native']",
            "port", JBossPropertySet.JBOSS_MANAGEMENT_PORT);

        setProperty(JBossPropertySet.JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT, "4712");
        addXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='txn-recovery-environment']",
            "port", JBossPropertySet.JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT);

        setProperty(JBossPropertySet.JBOSS_TRANSACTION_STATUS_MANAGER_PORT, "1099");
        addXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='txn-status-manager']",
            "port", JBossPropertySet.JBOSS_TRANSACTION_STATUS_MANAGER_PORT);

        getProperties().remove(GeneralPropertySet.RMI_PORT);
        removeXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='jndi']",
            "port");

        removeXmlReplacement(
            "configuration/standalone.xml",
            "//server/profile/subsystem/periodic-rotating-file-handler/level",
            "name");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     * @see JBoss7xStandaloneLocalConfiguration#configure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer c) throws Exception
    {
        if (!(c instanceof InstalledLocalContainer))
        {
            throw new CargoException("Only InstalledLocalContainers are supported, got "
                + c.getClass().getName());
        }

        InstalledLocalContainer container = (InstalledLocalContainer) c;

        getLogger().info("Configuring JBoss using the [" + CONFIGURATION
            + "] server configuration", this.getClass().getName());

        setProperty("cargo.jboss.logging",
            getJBossLogLevel(getPropertyValue(GeneralPropertySet.LOGGING)));

        setupConfigurationDir();

        // Copy initial configuration
        String initialConfiguration = getFileHandler().append(container.getHome(), CONFIGURATION);
        getFileHandler().copyDirectory(initialConfiguration, getHome());

        String configurationXML = getFileHandler().append(getHome(),
            "/configuration/standalone.xml");
        if (!getFileHandler().exists(configurationXML))
        {
            throw new CargoException("Missing configuration XML file: " + configurationXML);
        }

        // Apply configuration
        String deployments = getFileHandler().append(getHome(), "deployments");

        // Deploy the CPC (Cargo Ping Component) to the deployments directory
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
            new File(deployments, "cargocpc.war"));

        // Deploy with user defined deployables with the appropriate deployer
        JBoss7xInstalledLocalDeployer deployer = new JBoss7xInstalledLocalDeployer(container);
        deployer.deploy(getDeployables());
    }

    /**
     * Translate Cargo logging levels into JBoss logging levels.
     * 
     * @param cargoLogLevel Cargo logging level
     * @return the corresponding JBoss logging level
     */
    private String getJBossLogLevel(String cargoLogLevel)
    {
        String level;

        if (LoggingLevel.LOW.equalsLevel(cargoLogLevel))
        {
            level = "ERROR";
        }
        else if (LoggingLevel.MEDIUM.equalsLevel(cargoLogLevel))
        {
            level = "INFO";
        }
        else
        {
            level = "DEBUG";
        }

        return level;
    }

}
