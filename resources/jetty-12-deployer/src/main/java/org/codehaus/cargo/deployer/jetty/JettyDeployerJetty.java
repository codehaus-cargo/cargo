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
 * Provides common Jetty server operations for the Cargo deployer.<br>
 * <br>
 * The EE-specific subclasses provide the actual web application context
 * implementation.
 *
 * @see org.eclipse.jetty.server.Server
 * @see org.eclipse.jetty.server.Handler
 * @see org.eclipse.jetty.server.handler.ContextHandlerCollection
 */
public abstract class JettyDeployerJetty extends JettyDeployerServlet
{
    /**
     * Web application context containing the deployer servlet.
     */
    private final Object context;

    /**
     * Creates the Jetty deployer.
     *
     * @param context current web application context
     */
    protected JettyDeployerJetty(Object context)
    {
        this.context = context;
    }

    /**
     * Returns the current Jetty server.
     *
     * @return Jetty server
     */
    protected abstract Server getServer();

    /**
     * Returns the context path of a web application.
     *
     * @param contextHandler web application context
     * @return context path
     */
    protected abstract String getContextPath(Object contextHandler);

    /**
     * Determines whether an object is a web application context.
     *
     * @param handler Jetty handler
     * @return {@code true} if the handler is a web application context
     */
    protected abstract boolean isWebAppContext(Handler handler);

    /**
     * Deploys a WAR.
     *
     * @param contextPath context path
     * @param warFile WAR file
     * @throws Exception if deployment fails
     */
    protected abstract void deployWebApp(
        String contextPath, File warFile)
        throws Exception;

    /**
     * Stops a web application and returns its WAR location.
     *
     * @param contextHandler web application context
     * @return WAR location
     * @throws Exception if undeployment fails
     */
    protected abstract String undeployWebApp(Object contextHandler)
        throws Exception;

    /**
     * Returns the Jetty context handler collection.
     *
     * @return context handler collection
     */
    protected ContextHandlerCollection getContextHandlerCollection()
    {
        return findContextHandlerCollection(getServer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JettyDeployer.Context createContextAdapter()
    {
        final ContextHandlerCollection contextCollection =
            getContextHandlerCollection();

        return new JettyDeployer.Context()
        {
            @Override
            public Object getContextHandler(String contextPath)
            {
                for (Handler handler : contextCollection.getHandlers())
                {
                    if (isWebAppContext(handler)
                        && contextPath.equals(
                            getContextPath(handler)))
                    {
                        return handler;
                    }
                }

                return null;
            }

            @Override
            public void deploy(
                String contextPath, File warFile)
                throws Exception
            {
                deployWebApp(contextPath, warFile);
            }

            @Override
            public String undeploy(Object contextHandler)
                throws Exception
            {
                return undeployWebApp(contextHandler);
            }
        };
    }

    /**
     * Finds the context handler collection from the Jetty server.
     *
     * @param server Jetty server
     * @return context handler collection
     */
    protected static ContextHandlerCollection
        findContextHandlerCollection(Server server)
    {
        for (Handler handler : server.getHandlers())
        {
            if (handler instanceof ContextHandlerCollection)
            {
                return (ContextHandlerCollection) handler;
            }
        }

        throw new IllegalStateException(
            "Cannot find a ContextHandlerCollection");
    }

    /**
     * Returns the current web application context.
     *
     * @return current web application context
     */
    protected Object getContext()
    {
        return this.context;
    }
}
