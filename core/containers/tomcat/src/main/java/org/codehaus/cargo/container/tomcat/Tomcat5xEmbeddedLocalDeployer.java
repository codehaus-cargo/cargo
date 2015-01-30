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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5xEmbedded;
import org.codehaus.cargo.module.webapp.tomcat.TomcatWarArchive;
import org.jdom.Attribute;

/**
 * {@link org.codehaus.cargo.container.deployer.Deployer} for deploying to
 * {@link Tomcat5xEmbeddedLocalContainer embedded Tomcat container}.
 * 
 * @version $Id$
 */
public class Tomcat5xEmbeddedLocalDeployer extends AbstractLocalDeployer
{
    /**
     * The container that this deployer acts on.
     */
    private final Tomcat5xEmbeddedLocalContainer container;

    /**
     * Map from {@link Deployable} to {@link Tomcat5xEmbedded.Context}, representing deployed
     * objects.
     */
    private final Map<Deployable, Tomcat5xEmbedded.Context> deployed =
        new HashMap<Deployable, Tomcat5xEmbedded.Context>();

    /**
     * Creates a new deployer for {@link Tomcat5xEmbeddedLocalContainer}.
     * 
     * @param container The container to which this deployer will work. This parameter is typed as
     * {@link EmbeddedLocalContainer} due to the Cargo generic API requirement, but it has to be a
     * {@link Tomcat5xEmbeddedLocalContainer}.
     */
    public Tomcat5xEmbeddedLocalDeployer(EmbeddedLocalContainer container)
    {
        super(container);
        this.container = (Tomcat5xEmbeddedLocalContainer) container;
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
            String home = container.getConfiguration().getHome();
            docBase = getFileHandler().append(home, "webapps/" + war.getContext());
            getFileHandler().explode(war.getFile(), docBase);
        }
        else
        {
            docBase = war.getFile();
        }

        Tomcat5xEmbedded.Context context = container.getController().createContext(
            '/' + war.getContext(), docBase);

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
        deployed.put(deployable, context);
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        Tomcat5xEmbedded.Context context = getExistingContext(deployable);
        container.getHost().removeChild(context);
        deployed.remove(deployable);
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#redeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        Tomcat5xEmbedded.Context context = null;
        try
        {
            context = getExistingContext(deployable);
        }
        catch (ContainerException e)
        {
            // Deployable not deployed, so simply deploy it
            deploy(deployable);
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
        getExistingContext(deployable).setAvailable(true);
    }

    /**
     * {@inheritDoc}
     * @see AbstractLocalDeployer#stop(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void stop(Deployable deployable)
    {
        getExistingContext(deployable).setAvailable(false);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.deployer.AbstractLocalDeployer#getType()
     */
    public DeployerType getType()
    {
        return DeployerType.EMBEDDED;
    }

    /**
     * Gets the context that represents a deployed {@link Deployable}.
     * 
     * @param deployable the deployable object that has deployed on Tomcat.
     * @return always non-null
     */
    private Tomcat5xEmbedded.Context getExistingContext(Deployable deployable)
    {
        Tomcat5xEmbedded.Context context = deployed.get(deployable);
        if (context == null)
        {
            throw new ContainerException("Not deployed yet: " + deployable);
        }
        return context;
    }
}
