/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedLocalDeployer;

/**
 * A deployer for webapps that deploys to a Jetty 7.x onwards instance running embedded.
 */
public class Jetty7x8x9x10x11xEmbeddedLocalDeployer extends AbstractJettyEmbeddedLocalDeployer
{
    /**
     * The class representing org.eclipse.jetty.webapp.WebAppContext.
     */
    private Class webAppContextClass;

    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedLocalDeployer#AbstractJettyEmbeddedLocalDeployer(EmbeddedLocalContainer)
     */
    public Jetty7x8x9x10x11xEmbeddedLocalDeployer(EmbeddedLocalContainer container)
    {
        super(container);
        try
        {
            webAppContextClass =
                ((Jetty7xEmbeddedLocalContainer) getContainer()).getClassLoader().loadClass(
                    "org.eclipse.jetty.webapp.WebAppContext");
        }
        catch (Exception e)
        {
            throw new ContainerException(
                "Failed to create Eclipse Jetty embedded local deployer", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object deployWebApp(Deployable deployable)
    {
        getLogger().info("Deploying [" + deployable.getFile() + "]", this.getClass().getName());

        if (deployable.getType() == DeployableType.WAR)
        {
            try
            {
                Jetty7xEmbeddedLocalContainer container =
                    (Jetty7xEmbeddedLocalContainer) getContainer();

                Object webAppContext = container.createHandler(deployable);
                container.addHandler(webAppContext);

                // set up virtual hosts
                String[] virtualHosts = getVirtualHosts();
                webAppContextClass.getMethod("setVirtualHosts", virtualHosts.getClass())
                    .invoke(webAppContext, virtualHosts);

                // check if extracting the war is wanted
                if (getExtractWar() != null)
                {
                    webAppContextClass.getMethod("setExtractWAR", Boolean.TYPE)
                        .invoke(webAppContext, getExtractWar());
                }

                if (getCopyWebApp() != null)
                {
                    webAppContextClass.getMethod("setCopyDir", Boolean.TYPE)
                        .invoke(webAppContext, getCopyWebApp());
                }

                if (getParentLoaderPriority() != null)
                {
                    // check if user wants to invert the class loading hierarchy
                    webAppContextClass.getMethod("setParentLoaderPriority", Boolean.TYPE)
                        .invoke(webAppContext, getParentLoaderPriority());
                }

                return webAppContext;
            }
            catch (Exception e)
            {
                throw new ContainerException("Failed to deploy [" + deployable.getFile() + "]", e);
            }
        }

        throw new ContainerException("Only WAR archives are supported for deployment in Jetty. "
            + "Got [" + deployable.getFile() + "]");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeployWebApp(Deployable deployable)
    {
        stop(deployable);
        Jetty7xEmbeddedLocalContainer container = (Jetty7xEmbeddedLocalContainer) getContainer();
        Object deployedWebAppContext = getDeployedWebAppContext(deployable);

        try
        {
            container.removeHandler(deployedWebAppContext);
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to undeploy [" + deployable.getFile() + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Deployable deployable)
    {
        try
        {
            Object deployedWebAppContext = getDeployedWebAppContext(deployable);
            webAppContextClass.getMethod("start").invoke(deployedWebAppContext);
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to start [" + deployable.getFile() + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(Deployable deployable)
    {
        try
        {
            Object deployedWebAppContext = getDeployedWebAppContext(deployable);
            webAppContextClass.getMethod("stop").invoke(deployedWebAppContext);
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to stop [" + deployable.getFile() + "]", e);
        }
    }
}
