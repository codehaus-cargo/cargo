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
package org.codehaus.cargo.container.orion;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.orion.internal.OrionStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers Orion support into default factories.
 * 
 */
public class OrionFactoryRegistry extends AbstractFactoryRegistry
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
        configurationCapabilityFactory.registerConfigurationCapability("oc4j9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            OrionStandaloneLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("oc4j10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            OrionStandaloneLocalConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("oc4j9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Oc4j9xStandaloneLocalConfiguration.class);

        configurationFactory.registerConfiguration("oc4j10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Oc4j10xExistingLocalConfiguration.class);
    }

    /**
     * Register deployer. Doesn't register anything.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
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
        containerFactory.registerContainer("oc4j9x", ContainerType.INSTALLED,
            Oc4j9xInstalledLocalContainer.class);

        containerFactory.registerContainer("oc4j10x", ContainerType.INSTALLED,
            Oc4j10xInstalledLocalContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("oc4j9x",
            J2EEContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("oc4j10x",
            J2EEContainerCapability.class);
    }

}
