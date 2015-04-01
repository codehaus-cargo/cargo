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
package org.codehaus.cargo.container.jetty.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.spi.AbstractEmbeddedLocalContainer;
import org.codehaus.cargo.util.CargoException;

/**
 * Common code for all Jetty embedded container implementations.
 * 
 */
public abstract class AbstractJettyEmbeddedLocalContainer
    extends AbstractEmbeddedLocalContainer
{
    /**
     * Jetty Server object. Note that we use an Object as we're calling the Jetty API by
     * introspection only. This is order not to have any dependency with the Jetty jar for building.
     */
    protected Object server;

    /**
     * Capability of the Jetty Embedded container.
     */
    private ContainerCapability capability = new ServletContainerCapability();

    /**
     * Parsed version of the container.
     */
    private String version;

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public AbstractJettyEmbeddedLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * @return the Jetty Server object
     */
    public Object getServer()
    {
        return this.server;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#waitForCompletion(boolean)
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (waitForStarting)
        {
            long timeout = System.currentTimeMillis() + this.getTimeout();
            while (System.currentTimeMillis() < timeout)
            {
                Thread.sleep(1000);

                Method isStarted;
                try
                {
                    isStarted = getServer().getClass().getMethod("isStarted", null);
                }
                catch (NoSuchMethodException e)
                {
                    throw new ContainerException("Cannot find method isStarted", e);
                }
                Boolean started;
                try
                {
                    started = (Boolean) isStarted.invoke(getServer(), null);
                }
                catch (IllegalAccessException e)
                {
                    throw new ContainerException("Cannot execute method isStarted", e);
                }
                catch (InvocationTargetException e)
                {
                    throw new ContainerException("Cannot execute method isStarted", e);
                }
                if (started)
                {
                    return;
                }
            }

            throw new ContainerException("Server did not start after "
                    + Long.toString(this.getTimeout()) + " milliseconds");
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }

    /**
     * Create a Jetty Server Object.
     * @exception Exception in case of error
     */
    protected synchronized void createServerObject() throws Exception
    {
        if (this.server == null)
        {
            try
            {
                this.server = getClassLoader().loadClass("org.mortbay.jetty.Server").newInstance();
            }
            catch (Exception e)
            {
                throw new ContainerException("Failed to create Jetty Server instance", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractEmbeddedLocalContainer#doStop()
     */
    @Override
    protected void doStop() throws Exception
    {
        createServerObject();
        JettyExecutorThread jettyRunner = new JettyExecutorThread(getServer(), false);
        jettyRunner.setLogger(getLogger());
        jettyRunner.start();
    }

    /**
     * Returns the version of the Jetty installation.
     * 
     * @return The Jetty version
     */
    protected synchronized String getVersion()
    {
        if (this.version == null)
        {
            try
            {
                createServerObject();
            }
            catch (Exception e)
            {
                throw new CargoException("Cannot create Jetty embedded server", e);
            }
            this.version = this.server.getClass().getPackage().getImplementationVersion();
            if (this.version == null)
            {
                throw new CargoException("The class " + this.server.getClass()
                    + " doesn't have any Implementation Version");
            }
            this.version += " Embedded";
        }

        return this.version;
    }

}
