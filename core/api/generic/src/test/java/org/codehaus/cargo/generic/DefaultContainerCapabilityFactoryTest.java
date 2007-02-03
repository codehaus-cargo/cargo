/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.deployable.DeployableType;

/**
 * Unit tests for {@link org.codehaus.cargo.generic.DefaultContainerCapabilityFactory}.
 *
 * @version $Id$
 */
public class DefaultContainerCapabilityFactoryTest extends TestCase
{
    public void testCreateContainerCapability()
    {
        ContainerCapabilityFactory factory = new DefaultContainerCapabilityFactory();
        factory.registerContainerCapability("containerId", J2EEContainerCapability.class);
        ContainerCapability capability = factory.createContainerCapability("containerId");

        assertTrue(capability.supportsDeployableType(DeployableType.EAR));
    }
}
