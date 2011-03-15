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
package org.codehaus.cargo.container.spi;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Default container implementation that all local embedded container implementations must extend.
 * 
 * @version $Id$
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
    public void setClassLoader(ClassLoader classLoader)
    {
        this.classLoader = classLoader;
    }

    /**
     * @return the custom classloader to use for loading the Embedded container's classes.
     */
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
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#startInternal()
     */
    @Override
    protected final void startInternal() throws Exception
    {
        doStart();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#stopInternal()
     */
    @Override
    protected final void stopInternal() throws Exception
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
     * {@inheritDoc} Waits 5 seconds after having stopped container.
     * @param waitForStarting if true then wait for container start, if false wait for container
     * stop
     * @throws InterruptedException if the thread sleep is interrupted
     */
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        super.waitForCompletion(waitForStarting);

        if (!waitForStarting)
        {
            // Many container do not fully stop even after having destroyed all their sockets;
            // as a result wait 5 more seconds and call GC (for embedded containers)
            Thread.sleep(5000);
            System.gc();
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getType()
     */
    public ContainerType getType()
    {
        return ContainerType.EMBEDDED;
    }
}
