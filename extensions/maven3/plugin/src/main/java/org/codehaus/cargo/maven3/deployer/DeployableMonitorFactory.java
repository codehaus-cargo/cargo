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
package org.codehaus.cargo.maven3.deployer;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.maven3.configuration.Deployable;

/**
 * Create a {@link DeployableMonitor} knowing how to check status of deployable.
 */
public interface DeployableMonitorFactory
{

    /**
     * Create a {@link DeployableMonitor} instance which is able to check status of deployable
     * using passed parameters, for example if Deployable has complete URL defined then is used
     * URLDeployableMonitor.
     * 
     * @param container The container for which we need to check status of deployable.
     * @param deployable The deployable to be checked.
     * @return the deployable monitor instance
     */
    DeployableMonitor createDeployableMonitor(Container container, Deployable deployable);
}
