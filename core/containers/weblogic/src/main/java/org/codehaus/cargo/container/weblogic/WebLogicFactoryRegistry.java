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
package org.codehaus.cargo.container.weblogic;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogic9x10xAnd103xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogicExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogicStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers WebLogic support into default factories.
 * 
 * @version $Id$
 */
public class WebLogicFactoryRegistry extends AbstractFactoryRegistry
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
        configurationCapabilityFactory.registerConfigurationCapability("weblogic8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogicStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic9x10xAnd103xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic9x10xAnd103xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic103x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic9x10xAnd103xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic103x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("weblogic8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogicStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("weblogic9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic9xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic9xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("weblogic10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic10xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic9xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("weblogic103x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic103xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic103x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic9xExistingLocalConfiguration.class);
    }

    /**
     * Register deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("weblogic8x", DeployerType.INSTALLED,
            WebLogic8xSwitchableLocalDeployer.class);
        deployerFactory.registerDeployer("weblogic9x", DeployerType.INSTALLED,
            WebLogicCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("weblogic10x", DeployerType.INSTALLED,
            WebLogicCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("weblogic103x", DeployerType.INSTALLED,
            WebLogicCopyingInstalledLocalDeployer.class);
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
        containerFactory.registerContainer("weblogic8x", ContainerType.INSTALLED,
            WebLogic8xInstalledLocalContainer.class);

        containerFactory.registerContainer("weblogic9x", ContainerType.INSTALLED,
            WebLogic9xInstalledLocalContainer.class);

        containerFactory.registerContainer("weblogic10x", ContainerType.INSTALLED,
            WebLogic10xInstalledLocalContainer.class);

        containerFactory.registerContainer("weblogic103x", ContainerType.INSTALLED,
            WebLogic103xInstalledLocalContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("weblogic8x",
            J2EEContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("weblogic9x",
            J2EEContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("weblogic10x",
            J2EEContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("weblogic103x",
            J2EEContainerCapability.class);
    }

}
