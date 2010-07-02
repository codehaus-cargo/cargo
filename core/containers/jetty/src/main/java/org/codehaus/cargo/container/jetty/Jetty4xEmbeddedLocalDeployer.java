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
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyEmbeddedLocalDeployer;

/**
 * Deployer to deploy to a Jetty 4.x (embedded) container.
 * 
 * @version $Id$
 */
public class Jetty4xEmbeddedLocalDeployer extends AbstractJettyEmbeddedLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractJettyEmbeddedLocalDeployer#AbstractJettyEmbeddedLocalDeployer(EmbeddedLocalContainer)
     */
    public Jetty4xEmbeddedLocalDeployer(EmbeddedLocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(Deployable)
     */
    @Override
    public Object deployWebApp(Deployable deployable)
    {
        getLogger().info("Deploying [" + deployable.getFile() + "]", this.getClass().getName());

        if (deployable.getType() == DeployableType.WAR)
        {
            try
            {
                Jetty4xEmbeddedLocalContainer container =
                    (Jetty4xEmbeddedLocalContainer) getContainer();

                Object context = container.getServer().getClass().getMethod("addWebApplication",
                    new Class[] {String.class, String.class}).invoke(
                        container.getServer(),
                        new Object[] {"/" + ((WAR) deployable).getContext(), deployable.getFile()});

                // Activate context by stoppping and re-starting it
                Class wc = container.getClassLoader().loadClass(
                    "org.mortbay.jetty.servlet.WebApplicationContext");
                wc.getMethod("stop", null).invoke(context, null);
                wc.getMethod("start", null).invoke(context, null);
                return context;
            }
            catch (Exception e)
            {
                throw new ContainerException("Failed to deploy [" + deployable.getFile() + "]", e);
            }
        }

        throw new ContainerException("Only WAR archives are supported for deployment "
            + "in Jetty. Got [" + deployable.getFile() + "]");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(Deployable)
     */
    @Override
    public void undeployWebApp(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }
}
