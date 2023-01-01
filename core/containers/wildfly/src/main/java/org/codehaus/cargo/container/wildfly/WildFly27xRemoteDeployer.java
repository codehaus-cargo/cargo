/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.wildfly.internal.AbstractWildFlyRemoteDeployer;

/**
 * Remote deployer that uses the remote API to deploy to WildFly 27.x.
 */
public class WildFly27xRemoteDeployer extends AbstractWildFlyRemoteDeployer
{
    /**
     * @param container the container containing the configuration to use to find the deployer
     * properties such as url, user name and password to use to connect to the deployer
     */
    public WildFly27xRemoteDeployer(RemoteContainer container)
    {
        super(container);
    }
}
