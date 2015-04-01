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

import org.codehaus.cargo.container.ContainerCapability;

/**
 * Allow finding a Container's capability by container id.
 * 
 */
public interface ContainerCapabilityFactory
{
    /**
     * @param containerId the id of the container to register
     * @param containerCapabilityClass the container capability implementation class to register
     */
    void registerContainerCapability(String containerId,
        Class<? extends ContainerCapability> containerCapabilityClass);

    /**
     * Create a {@link ContainerCapability} instance.
     * 
     * @param containerId the id under which the container will be looked up
     * @return the container capability instance matching the parameter passed
     */
    ContainerCapability createContainerCapability(String containerId);
}
