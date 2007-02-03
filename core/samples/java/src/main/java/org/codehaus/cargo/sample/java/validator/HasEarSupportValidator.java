/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.sample.java.validator;

import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.generic.ContainerCapabilityFactory;
import org.codehaus.cargo.generic.DefaultContainerCapabilityFactory;

/**
 * Validate that a container supports EAR archives.
 * 
 * @version $Id$
 */
public class HasEarSupportValidator implements Validator
{
    private ContainerCapabilityFactory factory = new DefaultContainerCapabilityFactory();

    public boolean validate(String containerId, ContainerType type)
    {
        return this.factory.createContainerCapability(containerId).supportsDeployableType(
            DeployableType.EAR);
    }
}
