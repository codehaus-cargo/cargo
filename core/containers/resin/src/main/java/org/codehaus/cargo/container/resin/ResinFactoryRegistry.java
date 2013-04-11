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
package org.codehaus.cargo.container.resin;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.resin.internal.Resin2xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.resin.internal.Resin2xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.resin.internal.Resin3xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.resin.internal.Resin3xStandaloneLocalConfigurationCapability;
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
            Resin2xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("resin2x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Resin2xExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("resin3x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Resin3xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("resin3x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Resin3xExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("resin31x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Resin3xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("resin31x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Resin3xExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("resin4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Resin3xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("resin4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Resin3xExistingLocalConfigurationCapability.class);
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
            Resin2xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("resin3x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Resin3xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("resin3x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Resin3xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("resin31x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Resin31xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("resin31x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Resin3xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("resin4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Resin4xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("resin4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Resin3xExistingLocalConfiguration.class);
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

        deployerFactory.registerDeployer("resin31x", DeployerType.INSTALLED,
            ResinInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("resin4x", DeployerType.INSTALLED,
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

        containerFactory.registerContainer("resin31x", ContainerType.INSTALLED,
            Resin31xInstalledLocalContainer.class);

        containerFactory.registerContainer("resin4x", ContainerType.INSTALLED,
            Resin4xInstalledLocalContainer.class);
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

        containerCapabilityFactory.registerContainerCapability("resin31x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("resin4x",
            ServletContainerCapability.class);
    }

}
