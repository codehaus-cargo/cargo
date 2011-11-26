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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.jboss.deployable.JBossWAR;
import org.codehaus.cargo.container.jboss.internal.JBoss4xContainerCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss4xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss42xContainerCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss5xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss71xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xContainerCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBossContainerCapability;
import org.codehaus.cargo.container.jboss.internal.JBossExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBossStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers JBoss support into default factories.
 * 
 * @version $Id$
 */
public class JBossFactoryRegistry extends AbstractFactoryRegistry
{

    /**
     * Registers additional {@link org.codehaus.cargo.container.deployable.Deployable}
     * implementations, that the JBoss containers support, to the given {@link DeployableFactory}.
     * 
     * @param deployableFactory Factory on which to register.
     */
    @Override
    protected void register(DeployableFactory deployableFactory)
    {
        deployableFactory.registerDeployable("jboss3x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss4x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss42x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss5x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss51x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss6x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss61x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss7x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss71x", DeployableType.WAR, JBossWAR.class);
    }

    /**
     * Register configuration capabilities.
     * 
     * @param configurationCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationCapabilityFactory configurationCapabilityFactory)
    {
        configurationCapabilityFactory.registerConfigurationCapability("jboss3x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBossStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss3x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBossExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBossStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBossExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss4x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss4xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss42x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBossStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss42x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBossExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss42x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss4xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss5x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBossStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss5x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBossExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss5x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss5xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss51x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBossStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss51x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBossExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss51x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss5xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss6x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBossStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss6x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBossExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss6x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss5xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss61x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBossStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss61x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBossExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss61x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss5xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss7x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss7xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss7x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss7xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss7x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss7xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss71x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss71xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss71x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss7xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss71x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss7xRuntimeConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("jboss3x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBossStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss3x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBossExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("jboss4x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBossStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss4x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBossExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss4x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss4xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss42x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss42xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss42x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBossExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss42x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss4xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss5x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss5xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss5x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBossExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss5x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss5xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss51x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss51xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss51x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBossExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss51x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss5xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss6x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss6xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss6x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBossExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss6x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss5xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss61x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss6xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss61x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBossExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss61x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss5xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss7x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss7xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss7x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss7xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss7x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss7xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss71x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss71xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss71x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss7xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss71x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss7xRuntimeConfiguration.class);
    }

    /**
     * Register deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("jboss3x", DeployerType.INSTALLED,
            JBossInstalledLocalDeployer.class);

        deployerFactory.registerDeployer("jboss4x", DeployerType.INSTALLED,
            JBossInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss4x", DeployerType.REMOTE,
            JBoss4xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss42x", DeployerType.INSTALLED,
            JBossInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss42x", DeployerType.REMOTE,
            JBoss4xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss5x", DeployerType.INSTALLED,
            JBossInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss5x", DeployerType.REMOTE,
            JBoss5xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss51x", DeployerType.INSTALLED,
            JBossInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss51x", DeployerType.REMOTE,
            JBoss51xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss6x", DeployerType.INSTALLED,
            JBossInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss6x", DeployerType.REMOTE,
            JBoss51xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss61x", DeployerType.INSTALLED,
            JBossInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss61x", DeployerType.REMOTE,
            JBoss51xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss7x", DeployerType.INSTALLED,
            JBoss7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss7x", DeployerType.REMOTE,
            JBoss7xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss71x", DeployerType.INSTALLED,
            JBoss7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss71x", DeployerType.REMOTE,
            JBoss7xRemoteDeployer.class);
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
        containerFactory.registerContainer("jboss3x", ContainerType.INSTALLED,
            JBoss3xInstalledLocalContainer.class);

        containerFactory.registerContainer("jboss4x", ContainerType.INSTALLED,
            JBoss4xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss4x", ContainerType.REMOTE,
            JBoss4xRemoteContainer.class);

        containerFactory.registerContainer("jboss42x", ContainerType.INSTALLED,
            JBoss42xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss42x", ContainerType.REMOTE,
            JBoss42xRemoteContainer.class);

        containerFactory.registerContainer("jboss5x", ContainerType.INSTALLED,
            JBoss5xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss5x", ContainerType.REMOTE,
            JBoss5xRemoteContainer.class);

        containerFactory.registerContainer("jboss51x", ContainerType.INSTALLED,
            JBoss51xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss51x", ContainerType.REMOTE,
            JBoss51xRemoteContainer.class);

        containerFactory.registerContainer("jboss6x", ContainerType.INSTALLED,
            JBoss6xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss6x", ContainerType.REMOTE,
            JBoss6xRemoteContainer.class);

        containerFactory.registerContainer("jboss61x", ContainerType.INSTALLED,
            JBoss61xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss61x", ContainerType.REMOTE,
            JBoss61xRemoteContainer.class);

        containerFactory.registerContainer("jboss7x", ContainerType.INSTALLED,
            JBoss7xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss7x", ContainerType.REMOTE,
            JBoss7xRemoteContainer.class);

        containerFactory.registerContainer("jboss71x", ContainerType.INSTALLED,
            JBoss71xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss71x", ContainerType.REMOTE,
            JBoss71xRemoteContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("jboss3x",
            JBossContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss4x",
            JBoss4xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss42x",
            JBoss42xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss5x",
            JBoss42xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss51x",
            JBoss42xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss6x",
            JBoss42xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss61x",
            JBoss42xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss7x",
            JBoss7xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss71x",
            JBoss7xContainerCapability.class);
    }

}
