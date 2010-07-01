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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedLocalDeployer;

/**
 * A deployer for webapps that deploys to a Jetty 6.x instance running embedded.
 * 
 * @version $Id$
 */
public class Jetty6xEmbeddedLocalDeployer extends AbstractJettyEmbeddedLocalDeployer
{
    /**
     * The class representing org.mortbay.jetty.webapp.WebAppContext.
     */
    private Class webAppContextClass;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractJettyEmbeddedLocalDeployer#AbstractJettyEmbeddedLocalDeployer(EmbeddedLocalContainer)
     */
    public Jetty6xEmbeddedLocalDeployer(EmbeddedLocalContainer container)
    {
        super(container);
        try
        {
            webAppContextClass =
                ((Jetty6xEmbeddedLocalContainer) getContainer()).getClassLoader().loadClass(
                    "org.mortbay.jetty.webapp.WebAppContext");
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to create Jetty6xEmbeddedLocalDeployer", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedLocalDeployer#deployWebApp(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public Object deployWebApp(Deployable deployable)
    {
        getLogger().info("Deploying [" + deployable.getFile() + "]", this.getClass().getName());

        if (deployable.getType() == DeployableType.WAR)
        {
            try
            {
                Jetty6xEmbeddedLocalContainer container =
                    (Jetty6xEmbeddedLocalContainer) getContainer();

                Object webAppContext = container.createHandler(deployable);
                container.addHandler(webAppContext);

                // set up virtual hosts
                String[] virtualHosts = getVirtualHosts();
                for (int i = 0; 
                    virtualHosts != null && i < virtualHosts.length; 
                    i++)
                {
                    webAppContextClass.getMethod("setVirtualHosts",
                        new Class[] {virtualHosts.getClass()}).invoke(webAppContext, 
                            new Object[] {virtualHosts[i]});
                }

                // check if extracting the war is wanted
                if (getExtractWar() != null)
                {
                    webAppContextClass.getMethod("setExtractWAR", new Class[] {Boolean.TYPE})
                        .invoke(webAppContext, new Object[] {getExtractWar()});
                }

                if (getCopyWebApp() != null)
                {
                    webAppContextClass.getMethod("setCopyDir", new Class[] {Boolean.TYPE})
                        .invoke(webAppContext, new Object[] {getCopyWebApp()});
                }

                if (getParentLoaderPriority() != null)
                {
                    // check if user wants to invert the class loading
                    // hierarchy
                    webAppContextClass.getMethod("setParentLoaderPriority",
                        new Class[] {Boolean.TYPE}).invoke(webAppContext,
                            new Object[] {getParentLoaderPriority()});
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
     * 
     * @see org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedLocalDeployer#undeployWebApp(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeployWebApp(Deployable deployable)
    {
        stop(deployable);
        Jetty6xEmbeddedLocalContainer container = (Jetty6xEmbeddedLocalContainer) getContainer();
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
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        undeploy(deployable);
        deploy(deployable);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#start(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void start(Deployable deployable)
    {
        try
        {
            Object deployedWebAppContext = getDeployedWebAppContext(deployable);
            webAppContextClass.getMethod("start", null).invoke(deployedWebAppContext, null);
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to start [" + deployable.getFile() + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#stop(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void stop(Deployable deployable)
    {
        try
        {
            Object deployedWebAppContext = getDeployedWebAppContext(deployable);
            webAppContextClass.getMethod("stop", null).invoke(deployedWebAppContext, null);
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to stop [" + deployable.getFile() + "]", e);
        }
    }
}
