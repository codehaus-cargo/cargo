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

/**
 * Listener that is triggered when the deployment status of a 
 * {@link org.codehaus.cargo.container.deployable.Deployable} changes.
 * 
 * @version $Id$
 */
public interface DeployableMonitorListener
{
    /**
     * The {@link org.codehaus.cargo.container.deployable.Deployable} has finished deploying.
     */
    void deployed();

    /**
     * The {@link org.codehaus.cargo.container.deployable.Deployable} has finished undeploying.
     */
    void undeployed();
}
