/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.jonas.internal.Jonas4xContainerCapability;
import org.codehaus.cargo.container.jonas.internal.Jonas5xContainerCapability;
import org.codehaus.cargo.container.jonas.internal.JonasExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jonas.internal.JonasRuntimeConfigurationCapability;
import org.codehaus.cargo.container.jonas.internal.JonasStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers JOnAS support into default factories.
 * 
 * @version $Id$
 */
public class JonasFactoryRegistry extends AbstractFactoryRegistry
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
        configurationCapabilityFactory.registerConfigurationCapability("jonas4x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JonasRuntimeConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jonas4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JonasExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jonas4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JonasStandaloneLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jonas5x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JonasRuntimeConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jonas5x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JonasExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jonas5x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JonasStandaloneLocalConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("jonas4x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JonasRuntimeConfiguration.class);
        configurationFactory.registerConfiguration("jonas4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jonas4xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jonas4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jonas4xStandaloneLocalConfiguration.class);

        configurationFactory.registerConfiguration("jonas5x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JonasRuntimeConfiguration.class);
        configurationFactory.registerConfiguration("jonas5x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            Jonas5xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jonas5x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Jonas5xStandaloneLocalConfiguration.class);
    }

    /**
     * Register deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("jonas4x", DeployerType.REMOTE,
            Jonas4xJsr160RemoteDeployer.class);
        deployerFactory.registerDeployer("jonas4x", DeployerType.INSTALLED,
            Jonas4xInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("jonas5x", DeployerType.REMOTE,
            Jonas5xJsr160RemoteDeployer.class);
        deployerFactory.registerDeployer("jonas5x", DeployerType.INSTALLED,
            Jonas5xInstalledLocalDeployer.class);
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
        containerFactory.registerContainer("jonas4x", ContainerType.REMOTE,
            Jonas4xRemoteContainer.class);
        containerFactory.registerContainer("jonas4x", ContainerType.INSTALLED,
            Jonas4xInstalledLocalContainer.class);

        containerFactory.registerContainer("jonas5x", ContainerType.REMOTE,
            Jonas5xRemoteContainer.class);
        containerFactory.registerContainer("jonas5x", ContainerType.INSTALLED,
            Jonas5xInstalledLocalContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("jonas4x",
            Jonas4xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jonas5x",
            Jonas5xContainerCapability.class);
    }

}
