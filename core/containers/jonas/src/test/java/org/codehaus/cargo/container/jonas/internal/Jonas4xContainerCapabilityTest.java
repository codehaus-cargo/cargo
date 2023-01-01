/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas.internal;

import junit.framework.TestCase;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.deployable.DeployableType;

/**
 * Unit tests for {@link Jonas4xContainerCapabilityTest}.
 */
public class Jonas4xContainerCapabilityTest extends TestCase
{
    /**
     * Container capability.
     */
    protected ContainerCapability capability;

    /**
     * Creates the test container capability. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.capability = new Jonas4xContainerCapability();
    }

    /**
     * test the list of Supported Deployable Types
     */
    public void testSupportedDeployableTypes()
    {
        testSupportedDeployableType(DeployableType.FILE);
        testSupportedDeployableType(DeployableType.WAR);
        testSupportedDeployableType(DeployableType.RAR);
        testSupportedDeployableType(DeployableType.EJB);
        testSupportedDeployableType(DeployableType.EAR);
    }

    /**
     * test one DeployableType
     * @param type Type to test
     */
    private void testSupportedDeployableType(DeployableType type)
    {
        assertTrue("DeployableType " + type + " is not supported", capability
            .supportsDeployableType(type));
    }
}
