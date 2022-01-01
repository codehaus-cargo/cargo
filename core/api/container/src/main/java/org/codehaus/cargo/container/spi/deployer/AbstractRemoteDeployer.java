/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.container.spi.deployer;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.deployer.DeployerType;

/**
 * Base deployer to deploy to containers without any reference to where the container is installed
 * (be it on the same machine or on another one).
 */
public abstract class AbstractRemoteDeployer extends AbstractDeployer
{
    /**
     * @param container the remote container into which to perform deployment operations
     */
    public AbstractRemoteDeployer(Container container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeployerType getType()
    {
        return DeployerType.REMOTE;
    }
}
