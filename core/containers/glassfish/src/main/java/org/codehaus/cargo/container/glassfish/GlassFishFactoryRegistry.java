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
package org.codehaus.cargo.container.glassfish;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
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
 * 
 * @version $Id$
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
            GlassFishStandaloneLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("glassfish3x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GlassFishStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("glassfish3x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            GlassFish3xRuntimeConfigurationCapability.class);
    }

    /**
     * Register standalone configuration.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("glassfish2x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, GlassFishStandaloneLocalConfiguration.class);

        configurationFactory.registerConfiguration("glassfish3x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, GlassFishStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("glassfish3x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, GlassFish3xRuntimeConfiguration.class);
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
            GlassFishInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("glassfish3x", DeployerType.INSTALLED,
            GlassFishInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("glassfish3x", DeployerType.REMOTE,
            GlassFish3xRemoteDeployer.class);
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
            GlassFish3xContainerCapability.class);
    }

}
