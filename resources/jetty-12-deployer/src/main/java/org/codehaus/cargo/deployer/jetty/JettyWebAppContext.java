/*
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.cargo.deployer.jetty;

import java.io.File;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * Common Jetty context operations used by the Cargo Jetty deployer.
 *
 * @param <T> Jetty WebAppContext type
 */
public abstract class JettyWebAppContext<T> implements JettyDeployer.Context
{
    /**
     * The Jetty WebAppContext containing the deployer servlet.
     */
    private final T currentContext;

    /**
     * Creates a context adapter.
     *
     * @param currentContext current Jetty WebAppContext
     */
    protected JettyWebAppContext(T currentContext)
    {
        this.currentContext = currentContext;
    }

    /**
     * Returns the current WebAppContext.
     *
     * @return current WebAppContext
     */
    protected T getCurrentContext()
    {
        return this.currentContext;
    }

    /**
     * Returns the Jetty server.
     *
     * @return Jetty server
     */
    protected abstract Server getServer();

    /**
     * Returns whether a handler is a WebAppContext.
     *
     * @param handler Jetty handler
     * @return {@code true} if the handler is a WebAppContext
     */
    protected abstract boolean isWebAppContext(Handler handler);

    /**
     * Returns the context path of a WebAppContext.
     *
     * @param handler WebAppContext handler
     * @return context path
     */
    protected abstract String getContextPath(Handler handler);

    /**
     * Creates and starts a WebAppContext.
     *
     * @param contextPath context path
     * @param warFile WAR file
     * @param contextCollection context handler collection
     * @throws Exception if deployment fails
     */
    protected abstract void deployWebApp(String contextPath, File warFile,
        ContextHandlerCollection contextCollection) throws Exception;

    /**
     * Stops and removes a WebAppContext.
     *
     * @param contextHandler context handler
     * @param contextCollection context handler collection
     * @return WAR location
     * @throws Exception if undeployment fails
     */
    protected abstract String undeployWebApp(Object contextHandler,
        ContextHandlerCollection contextCollection) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getContextHandler(String contextPath)
    {
        ContextHandlerCollection contextCollection =
            findContextHandlerCollection(getServer());

        for (Handler handler : contextCollection.getHandlers())
        {
            if (isWebAppContext(handler)
                && contextPath.equals(getContextPath(handler)))
            {
                return handler;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(String contextPath, File warFile) throws Exception
    {
        ContextHandlerCollection contextCollection =
            findContextHandlerCollection(getServer());

        deployWebApp(contextPath, warFile, contextCollection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String undeploy(Object contextHandler) throws Exception
    {
        ContextHandlerCollection contextCollection =
            findContextHandlerCollection(getServer());

        return undeployWebApp(contextHandler, contextCollection);
    }

    /**
     * Finds the context handler collection from the Jetty server.
     *
     * @param server Jetty server
     * @return context handler collection
     */
    private static ContextHandlerCollection findContextHandlerCollection(Server server)
    {
        for (Handler handler : server.getHandlers())
        {
            if (handler instanceof ContextHandlerCollection)
            {
                return (ContextHandlerCollection) handler;
            }
        }

        throw new IllegalStateException("Cannot find a ContextHandlerCollection");
    }
}
