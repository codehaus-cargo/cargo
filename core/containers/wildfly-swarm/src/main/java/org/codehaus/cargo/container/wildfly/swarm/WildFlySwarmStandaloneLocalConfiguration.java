/*
 * ========================================================================
 *
 *  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm;

import java.io.File;
import java.io.Flushable;
import java.io.IOException;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.container.wildfly.swarm.internal.WildFlySwarmStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.ConfigurationContext;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.WildFlySwarmConfiguratorFactory;
import org.codehaus.cargo.container.wildfly.swarm.internal.configuration.yaml.WildFlySwarmYamlConfiguratorFactory;

/**
 * WildFly Swarm standalone container configuration.
 */
public class WildFlySwarmStandaloneLocalConfiguration extends AbstractStandaloneLocalConfiguration
{

    /**
     * Default Swarm project name.
     */
    private static final String DEFAULT_PROJECT_CONFIG_NAME = "cargo";

    /**
     * WildFly Swarm capability instance.
     */
    private static final ConfigurationCapability CAPABILITY =
            new WildFlySwarmStandaloneLocalConfigurationCapability();

    /**
     * Reference to a configurator factory.
     */
    private final WildFlySwarmConfiguratorFactory configuratorFactory;

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfiguration#AbstractStandaloneLocalConfiguration(String)
     */
    public WildFlySwarmStandaloneLocalConfiguration(String home)
    {
        super(home);
        ConfigurationContext context = new ConfigurationContext(
                getFileHandler(),
                getHome(),
                getSwarmProjectDescriptor()
        );
        this.configuratorFactory = new WildFlySwarmYamlConfiguratorFactory(context);
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
    protected void doConfigure(LocalContainer container) throws Exception
    {
        setupConfigurationDir();

        configureUsers();
        flush();
    }

    /**
     * Resolves Swarm project descriptor file.
     * @return Swarm project descriptor file.
     */
    public File getSwarmProjectDescriptor()
    {
        String projectNameProperty = getPropertyValue(WildFlySwarmPropertySet.SWARM_PROJECT_NAME);
        String projectName = projectNameProperty != null
                ? projectNameProperty : DEFAULT_PROJECT_CONFIG_NAME;

        return new File(getHome(), getSwarmProjectDescriptorName(projectName));
    }

    /**
     * Constructs Swarm project descriptor file name.
     * @param projectName Swarm project name.
     * @return Swarm project descriptor file name.
     */
    private String getSwarmProjectDescriptorName(String projectName)
    {
        return "project-" + projectName + ".yaml";
    }

    /**
     * Configure user accounts.
     */
    private void configureUsers()
    {
        configuratorFactory.userAccountsConfigurator().configureApplicationUsers(getUsers());
    }

    /**
     * Flushes the configuration changes.
     */
    private void flush()
    {
        try
        {
            ((Flushable) configuratorFactory).flush();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
