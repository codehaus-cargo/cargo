/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat;

import java.util.Map;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaEmbeddedLocalContainer;
import org.codehaus.cargo.container.tomcat.internal.TomcatEmbedded;
import org.codehaus.cargo.module.webapp.tomcat.TomcatWarArchive;
import org.jdom.Attribute;

/**
 * {@link org.codehaus.cargo.container.deployer.Deployer} for deploying to
 * {@link AbstractCatalinaEmbeddedLocalContainer embedded Tomcat container}.
 * 
 */
public class TomcatEmbeddedLocalDeployer extends AbstractLocalDeployer
{
    /**
     * The container that this deployer acts on.
     */
    private final AbstractCatalinaEmbeddedLocalContainer container;

    /**
     * Creates a new deployer for {@link AbstractCatalinaEmbeddedLocalContainer}.
     * 
     * @param container The container to which this deployer will work. This parameter is typed as
     * {@link EmbeddedLocalContainer} due to the Cargo generic API requirement, but it has to be a
     * {@link AbstractCatalinaEmbeddedLocalContainer}.
     */
    public TomcatEmbeddedLocalDeployer(EmbeddedLocalContainer container)
    {
        super(container);
        this.container = (AbstractCatalinaEmbeddedLocalContainer) container;
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#deploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        if (container.getController() == null)
        {
            // not yet started. defer the deployment until the container is started
            container.scheduleDeployment(deployable);
            return;
        }

        WAR war = (WAR) deployable;

        String docBase;
        if (!war.isExpanded())
        {
            String webappsDirectory = getFileHandler().append(
                container.getConfiguration().getHome(),
                    container.getConfiguration().getPropertyValue(
                        TomcatPropertySet.WEBAPPS_DIRECTORY));
            docBase = getFileHandler().append(webappsDirectory, war.getContext());
            getFileHandler().explode(war.getFile(), docBase);
        }
        else
        {
            docBase = war.getFile();
        }

        TomcatEmbedded.Context context = container.getController().createContext(
            '/' + war.getContext(), docBase);

        // The Tomcat 8.x container's createContext method actually deploys the full WAR,
        // as a result we don't need the whole logic below.
        if (container instanceof Tomcat5xEmbeddedLocalContainer)
        {
            try
            {
                TomcatWarArchive twar = new TomcatWarArchive(docBase);
                if (twar.getTomcatContextXml() != null)
                {
                    for (Map.Entry<Attribute, Attribute> param : twar.getTomcatContextXml().
                        getParameters().entrySet())
                    {
                        String key = param.getKey().getValue();
                        String value = param.getValue().getValue();

                        context.addParameter(key, value);
                    }
                }
            }
            catch (Exception e)
            {
                throw new ContainerException("Failed to parse Tomcat WAR file "
                    + "in [" + docBase + "]", e);
            }

            container.getHost().addChild(context);
        }
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        WAR war = (WAR) deployable;
        TomcatEmbedded.Context context = container.getHost().findChild(war.getContext());
        container.getHost().removeChild(context);
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#redeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        TomcatEmbedded.Context context;        
        try
        {
            WAR war = (WAR) deployable;
            context = container.getHost().findChild(war.getContext());
        }
        catch (NullPointerException e)
        {
            // Deployable not deployed, so simply deploy it
            deploy(deployable);
            return;
        }
        if (context != null)
        {
            context.reload();
        }
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#redeploy(org.codehaus.cargo.container.deployable.Deployable, org.codehaus.cargo.container.deployer.DeployableMonitor)
     */
    @Override
    public void redeploy(Deployable deployable, DeployableMonitor monitor)
    {
        this.redeploy(deployable);

        // Wait for the Deployable to be redeployed
        DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
        watchdog.setLogger(getLogger());
        watchdog.watchForAvailability();
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#start(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void start(Deployable deployable)
    {
        WAR war = (WAR) deployable;
        container.getHost().findChild(war.getContext()).setAvailable(true);
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#stop(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void stop(Deployable deployable)
    {
        WAR war = (WAR) deployable;
        container.getHost().findChild(war.getContext()).setAvailable(false);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer#getType()
     */
    public DeployerType getType()
    {
        return DeployerType.EMBEDDED;
    }
}
