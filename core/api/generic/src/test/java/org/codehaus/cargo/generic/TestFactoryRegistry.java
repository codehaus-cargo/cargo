/*
 * ========================================================================
 *
 * Copyright 2006-2008 Vincent Massol.
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
package org.codehaus.cargo.generic;

import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;

/**
 * To be discovered by {@link AbstractFactoryRegistry}.
 * 
 * @version $Id: $
 */
public class TestFactoryRegistry extends AbstractFactoryRegistry
{
    protected void register(DeployableFactory factory)
    {
        factory.registerDeployable("super-container", DeployableType.WAR, SuperContainerWar.class);
    }

    protected void register(ConfigurationCapabilityFactory factory)
    {
        factory.registerConfigurationCapability("super-container", ContainerType.INSTALLED,
            ConfigurationType.STANDALONE, SuperConfigurationCapability.class);
    }

    protected void register(ConfigurationFactory factory) {
    }

    protected void register(DeployerFactory factory) {
    }

    protected void register(PackagerFactory factory) {
    }

    protected void register(ContainerFactory factory) {
    }

    protected void register(ContainerCapabilityFactory factory) {
    }
}
