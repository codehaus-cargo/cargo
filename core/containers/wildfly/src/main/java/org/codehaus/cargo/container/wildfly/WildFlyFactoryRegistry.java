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
package org.codehaus.cargo.container.wildfly;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.jboss.deployable.JBossWAR;
import org.codehaus.cargo.container.wildfly.internal.WildFly8xContainerCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFly8xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFly8xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFly8xStandaloneLocalConfigurationCapability;
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
 * 
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
            WildFly8xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly8x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFly8xRuntimeConfigurationCapability.class);
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
            WildFly8xContainerCapability.class);
    }

}
