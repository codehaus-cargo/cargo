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

import org.eclipse.jetty.ee9.webapp.WebAppContext;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 * Jetty EE9 implementation of the Cargo deployer context.
 *
 * @see org.eclipse.jetty.ee9.webapp.WebAppContext
 */
public class JettyDeployerJakartaEE9 extends JettyDeployerJetty
{
    /**
     * Creates an EE9 Jetty deployer.
     *
     * @param context current web application context
     */
    public JettyDeployerJakartaEE9(WebAppContext context)
    {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Server getServer()
    {
        return ((WebAppContext) getContext()).getServer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getContextPath(Object contextHandler)
    {
        return ((WebAppContext) contextHandler).getContextPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isWebAppContext(Handler handler)
    {
        return handler instanceof WebAppContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void deployWebApp(
        String contextPath, File warFile)
        throws Exception
    {
        WebAppContext webAppContext = new WebAppContext();

        webAppContext.setContextPath(contextPath);
        webAppContext.setWar(warFile.getAbsolutePath());

        getContextHandlerCollection().addHandler(webAppContext);
        webAppContext.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String undeployWebApp(Object contextHandler)
        throws Exception
    {
        WebAppContext webAppContext =
            (WebAppContext) contextHandler;

        String webAppLocation = webAppContext.getWar();

        webAppContext.stop();
        getContextHandlerCollection().removeHandler(
            (Handler) webAppContext);

        return webAppLocation;
    }
}
