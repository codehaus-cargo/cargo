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

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;

/**
 * Mock for {@link RemoteContainer}. We need a static class rather than using a dynamic mock (which
 * we could get using JMock for example) for when we're testing factory classes which create an
 * object out of a class name.
 * 
 * @version $Id$
 */
public class RemoteContainerStub extends AbstractContainerStub implements RemoteContainer
{
    /**
     * Dummy id.
     */
    public static final String ID = "myRemoteContainer";

    /**
     * Dummy name.
     */
    public static final String NAME = "My Remote Container";

    /**
     * Configuration.
     */
    private RuntimeConfiguration configuration;

    /**
     * Allows creating a container with no configuration for test that do not require a
     * configuration.
     */
    public RemoteContainerStub()
    {
        this(null);
    }

    /**
     * Saves the configuration and sets the id and name. {@inheritDoc}
     * @param configuration Container configuration.
     */
    public RemoteContainerStub(RuntimeConfiguration configuration)
    {
        setConfiguration(configuration);
        setId(ID);
        setName(NAME);
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @return Nothing.
     */
    @Override
    public ContainerCapability getCapability()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @return {@link ContainerType#REMOTE}
     */
    public ContainerType getType()
    {
        return ContainerType.REMOTE;
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration to set.
     */
    public void setConfiguration(RuntimeConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     * @return Previously set configuration.
     */
    public RuntimeConfiguration getConfiguration()
    {
        return this.configuration;
    }
}
