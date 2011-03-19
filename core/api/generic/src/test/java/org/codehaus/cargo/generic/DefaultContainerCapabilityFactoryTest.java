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

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;

/**
 * Unit tests for {@link org.codehaus.cargo.generic.DefaultContainerCapabilityFactory}.
 * 
 * @version $Id$
 */
public class DefaultContainerCapabilityFactoryTest extends TestCase
{
    /**
     * Container capability factory.
     */
    private ContainerCapabilityFactory factory;

    /**
     * Creates the container capability factory. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        this.factory = new DefaultContainerCapabilityFactory();
    }

    /**
     * Test container capability.
     */
    public void testCreateContainerCapability()
    {
        factory.registerContainerCapability("containerId", J2EEContainerCapability.class);
        ContainerCapability capability = factory.createContainerCapability("containerId");

        assertTrue(capability.supportsDeployableType(DeployableType.EAR));
    }
}
