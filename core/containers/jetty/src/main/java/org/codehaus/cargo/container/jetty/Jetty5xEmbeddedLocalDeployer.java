/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.io.File;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedLocalDeployer;

/**
 * Deploys webapps to a Jetty 5.x instance running embedded.
 */
public class Jetty5xEmbeddedLocalDeployer extends AbstractJettyEmbeddedLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedLocalDeployer#AbstractJettyEmbeddedLocalDeployer(EmbeddedLocalContainer)
     */
    public Jetty5xEmbeddedLocalDeployer(EmbeddedLocalContainer container)
    {
        super(container);
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
                Jetty5xEmbeddedLocalContainer container =
                    (Jetty5xEmbeddedLocalContainer) getContainer();

                Object webapp = container.getServer().getClass().getMethod(
                    "addWebApplication", String.class, String.class).invoke(
                        container.getServer(), getContext(deployable), deployable.getFile());

                webapp.getClass().getMethod("setDefaultsDescriptor", String.class).invoke(
                    webapp,
                    new File(container.getConfiguration().getHome(),
                        "etc/webdefault.xml").toURI().toString());

                // set up virtual hosts
                String[] virtualHosts = getVirtualHosts();
                if (virtualHosts != null)
                {
                    for (String virtualHost : virtualHosts)
                    {
                        webapp.getClass().getMethod("addVirtualHost", String.class)
                            .invoke(webapp, virtualHost);
                    }
                }

                // check if extracting the war is wanted
                if (getExtractWar() != null)
                {
                    webapp.getClass().getMethod("setExtractWAR", Boolean.TYPE)
                        .invoke(webapp, getExtractWar());
                }

                if (getParentLoaderPriority() != null)
                {
                    // check if user wants to invert the class loading hierarchy
                    webapp.getClass()
                        .getMethod("setClassLoaderJava2Compliant", Boolean.TYPE)
                        .invoke(webapp, getParentLoaderPriority());
                }

                // check if a default realm has been set for the server, if so, use it
                container.setDefaultRealm(webapp);

                // Activate context
                webapp.getClass().getMethod("start").invoke(webapp);
                return webapp;
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
        try
        {
            Object webapp = getDeployedWebAppContext(deployable);
            webapp.getClass().getMethod("destroy").invoke(webapp);
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
            Object webapp = getDeployedWebAppContext(deployable);
            webapp.getClass().getMethod("start").invoke(webapp);
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
            Object webapp = getDeployedWebAppContext(deployable);
            webapp.getClass().getMethod("stop").invoke(webapp);
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to stop [" + deployable.getFile() + "]", e);
        }
    }
}
