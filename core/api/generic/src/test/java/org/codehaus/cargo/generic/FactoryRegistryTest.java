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
package org.codehaus.cargo.generic;

import junit.framework.TestCase;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;

/**
 * Tests the discovery behavior.
 * 
 * The class name can't be {@code AbstractFactoryRegistryTest} or else the test will be skipped.
 * 
 * @version $Id$
 */
public class FactoryRegistryTest extends TestCase
{
    /**
     * Test the {@link DefaultDeployableFactory}.
     */
    public void testDefaultDeployableFactory()
    {
        DefaultDeployableFactory f = new DefaultDeployableFactory(getClass().getClassLoader());
        Deployable war = f.createDeployable("super-container", ".", DeployableType.WAR);
        assertTrue(war instanceof SuperContainerWar);
    }

    /**
     * Test the {@link ConfigurationCapabilityFactory}.
     */
    public void testConfigurationCapabilityFactory()
    {
        ConfigurationCapabilityFactory f = new DefaultConfigurationCapabilityFactory(getClass()
            .getClassLoader());
        ConfigurationCapability cc = f.createConfigurationCapability("super-container",
            ContainerType.INSTALLED, ConfigurationType.STANDALONE);
        assertTrue(cc instanceof SuperConfigurationCapability);
    }
}
