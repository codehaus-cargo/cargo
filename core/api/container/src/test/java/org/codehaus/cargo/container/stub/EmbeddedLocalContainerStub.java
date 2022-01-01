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

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Mock for {@link EmbeddedLocalContainer}. We need a static class rather than using a dynamic mock
 * (which we could get using JMock for example) for when we're testing factory classes which create
 * an object out of a class name.
 */
public class EmbeddedLocalContainerStub
    extends AbstractLocalContainerStub implements EmbeddedLocalContainer
{
    /**
     * Dummy id.
     */
    public static final String ID = "myEmbeddedLocalContainer";

    /**
     * Dummy name.
     */
    public static final String NAME = "My Embedded Local Container";

    /**
     * Classloader.
     */
    private ClassLoader classLoader;

    /**
     * Allows creating a container with no configuration for test that do not require a
     * configuration.
     */
    public EmbeddedLocalContainerStub()
    {
        this(null);
    }

    /**
     * Saves the configuration and sets the id and name. {@inheritDoc}
     * @param configuration Container configuration.
     */
    public EmbeddedLocalContainerStub(LocalConfiguration configuration)
    {
        super(configuration);
        setId(ID);
        setName(NAME);
    }

    /**
     * {@inheritDoc}
     * @return {@link ContainerType#EMBEDDED}
     */
    @Override
    public ContainerType getType()
    {
        return ContainerType.EMBEDDED;
    }

    /**
     * {@inheritDoc}
     * @param classLoader Embedded local container's classloader.
     */
    @Override
    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    /**
     * {@inheritDoc}
     * @return Embedded local container's classloader.
     */
    @Override
    public ClassLoader getClassLoader()
    {
        ClassLoader result;

        if (this.classLoader == null)
        {
            result = getClass().getClassLoader();
        }
        else
        {
            result = this.classLoader;
        }
        return result;
    }
}
