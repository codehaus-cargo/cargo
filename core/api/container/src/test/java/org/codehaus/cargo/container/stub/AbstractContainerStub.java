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
package org.codehaus.cargo.container.stub;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.NullLogger;

/**
 * Code common to all container stubs.
 * 
 * @version $Id$
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
     * {@inheritdoc}
     * @return Container id.
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * {@inheritdoc}
     * @param id Container id to set.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * {@inheritdoc}
     * @return Container name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * {@inheritdoc}
     * @param name Container name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * {@inheritdoc}
     * @return Container capability.
     */
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * {@inheritdoc}
     * @param capability Container capability to set.
     */
    public void setCapability(ContainerCapability capability)
    {
        this.capability = capability;
    }

    /**
     * {@inheritdoc}
     * @return The last saved container state.
     */
    public State getState()
    {
        return this.state;
    }

    /**
     * {@inheritdoc}
     * @param state Container state to save.
     */
    public void setState(State state)
    {
        this.state = state;
    }

    /**
     * Doesn't do anything. {@inheritdoc}
     * @param logger Ignored.
     */
    public void setLogger(Logger logger)
    {
        // Voluntarily not doing anything for testing
    }

    /**
     * {@inheritdoc}
     * @return {@link NullLogger}
     */
    public Logger getLogger()
    {
        return new NullLogger();
    }
}
