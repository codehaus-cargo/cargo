/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
import org.codehaus.cargo.container.weblogic.internal.WebLogic14x15xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogic8xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogic9x10x103x12xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogicExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogicWlstExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogicWlstRuntimeConfigurationCapability;
import org.codehaus.cargo.container.weblogic.internal.WebLogicWlstStandaloneLocalConfigurationCapability;
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
            WebLogic8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic9x10x103x12xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic9x10x103x12xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic103x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic9x10x103x12xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic103x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic103x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogicWlstRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic12x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic9x10x103x12xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic12x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic121x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogicWlstStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic121x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicWlstExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic121x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogicWlstRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic122x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogicWlstStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic122x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicWlstExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic122x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogicWlstRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic14x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic14x15xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic14x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicWlstExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic14x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogicWlstRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("weblogic15x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic14x15xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic15x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogicWlstExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("weblogic15x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogicWlstRuntimeConfigurationCapability.class);
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
            WebLogic8xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic8xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("weblogic9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic9xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic9x10x12x14x15xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("weblogic10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic10xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic9x10x12x14x15xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("weblogic103x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic103xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic103x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic9x10x12x14x15xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic103x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogic103xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("weblogic12x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic12xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic12x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic9x10x12x14x15xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("weblogic121x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic121xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic121x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic121xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic121x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogic121xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("weblogic122x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic122xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic122x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic122xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic122x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogic122xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("weblogic14x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic14xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic14x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic14xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic14x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogic122xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("weblogic15x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WebLogic15xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic15x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WebLogic15xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("weblogic15x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WebLogic122xRuntimeConfiguration.class);
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
            WebLogic9x10x12x14x15xCopyingInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("weblogic10x", DeployerType.INSTALLED,
            WebLogic9x10x12x14x15xCopyingInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("weblogic103x", DeployerType.INSTALLED,
            WebLogic9x10x12x14x15xCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("weblogic103x", DeployerType.REMOTE,
            WebLogicWlstRemoteDeployer.class);

        deployerFactory.registerDeployer("weblogic12x", DeployerType.INSTALLED,
            WebLogic9x10x12x14x15xCopyingInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("weblogic121x", DeployerType.INSTALLED,
            WebLogic9x10x12x14x15xCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("weblogic121x", DeployerType.REMOTE,
            WebLogicWlstRemoteDeployer.class);

        deployerFactory.registerDeployer("weblogic122x", DeployerType.INSTALLED,
            WebLogic9x10x12x14x15xCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("weblogic122x", DeployerType.REMOTE,
            WebLogicWlstRemoteDeployer.class);

        deployerFactory.registerDeployer("weblogic14x", DeployerType.INSTALLED,
            WebLogic9x10x12x14x15xCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("weblogic14x", DeployerType.REMOTE,
            WebLogicWlstRemoteDeployer.class);

        deployerFactory.registerDeployer("weblogic15x", DeployerType.INSTALLED,
            WebLogic9x10x12x14x15xCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("weblogic15x", DeployerType.REMOTE,
            WebLogicWlstRemoteDeployer.class);
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
        containerFactory.registerContainer("weblogic103x", ContainerType.REMOTE,
            WebLogic103xRemoteContainer.class);

        containerFactory.registerContainer("weblogic12x", ContainerType.INSTALLED,
            WebLogic12xInstalledLocalContainer.class);

        containerFactory.registerContainer("weblogic121x", ContainerType.INSTALLED,
            WebLogic121xInstalledLocalContainer.class);
        containerFactory.registerContainer("weblogic121x", ContainerType.REMOTE,
            WebLogic121xRemoteContainer.class);

        containerFactory.registerContainer("weblogic122x", ContainerType.INSTALLED,
            WebLogic122xInstalledLocalContainer.class);
        containerFactory.registerContainer("weblogic122x", ContainerType.REMOTE,
            WebLogic122xRemoteContainer.class);

        containerFactory.registerContainer("weblogic14x", ContainerType.INSTALLED,
            WebLogic14xInstalledLocalContainer.class);
        containerFactory.registerContainer("weblogic14x", ContainerType.REMOTE,
            WebLogic14xRemoteContainer.class);

        containerFactory.registerContainer("weblogic15x", ContainerType.INSTALLED,
            WebLogic15xInstalledLocalContainer.class);
        containerFactory.registerContainer("weblogic15x", ContainerType.REMOTE,
            WebLogic15xRemoteContainer.class);
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

        containerCapabilityFactory.registerContainerCapability("weblogic12x",
            J2EEContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("weblogic121x",
            J2EEContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("weblogic122x",
            J2EEContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("weblogic14x",
            J2EEContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("weblogic15x",
            J2EEContainerCapability.class);
    }
}
