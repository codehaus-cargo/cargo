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
package org.codehaus.cargo.maven2;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.DeployableMonitorListener;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.maven2.configuration.Deployable;

/**
 * Common mojo for all deployer actions (start deployable, stop deployable, deploy deployable,
 * undeploy deployable, etc).
 * 
 * @version $Id$
 */
public abstract class AbstractDeployerMojo extends AbstractCargoMojo
{
    /**
     * {@link DeployableMonitorListener} that logs.
     */
    public class DeployerListener implements DeployableMonitorListener
    {
        /**
         * {@link Deployable} to listen.
         */
        private org.codehaus.cargo.container.deployable.Deployable deployable;

        /**
         * Saves all attributes.
         * @param deployable {@link Deployable} to listen.
         */
        public DeployerListener(org.codehaus.cargo.container.deployable.Deployable deployable)
        {
            this.deployable = deployable;
        }

        /**
         * {@inheritDoc}.
         */
        public void deployed()
        {
            getLog().debug("Watchdog finds [" + this.deployable.getFile() + "] deployed.");
        }

        /**
         * {@inheritDoc}.
         */
        public void undeployed()
        {
            getLog().debug("Watchdog finds [" + this.deployable.getFile() + "] not deployed yet.");
        }
    }

    /**
     * Deployer factory.
     */
    private DeployerFactory deployerFactory = new DefaultDeployerFactory();

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#doExecute()
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        if (getCargoProject().getPackaging() == null || !getCargoProject().isJ2EEPackaging())
        {
            if (getDeployerElement() == null || getDeployerElement().getDeployables() == null
                || getDeployerElement().getDeployables().length == 0)
            {
                getLog().info("There's nothing to deploy or undeploy");
                return;
            }
        }

        org.codehaus.cargo.container.Container container = createContainer();
        org.codehaus.cargo.container.deployer.Deployer deployer = createDeployer(container);

        performDeployerActionOnAllDeployables(container, deployer);
    }

    /**
     * @param factory Deployer factory.
     */
    public void setDeployerFactory(DeployerFactory factory)
    {
        this.deployerFactory = factory;
    }

    /**
     * @return Deployer factory.
     */
    public DeployerFactory getDeployerFactory()
    {
        return this.deployerFactory;
    }

    /**
     * Create a deployer.
     * @param container Container.
     * @return Deployer for <code>container</code>.
     * @throws MojoExecutionException If deployer creation fails.
     */
    protected org.codehaus.cargo.container.deployer.Deployer createDeployer(
        org.codehaus.cargo.container.Container container) throws MojoExecutionException
    {
        org.codehaus.cargo.container.deployer.Deployer deployer;

        // Use a deployer matching the container's type if none is specified.
        // @see DeployerFactory#createDeployer(Container)
        if (getDeployerElement() == null)
        {
            deployer = getDeployerFactory().createDeployer(container);
        }
        else
        {
            deployer = getDeployerElement().createDeployer(container);
        }

        return deployer;
    }

    /**
     * Perform deployment action on all deployables (defined in the deployer configuration element
     * and on the autodeployable).
     * 
     * @param container the container to deploy into
     * @param deployer the deployer to use to deploy into the container
     * @throws MojoExecutionException in case of a deployment error
     */
    private void performDeployerActionOnAllDeployables(
        org.codehaus.cargo.container.Container container,
        org.codehaus.cargo.container.deployer.Deployer deployer) throws MojoExecutionException
    {
        getLog().debug("Performing deployment action into [" + container.getName() + "]...");

        List<Deployable> deployableElements = new ArrayList<Deployable>();

        // Perform deployment action on all deployables defined in the deployer config element
        if (getDeployerElement() != null && getDeployerElement().getDeployables() != null)
        {
            for (Deployable deployableElement : getDeployerElement().getDeployables())
            {
                deployableElements.add(deployableElement);
            }
        }

        // Perform deployment action on all deployables defined in the configuration config element
        if (getConfigurationElement() != null
            && getConfigurationElement().getDeployables() != null)
        {
            for (Deployable deployableElement : getConfigurationElement().getDeployables())
            {
                if (!deployableElements.contains(deployableElement))
                {
                    deployableElements.add(deployableElement);
                }
            }
        }

        for (Deployable deployableElement : deployableElements)
        {
            org.codehaus.cargo.container.deployable.Deployable deployable =
                deployableElement.createDeployable(container.getId(), getCargoProject());
            URL pingURL = deployableElement.getPingURL();
            Long pingTimeout = deployableElement.getPingTimeout();
            performDeployerActionOnSingleDeployable(deployer, deployable, pingURL, pingTimeout);
        }

        // Perform deployment action on the autodeployable (if any).
        if (getCargoProject().getPackaging() != null && getCargoProject().isJ2EEPackaging())
        {
            Deployable[] deployableElementsArray = new Deployable[deployableElements.size()];
            deployableElements.toArray(deployableElementsArray);

            if (!containsAutoDeployable(deployableElementsArray))
            {
                // The ping URL is always null here because if the user has specified a ping URL
                // then the auto deployable has already been deployed as it's been explicitely
                // specified by the user...
                performDeployerActionOnSingleDeployable(deployer,
                    createAutoDeployDeployable(container), null, null);
            }
        }
    }

    /**
     * Perform a deployer action on a single deployable.
     * @param deployer Deployer.
     * @param deployable Deployable.
     * @param pingURL Application ping URL.
     * @param pingTimeout Timeout (milliseconds).
     */
    protected abstract void performDeployerActionOnSingleDeployable(
        org.codehaus.cargo.container.deployer.Deployer deployer,
        org.codehaus.cargo.container.deployable.Deployable deployable, URL pingURL,
        Long pingTimeout);

    /**
     * Create a deployable monitor.
     * @param pingURL Ping URL.
     * @param pingTimeout Ping timeout (milliseconds).
     * @param deployable {@link Deployable} to monitor.
     * @return Deployable monitor with specified arguments.
     */
    protected DeployableMonitor createDeployableMonitor(URL pingURL, Long pingTimeout,
        org.codehaus.cargo.container.deployable.Deployable deployable)
    {
        DeployableMonitor monitor;
        if (pingTimeout == null)
        {
            monitor = new URLDeployableMonitor(pingURL);
        }
        else
        {
            monitor = new URLDeployableMonitor(pingURL, pingTimeout.longValue());
        }
        DeployerListener listener = new DeployerListener(deployable);
        monitor.registerListener(listener);
        return monitor;
    }
}
