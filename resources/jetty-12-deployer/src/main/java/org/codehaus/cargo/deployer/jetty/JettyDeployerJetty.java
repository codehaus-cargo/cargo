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
import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.jetty.deploy.StandardContextHandlerFactory;
import org.eclipse.jetty.deploy.StandardDeployer;
import org.eclipse.jetty.server.Deployable;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.Attributes;
import org.eclipse.jetty.util.component.Environment;

/**
 * Provides common Jetty server operations for the Cargo deployer.
 *
 * <p>The EE-specific subclasses provide the actual web application context
 * implementation and identify the Jetty environment in which the application
 * must be deployed.</p>
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
     * Returns the Jetty environment name used by this deployer.
     *
     * @return environment name
     */
    protected abstract String getEnvironmentName();

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
     * Returns the Jetty StandardDeployer.
     *
     * <p>The deployer is normally installed in the server's component tree.
     * The contained-bean lookup is also used because a
     * {@code DeploymentScanner} may own the deployer.</p>
     *
     * @return StandardDeployer
     */
    protected StandardDeployer getStandardDeployer()
    {
        StandardDeployer deployer =
            getServer().getBean(StandardDeployer.class);

        if (deployer == null)
        {
            for (StandardDeployer candidate :
                getServer().getContainedBeans(StandardDeployer.class))
            {
                deployer = candidate;
                break;
            }
        }

        if (deployer == null)
        {
            throw new IllegalStateException("Cannot find Jetty StandardDeployer");
        }

        return deployer;
    }

    /**
     * Returns the Jetty context handler collection used by the deployer.
     *
     * @return context handler collection
     */
    protected ContextHandlerCollection getContextHandlerCollection()
    {
        return getStandardDeployer().getContexts();
    }

    /**
     * Deploys a WAR.
     *
     * <p>The context is deliberately created through
     * {@link StandardContextHandlerFactory}. This is important in Jetty 12.1
     * because the factory creates and configures the web application using
     * the appropriate Jetty environment and environment classloader.</p>
     *
     * @param contextPath context path
     * @param warFile WAR file
     * @throws Exception if deployment fails
     */
    protected void deployWebApp(String contextPath, File warFile) throws Exception
    {
        Environment environment = Environment.get(getEnvironmentName());

        if (environment == null)
        {
            throw new IllegalStateException(
                "Cannot find Jetty environment [" + getEnvironmentName() + "]");
        }

        Attributes deployAttributes = new Attributes.Mapped();
        deployAttributes.setAttribute(
            Deployable.CONTEXT_PATH, contextPath);

        Path warPath = warFile.toPath();

        StandardContextHandlerFactory factory =
            new StandardContextHandlerFactory();

        ContextHandler contextHandler =
            factory.newContextHandler(
                getServer(),
                environment,
                warPath,
                Collections.emptySet(),
                deployAttributes);

        getStandardDeployer().deploy(contextHandler);
    }

    /**
     * Undeploys a web application.
     *
     * @param contextHandler web application context
     * @return WAR location
     * @throws Exception if undeployment fails
     */
    protected String undeployWebApp(Object contextHandler)
        throws Exception
    {
        ContextHandler handler = (ContextHandler) contextHandler;

        Object war = handler.getAttribute(Deployable.WAR);

        String webAppLocation = war == null ? null : war.toString();

        getStandardDeployer().undeploy(handler);

        /*
         * StandardDeployer stops and removes the context from the
         * ContextHandlerCollection. Destroy it as well, matching the
         * lifecycle used by Jetty's DeploymentScanner.
         */
        handler.destroy();

        return webAppLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JettyDeployer.Context createContextAdapter()
    {
        final ContextHandlerCollection contextCollection = getContextHandlerCollection();

        return new JettyDeployer.Context()
        {
            @Override
            public Object getContextHandler(String contextPath)
            {
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

            @Override
            public void deploy(String contextPath, File warFile) throws Exception
            {
                deployWebApp(contextPath, warFile);
            }

            @Override
            public String undeploy(Object contextHandler) throws Exception
            {
                return undeployWebApp(contextHandler);
            }
        };
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
