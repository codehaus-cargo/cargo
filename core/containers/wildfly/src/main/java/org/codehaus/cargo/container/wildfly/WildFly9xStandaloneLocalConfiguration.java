/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.jboss.JBoss7xInstalledLocalDeployer;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.wildfly.internal.AbstractWildFlyInstalledLocalContainer;
import org.codehaus.cargo.container.wildfly.internal.AbstractWildFlyStandaloneLocalConfiguration;
import org.codehaus.cargo.container.wildfly.internal.WildFly9xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFly9xCliConfigurationFactory;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFlyCliConfigurationFactory;
import org.codehaus.cargo.container.wildfly.internal.util.WildFlyModuleUtils;

/**
 * WildFly 9.x standalone local configuration.
 */
public class WildFly9xStandaloneLocalConfiguration
    extends AbstractWildFlyStandaloneLocalConfiguration
{

    /**
     * WildFly container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WildFly9xStandaloneLocalConfigurationCapability();

    /**
     * CLI configuration factory.
     */
    private WildFly9xCliConfigurationFactory factory =
            new WildFly9xCliConfigurationFactory(this);

    /**
     * {@inheritDoc}
     * @see WildFly8xStandaloneLocalConfiguration#WildFly8xStandaloneLocalConfiguration(String)
     */
    public WildFly9xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WildFlyCliConfigurationFactory getConfigurationFactory()
    {
        return factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer c) throws Exception
    {
        AbstractWildFlyInstalledLocalContainer container =
            (AbstractWildFlyInstalledLocalContainer) c;
        super.doConfigure(c);

        List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();

        configurationScript.add(factory.startEmbedServerScript());
        configurationScript.add(factory.configurePortsScript());
        configurationScript.add(factory.loggingScript());

        // add modules
        for (String classpathElement : container.getExtraClasspath())
        {
            addModuleScript(classpathElement, container, configurationScript);
        }
        for (String classpathElement : container.getSharedClasspath())
        {
            addModuleScript(classpathElement, container, configurationScript);
        }

        // add DataSources
        for (DataSource ds : getDataSources())
        {
            String driverModule = WildFlyModuleUtils.getDataSourceDriverModuleName(container, ds);
            configurationScript.add(factory.dataSourceDriverScript(ds, driverModule));
            configurationScript.add(factory.dataSourceScript(ds));
        }

        // add Resources
        for (Resource resource : getResources())
        {
            configurationScript.add(factory.resourceScript(resource));
        }

        // add system properties to configuration - to be persistent
        for (Entry<String, String> systemProperty : container.getSystemProperties().entrySet())
        {
            configurationScript.add(factory.systemPropertyScript(
                    systemProperty.getKey(), systemProperty.getValue()));
        }

        // add custom embedded scripts
        for (Map.Entry<String, String> property : getProperties().entrySet())
        {
            String propertyName = property.getKey();
            if (propertyName.startsWith(WildFlyPropertySet.CLI_EMBEDDED_SCRIPT))
            {
                String scriptPath = property.getValue();
                configurationScript.add(factory.customScript(scriptPath));
            }
        }

        container.executeScript(configurationScript);

        // execute CLI scripts
        for (Map.Entry<String, String> property : getProperties().entrySet())
        {
            String propertyName = property.getKey();
            if (propertyName.startsWith(WildFlyPropertySet.CLI_OFFLINE_SCRIPT))
            {
                String scriptPath = property.getValue();
                container.executeScriptFiles(Arrays.asList(scriptPath));
            }
        }

        // CARGO-1601: Workaround for WFCORE-1373, where WildFly 10.x writes configuration changes
        // directly into the container directory instead of the configuration directory
        String configuration = getPropertyValue(JBossPropertySet.CONFIGURATION);
        String configurationXmlHistory = getFileHandler().append(container.getHome(),
            configuration + "/configuration/" + configuration + "_xml_history");
        if (getFileHandler().isDirectory(configurationXmlHistory))
        {
            String configurationXmlFile = getFileHandler().append(container.getHome(),
                configuration + "/configuration/" + configuration + ".xml");

            // copy the changed configuration file into the configuration folder
            getFileHandler().copyFile(configurationXmlFile,
                getFileHandler().append(getHome(), "configuration/" + configuration + ".xml"),
                    true);

            // restore original configuration file in the container folder
            String configurationXmlInitial = getFileHandler().append(
                configurationXmlHistory, configuration + ".initial.xml");
            getFileHandler().copyFile(configurationXmlInitial, configurationXmlFile, true);
            getFileHandler().delete(configurationXmlHistory);
        }

        // deploy deployables
        JBoss7xInstalledLocalDeployer deployer = new JBoss7xInstalledLocalDeployer(container);
        deployer.deploy(getDeployables());
    }
}
