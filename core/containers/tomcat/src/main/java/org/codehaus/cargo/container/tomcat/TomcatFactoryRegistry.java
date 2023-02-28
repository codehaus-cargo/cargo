/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.packager.PackagerType;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.Tomcat6xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.Tomcat7xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.Tomcat8x9x10x11xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.TomcatExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4x5x6xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.Tomcat7x8x9x10x11xRuntimeConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers Tomcat support into default factories.
 */
public class TomcatFactoryRegistry extends AbstractFactoryRegistry
{

    /**
     * Register deployable factory.
     * 
     * @param deployableFactory Factory on which to register.
     */
    @Override
    protected void register(DeployableFactory deployableFactory)
    {
        deployableFactory.registerDeployable("tomcat5x", DeployableType.WAR,
            TomcatWAR.class);
        deployableFactory.registerDeployable("tomcat6x", DeployableType.WAR,
            TomcatWAR.class);
        deployableFactory.registerDeployable("tomcat7x", DeployableType.WAR,
            TomcatWAR.class);
        deployableFactory.registerDeployable("tomcat8x", DeployableType.WAR,
            TomcatWAR.class);
        deployableFactory.registerDeployable("tomcat9x", DeployableType.WAR,
            TomcatWAR.class);
        deployableFactory.registerDeployable("tomcat10x", DeployableType.WAR,
            TomcatWAR.class);
        deployableFactory.registerDeployable("tomcat11x", DeployableType.WAR,
            TomcatWAR.class);
    }

    /**
     * Register configuration capabilities.
     * 
     * @param configurationCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationCapabilityFactory configurationCapabilityFactory)
    {
        configurationCapabilityFactory.registerConfigurationCapability("tomcat4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat4xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat4x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat4x5x6xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat5xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat5xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat4x5x6xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat6xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat6xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat4x5x6xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat7xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat7xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat7x8x9x10x11xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
                Tomcat8x9x10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
                Tomcat8x9x10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat7x8x9x10x11xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat8x9x10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat9x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat8x9x10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat9x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat9x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat7x8x9x10x11xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat8x9x10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat10x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat8x9x10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat10x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat10x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat7x8x9x10x11xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat11x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat8x9x10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat11x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat8x9x10x11xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat11x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat11x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat11x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat7x8x9x10x11xRuntimeConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("tomcat4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat4xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat4x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat4xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("tomcat5x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat5xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat5x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat5xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat5x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat5x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat5x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat5xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("tomcat6x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat6xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat6x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat6xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat6x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat6x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat6x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat6xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("tomcat7x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat7xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat7x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat7xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat7x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat7x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat7x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat7xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("tomcat8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat8xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat8x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat8xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat8x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat8x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat8xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("tomcat9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat9xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat9x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat9xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat9x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat9x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat9xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("tomcat10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat10xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat10x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat10xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat10x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat10x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat10xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("tomcat11x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat11xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat11x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat11xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat11x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat11x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("tomcat11x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            Tomcat11xRuntimeConfiguration.class);
    }

    /**
     * Register deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("tomcat4x", DeployerType.INSTALLED,
            TomcatCopyingInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("tomcat4x", DeployerType.REMOTE,
            Tomcat4xRemoteDeployer.class);

        deployerFactory.registerDeployer("tomcat5x", DeployerType.INSTALLED,
            TomcatCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("tomcat5x", DeployerType.REMOTE,
            Tomcat5xRemoteDeployer.class);
        deployerFactory.registerDeployer("tomcat5x", DeployerType.EMBEDDED,
            TomcatEmbeddedLocalDeployer.class);

        deployerFactory.registerDeployer("tomcat6x", DeployerType.INSTALLED,
             TomcatCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("tomcat6x", DeployerType.REMOTE,
            Tomcat6xRemoteDeployer.class);
        deployerFactory.registerDeployer("tomcat6x", DeployerType.EMBEDDED,
            TomcatEmbeddedLocalDeployer.class);

        deployerFactory.registerDeployer("tomcat7x", DeployerType.INSTALLED,
             TomcatCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("tomcat7x", DeployerType.REMOTE,
            Tomcat7xRemoteDeployer.class);
        deployerFactory.registerDeployer("tomcat7x", DeployerType.EMBEDDED,
            TomcatEmbeddedLocalDeployer.class);

        deployerFactory.registerDeployer("tomcat8x", DeployerType.INSTALLED,
             TomcatCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("tomcat8x", DeployerType.REMOTE,
            Tomcat8xRemoteDeployer.class);
        deployerFactory.registerDeployer("tomcat8x", DeployerType.EMBEDDED,
            TomcatEmbeddedLocalDeployer.class);

        deployerFactory.registerDeployer("tomcat9x", DeployerType.INSTALLED,
             TomcatCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("tomcat9x", DeployerType.REMOTE,
            Tomcat9xRemoteDeployer.class);
        deployerFactory.registerDeployer("tomcat9x", DeployerType.EMBEDDED,
            TomcatEmbeddedLocalDeployer.class);

        deployerFactory.registerDeployer("tomcat10x", DeployerType.INSTALLED,
             TomcatCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("tomcat10x", DeployerType.REMOTE,
            Tomcat10xRemoteDeployer.class);
        deployerFactory.registerDeployer("tomcat10x", DeployerType.EMBEDDED,
            TomcatEmbeddedLocalDeployer.class);

        deployerFactory.registerDeployer("tomcat11x", DeployerType.INSTALLED,
                TomcatCopyingInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("tomcat11x", DeployerType.REMOTE,
            Tomcat11xRemoteDeployer.class);
        deployerFactory.registerDeployer("tomcat11x", DeployerType.EMBEDDED,
            TomcatEmbeddedLocalDeployer.class);
    }

    /**
     * Register packager.
     * 
     * @param packagerFactory Factory on which to register.
     */
    @Override
    protected void register(PackagerFactory packagerFactory)
    {
        packagerFactory.registerPackager("tomcat4x", PackagerType.DIRECTORY,
            TomcatDirectoryPackager.class);
        packagerFactory.registerPackager("tomcat5x", PackagerType.DIRECTORY,
            TomcatDirectoryPackager.class);
        packagerFactory.registerPackager("tomcat6x", PackagerType.DIRECTORY,
            TomcatDirectoryPackager.class);
        packagerFactory.registerPackager("tomcat7x", PackagerType.DIRECTORY,
            TomcatDirectoryPackager.class);
        packagerFactory.registerPackager("tomcat8x", PackagerType.DIRECTORY,
            TomcatDirectoryPackager.class);
        packagerFactory.registerPackager("tomcat9x", PackagerType.DIRECTORY,
            TomcatDirectoryPackager.class);
        packagerFactory.registerPackager("tomcat10x", PackagerType.DIRECTORY,
            TomcatDirectoryPackager.class);
        packagerFactory.registerPackager("tomcat11x", PackagerType.DIRECTORY,
            TomcatDirectoryPackager.class);
    }

    /**
     * Register container.
     * 
     * @param containerFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerFactory containerFactory)
    {
        containerFactory.registerContainer("tomcat4x", ContainerType.INSTALLED,
            Tomcat4xInstalledLocalContainer.class);
        containerFactory.registerContainer("tomcat4x", ContainerType.REMOTE,
            Tomcat4xRemoteContainer.class);

        containerFactory.registerContainer("tomcat5x", ContainerType.INSTALLED,
            Tomcat5xInstalledLocalContainer.class);
        containerFactory.registerContainer("tomcat5x", ContainerType.REMOTE,
            Tomcat5xRemoteContainer.class);
        containerFactory.registerContainer("tomcat5x", ContainerType.EMBEDDED,
            Tomcat5xEmbeddedLocalContainer.class);

        containerFactory.registerContainer("tomcat6x", ContainerType.INSTALLED,
            Tomcat6xInstalledLocalContainer.class);
        containerFactory.registerContainer("tomcat6x", ContainerType.REMOTE,
            Tomcat6xRemoteContainer.class);
        containerFactory.registerContainer("tomcat6x", ContainerType.EMBEDDED,
            Tomcat6xEmbeddedLocalContainer.class);

        containerFactory.registerContainer("tomcat7x", ContainerType.INSTALLED,
            Tomcat7xInstalledLocalContainer.class);
        containerFactory.registerContainer("tomcat7x", ContainerType.REMOTE,
            Tomcat7xRemoteContainer.class);
        containerFactory.registerContainer("tomcat7x", ContainerType.EMBEDDED,
            Tomcat7xEmbeddedLocalContainer.class);

        containerFactory.registerContainer("tomcat8x", ContainerType.INSTALLED,
            Tomcat8xInstalledLocalContainer.class);
        containerFactory.registerContainer("tomcat8x", ContainerType.REMOTE,
            Tomcat8xRemoteContainer.class);
        containerFactory.registerContainer("tomcat8x", ContainerType.EMBEDDED,
            Tomcat8xEmbeddedLocalContainer.class);

        containerFactory.registerContainer("tomcat9x", ContainerType.INSTALLED,
            Tomcat9xInstalledLocalContainer.class);
        containerFactory.registerContainer("tomcat9x", ContainerType.REMOTE,
            Tomcat9xRemoteContainer.class);
        containerFactory.registerContainer("tomcat9x", ContainerType.EMBEDDED,
            Tomcat9xEmbeddedLocalContainer.class);

        containerFactory.registerContainer("tomcat10x", ContainerType.INSTALLED,
            Tomcat10xInstalledLocalContainer.class);
        containerFactory.registerContainer("tomcat10x", ContainerType.REMOTE,
            Tomcat10xRemoteContainer.class);
        containerFactory.registerContainer("tomcat10x", ContainerType.EMBEDDED,
            Tomcat10xEmbeddedLocalContainer.class);

        containerFactory.registerContainer("tomcat11x", ContainerType.INSTALLED,
            Tomcat11xInstalledLocalContainer.class);
        containerFactory.registerContainer("tomcat11x", ContainerType.REMOTE,
            Tomcat11xRemoteContainer.class);
        containerFactory.registerContainer("tomcat11x", ContainerType.EMBEDDED,
            Tomcat11xEmbeddedLocalContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("tomcat4x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("tomcat5x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("tomcat6x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("tomcat7x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("tomcat8x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("tomcat9x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("tomcat10x",
            ServletContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("tomcat11x",
            ServletContainerCapability.class);
    }

}
