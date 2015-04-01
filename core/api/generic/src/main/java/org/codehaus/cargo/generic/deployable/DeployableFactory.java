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
package org.codehaus.cargo.generic.deployable;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;

/**
 * Factory to create Deployable instances.
 * 
 */
public interface DeployableFactory
{
    /**
     * Registers a deployable implementation against a container.
     * 
     * @param containerId the container id attached to this deployable class
     * @param deployableType the type to differentiate this deployable from others for the specified
     * container
     * @param deployableClass the deployable implementation class to register
     */
    void registerDeployable(String containerId, DeployableType deployableType,
        Class<? extends Deployable> deployableClass);

    /**
     * Checks if the deployable specified by the deployable type is registered for a specific
     * container.
     * 
     * @param containerId the container id attached to the deployable type class
     * @param deployableType the type to differentiate this deployable from others for the specified
     * container
     * @return true if the specified deployable is already registered or false otherwise
     */
    boolean isDeployableRegistered(String containerId, DeployableType deployableType);

    /**
     * Creates an Deployable instance, wrapping a file or a directory.
     * 
     * @param containerId the container id for which to create the deployable for
     * @param deployableLocation the location of the deployable being wrapped, which must point to
     * a file or a directory (for an expanded deployable)
     * @param deployableType the deployable type to create
     * @return the {@link org.codehaus.cargo.container.deployable.Deployable} instance
     */
    Deployable createDeployable(String containerId, String deployableLocation,
        DeployableType deployableType);
}
