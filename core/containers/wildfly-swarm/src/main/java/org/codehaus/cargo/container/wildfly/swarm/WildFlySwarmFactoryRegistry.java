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
package org.codehaus.cargo.container.wildfly.swarm;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.jboss.deployable.JBossWAR;
import org.codehaus.cargo.container.wildfly.swarm.internal.WildFlySwarmContainerCapability;
import org.codehaus.cargo.container.wildfly.swarm.internal.WildFlySwarmStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.generic.AbstractFactoryRegistry;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;

/**
 * Factory registry for WildFly Swarm containers.
 * */
public class WildFlySwarmFactoryRegistry extends AbstractFactoryRegistry
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected void register(DeployableFactory factory)
    {
        //no deployments are supported
        factory.registerDeployable(WildFlySwarm2017xInstalledLocalContainer.CONTAINER_ID,
                DeployableType.WAR,
                JBossWAR.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void register(ConfigurationCapabilityFactory factory)
    {
        factory.registerConfigurationCapability(
            WildFlySwarm2017xInstalledLocalContainer.CONTAINER_ID,
            ContainerType.INSTALLED,
            ConfigurationType.STANDALONE,
            WildFlySwarmStandaloneLocalConfigurationCapability.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void register(ConfigurationFactory factory)
    {
        factory.registerConfiguration(
            WildFlySwarm2017xInstalledLocalContainer.CONTAINER_ID,
            ContainerType.INSTALLED,
            ConfigurationType.STANDALONE,
            WildFlySwarmStandaloneLocalConfiguration.class
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void register(DeployerFactory factory)
    {
        //no deployments are supported
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void register(PackagerFactory factory)
    {
        //no packages are supported
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void register(ContainerFactory factory)
    {
        factory.registerContainer(
            WildFlySwarm2017xInstalledLocalContainer.CONTAINER_ID,
            ContainerType.INSTALLED,
            WildFlySwarm2017xInstalledLocalContainer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void register(ContainerCapabilityFactory factory)
    {
        factory.registerContainerCapability(
            WildFlySwarm2017xInstalledLocalContainer.CONTAINER_ID,
            WildFlySwarmContainerCapability.class);
    }
}
