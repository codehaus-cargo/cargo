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
package org.codehaus.cargo.container.wildfly;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.jboss.deployable.JBossWAR;
import org.codehaus.cargo.container.wildfly.internal.WildFlyContainerCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFlyExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFlyRuntimeConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFly8xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.WildFly9xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Registers JBoss and WildFly support into default factories.
 */
public class WildFlyFactoryRegistry extends AbstractFactoryRegistry
{

    /**
     * Registers additional {@link org.codehaus.cargo.container.deployable.Deployable}
     * implementations, that the WildFly containers support, to the given
     * {@link DeployableFactory}.
     * 
     * @param deployableFactory Factory on which to register.
     */
    @Override
    protected void register(DeployableFactory deployableFactory)
    {
        deployableFactory.registerDeployable("wildfly8x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly9x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly10x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly11x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly12x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly13x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly14x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly15x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly16x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly17x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly18x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly19x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly20x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly21x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly22x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly23x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly24x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly25x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly26x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly27x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly28x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly29x", DeployableType.WAR, JBossWAR.class);
        deployableFactory.registerDeployable("wildfly30x", DeployableType.WAR, JBossWAR.class);
    }

    /**
     * Register configuration capabilities.
     * 
     * @param configurationCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationCapabilityFactory configurationCapabilityFactory)
    {
        configurationCapabilityFactory.registerConfigurationCapability("wildfly8x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly8xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly8x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly8x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly9x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly9x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly9x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly10x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly10x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly10x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly11x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly11x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly11x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly12x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly12x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly12x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly13x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly13x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly13x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly14x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly14x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly14x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly15x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly15x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly15x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly16x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly16x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly16x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly17x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly17x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly17x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly18x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly18x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly18x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly19x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly19x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly19x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly20x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly20x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly20x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly21x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly21x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly21x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly22x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly22x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly22x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly23x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly23x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly23x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly24x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly24x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly24x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly25x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly25x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly25x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly26x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly26x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly26x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly27x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly27x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly27x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly28x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly28x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly28x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly29x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly29x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly29x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);

        configurationCapabilityFactory.registerConfigurationCapability("wildfly30x",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE,
            WildFly9xStandaloneLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly30x",
            ContainerType.INSTALLED, ConfigurationType.EXISTING,
            WildFlyExistingLocalConfigurationCapability.class);
        configurationCapabilityFactory.registerConfigurationCapability("wildfly30x",
            ContainerType.REMOTE, ConfigurationType.RUNTIME,
            WildFlyRuntimeConfigurationCapability.class);
    }

    /**
     * Register configuration factories.
     * 
     * @param configurationFactory Factory on which to register.
     */
    @Override
    protected void register(ConfigurationFactory configurationFactory)
    {
        configurationFactory.registerConfiguration("wildfly8x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly8xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly8x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly8xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly8x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly8xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly9x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly9xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly9x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly9xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly9x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly9xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly10x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly10xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly10x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly10xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly10x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly10xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly11x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly11xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly11x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly11xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly11x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly11xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly12x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly12xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly12x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly12xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly12x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly12xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly13x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly13xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly13x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly13xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly13x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly13xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly14x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly14xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly14x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly14xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly14x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly14xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly15x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly15xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly15x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly15xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly15x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly15xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly16x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly16xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly16x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly16xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly16x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly16xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly17x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly17xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly17x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly17xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly17x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly17xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly18x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly18xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly18x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly18xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly18x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly18xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly19x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly19xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly19x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly19xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly19x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly19xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly20x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly20xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly20x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly20xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly20x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly20xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly21x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly21xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly21x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly21xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly21x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly21xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly22x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly22xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly22x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly22xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly22x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly22xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly23x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly23xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly23x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly23xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly23x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly23xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly24x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly24xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly24x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly24xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly24x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly24xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly25x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly25xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly25x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly25xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly25x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly25xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly26x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly26xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly26x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly26xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly26x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly26xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly27x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly27xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly27x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly27xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly27x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly27xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly28x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly28xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly28x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly28xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly28x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly28xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly29x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly29xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly29x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly29xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly29x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly29xRuntimeConfiguration.class);

        configurationFactory.registerConfiguration("wildfly30x", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, WildFly30xStandaloneLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly30x", ContainerType.INSTALLED,
            ConfigurationType.EXISTING, WildFly30xExistingLocalConfiguration.class);
        configurationFactory.registerConfiguration("wildfly30x", ContainerType.REMOTE,
            ConfigurationType.RUNTIME, WildFly30xRuntimeConfiguration.class);
    }

    /**
     * Register deployer.
     * 
     * @param deployerFactory Factory on which to register.
     */
    @Override
    protected void register(DeployerFactory deployerFactory)
    {
        deployerFactory.registerDeployer("wildfly8x", DeployerType.INSTALLED,
            WildFly8xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly8x", DeployerType.REMOTE,
            WildFly8xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly9x", DeployerType.INSTALLED,
            WildFly9xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly9x", DeployerType.REMOTE,
            WildFly9xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly10x", DeployerType.INSTALLED,
            WildFly10xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly10x", DeployerType.REMOTE,
            WildFly10xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly11x", DeployerType.INSTALLED,
            WildFly11xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly11x", DeployerType.REMOTE,
            WildFly11xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly12x", DeployerType.INSTALLED,
            WildFly12xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly12x", DeployerType.REMOTE,
            WildFly12xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly13x", DeployerType.INSTALLED,
            WildFly13xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly13x", DeployerType.REMOTE,
            WildFly13xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly14x", DeployerType.INSTALLED,
            WildFly14xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly14x", DeployerType.REMOTE,
            WildFly14xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly15x", DeployerType.INSTALLED,
            WildFly15xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly15x", DeployerType.REMOTE,
            WildFly15xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly16x", DeployerType.INSTALLED,
            WildFly16xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly16x", DeployerType.REMOTE,
            WildFly16xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly17x", DeployerType.INSTALLED,
            WildFly17xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly17x", DeployerType.REMOTE,
            WildFly17xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly18x", DeployerType.INSTALLED,
            WildFly18xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly18x", DeployerType.REMOTE,
            WildFly18xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly19x", DeployerType.INSTALLED,
            WildFly19xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly19x", DeployerType.REMOTE,
            WildFly19xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly20x", DeployerType.INSTALLED,
            WildFly20xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly20x", DeployerType.REMOTE,
            WildFly20xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly21x", DeployerType.INSTALLED,
            WildFly21xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly21x", DeployerType.REMOTE,
            WildFly21xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly22x", DeployerType.INSTALLED,
            WildFly22xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly22x", DeployerType.REMOTE,
            WildFly22xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly23x", DeployerType.INSTALLED,
            WildFly23xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly23x", DeployerType.REMOTE,
            WildFly23xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly24x", DeployerType.INSTALLED,
            WildFly24xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly24x", DeployerType.REMOTE,
            WildFly24xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly25x", DeployerType.INSTALLED,
            WildFly25xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly25x", DeployerType.REMOTE,
            WildFly25xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly26x", DeployerType.INSTALLED,
            WildFly26xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly26x", DeployerType.REMOTE,
            WildFly26xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly27x", DeployerType.INSTALLED,
            WildFly27xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly27x", DeployerType.REMOTE,
            WildFly27xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly28x", DeployerType.INSTALLED,
            WildFly28xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly28x", DeployerType.REMOTE,
            WildFly28xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly29x", DeployerType.INSTALLED,
            WildFly29xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly29x", DeployerType.REMOTE,
            WildFly29xRemoteDeployer.class);

        deployerFactory.registerDeployer("wildfly30x", DeployerType.INSTALLED,
            WildFly30xInstalledLocalDeployer.class);
        deployerFactory.registerDeployer("wildfly30x", DeployerType.REMOTE,
            WildFly30xRemoteDeployer.class);
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
        containerFactory.registerContainer("wildfly8x", ContainerType.INSTALLED,
            WildFly8xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly8x", ContainerType.REMOTE,
            WildFly8xRemoteContainer.class);

        containerFactory.registerContainer("wildfly9x", ContainerType.INSTALLED,
            WildFly9xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly9x", ContainerType.REMOTE,
            WildFly9xRemoteContainer.class);

        containerFactory.registerContainer("wildfly10x", ContainerType.INSTALLED,
            WildFly10xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly10x", ContainerType.REMOTE,
            WildFly10xRemoteContainer.class);

        containerFactory.registerContainer("wildfly11x", ContainerType.INSTALLED,
            WildFly11xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly11x", ContainerType.REMOTE,
            WildFly11xRemoteContainer.class);

        containerFactory.registerContainer("wildfly12x", ContainerType.INSTALLED,
            WildFly12xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly12x", ContainerType.REMOTE,
            WildFly12xRemoteContainer.class);

        containerFactory.registerContainer("wildfly13x", ContainerType.INSTALLED,
            WildFly13xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly13x", ContainerType.REMOTE,
            WildFly13xRemoteContainer.class);

        containerFactory.registerContainer("wildfly14x", ContainerType.INSTALLED,
            WildFly14xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly14x", ContainerType.REMOTE,
            WildFly14xRemoteContainer.class);

        containerFactory.registerContainer("wildfly15x", ContainerType.INSTALLED,
            WildFly15xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly15x", ContainerType.REMOTE,
            WildFly15xRemoteContainer.class);

        containerFactory.registerContainer("wildfly16x", ContainerType.INSTALLED,
            WildFly16xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly16x", ContainerType.REMOTE,
            WildFly16xRemoteContainer.class);

        containerFactory.registerContainer("wildfly17x", ContainerType.INSTALLED,
            WildFly17xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly17x", ContainerType.REMOTE,
            WildFly17xRemoteContainer.class);

        containerFactory.registerContainer("wildfly18x", ContainerType.INSTALLED,
            WildFly18xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly18x", ContainerType.REMOTE,
            WildFly18xRemoteContainer.class);

        containerFactory.registerContainer("wildfly19x", ContainerType.INSTALLED,
            WildFly19xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly19x", ContainerType.REMOTE,
            WildFly19xRemoteContainer.class);

        containerFactory.registerContainer("wildfly20x", ContainerType.INSTALLED,
            WildFly20xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly20x", ContainerType.REMOTE,
            WildFly20xRemoteContainer.class);

        containerFactory.registerContainer("wildfly21x", ContainerType.INSTALLED,
            WildFly21xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly21x", ContainerType.REMOTE,
            WildFly21xRemoteContainer.class);

        containerFactory.registerContainer("wildfly22x", ContainerType.INSTALLED,
            WildFly22xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly22x", ContainerType.REMOTE,
            WildFly22xRemoteContainer.class);

        containerFactory.registerContainer("wildfly23x", ContainerType.INSTALLED,
            WildFly23xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly23x", ContainerType.REMOTE,
            WildFly23xRemoteContainer.class);

        containerFactory.registerContainer("wildfly24x", ContainerType.INSTALLED,
            WildFly24xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly24x", ContainerType.REMOTE,
            WildFly24xRemoteContainer.class);

        containerFactory.registerContainer("wildfly25x", ContainerType.INSTALLED,
            WildFly25xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly25x", ContainerType.REMOTE,
            WildFly25xRemoteContainer.class);

        containerFactory.registerContainer("wildfly26x", ContainerType.INSTALLED,
            WildFly26xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly26x", ContainerType.REMOTE,
            WildFly26xRemoteContainer.class);

        containerFactory.registerContainer("wildfly27x", ContainerType.INSTALLED,
            WildFly27xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly27x", ContainerType.REMOTE,
            WildFly27xRemoteContainer.class);

        containerFactory.registerContainer("wildfly28x", ContainerType.INSTALLED,
            WildFly28xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly28x", ContainerType.REMOTE,
            WildFly28xRemoteContainer.class);

        containerFactory.registerContainer("wildfly29x", ContainerType.INSTALLED,
            WildFly29xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly29x", ContainerType.REMOTE,
            WildFly29xRemoteContainer.class);

        containerFactory.registerContainer("wildfly30x", ContainerType.INSTALLED,
            WildFly30xInstalledLocalContainer.class);
        containerFactory.registerContainer("wildfly30x", ContainerType.REMOTE,
            WildFly30xRemoteContainer.class);
    }

    /**
     * Register container capabilities.
     * 
     * @param containerCapabilityFactory Factory on which to register.
     */
    @Override
    protected void register(ContainerCapabilityFactory containerCapabilityFactory)
    {
        containerCapabilityFactory.registerContainerCapability("wildfly8x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly9x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly10x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly11x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly12x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly13x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly14x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly15x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly16x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly17x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly18x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly19x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly20x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly21x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly22x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly23x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly24x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly25x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly26x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly27x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly28x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly29x",
            WildFlyContainerCapability.class);
        containerCapabilityFactory.registerContainerCapability("wildfly30x",
            WildFlyContainerCapability.class);
    }

}
