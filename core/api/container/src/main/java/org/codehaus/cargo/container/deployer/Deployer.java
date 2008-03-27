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
package org.codehaus.cargo.container.deployer;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.util.log.Loggable;

/**
 * API to manage all deployment aspects of {@link Deployable}: deploy, undeploy, start, stop
 * and restart. 
 *  
 * @version $Id$
 */
public interface Deployer extends Loggable
{
    /**
     * Deploy a {@link Deployable} to the running container and make it available for requests.
     *  
     * @param deployable the {@link Deployable} to deploy
     */
    void deploy(Deployable deployable);

    /**
     * Deploy a {@link Deployable} to the running container and make it available for requests.
     * Waits for the {@link Deployable} to be fully deployed before returning.
     *  
     * @param deployable the {@link Deployable} to deploy
     * @param monitor the monitor that checks for deployment status
     */
    void deploy(Deployable deployable, DeployableMonitor monitor);

    /**
     * Undeploy a {@link Deployable} from the running container. The service becomes unavailable
     * for requests.
     *  
     * @param deployable the {@link Deployable} to undeploy
     */
    void undeploy(Deployable deployable);
    
    /**
     * Undeploy a {@link Deployable} to the running container.
     * Waits for the {@link Deployable} to be fully undeployed before returning.
     *  
     * @param deployable the {@link Deployable} to deploy
     * @param monitor the monitor that checks for deployment status
     */
    void undeploy(Deployable deployable, DeployableMonitor monitor);


    /**
     * <p>Redeploy a {@link Deployable} already deployed to the running container. The service
     * becomes available for requests.
     * <p>Note that this method will be unsupported by the {@link Deployer}s based on the
     * {@link org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer}.
     * @param deployable the {@link Deployable} to redeploy
     * @see Deployer#deploy(Deployable)
     * @see Deployer#undeploy(Deployable)
     */
    void redeploy(Deployable deployable);

    /**
     * Starts a {@link Deployable} that is already deployed in the running container but that is
     * not servicing requests.
     *  
     * @param deployable the {@link Deployable} to start
     */
    void start(Deployable deployable);

    /**
     * Stop a {@link Deployable} that is already deployed in the running container in order to 
     * prevent it from servicing requests.
     *  
     * @param deployable the {@link Deployable} to stop
     */
    void stop(Deployable deployable);

    /**
     * @return the deployer's type (local, remote, etc)
     */
    DeployerType getType();
}
