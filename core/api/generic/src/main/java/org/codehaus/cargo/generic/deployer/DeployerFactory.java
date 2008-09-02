/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.generic.deployer;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;

/**
 * Create a {@link Deployer} knowing the container to which it is attached and the deployer type.
 * 
 * @version $Id$
 */
public interface DeployerFactory
{
    /**
     * Registers a {@link org.codehaus.cargo.container.deployer.Deployer} implementation.
     * 
     * @param containerId the container attached to this deployerClass
     * @param deployerType the deployer's type (local, remote, etc)
     * @param deployerClass the deployer implementation class to register
     */
    void registerDeployer(String containerId, DeployerType deployerType, Class deployerClass);

    /**
     * @param containerId the container attached to this deployer class
     * @param deployerType the type to differentiate this deployer from others for the specified
     *        container
     * @return true if the specified deployer is already registered or false otherwise 
     */
    boolean isDeployerRegistered(String containerId, DeployerType deployerType);

    /**
     * @param containerId the container attached to this deployer class
     * @param deployerType the deployer's type
     * @return the deployer implementation class
     */
    Class getDeployerClass(String containerId, DeployerType deployerType);

    /**
     * Create a {@link Deployer} instance matching the specified container and type.
     * 
     * @param container the container for which we need to create a deployer instance
     * @param deployerType the deployer's type (local, remote, etc)
     * @return the deployer instance
     */
    Deployer createDeployer(Container container, DeployerType deployerType);

    /**
     * Create a {@link Deployer} instance whose type matches the container's type. For example
     * this creates a local deployer when the container's instance passed is a local container and
     * a remote deployer when the container's instance passed is a remote container.
     *
     * @param container the container for which we need to create a deployer instance
     * @return the deployer instance
     */
    Deployer createDeployer(Container container);
}
