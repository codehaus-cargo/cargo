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
package org.codehaus.cargo.container.spi;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;

/**
 * Base implementation of a remote container.
 * 
 */
public abstract class AbstractRemoteContainer extends AbstractContainer implements RemoteContainer
{
    /**
     * The runtime configuration implementation to use.
     */
    private RuntimeConfiguration configuration;

    /**
     * Default constructor.
     * @param configuration the configuration to associate to this container. It can be changed
     * later on by calling {@link #setConfiguration(RuntimeConfiguration)}
     */
    public AbstractRemoteContainer(RuntimeConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     * @see RemoteContainer#setConfiguration(RuntimeConfiguration)
     */
    public void setConfiguration(RuntimeConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     * @see RemoteContainer#getConfiguration()
     */
    public RuntimeConfiguration getConfiguration()
    {
        return this.configuration;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getState()
     */
    public State getState()
    {
        return State.STARTED;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getType()
     */
    public ContainerType getType()
    {
        return ContainerType.REMOTE;
    }
}
