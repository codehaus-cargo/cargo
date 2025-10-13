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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty10x11xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty10x11xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty12xEmbeddedStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty12xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty12xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty5xEmbeddedStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty6x7x8x9x10x11xEmbeddedStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty7x8x9xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.JettyExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.JettyRuntimeConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.JettyStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.packager.PackagerType;
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
        configurationCapabilityFactory.registerConfigurationCapability("jetty5x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty5xEmbeddedStandaloneLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty6x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6x7x8x9x10x11xEmbeddedStandaloneLocalConfigurationCapability.class);
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
            Jetty6x7x8x9x10x11xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty7x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty7x8x9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty7x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JettyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty7x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty8x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6x7x8x9x10x11xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty7x8x9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JettyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty8x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty9x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6x7x8x9x10x11xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty7x8x9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JettyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty9x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty10x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6x7x8x9x10x11xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty10x11xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty10x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty11x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty6x7x8x9x10x11xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty11x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty11x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty10x11xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty11x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jetty12x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty12xEmbeddedStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty12x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty12xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jetty12x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty12xExistingLocalConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
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

        configurationFactory.registerConfiguration("jetty10x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty10xEmbeddedStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty10xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty10xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty10x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jetty11x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty11xEmbeddedStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty11x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty11xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty11x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty11xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty11x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JettyRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jetty12x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Jetty12xEmbeddedStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty12x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jetty12xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jetty12x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jetty12xExistingLocalConfiguration.class);
    }

    /**
     * Register deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("jetty5x", DeployerType.EMBEDDED,
            Jetty5xEmbeddedLocalDeployer.class);

        deployerFactory.registerDeployer("jetty6x", DeployerType.EMBEDDED,
            Jetty6xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty6x", DeployerType.INSTALLED,
            Jetty6xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty6x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);

        deployerFactory.registerDeployer("jetty7x", DeployerType.EMBEDDED,
            Jetty7x8x9x10x11xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty7x", DeployerType.INSTALLED,
            Jetty7x8xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty7x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);

        deployerFactory.registerDeployer("jetty8x", DeployerType.EMBEDDED,
            Jetty7x8x9x10x11xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty8x", DeployerType.INSTALLED,
            Jetty7x8xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty8x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);

        deployerFactory.registerDeployer("jetty9x", DeployerType.EMBEDDED,
            Jetty7x8x9x10x11xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty9x", DeployerType.INSTALLED,
            Jetty9x10x11xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty9x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);

        deployerFactory.registerDeployer("jetty10x", DeployerType.EMBEDDED,
            Jetty7x8x9x10x11xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty10x", DeployerType.INSTALLED,
            Jetty9x10x11xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty10x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);

        deployerFactory.registerDeployer("jetty11x", DeployerType.EMBEDDED,
            Jetty7x8x9x10x11xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty11x", DeployerType.INSTALLED,
            Jetty9x10x11xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jetty11x", DeployerType.REMOTE,
            JettyRemoteDeployer.class);

        deployerFactory.registerDeployer("jetty12x", DeployerType.EMBEDDED,
            Jetty12xEmbeddedLocalDeployer.class);
        deployerFactory.registerDeployer("jetty12x", DeployerType.INSTALLED,
            Jetty12xInstalledLocalDeployer.class);
    }

    /**
     * Register packager. Doesn't register anything.
     * 
     * @param packagerFactory Factory on which to register.
     */
    @Override
    protected void register(PackagerFactory packagerFactory)
    {
        packagerFactory.registerPackager("jetty6x", PackagerType.DIRECTORY,
            JettyDirectoryPackager.class);
        packagerFactory.registerPackager("jetty7x", PackagerType.DIRECTORY,
            JettyDirectoryPackager.class);
        packagerFactory.registerPackager("jetty8x", PackagerType.DIRECTORY,
            JettyDirectoryPackager.class);
        packagerFactory.registerPackager("jetty9x", PackagerType.DIRECTORY,
            JettyDirectoryPackager.class);
        packagerFactory.registerPackager("jetty10x", PackagerType.DIRECTORY,
            JettyDirectoryPackager.class);
        packagerFactory.registerPackager("jetty11x", PackagerType.DIRECTORY,
            JettyDirectoryPackager.class);
        packagerFactory.registerPackager("jetty12x", PackagerType.DIRECTORY,
            JettyDirectoryPackager.class);
    }

    /**
     * Register container.
     * 
     * @param containerFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerFactory containerFactory)
    {
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

        containerFactory.registerContainer("jetty10x", ContainerType.EMBEDDED,
            Jetty10xEmbeddedLocalContainer.class);
        containerFactory.registerContainer("jetty10x", ContainerType.INSTALLED,
            Jetty10xInstalledLocalContainer.class);
        containerFactory.registerContainer("jetty10x", ContainerType.REMOTE,
            Jetty10xRemoteContainer.class);

        containerFactory.registerContainer("jetty11x", ContainerType.EMBEDDED,
            Jetty11xEmbeddedLocalContainer.class);
        containerFactory.registerContainer("jetty11x", ContainerType.INSTALLED,
            Jetty11xInstalledLocalContainer.class);
        containerFactory.registerContainer("jetty11x", ContainerType.REMOTE,
            Jetty11xRemoteContainer.class);

        containerFactory.registerContainer("jetty12x", ContainerType.EMBEDDED,
            Jetty12xEmbeddedLocalContainer.class);
        containerFactory.registerContainer("jetty12x", ContainerType.INSTALLED,
            Jetty12xInstalledLocalContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
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

        containerCapabilityFactory.registerContainerCapability("jetty10x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jetty11x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jetty12x",
            ServletContainerCapability.class);
    }

}
