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

import java.util.Map;
import java.util.Set;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.Configuration;

/**
 * Allow instantiating a container by id (e.g. "resin3x").
 * 
 */
public interface ContainerFactory
{
    /**
     * @param containerId the id of the container to register
     * @param containerType the container type to register ({@link ContainerType#INSTALLED},
     *            {@link ContainerType#EMBEDDED} or {@link ContainerType#REMOTE})
     * @param containerClass the container implementation class to register
     */
    void registerContainer(String containerId, ContainerType containerType,
        Class<? extends Container> containerClass);

    /**
     * @param containerId the id of the container for which to retrieve the implementation class
     * @param containerType the container's type ({@link ContainerType#INSTALLED},
     *            {@link ContainerType#EMBEDDED} or {@link ContainerType#REMOTE})
     * @return the container implementation class
     */
    Class<? extends Container> getContainerClass(String containerId, ContainerType containerType);

    /**
     * @param containerId the id of the container to check
     * @param containerType the container type
     * @return true if the specified container and type is already registered or false otherwise
     */
    boolean isContainerRegistered(String containerId, ContainerType containerType);

    /**
     * @return the list of container ids that have been registered as Map.
     */
    Map<String, Set<ContainerType>> getContainerIds();

    /**
     * Create a container instance instantiated using the specified configuration.
     * 
     * @param containerId the name under which the container will be looked up
     * @param containerType the container's type (local installed, local embedded, remote, etc)
     * @param configuration the configuration to pass to the container's constructor
     * @return the container whose class name matches the parameter passed
     */
    Container createContainer(String containerId, ContainerType containerType,
        Configuration configuration);
}
