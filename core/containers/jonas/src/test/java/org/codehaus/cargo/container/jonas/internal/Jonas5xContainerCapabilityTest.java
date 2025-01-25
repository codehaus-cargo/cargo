/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.deployable.DeployableType;

/**
 * Unit tests for {@link Jonas5xContainerCapability}.
 */
public class Jonas5xContainerCapabilityTest extends Jonas4xContainerCapabilityTest
{
    /**
     * Creates the test container capability.
     */
    @BeforeEach
    protected void setUp()
    {
        this.capability = new Jonas5xContainerCapability();
    }

    /**
     * test the list of Supported Deployable Types
     */
    @Test
    @Override
    public void testSupportedDeployableTypes()
    {
        super.testSupportedDeployableTypes();

        testSupportedDeployableType(DeployableType.BUNDLE);
    }

    /**
     * test one DeployableType
     * @param type Type to test
     */
    private void testSupportedDeployableType(DeployableType type)
    {
        Assertions.assertTrue(capability.supportsDeployableType(type),
            "DeployableType " + type + " is not supported");
    }
}
