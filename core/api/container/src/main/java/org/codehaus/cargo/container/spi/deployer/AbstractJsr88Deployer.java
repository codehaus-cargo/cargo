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
package org.codehaus.cargo.container.spi.deployer;

import java.io.File;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.util.CargoException;

/**
 * JSR-88 remote deployer.
 *
 * @version $Id$
 */
public abstract class AbstractJsr88Deployer extends AbstractRemoteDeployer
{

    /**
     * The run time configuration.
     */
    private final RuntimeConfiguration configuration;

    /**
     * Constructor.
     *
     * @param container the remote container
     */
    public AbstractJsr88Deployer(RemoteContainer container)
    {
        this.configuration = container.getConfiguration();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        DeploymentManager deploymentManager;
        try
        {
            deploymentManager = this.getDeploymentManager();
        }
        catch (DeploymentManagerCreationException e)
        {
            throw new CargoException("Cannot create the DeploymentManager", e);
        }

        ProgressObject progressObject = deploymentManager.distribute(
            deploymentManager.getTargets(), new File(deployable.getFile()), null);
        this.waitForProgressObject(progressObject);

        progressObject = deploymentManager.start(progressObject.getResultTargetModuleIDs());
        this.waitForProgressObject(progressObject);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        DeploymentManager deploymentManager;
        try
        {
            deploymentManager = this.getDeploymentManager();
        }
        catch (DeploymentManagerCreationException e)
        {
            throw new CargoException("Cannot create the DeploymentManager", e);
        }

        TargetModuleID[] targetModules;
        try
        {
            targetModules = this.findTargetModule(deploymentManager, deployable);
        }
        catch (TargetException e)
        {
            throw new CargoException("Cannot communicate with the server", e);
        }

        ProgressObject progressObject = deploymentManager.stop(targetModules);
        this.waitForProgressObject(progressObject);

        progressObject = deploymentManager.undeploy(targetModules);
        this.waitForProgressObject(progressObject);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        DeploymentManager deploymentManager;
        try
        {
            deploymentManager = this.getDeploymentManager();
        }
        catch (DeploymentManagerCreationException e)
        {
            throw new CargoException("Cannot create the DeploymentManager", e);
        }

        TargetModuleID[] targetModules = null;
        try
        {
            targetModules = this.findTargetModule(deploymentManager, deployable);
        }
        catch (TargetException e)
        {
            throw new CargoException("Cannot communicate with the server", e);
        }
        catch (CargoException e)
        {
            // This means that the target module cannot be found, ignore
        }

        if (targetModules != null)
        {
            ProgressObject progressObject = deploymentManager.stop(targetModules);
            this.waitForProgressObject(progressObject);

            progressObject = deploymentManager.undeploy(targetModules);
            this.waitForProgressObject(progressObject);
        }

        this.deploy(deployable);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.deployer.Deployer#start(Deployable)
     */
    @Override
    public void start(Deployable deployable)
    {
        DeploymentManager deploymentManager;
        try
        {
            deploymentManager = this.getDeploymentManager();
        }
        catch (DeploymentManagerCreationException e)
        {
            throw new CargoException("Cannot create the DeploymentManager", e);
        }

        TargetModuleID[] targetModules = null;
        try
        {
            targetModules = this.findTargetModule(deploymentManager, deployable);
        }
        catch (TargetException e)
        {
            throw new CargoException("Cannot communicate with the server", e);
        }

        ProgressObject progressObject = deploymentManager.start(targetModules);
        this.waitForProgressObject(progressObject);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.codehaus.cargo.container.deployer.Deployer#stop(Deployable)
     */
    @Override
    public void stop(Deployable deployable)
    {
        DeploymentManager deploymentManager;
        try
        {
            deploymentManager = this.getDeploymentManager();
        }
        catch (DeploymentManagerCreationException e)
        {
            throw new CargoException("Cannot create the DeploymentManager", e);
        }

        TargetModuleID[] targetModules = null;
        try
        {
            targetModules = this.findTargetModule(deploymentManager, deployable);
        }
        catch (TargetException e)
        {
            throw new CargoException("Cannot communicate with the server", e);
        }

        ProgressObject progressObject = deploymentManager.stop(targetModules);
        this.waitForProgressObject(progressObject);
    }

    /**
     * Waits for a progress object.
     *
     * @param progressObject Progress object.
     * @throws CargoException If timeout or deployment fails.
     */
    private void waitForProgressObject(ProgressObject progressObject) throws CargoException
    {
        long timeout = System.currentTimeMillis() + this.getTimeout();
        while (System.currentTimeMillis() < timeout)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                throw new CargoException("Thread.sleep has failed", e);
            }

            DeploymentStatus status = progressObject.getDeploymentStatus();
            if (status.isCompleted())
            {
                return;
            }
            else if (status.isFailed())
            {
                throw new CargoException("Deployment has failed: " + status.getMessage());
            }
        }

        throw new CargoException("Deployment has timed out after " + this.getTimeout()
            + " milliseconds");
    }

    /**
     * @return Timeout.
     */
    private long getTimeout()
    {
        return 120000;
    }

    /**
     * @return The JSR-88 deployment manager for the target server.
     * @throws CargoException If some parameters are incorrect.
     * @throws DeploymentManagerCreationException If deployment manager creation fails.
     */
    protected abstract DeploymentManager getDeploymentManager() throws CargoException,
        DeploymentManagerCreationException;

    /**
     * Finds a JSR-88 module
     *
     * @param deploymentManager Deployment manager.
     * @param deployable Deployable to look for.
     * @return JSR-88 module for the given deployable, put in an array.
     * @throws CargoException If module not found.
     * @throws TargetException If cannot reach server.
     */
    private TargetModuleID[] findTargetModule(DeploymentManager deploymentManager,
        Deployable deployable) throws CargoException, TargetException
    {
        File moduleFile = new File(deployable.getFile());
        String moduleName = moduleFile.getName();
        int extensionSeparator = moduleName.lastIndexOf('.');
        if (extensionSeparator != -1)
        {
            moduleName = moduleName.substring(0, extensionSeparator);
        }

        ModuleType moduleType;
        if (DeployableType.EAR.equals(deployable.getType()))
        {
            moduleType = ModuleType.EAR;
        }
        else if (DeployableType.EJB.equals(deployable.getType()))
        {
            moduleType = ModuleType.EJB;
        }
        else if (DeployableType.RAR.equals(deployable.getType()))
        {
            moduleType = ModuleType.RAR;
        }
        else if (DeployableType.WAR.equals(deployable.getType()))
        {
            moduleType = ModuleType.WAR;
        }
        else
        {
            throw new IllegalArgumentException("CARGO deployable type " + deployable.getType()
                + " has no JSR-88 match and cannot be remotely deployed.");
        }

        TargetModuleID targetModule = null;
        TargetModuleID[] modules = deploymentManager.getRunningModules(moduleType,
            deploymentManager.getTargets());

        StringBuilder sb = new StringBuilder();
        sb.append("Cannot find the module \"");
        sb.append(moduleName);
        sb.append("\". Available modules:");
        for (TargetModuleID module : modules)
        {
            String moduleId = module.getModuleID();

            if (moduleName.equals(moduleId))
            {
                targetModule = module;
                break;
            }
            sb.append("\n\t- ");
            sb.append(moduleId);
        }

        if (targetModule == null)
        {
            throw new CargoException(sb.toString());
        }

        TargetModuleID[] targetModules = new TargetModuleID[1];
        targetModules[0] = targetModule;
        return targetModules;
    }

    /**
     * @return The run time configuration.
     */
    protected RuntimeConfiguration getRuntimeConfiguration()
    {
        return this.configuration;
    }

}
