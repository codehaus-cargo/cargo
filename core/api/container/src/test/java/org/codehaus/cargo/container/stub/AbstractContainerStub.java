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
package org.codehaus.cargo.container.stub;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.NullLogger;

/**
 * Code common to all container stubs.
 */
public abstract class AbstractContainerStub implements Container
{
    /**
     * Container id.
     */
    private String id;

    /**
     * Container name.
     */
    private String name;

    /**
     * Container capability.
     */
    private ContainerCapability capability;

    /**
     * Container state.
     */
    private State state;

    /**
     * {@inheritDoc}
     * @return Container id.
     */
    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * Sets the container id.
     * @param id Container id to set.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * {@inheritDoc}
     * @return Container name.
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * Sets the container name.
     * @param name Container name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     * @return Container capability.
     */
    @Override
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * Sets the container capability.
     * @param capability Container capability to set.
     */
    public void setCapability(ContainerCapability capability)
    {
        this.capability = capability;
    }

    /**
     * {@inheritDoc}
     * @return The last saved container state.
     */
    @Override
    public State getState()
    {
        return this.state;
    }

    /**
     * Sets the container state.
     * @param state Container state to save.
     */
    public void setState(State state)
    {
        this.state = state;
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     * @param logger Ignored.
     */
    @Override
    public void setLogger(Logger logger)
    {
        // Voluntarily not doing anything for testing
    }

    /**
     * {@inheritDoc}
     * @return {@link NullLogger}
     */
    @Override
    public Logger getLogger()
    {
        return new NullLogger();
    }
}
