/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.NullLogger;

/**
 * Code common to all container stubs.
 *
 * @version $Id$
 */
public abstract class AbstractContainerStub implements Container
{
    private String id;
    private String name;
    private ContainerCapability capability;
    private State state;

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    public void setCapability(ContainerCapability capability)
    {
        this.capability = capability;
    }

    public State getState()
    {
        return this.state;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public void setLogger(Logger logger)
    {
        // Voluntarily not doing anything for testing
    }

    public Logger getLogger()
    {
        return new NullLogger();
    }
}
