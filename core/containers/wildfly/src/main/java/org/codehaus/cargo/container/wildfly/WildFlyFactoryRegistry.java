/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2018 Ali Tokmen.
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

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.jboss.deployable.JBossWAR;
import org.codehaus.cargo.container.wildfly.internal.WildFlyContainerCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFlyExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFlyRuntimeConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFly8xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFly9xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers JBoss and WildFly support into default factories.
 */
public class WildFlyFactoryRegistry extends AbstractFactoryRegistry
{

    /**
     * Registers additional {@link org.codehaus.cargo.container.deployable.Deployable}
     * implementations, that the WildFly containers support, to the given
     * {@link DeployableFactory}.
     * 
     * @param deployableFactory Factory on which to register.
     */
    @Override
    protected void register(DeployableFactory deployableFactory)
    {
        deployableFactory.registerDeployable("wildfly8x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly9x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly10x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly11x", DeployableType.WAR, JBossWAR.class);
    }

    /**
     * Register configuration capabilities.
     * 
     * @param configurationCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationCapabilityFactory configurationCapabilityFactory)
    {
        configurationCapabilityFactory.registerConfigurationCapability("wildfly8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly8x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly9x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly10x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly11x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly11x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly11x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("wildfly8x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly8xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly8x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly8xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly8x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly8xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly9x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly9xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly9x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly9xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly9x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly9xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly10x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly10xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly10x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly10xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly10x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly10xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly11x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly11xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly11x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly11xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly11x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly11xRuntimeConfiguration.class);
    }

    /**
     * Register deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("wildfly8x", DeployerType.INSTALLED,
            WildFly8xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly8x", DeployerType.REMOTE,
            WildFly8xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly9x", DeployerType.INSTALLED,
            WildFly9xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly9x", DeployerType.REMOTE,
            WildFly9xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly10x", DeployerType.INSTALLED,
            WildFly10xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly10x", DeployerType.REMOTE,
            WildFly10xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly11x", DeployerType.INSTALLED,
            WildFly11xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly11x", DeployerType.REMOTE,
            WildFly11xRemoteDeployer.class);
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
        containerFactory.registerContainer("wildfly8x", ContainerType.INSTALLED,
            WildFly8xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly8x", ContainerType.REMOTE,
            WildFly8xRemoteContainer.class);

        containerFactory.registerContainer("wildfly9x", ContainerType.INSTALLED,
            WildFly9xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly9x", ContainerType.REMOTE,
            WildFly9xRemoteContainer.class);

        containerFactory.registerContainer("wildfly10x", ContainerType.INSTALLED,
            WildFly10xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly10x", ContainerType.REMOTE,
            WildFly10xRemoteContainer.class);

        containerFactory.registerContainer("wildfly11x", ContainerType.INSTALLED,
            WildFly11xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly11x", ContainerType.REMOTE,
            WildFly11xRemoteContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("wildfly8x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly9x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly10x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly11x",
            WildFlyContainerCapability.class);
    }

}
