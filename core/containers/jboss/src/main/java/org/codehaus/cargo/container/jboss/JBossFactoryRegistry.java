/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import org.codehaus.cargo.container.jboss.internal.JBoss5x6xContainerCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss5xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss5x6xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss5xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss6xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss6xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss71xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss71xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss72xContainerCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss72xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss75xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xContainerCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss7xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss3xContainerCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss42xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss42xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss3x4xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss3x4xStandaloneLocalConfigurationCapability;
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
        deployableFactory.registerDeployable("jboss72x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss73x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss74x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("jboss75x", DeployableType.WAR, JBossWAR.class);
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
            JBoss3x4xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss3x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss3x4xExistingLocalConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss4x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss3x4xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss4x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss3x4xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss4x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss4xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss42x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss42xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss42x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss42xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss42x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss4xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss5x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss5xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss5x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss5xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss5x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss5x6xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss51x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss5xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss51x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss5xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss51x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss5x6xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss6x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss6xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss6x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss6xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss6x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss5x6xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss61x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss6xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss61x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss6xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss61x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss5x6xRuntimeConfigurationCapability.class);

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
            JBoss71xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss71x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss7xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss72x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss72xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss72x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss71xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss72x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss7xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss73x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss72xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss73x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss71xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss73x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss7xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss74x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss72xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss74x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss71xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss74x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            JBoss7xRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("jboss75x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            JBoss75xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss75x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            JBoss71xExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("jboss75x",
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
            ConfigurationType.STANDALONE, JBoss3x4xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss3x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss3x4xExistingLocalConfiguration.class);

        configurationFactory.registerConfiguration("jboss4x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss3x4xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss4x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss3x4xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss4x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss4xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss42x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss42xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss42x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss42xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss42x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss4xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss5x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss5xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss5x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss5xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss5x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss5x6xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss51x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss51xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss51x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss5xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss51x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss5x6xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss6x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss6xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss6x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss6xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss6x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss5x6xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss61x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss6xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss61x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss6xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss61x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss5x6xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss7x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss7xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss7x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss7xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss7x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss7xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss71x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss71xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss71x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss71xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss71x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss7xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss72x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss72xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss72x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss71xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss72x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss7xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss73x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss73xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss73x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss71xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss73x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss7xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss74x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss73xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss74x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss71xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss74x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, JBoss7xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("jboss75x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, JBoss75xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss75x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, JBoss71xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("jboss75x", ContainerType.REMOTE,
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
            JBoss51x6xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss6x", DeployerType.INSTALLED,
            JBossInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss6x", DeployerType.REMOTE,
            JBoss51x6xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss61x", DeployerType.INSTALLED,
            JBossInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss61x", DeployerType.REMOTE,
            JBoss51x6xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss7x", DeployerType.INSTALLED,
            JBoss7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss7x", DeployerType.REMOTE,
            JBoss7xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss71x", DeployerType.INSTALLED,
            JBoss7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss71x", DeployerType.REMOTE,
            JBoss7xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss72x", DeployerType.INSTALLED,
            JBoss7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss72x", DeployerType.REMOTE,
            JBoss7xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss73x", DeployerType.INSTALLED,
            JBoss7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss73x", DeployerType.REMOTE,
            JBoss7xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss74x", DeployerType.INSTALLED,
            JBoss7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss74x", DeployerType.REMOTE,
            JBoss7xRemoteDeployer.class);

        deployerFactory.registerDeployer("jboss75x", DeployerType.INSTALLED,
            JBoss7xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("jboss75x", DeployerType.REMOTE,
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

        containerFactory.registerContainer("jboss72x", ContainerType.INSTALLED,
            JBoss72xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss72x", ContainerType.REMOTE,
            JBoss72xRemoteContainer.class);

        containerFactory.registerContainer("jboss73x", ContainerType.INSTALLED,
            JBoss73xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss73x", ContainerType.REMOTE,
            JBoss73xRemoteContainer.class);

        containerFactory.registerContainer("jboss74x", ContainerType.INSTALLED,
            JBoss74xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss74x", ContainerType.REMOTE,
            JBoss74xRemoteContainer.class);

        containerFactory.registerContainer("jboss75x", ContainerType.INSTALLED,
            JBoss75xInstalledLocalContainer.class);
        containerFactory.registerContainer("jboss75x", ContainerType.REMOTE,
            JBoss75xRemoteContainer.class);
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
            JBoss3xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss4x",
            JBoss4xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss42x",
            JBoss4xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss5x",
            JBoss5x6xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss51x",
            JBoss5x6xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss6x",
            JBoss5x6xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss61x",
            JBoss5x6xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss7x",
            JBoss7xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss71x",
            JBoss7xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss72x",
            JBoss72xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss73x",
            JBoss72xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss74x",
            JBoss72xContainerCapability.class);

        containerCapabilityFactory.registerContainerCapability("jboss75x",
            JBoss72xContainerCapability.class);
    }

}
