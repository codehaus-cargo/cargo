/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.glassfish;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.glassfish.internal.GlassFish2xContainerCapability;
import org.codehaus.cargo.container.glassfish.internal.GlassFish2xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.glassfish.internal.GlassFish3x4x5x6x7xContainerCapability;
import org.codehaus.cargo.container.glassfish.internal.GlassFish3x4x5xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.glassfish.internal.GlassFish3xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.glassfish.internal.GlassFish4x5x6x7x8xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.glassfish.internal.GlassFishExistingLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers GlassFish support into default factories.
 */
public class GlassFishFactoryRegistry extends AbstractFactoryRegistry
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
        configurationCapabilityFactory.registerConfigurationCapability("glassfish2x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish2xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish2x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("glassfish3x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish3xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish3x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish3x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            GlassFish3x4x5xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("glassfish4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish4x5x6x7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish4x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            GlassFish3x4x5xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("glassfish5x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish4x5x6x7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish5x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish5x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            GlassFish3x4x5xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("glassfish6x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish4x5x6x7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish6x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("glassfish7x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish4x5x6x7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish7x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("glassfish8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish4x5x6x7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfigurationCapability.class);
    }

    /**
     * Register standalone configuration.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("glassfish2x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish2xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish2x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("glassfish3x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish3xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish3x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish3x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            GlassFish3xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("glassfish4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish4xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish4x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            GlassFish4xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("glassfish5x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish5xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish5x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish5x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            GlassFish5xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("glassfish6x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish6xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish6x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("glassfish7x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish7xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish7x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("glassfish8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFish8xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GlassFishExistingLocalConfiguration.class);
    }

    /**
     * Register installed local deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("glassfish2x", DeployerType.INSTALLED,
            GlassFish2xInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("glassfish3x", DeployerType.INSTALLED,
            GlassFish3xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("glassfish3x", DeployerType.REMOTE,
            GlassFish3xRemoteDeployer.class);

        deployerFactory.registerDeployer("glassfish4x", DeployerType.INSTALLED,
            GlassFish4xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("glassfish4x", DeployerType.REMOTE,
            GlassFish4xRemoteDeployer.class);

        deployerFactory.registerDeployer("glassfish5x", DeployerType.INSTALLED,
            GlassFish5xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("glassfish5x", DeployerType.REMOTE,
            GlassFish5xRemoteDeployer.class);

        deployerFactory.registerDeployer("glassfish6x", DeployerType.INSTALLED,
            GlassFish6xInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("glassfish7x", DeployerType.INSTALLED,
            GlassFish7xInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("glassfish8x", DeployerType.INSTALLED,
            GlassFish8xInstalledLocalDeployer.class);
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
     * Register installed local container.
     * 
     * @param containerFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerFactory containerFactory)
    {
        containerFactory.registerContainer("glassfish2x", ContainerType.INSTALLED,
            GlassFish2xInstalledLocalContainer.class);

        containerFactory.registerContainer("glassfish3x", ContainerType.INSTALLED,
            GlassFish3xInstalledLocalContainer.class);
        containerFactory.registerContainer("glassfish3x", ContainerType.REMOTE,
            GlassFish3xRemoteContainer.class);

        containerFactory.registerContainer("glassfish4x", ContainerType.INSTALLED,
            GlassFish4xInstalledLocalContainer.class);
        containerFactory.registerContainer("glassfish4x", ContainerType.REMOTE,
            GlassFish4xRemoteContainer.class);

        containerFactory.registerContainer("glassfish5x", ContainerType.INSTALLED,
            GlassFish5xInstalledLocalContainer.class);
        containerFactory.registerContainer("glassfish5x", ContainerType.REMOTE,
            GlassFish5xRemoteContainer.class);

        containerFactory.registerContainer("glassfish6x", ContainerType.INSTALLED,
            GlassFish6xInstalledLocalContainer.class);

        containerFactory.registerContainer("glassfish7x", ContainerType.INSTALLED,
            GlassFish7xInstalledLocalContainer.class);

        containerFactory.registerContainer("glassfish8x", ContainerType.INSTALLED,
            GlassFish8xInstalledLocalContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("glassfish2x",
            GlassFish2xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("glassfish3x",
            GlassFish3x4x5x6x7xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("glassfish4x",
            GlassFish3x4x5x6x7xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("glassfish5x",
            GlassFish3x4x5x6x7xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("glassfish6x",
            GlassFish3x4x5x6x7xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("glassfish7x",
            GlassFish3x4x5x6x7xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("glassfish8x",
            GlassFish3x4x5x6x7xContainerCapability.class);
    }

}
