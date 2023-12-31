/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.log.Loggable;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.NullLogger;

/**
 * Executor that executes by introspection a Jetty Server object.
 */
public class JettyExecutorThread extends Thread implements Loggable
{
    /**
     * The log for cargo output.
     */
    private Logger log = new NullLogger();

    /**
     * Represents a Jetty Server object.
     */
    private Object server;

    /**
     * If true then Jetty must be started. Otherwise Jetty must be stopped.
     */
    private boolean isForStart;

    /**
     * @param server the reference to a Jetty Server object
     * @param isForStart if true Jetty must be started. Otherwise Jetty must be stopped
     */
    public JettyExecutorThread(Object server, boolean isForStart)
    {
        this.server = server;
        this.isForStart = isForStart;
    }

    /**
     * Start or stop Jetty by introspection.
     */
    @Override
    public void run()
    {
        try
        {
            if (this.isForStart)
            {
                this.server.getClass().getMethod("start").invoke(this.server);

                try
                {
                    Object threadPool =
                        this.server.getClass().getMethod("getThreadPool").invoke(this.server);
                    threadPool.getClass().getMethod("join").invoke(threadPool);
                }
                catch (NoSuchMethodException e)
                {
                    getLogger().info("Ignoring unimplemented method server.getThreadPool().join()",
                        getClass().getName());
                }
            }
            else
            {
                this.server.getClass().getMethod("stop").invoke(this.server);

                try
                {
                    this.server.getClass().getMethod("destroy").invoke(this.server);
                }
                catch (NoSuchMethodException e)
                {
                    getLogger().info("Ignoring unimplemented method server.destroy()",
                        getClass().getName());
                }
            }
        }
        catch (Exception e)
        {
            if (e.getCause() != null && e.getCause() instanceof IllegalStateException
                && "!STOPPED".equals(e.getCause().getMessage()))
            {
                return;
            }

            String message = "Failed to " + (this.isForStart ? "start" : "stop")
                    + " the Jetty container";
            getLogger().warn(message + ": " + e, this.getClass().getName());
            throw new ContainerException(message, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogger(Logger logger)
    {
        this.log = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLogger()
    {
        return this.log;
    }
}
