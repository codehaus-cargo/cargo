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
package org.codehaus.cargo.container.deployer;

import org.codehaus.cargo.util.log.Loggable;

/**
 * Monitor the deployment status of a {@link org.codehaus.cargo.container.deployable.Deployable}.
 * 
 * @version $Id$
 */
public interface DeployableMonitor extends Loggable
{
    /**
     * @return a string identifying the deployable that is monitored
     */
    String getDeployableName();

    /**
     * Register a listener that we will warn about the deployment status of the associated 
     * {@link org.codehaus.cargo.container.deployable.Deployable}.
     * 
     * @param listener the listener to register
     */
    void registerListener(DeployableMonitorListener listener);
    
    /**
     * Check the deployment status of the associated
     * {@link org.codehaus.cargo.container.deployable.Deployable} and tell the registered 
     * {@link DeployableMonitorListener} about the status.
     */
    void monitor();

    /**
     * @return the timeout after which we stop monitoring the
     * {@link org.codehaus.cargo.container.deployable.Deployable} 
     *
     */
    long getTimeout();
}
