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

import org.eclipse.jetty.ee8.webapp.WebAppContext;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

/**
 * Provides Jetty 12.1 Java EE 8 WebAppContext operations.
 */
public class JettyWebAppContextJavax implements JettyDeployer.Context
{
    /**
     * Web application context containing the deployer servlet.
     */
    private final WebAppContext context;

    /**
     * Creates a context adapter.
     *
     * @param context current WebAppContext
     */
    public JettyWebAppContextJavax(WebAppContext context)
    {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getContextHandler(String contextPath)
    {
        ContextHandlerCollection contexts =
            findContextHandlerCollection(context.getServer());

        for (Handler handler : contexts.getHandlers())
        {
            if (handler instanceof WebAppContext
                && contextPath.equals(
                    ((WebAppContext) handler).getContextPath()))
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
        ContextHandlerCollection contexts =
            findContextHandlerCollection(context.getServer());

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath(contextPath);
        webAppContext.setWar(warFile.getAbsolutePath());

        contexts.addHandler(webAppContext);
        webAppContext.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String undeploy(Object contextHandler) throws Exception
    {
        WebAppContext webAppContext =
            (WebAppContext) contextHandler;

        String webAppLocation = webAppContext.getWar();

        webAppContext.stop();

        ContextHandlerCollection contexts =
            findContextHandlerCollection(context.getServer());

        contexts.removeHandler((Handler) webAppContext);

        return webAppLocation;
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
