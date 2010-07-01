/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container.resin;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.resin.internal.ResinExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.resin.internal.ResinStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers Resin support into default factories.
 * 
 * @version $Id$
 */
public class ResinFactoryRegistry extends AbstractFactoryRegistry
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
        configurationCapabilityFactory.registerConfigurationCapability("resin2x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            ResinStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("resin2x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            ResinExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("resin3x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            ResinStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("resin3x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            ResinExistingLocalConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     *
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("resin2x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Resin2xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("resin2x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            ResinExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("resin3x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Resin3xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("resin3x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            ResinExistingLocalConfiguration.class);
    }

    /**
     * Register deployer.
     *
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("resin2x", DeployerType.INSTALLED,
            ResinInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("resin3x", DeployerType.INSTALLED,
            ResinInstalledLocalDeployer.class);
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
        containerFactory.registerContainer("resin2x", ContainerType.INSTALLED,
            Resin2xInstalledLocalContainer.class);

        containerFactory.registerContainer("resin3x", ContainerType.INSTALLED,
            Resin3xInstalledLocalContainer.class);
    }

    /**
     * Register container capabilities.
     *
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("resin2x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("resin3x",
            ServletContainerCapability.class);
    }

}
