/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.maven3;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.DeployableMonitorListener;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.maven3.configuration.Deployable;
import org.codehaus.cargo.maven3.deployer.DefaultDeployableMonitorFactory;
import org.codehaus.cargo.maven3.deployer.DeployableMonitorFactory;

/**
 * Common mojo for all deployer actions (start deployable, stop deployable, deploy deployable,
 * undeploy deployable, etc).
 */
public abstract class AbstractDeployerMojo extends AbstractCargoMojo
{
    /**
     * {@link DeployableMonitorListener} that logs.
     */
    public class DeployerListener implements DeployableMonitorListener
    {
        /**
         * {@link DeployableMonitor} to listen.
         */
        private DeployableMonitor monitor;

        /**
         * Saves all attributes.
         * @param monitor {@link DeployableMonitor} to listen.
         */
        public DeployerListener(DeployableMonitor monitor)
        {
            this.monitor = monitor;
        }

        /**
         * {@inheritDoc}.
         */
        @Override
        public void deployed()
        {
            getLog().debug("Watchdog finds [" + this.monitor.toString() + "] for deployable ["
                + this.monitor.getDeployableName() + "] deployed.");
        }

        /**
         * {@inheritDoc}.
         */
        @Override
        public void undeployed()
        {
            getLog().debug("Watchdog finds [" + this.monitor.toString() + "] for deployable ["
                + this.monitor.getDeployableName() + "] not deployed yet.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        if (getCargoProject().getPackaging() == null || !getCargoProject().isJ2EEPackaging())
        {
            if (getDeployablesElement() == null || getDeployablesElement().length == 0)
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
            deployer = createDeployerFactory().createDeployer(container);
        }
        else
        {
            deployer = getDeployerElement().createDeployer(container);
        }

        return deployer;
    }

    /**
     * @return Deployer factory.
     */
    protected DeployerFactory createDeployerFactory()
    {
        return new DefaultDeployerFactory();
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

        if (getDeployablesElement() != null)
        {
            for (Deployable deployableElement : getDeployablesElement())
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
            DeployableMonitor monitor = createDeployableMonitor(container, deployableElement,
                    deployable);

            performDeployerActionOnSingleDeployable(deployer, deployable, monitor);
        }

        // Perform deployment action on the autodeployable (if any).
        if (getCargoProject().getPackaging() != null && getCargoProject().isJ2EEPackaging())
        {
            if (deployableElements.isEmpty())
            {
                // Deployable monitor is null here because we cannot reliably know the URL
                // of the autodeployable
                performDeployerActionOnSingleDeployable(deployer,
                    createAutoDeployDeployable(container), null);
            }
        }
    }

    /**
     * Perform a deployer action on a single deployable.
     * @param deployer Deployer.
     * @param deployable Deployable.
     * @param monitor Deployable monitor.
     */
    protected abstract void performDeployerActionOnSingleDeployable(
        org.codehaus.cargo.container.deployer.Deployer deployer,
        org.codehaus.cargo.container.deployable.Deployable deployable,
        org.codehaus.cargo.container.deployer.DeployableMonitor monitor);

    /**
     * Create a deployable monitor.
     * @param container Container where is deployable deployed.
     * @param deployableElement {@link Deployable} containing monitoring info.
     * @param deployable {@link org.codehaus.cargo.container.deployable.Deployable} to monitor.
     * @return Deployable monitor with specified arguments.
     */
    private DeployableMonitor createDeployableMonitor(
            org.codehaus.cargo.container.Container container,
            Deployable deployableElement,
            org.codehaus.cargo.container.deployable.Deployable deployable)
    {
        DeployableMonitorFactory monitorFactory = new DefaultDeployableMonitorFactory();
        DeployableMonitor monitor = monitorFactory.
                createDeployableMonitor(container, deployableElement);

        if (monitor != null)
        {
            DeployerListener listener = new DeployerListener(monitor);
            monitor.registerListener(listener);
        }

        return monitor;
    }
}
