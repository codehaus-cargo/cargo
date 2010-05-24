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
package org.codehaus.cargo.container.geronimo;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.geronimo.deployable.GeronimoEAR;
import org.codehaus.cargo.container.geronimo.deployable.GeronimoEJB;
import org.codehaus.cargo.container.geronimo.deployable.GeronimoWAR;
import org.codehaus.cargo.container.geronimo.internal.GeronimoExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.geronimo.internal.GeronimoStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers Geronimo support into default factories.
 * 
 * @version $Id$
 */
public class GeronimoFactoryRegistry extends AbstractFactoryRegistry
{

    /**
     * Register deployable factory. Doesn't register anything.
     *
     * @param deployableFactory Factory on which to register.
     */
    @Override
    protected void register(DeployableFactory deployableFactory)
    {
        deployableFactory.registerDeployable("geronimo1x", DeployableType.WAR,
            GeronimoWAR.class);

        deployableFactory.registerDeployable("geronimo1x", DeployableType.EAR,
            GeronimoEAR.class);

        deployableFactory.registerDeployable("geronimo1x", DeployableType.EJB,
            GeronimoEJB.class);
    }

    /**
     * Register configuration capabilities.
     *
     * @param configurationCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationCapabilityFactory configurationCapabilityFactory)
    {
        configurationCapabilityFactory.registerConfigurationCapability("geronimo1x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Geronimo1xStandaloneLocalConfiguration.class);
        configurationCapabilityFactory.registerConfigurationCapability("geronimo1x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Geronimo1xExistingLocalConfiguration.class);
    }

    /**
     * Register configuration factories.
     *
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("geronimo1x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            GeronimoStandaloneLocalConfigurationCapability.class);
        configurationFactory.registerConfiguration("geronimo1x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            GeronimoExistingLocalConfigurationCapability.class);
    }

    /**
     * Register deployer.
     *
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("geronimo1x", DeployerType.INSTALLED,
            GeronimoInstalledLocalDeployer.class);
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
        containerFactory.registerContainer("geronimo1x", ContainerType.INSTALLED,
            Geronimo1xInstalledLocalContainer.class);
    }

    /**
     * Register container capabilities.
     *
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("geronimo1x",
            J2EEContainerCapability.class);
    }

}
