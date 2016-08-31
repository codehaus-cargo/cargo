/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2016 Ali Tokmen.
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

import java.io.File;
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
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     * @see WildFlyConfiguration#getConfigurationFactory()
     */
    public WildFlyCliConfigurationFactory getConfigurationFactory()
    {
        return factory;
    }

    /**
     * {@inheritDoc}
     * @see AbstractWildFlyStandaloneLocalConfiguration#doConfigure(LocalContainer)
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

        // Execute CLI scripts
        for (Map.Entry<String, String> property : getProperties().entrySet())
        {
            String propertyName = property.getKey();
            if (propertyName.startsWith(WildFlyPropertySet.CLI_OFFLINE_SCRIPT))
            {
                String scriptPath = property.getValue();
                container.executeScriptFiles(Arrays.asList(scriptPath));
            }
        }

        // deploy deployments
        String deployments;
        String altDeployDir = container.getConfiguration().
            getPropertyValue(JBossPropertySet.ALTERNATIVE_DEPLOYMENT_DIR);
        if (altDeployDir != null && !altDeployDir.equals(""))
        {
            container.getLogger().info("Using non-default deployment target directory "
                + altDeployDir, this.getClass().getName());
            deployments = getFileHandler().append(getHome(), altDeployDir);
        }
        else
        {
            deployments = getFileHandler().append(getHome(), "deployments");
        }
        getResourceUtils().copyResource(RESOURCE_PATH + "cargocpc.war",
                new File(deployments, "cargocpc.war"));
        JBoss7xInstalledLocalDeployer deployer = new JBoss7xInstalledLocalDeployer(container);
        deployer.deploy(getDeployables());
    }
}
