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
package org.codehaus.cargo.generic.deployable;

import junit.framework.TestCase;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Unit tests for {@link DefaultDeployableFactory}.
 *
 * @version $Id: $
 */
public class DefaultDeployableFactoryTest extends TestCase
{
    public void testCreateWARDeployable()
    {
        DeployableFactory factory = new DefaultDeployableFactory();
        Deployable deployable = factory.createDeployable("any container", "some/deployable",
            DeployableType.WAR);
        assertTrue(deployable instanceof WAR);
    }
}
