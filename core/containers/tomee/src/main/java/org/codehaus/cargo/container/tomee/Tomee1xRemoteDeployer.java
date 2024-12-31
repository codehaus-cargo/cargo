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
package org.codehaus.cargo.container.tomee;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.tomee.internal.AbstractTomeeRemoteDeployer;

/**
 * A special TomEE 1.x manager-based deployer to perform deployment to a remote container.
 */
public class Tomee1xRemoteDeployer extends AbstractTomeeRemoteDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractTomeeRemoteDeployer#AbstractTomeeRemoteDeployer(org.codehaus.cargo.container.RemoteContainer)
     */
    public Tomee1xRemoteDeployer(RemoteContainer container)
    {
        super(container);
    }
}
