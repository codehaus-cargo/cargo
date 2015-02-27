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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.packager.PackagerType;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5x6xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.Tomcat7x8xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.TomcatExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.internal.TomcatRuntimeConfigurationCapability;
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
 * 
 * @version $Id$
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
            TomcatRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat5x6xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat5x6xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat5x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            TomcatRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat5x6xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat5x6xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat6x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            TomcatRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat7x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            TomcatRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            Tomcat7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.EMBEDDED, ConfigurationType.STANDALONE,
            Tomcat7x8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.EMBEDDED, ConfigurationType.EXISTING,
            TomcatExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("tomcat8x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            TomcatRuntimeConfigurationCapability.class);
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
            TomcatRuntimeConfiguration.class);

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
            TomcatRuntimeConfiguration.class);

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
            TomcatRuntimeConfiguration.class);

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
            TomcatRuntimeConfiguration.class);

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
            TomcatRuntimeConfiguration.class);
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
    }

}
