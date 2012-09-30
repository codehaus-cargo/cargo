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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty4xEmbeddedStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty6xEmbeddedStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty7xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.JettyExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.JettyRuntimeConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.JettyStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers Jetty support into default factories.
 *
 * @version $Id$
 */
public class JettyFactoryRegistry extends AbstractFactoryRegistry
{

    /**
     * Register deployable factory. Doesn't register anything.
     *
     * @param deployableFactory Factory on which to register.
     */
    @Override
    protected void register(DeployableFactory deployableFactory)
    {
    }

    /**
     * Register configuration capabilities.
     *
     * @param configurationCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationCapabilityFactory configurationCapabilityFactory)
    {
        configurationCapabilityFactory.registerConfigurationCapability("jetty4x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty4xEmbeddedStandaloneLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty5x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty4xEmbeddedStandaloneLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty6x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty6x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JettyStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty6x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JettyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty6x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty7x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty7x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty7xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty7x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JettyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty7x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty8x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty7xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JettyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty8x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty9x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty7xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JettyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty9x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     *
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("jetty4x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty4xEmbeddedStandaloneLocalConfiguration.class);

        configurationFactory.registerConfiguration("jetty5x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty5xEmbeddedStandaloneLocalConfiguration.class);

        configurationFactory.registerConfiguration("jetty6x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6xEmbeddedStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty6x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty6xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty6x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty6xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty6x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jetty7x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty7xEmbeddedStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty7x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty7xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty7x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty7xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty7x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jetty8x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty8xEmbeddedStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty8xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty8xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty8x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jetty9x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty9xEmbeddedStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty9xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty9xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty9x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfiguration.class);
    }

    /**
     * Register deployer.
     *
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("jetty4x", DeployerType.EMBEDDED,
            Jetty4xEmbeddedLocalDeployer.class);

        deployerFactory.registerDeployer("jetty5x", DeployerType.EMBEDDED,
            Jetty5xEmbeddedLocalDeployer.class);

        deployerFactory.registerDeployer("jetty6x", DeployerType.EMBEDDED,
            Jetty6xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty6x", DeployerType.INSTALLED,
            Jetty6xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty6x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);

        deployerFactory.registerDeployer("jetty7x", DeployerType.EMBEDDED,
            Jetty7xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty7x", DeployerType.INSTALLED,
            Jetty7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty7x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);

        deployerFactory.registerDeployer("jetty8x", DeployerType.EMBEDDED,
            Jetty7xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty8x", DeployerType.INSTALLED,
            Jetty7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty8x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);

        deployerFactory.registerDeployer("jetty9x", DeployerType.EMBEDDED,
            Jetty7xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty9x", DeployerType.INSTALLED,
            Jetty7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty9x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);
    }

    /**
     * Register packager. Doesn't register anything.
     *
     * @param packagerFactory Factory on which to register.
     */
    @Override
    protected void register(PackagerFactory packagerFactory)
    {
    }

    /**
     * Register container.
     *
     * @param containerFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerFactory containerFactory)
    {
        containerFactory.registerContainer("jetty4x", ContainerType.EMBEDDED,
            Jetty4xEmbeddedLocalContainer.class);

        containerFactory.registerContainer("jetty5x", ContainerType.EMBEDDED,
            Jetty5xEmbeddedLocalContainer.class);

        containerFactory.registerContainer("jetty6x", ContainerType.EMBEDDED,
            Jetty6xEmbeddedLocalContainer.class);
        containerFactory.registerContainer("jetty6x", ContainerType.INSTALLED,
            Jetty6xInstalledLocalContainer.class);
        containerFactory.registerContainer("jetty6x", ContainerType.REMOTE,
            Jetty6xRemoteContainer.class);

        containerFactory.registerContainer("jetty7x", ContainerType.EMBEDDED,
            Jetty7xEmbeddedLocalContainer.class);
        containerFactory.registerContainer("jetty7x", ContainerType.INSTALLED,
            Jetty7xInstalledLocalContainer.class);
        containerFactory.registerContainer("jetty7x", ContainerType.REMOTE,
            Jetty7xRemoteContainer.class);

        containerFactory.registerContainer("jetty8x", ContainerType.EMBEDDED,
            Jetty8xEmbeddedLocalContainer.class);
        containerFactory.registerContainer("jetty8x", ContainerType.INSTALLED,
            Jetty8xInstalledLocalContainer.class);
        containerFactory.registerContainer("jetty8x", ContainerType.REMOTE,
            Jetty8xRemoteContainer.class);

        containerFactory.registerContainer("jetty9x", ContainerType.EMBEDDED,
            Jetty9xEmbeddedLocalContainer.class);
        containerFactory.registerContainer("jetty9x", ContainerType.INSTALLED,
            Jetty9xInstalledLocalContainer.class);
        containerFactory.registerContainer("jetty9x", ContainerType.REMOTE,
            Jetty9xRemoteContainer.class);
    }

    /**
     * Register container capabilities.
     *
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("jetty4x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jetty5x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jetty6x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jetty7x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jetty8x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jetty9x",
            ServletContainerCapability.class);
    }

}
