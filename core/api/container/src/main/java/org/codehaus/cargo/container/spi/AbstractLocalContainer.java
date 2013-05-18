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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.container.spi.util.ContainerUtils;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Default container implementation that all local container implementations must extend.
 * 
 * @version $Id$
 */
public abstract class AbstractLocalContainer extends AbstractContainer implements LocalContainer
{
    /**
     * The file to which output of the container should be written.
     */
    private String output;

    /**
     * Whether output of the container should be appended to an existing file, or the existing file
     * should be truncated.
     */
    private boolean append;

    /**
     * Default timeout for starting/stopping the container.
     */
    private long timeout = 120000L;

    /**
     * The local configuration implementation to use.
     */
    private LocalConfiguration configuration;

    /**
     * Container state. Default to unknown state.
     */
    private State state = State.UNKNOWN;

    /**
     * File utility class.
     */
    private FileHandler fileHandler;

    /**
     * Default constructor.
     * @param configuration the configuration to associate to this container. It can be changed
     * later on by calling {@link #setConfiguration(LocalConfiguration)}
     */
    public AbstractLocalContainer(LocalConfiguration configuration)
    {
        this.append = false;
        this.configuration = configuration;
        this.fileHandler = new DefaultFileHandler();
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#setOutput(String)
     */
    public final void setOutput(String output)
    {
        this.output = output;
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#setAppend(boolean)
     */
    public final void setAppend(boolean isAppend)
    {
        this.append = isAppend;
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#getOutput()
     */
    public final String getOutput()
    {
        return this.output;
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#isAppend()
     */
    public final boolean isAppend()
    {
        return this.append;
    }

    /**
     * Verify required properties have been set before executing any action.
     */
    protected void verify()
    {
        this.getConfiguration().applyPortOffset();

        // Nothing to verify. We still need this method so that extending classes do not need to
        // implement this method. Only if they have some checks to perform.
    }

    /**
     * Installed and Embedded containers do not have the same signature for their
     * <code>doStart</code> method. Thus we need to abstract it.
     * 
     * @throws Exception if any error is raised during the container start
     */
    protected abstract void startInternal() throws Exception;

    /**
     * Installed and Embedded containers do not have the same signature for their
     * <code>doStop</code> method. Thus we need to abstract it.
     * 
     * @throws Exception if any error is raised during the container stop
     */
    protected abstract void stopInternal() throws Exception;
    
    /**
     * Some containers may not fully stop and need to be forcibly stopped.
     * This method should be overridden for containers that support forcibly stopping the container.
     */
    protected void forceStopInternal()
    {
        // No implementation defined here
    }
    
    /**
     * Some containers may require some extra steps after startup.
     * 
     * @throws Exception if any error is raised during these executions
     */
    protected void executePostStartTasks() throws Exception
    {
        // No implementation defined here
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#start()
     */
    public final void start()
    {
        verify();

        // Ensure that the configuration is done before starting the container.
        getConfiguration().configure(this);

        // CARGO-365: Check if ports are in use
        for (Map.Entry<String, String> property : getConfiguration().getProperties().entrySet())
        {
            if (property.getKey().endsWith(".port") && property.getValue() != null)
            {
                try
                {
                    int port = Integer.parseInt(property.getValue());
                    if (!isPortShutdown(port, 0))
                    {
                        throw new ContainerException("Port number " + property.getValue()
                            + " (defined with the property " + property.getKey() + ") is in use. "
                                + "Please free it on the system or set it to a different port "
                                    + "in the container's configuration.");
                    }
                }
                catch (NumberFormatException e) 
                {
                    // We do nothing
                }
            }
        }

        getLogger().info(getName() + " starting...", this.getClass().getName());

        setState(State.STARTING);

        try
        {
            startInternal();

            // CARGO-712: If timeout is 0, don't wait at all
            if (getTimeout() != 0)
            {
                // Wait until the container is fully started
                waitForCompletion(true);
            }

            executePostStartTasks();

            setState(State.STARTED);
            getLogger().info(getName() + " started on port ["
                + getConfiguration().getPropertyValue(ServletPropertySet.PORT) + "]",
                this.getClass().getName());
        }
        catch (Exception e)
        {
            setState(State.UNKNOWN);
            throw new ContainerException("Failed to start the " + getName() + " container."
                + (getOutput() == null ? "" : " Check the [" + getOutput() + "] file "
                    + "containing the container logs for more details."), e);
        }
        finally 
        {
            this.getConfiguration().revertPortOffset();
        }
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#stop()
     */
    public final void stop()
    {
        verify();

        getLogger().info(getName() + " is stopping...", this.getClass().getName());
        setState(State.STOPPING);

        final boolean isAppend = isAppend();

        try
        {
            // CARGO-520: Always set append to "true" when stopping
            setAppend(true);

            stopInternal();

            // CARGO-712: If timeout is 0, don't wait at all
            if (getTimeout() != 0)
            {
                // Wait until the container is fully stopped
                waitForCompletion(false);
            }
                        
            // Force the container to stop, should it not already be stopped.
            // At this point, the container should already be stopped,
            // so this should have no effect if the container was properly stopped.
            forceStopInternal();            
        }
        catch (Exception e)
        {
            setState(State.UNKNOWN);
            throw new ContainerException("Failed to stop the " + getName() + " container."
                + (getOutput() == null ? "" : " Check the [" + getOutput() + "] file "
                    + "containing the container logs for more details."), e);
        }
        finally
        {
            setAppend(isAppend);
        }

        setState(State.STOPPED);
        getLogger().info(getName() + " is stopped", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#restart()
     */
    public void restart()
    {
        try
        {
            stop();
        }
        catch (Throwable t)
        {
            getLogger().info("The stop phase of the restart action has failed: " + t.toString(),
                this.getClass().getName());
        }

        start();
    }

    /**
     * Ping the WAR CPC to verify if the container is started or stopped.
     * 
     * @param waitForStarting if true then wait for container start, if false wait for container
     * stop
     * @throws InterruptedException if the thread sleep is interrupted
     */
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        LocalConfiguration config = getConfiguration();

        if (waitForStarting)
        {
            DeployableMonitor monitor =
                new URLDeployableMonitor(ContainerUtils.getCPCURL(config), getTimeout(),
                    "Cargo Ping Component used to verify if the container is started.");
            monitor.setLogger(getLogger());
            DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
            watchdog.setLogger(getLogger());

            watchdog.watch(waitForStarting);
        }
        else
        {
            long deadline = System.currentTimeMillis() + getTimeout();

            int connectTimeout = 0;
            for (Map.Entry<String, String> property : getConfiguration().getProperties().entrySet())
            {
                if (!property.getKey().endsWith(".port") || property.getValue() == null)
                {
                    continue;
                }
                int port;
                try
                {
                    port = Integer.parseInt(property.getValue());
                }
                catch (NumberFormatException e)
                {
                    continue;
                }
                if (port < 1 || port > 65535)
                {
                    continue;
                }

                waitForPortShutdown(port, connectTimeout, deadline);
                getLogger().debug("\tPort " + port + " is shutdown", this.getClass().getName());

                connectTimeout = 250;
                continue;
            }

            // Many container do not fully stop even after having destroyed all their sockets;
            // as a result wait 5 more seconds
            Thread.sleep(5000);
        }
    }

    /**
     * Waits for the shutdown of the specified server port.
     * 
     * @param port The port number.
     * @param connectTimeout The connect timeout.
     * @param deadline The deadline for the port to shutdown.
     * @throws InterruptedException If the thread was interrupted while waiting for the port
     *             shutdown.
     */
    private void waitForPortShutdown(int port, int connectTimeout, long deadline)
        throws InterruptedException
    {
        getLogger().debug("Waiting for port " + port + " to shutdown, deadline " + deadline,
            this.getClass().getName());

        while (true)
        {
            if (isPortShutdown(port, connectTimeout))
            {
                break;
            }

            if (System.currentTimeMillis() > deadline)
            {
                throw new ContainerException("Server port " + port
                    + " did not shutdown within the timeout period [" + getTimeout() + "]");
            }

            Thread.sleep(1000);
        }
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#setConfiguration(LocalConfiguration)
     */
    public void setConfiguration(LocalConfiguration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#getConfiguration()
     */
    public LocalConfiguration getConfiguration()
    {
        return this.configuration;
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#setTimeout(long)
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#getTimeout()
     */
    public long getTimeout()
    {
        return this.timeout;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getState()
     */
    public State getState()
    {
        return this.state;
    }

    /**
     * @param state the container current state
     */
    protected void setState(State state)
    {
        this.state = state;
    }

    /**
     * @return the Cargo file utility class
     */
    public FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the Cargo file utility class to use. This method is useful for unit
     * testing with Mock objects as it can be passed a test file handler that doesn't perform any
     * real file action.
     */
    public void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }


    /**
     * Checks if the specified server port is shutdown.
     * 
     * @param port The port number.
     * @param connectTimeout The connect timeout.
     * @return <code>true</code> if <code>port</code> is shut down, <code>false</code> otherwise.
     */
    private boolean isPortShutdown(int port, int connectTimeout)
    {
        Socket s = new Socket();
        try
        {
            getLogger().debug("\tConnection attempt with socket " + s + ", current time is "
                + System.currentTimeMillis(), this.getClass().getName());

            s.bind(null);

            // If the remote port is closed, s.connect will throw an exception
            s.connect(new InetSocketAddress("localhost", port), connectTimeout);
            getLogger().debug("\tSocket " + s + " for port " + port + " managed to connect",
                this.getClass().getName());

            try
            {
                s.shutdownOutput();
            }
            catch (IOException e)
            {
                // ignored, irrelevant
                getLogger().debug("\tFailed to shutdown output for socket " + s + ": " + e,
                    this.getClass().getName());
            }
            try
            {
                s.shutdownInput();
            }
            catch (IOException e)
            {
                // ignored, irrelevant
                getLogger().debug("\tFailed to shutdown input for socket " + s + ": " + e,
                    this.getClass().getName());
            }

            getLogger().debug("\tSocket " + s + " for port " + port + " shutdown",
                this.getClass().getName());
        }
        catch (IOException ignored)
        {
            // If an IOException has occured, this means port is shut down
            return true;
        }
        finally
        {
            try
            {
                s.close();
            }
            catch (IOException e)
            {
                // ignored, irrelevant
                getLogger().debug("\tFailed to close socket " + s + ": " + e,
                    this.getClass().getName());
            }
            finally
            {
                getLogger().debug("\tSocket " + s + " for port " + port + " closed",
                    this.getClass().getName());

                s = null;
                System.gc();
            }
        }

        return false;
    }
}
