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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;

/**
 * Common mojo for all deployer actions (start deployable, stop deployable, deploy deployable,
 * undeploy deployable, etc).
 * 
 * @version $Id$
 */
public abstract class AbstractDeployerMojo extends AbstractCargoMojo
{
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

        // Perform deployment action on all deployables defined in the deployer config element
        // (if any).
        if (getDeployerElement() != null && getDeployerElement().getDeployables() != null)
        {
            for (int i = 0; i < getDeployerElement().getDeployables().length; i++)
            {
                org.codehaus.cargo.container.deployable.Deployable deployable =
                    getDeployerElement().getDeployables()[i].createDeployable(
                        container.getId(), getCargoProject());
                URL pingURL = getDeployerElement().getDeployables()[i].getPingURL();
                Long pingTimeout = getDeployerElement().getDeployables()[i].getPingTimeout();
                performDeployerActionOnSingleDeployable(deployer, deployable, pingURL, pingTimeout);
            }
        }

        // Perform deployment action on the autodeployable (if any).
        if (getCargoProject().getPackaging() != null && getCargoProject().isJ2EEPackaging())
        {
            if (getDeployerElement() == null
                || getDeployerElement().getDeployables() == null
                || !containsAutoDeployable(getDeployerElement().getDeployables()))
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
}
