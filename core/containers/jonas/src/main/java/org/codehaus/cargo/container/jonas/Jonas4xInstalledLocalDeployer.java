/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.jonas.internal.Jonas4xAdmin;
import org.codehaus.cargo.container.jonas.internal.Jonas4xAdminImpl;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;
import org.codehaus.cargo.util.CargoException;

/**
 * Static deployer that deploys WAR, EAR, EJB and RAR to JOnAS.
 * 
 */
public class Jonas4xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * JOnAS admin used for hot deployment.
     */
    private Jonas4xAdmin admin;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public Jonas4xInstalledLocalDeployer(Jonas4xInstalledLocalContainer container)
    {
        this(container, new Jonas4xAdminImpl(container));
    }

    /**
     * Creation of a local deployer with a given Jonas4xAdmin object and file handler.
     * 
     * @param container the container to be used
     * @param admin the JOnAS admin to use for deployment
     */
    public Jonas4xInstalledLocalDeployer(LocalContainer container, Jonas4xAdmin admin)
    {
        super(container);
        this.admin = admin;
    }

    /**
     * {@inheritDoc}. We override the base implementation because JOnAS 4.x handles hot deployment
     * differently than just copying files over.
     */
    @Override
    protected void doDeploy(String deployableDir, Deployable deployable)
    {
        String jonasDeployableDir = deployableDir;

        boolean isRunning = admin.isServerRunning("ping", 0);
        if (!isRunning)
        {
            jonasDeployableDir = getFileHandler().append(jonasDeployableDir, "autoload");
        }

        super.doDeploy(jonasDeployableDir, deployable);

        if (isRunning)
        {
            // hot deployment through JOnAS admin
            String deployableName = getDeployableName(deployable);
            boolean deployed = admin.deploy(deployableName);
            if (!deployed)
            {
                throw new CargoException("Unable to deploy file " + deployableName
                    + " through JOnAS admin");
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable) throws CargoException
    {
        String deployableName = getDeployableName(deployable);
        boolean isRunning = false;

        isRunning = admin.isServerRunning("ping", 0);
        if (isRunning)
        {
            boolean undeployed = admin.unDeploy(deployableName);
            if (!undeployed)
            {
                throw new CargoException("Unable to undeploy file " + deployableName
                    + " through JOnAS admin");
            }
        }
    }

    /**
     * {@inheritDoc}. For JOnAS 4.x this is the <code>apps</code> directory for EARs,
     * <code>ejbjars</code> directory for EJBs, <code>rars</code> directory for RARs and the
     * <code>webapps</code> directory for WARs.
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        String jonasDeployableDir = getContainer().getConfiguration().getHome();
        if (DeployableType.WAR.equals(deployable.getType()))
        {
            return getFileHandler().append(jonasDeployableDir, "webapps");
        }
        else if (DeployableType.EAR.equals(deployable.getType()))
        {
            return getFileHandler().append(jonasDeployableDir, "apps");
        }
        else if (DeployableType.EJB.equals(deployable.getType()))
        {
            return getFileHandler().append(jonasDeployableDir, "ejbjars");
        }
        else if (DeployableType.RAR.equals(deployable.getType()))
        {
            return getFileHandler().append(jonasDeployableDir, "rars");
        }
        else
        {
            throw new ContainerException("Container " + getContainer().getName()
                + " cannot deploy " + deployable.getType() + " deployables");
        }
    }
}
