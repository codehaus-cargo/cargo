/*
 * ========================================================================
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
package org.codehaus.cargo.generic.deployable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Unit tests for {@link DefaultDeployableFactory}.
 */
public class DefaultDeployableFactoryTest
{
    /**
     * Test {@link WAR} deployable creation.
     */
    @Test
    public void testCreateWARDeployable()
    {
        DeployableFactory factory = new DefaultDeployableFactory();
        Deployable deployable = factory.createDeployable("any container", "some/deployable",
            DeployableType.WAR);
        Assertions.assertTrue(deployable instanceof WAR);
    }
}
