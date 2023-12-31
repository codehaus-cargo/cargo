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
package org.codehaus.cargo.container.payara;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.payara.internal.PayaraContainerCapability;
import org.codehaus.cargo.container.payara.internal.PayaraExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.payara.internal.PayaraRuntimeConfigurationCapability;
import org.codehaus.cargo.container.payara.internal.PayaraStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers Payara support into default factories.
 */
public class PayaraFactoryRegistry extends AbstractFactoryRegistry
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
        configurationCapabilityFactory.registerConfigurationCapability("payara",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            PayaraStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("payara",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            PayaraExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("payara",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            PayaraRuntimeConfigurationCapability.class);
    }

    /**
     * Register standalone configuration.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("payara",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            PayaraStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("payara",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            PayaraExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("payara",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            PayaraRuntimeConfiguration.class);
    }

    /**
     * Register installed local deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("payara", DeployerType.INSTALLED,
            PayaraInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("payara", DeployerType.REMOTE,
            PayaraRemoteDeployer.class);
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
        containerFactory.registerContainer("payara", ContainerType.INSTALLED,
            PayaraInstalledLocalContainer.class);
        containerFactory.registerContainer("payara", ContainerType.REMOTE,
            PayaraRemoteContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("payara",
            PayaraContainerCapability.class);
    }

}
