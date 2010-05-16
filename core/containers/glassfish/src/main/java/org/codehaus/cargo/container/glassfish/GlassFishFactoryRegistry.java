/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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
     * Container identifiers.
     *
     * SJSAS = Sun Java System Application Server SGES = Sun GlassFish Enterprise Server
     */
    private static final String[] IDS =
    {
        "glassfish2x",
        "glassfish3x",
        "sges2x",
        "sjsas91x"
    };

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
        for (String id : IDS)
        {
            configurationCapabilityFactory.registerConfigurationCapability(id,
                ContainerType.INSTALLED, ConfigurationType.STANDALONE,
                GlassFishStandaloneLocalConfigurationCapability.class);
        }
    }

    /**
     * Register standalone configuration.
     *
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        for (String id : IDS)
        {
            configurationFactory.registerConfiguration(id, ContainerType.INSTALLED,
                ConfigurationType.STANDALONE, GlassFishStandaloneLocalConfiguration.class);
        }
    }

    /**
     * Register installed local deployer.
     *
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        for (String id : IDS)
        {
            deployerFactory.registerDeployer(id, DeployerType.INSTALLED,
                GlassFishInstalledLocalDeployer.class);
        }
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
        for (String id : IDS)
        {
            containerFactory.registerContainer(id, ContainerType.INSTALLED,
                GlassFishInstalledLocalContainer.class);
        }
    }

    /**
     * Register container capabilities.
     *
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        for (String id : IDS)
        {
            containerCapabilityFactory.registerContainerCapability(id,
                GlassFishContainerCapability.class);
        }
    }

}
