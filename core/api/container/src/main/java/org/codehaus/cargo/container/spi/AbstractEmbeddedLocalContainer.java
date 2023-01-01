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
package org.codehaus.cargo.container.spi;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Default container implementation that all local embedded container implementations must extend.
 */
public abstract class AbstractEmbeddedLocalContainer
    extends AbstractLocalContainer implements EmbeddedLocalContainer
{
    /**
     * Classloader to use for loading the Embedded container's classes.
     */
    private ClassLoader classLoader;

    /**
     * {@inheritDoc}
     * @see AbstractLocalContainer#AbstractLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public AbstractEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * @param classLoader the custom classloader to use for loading the Embedded container's
     * classes.
     */
    @Override
    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    /**
     * @return the custom classloader to use for loading the Embedded container's classes.
     */
    @Override
    public ClassLoader getClassLoader()
    {
        ClassLoader cl = this.classLoader;

        if (this.classLoader == null)
        {
            cl = getClass().getClassLoader();
        }

        return cl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startInternal() throws Exception
    {
        doStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopInternal() throws Exception
    {
        doStop();
    }

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#start()} that all
     * containers extending this class must implement.
     * 
     * @throws Exception if any error is raised during the container start
     */
    protected abstract void doStart() throws Exception;

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#stop()} that all
     * containers extending this class must implement.
     * 
     * @throws Exception if any error is raised during the container stop
     */
    protected abstract void doStop() throws Exception;

    /**
     * Calls <code>System.gc()</code> after container has stopped. {@inheritDoc}
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        super.waitForCompletion(waitForStarting);

        if (!waitForStarting)
        {
            // Many container do not fully stop even after having destroyed all their sockets;
            // as a result call GC for embedded containers
            System.gc();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerType getType()
    {
        return ContainerType.EMBEDDED;
    }
}
