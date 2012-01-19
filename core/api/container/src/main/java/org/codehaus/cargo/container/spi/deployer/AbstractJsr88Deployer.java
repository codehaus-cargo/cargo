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
package org.codehaus.cargo.container.spi.deployer;

import java.io.File;
import java.lang.reflect.Constructor;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

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
     * File handler.
     */
    private final FileHandler fileHandler;

    /**
     * Constructor.
     * 
     * @param container the remote container
     */
    public AbstractJsr88Deployer(RemoteContainer container)
    {
        super(container);
        this.configuration = container.getConfiguration();
        this.fileHandler = new DefaultFileHandler();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        File deployableFile;
        File tempDirectory;
        if (deployable.getType() == DeployableType.WAR)
        {
            String localFileName;
            WAR war = (WAR) deployable;
            if (war.getContext().length() == 0)
            {
                localFileName = "rootContext.war";
            }
            else
            {
                localFileName = war.getContext() + ".war";
            }
            tempDirectory = new File(fileHandler.createUniqueTmpDirectory());
            deployableFile = new File(tempDirectory, localFileName);
            fileHandler.copyFile(deployable.getFile(), deployableFile.getAbsolutePath());
            deployableFile.deleteOnExit();
        }
        else if (deployable.getType() == DeployableType.EAR)
        {
            EAR ear = (EAR) deployable;
            String localFileName = ear.getName() + ".ear";
            tempDirectory = new File(fileHandler.createUniqueTmpDirectory());
            deployableFile = new File(tempDirectory, localFileName);
            fileHandler.copyFile(deployable.getFile(), deployableFile.getAbsolutePath());
            deployableFile.deleteOnExit();
        }
        else
        {
            deployableFile = new File(deployable.getFile());
            tempDirectory = null;
        }

        try
        {
            DeploymentManager deploymentManager = this.getDeploymentManager();
    
            ProgressObject progressObject = deploymentManager.distribute(
                deploymentManager.getTargets(), deployableFile, null);
            this.waitForProgressObject(progressObject);
    
            progressObject = deploymentManager.start(progressObject.getResultTargetModuleIDs());
            this.waitForProgressObject(progressObject);
        }
        finally
        {
            if (tempDirectory != null)
            {
                if (!deployableFile.delete())
                {
                    getLogger().warn("Cannot delete the temporary file: " + deployableFile,
                        this.getClass().getName());
                }

                if (!tempDirectory.delete())
                {
                    getLogger().warn("Cannot delete the temporary directory: " + tempDirectory,
                        this.getClass().getName());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        DeploymentManager deploymentManager = this.getDeploymentManager();

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
        DeploymentManager deploymentManager = this.getDeploymentManager();

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
        DeploymentManager deploymentManager = this.getDeploymentManager();

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
        DeploymentManager deploymentManager = this.getDeploymentManager();

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
     * @throws CargoException If anything fails.
     */
    private DeploymentManager getDeploymentManager() throws CargoException
    {
        DeploymentFactoryManager dfm = DeploymentFactoryManager.getInstance();

        String deploymentFactoryClassName = this.getDeploymentFactoryClassName();
        try
        {
            Class<?> deploymentFactoryClass = null;
            final ClassLoader tcccl = Thread.currentThread().getContextClassLoader();
            if (tcccl != null)
            {
                try
                {
                    deploymentFactoryClass = tcccl.loadClass(deploymentFactoryClassName);
                }
                catch (ClassNotFoundException e)
                {
                    deploymentFactoryClass = null;
                }
            }
            if (deploymentFactoryClass == null)
            {
                deploymentFactoryClass = this.getClass().getClassLoader().loadClass(
                    deploymentFactoryClassName);
            }

            Constructor<?> deploymentFactoryConstructor = deploymentFactoryClass.getConstructor();
            DeploymentFactory deploymentFactoryInstance = (DeploymentFactory)
                deploymentFactoryConstructor.newInstance();
            dfm.registerDeploymentFactory(deploymentFactoryInstance);
        }
        catch (ClassNotFoundException e)
        {
            throw new CargoException(
                "Cannot locate the JSR-88 deployer class " + deploymentFactoryClassName + "\n"
                    + "Make sure the target server's librarires are in CARGO's classpath.\n"
                    + "More information on: http://cargo.codehaus.org/JSR88", e);
        }
        catch (Throwable t)
        {
            throw new CargoException("Cannot create a JSR-88 deployer: " + t.getMessage(), t);
        }

        try
        {
            return this.getDeploymentManager(dfm);
        }
        catch (DeploymentManagerCreationException e)
        {
            throw new CargoException("Cannot create the DeploymentManager", e);
        }
    }

    /**
     * @return The class name of the JSR-88 deployment factory.
     */
    protected abstract String getDeploymentFactoryClassName();

    /**
     * @param dfm JSR-88 deployment factory manager with the target deployer factory registered.
     * @return The JSR-88 deployment manager for the target server.
     * @throws DeploymentManagerCreationException If deployment manager creation fails.
     */
    protected abstract DeploymentManager getDeploymentManager(DeploymentFactoryManager dfm)
        throws DeploymentManagerCreationException;

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
        String moduleName;
        if (deployable.getType() == DeployableType.WAR)
        {
            WAR war = (WAR) deployable;
            if (war.getContext().length() == 0)
            {
                moduleName = "rootContext";
            }
            else
            {
                moduleName = war.getContext();
            }
        }
        else if (deployable.getType() == DeployableType.EAR)
        {
            EAR ear = (EAR) deployable;
            moduleName = ear.getName();
        }
        else
        {
            File moduleFile = new File(deployable.getFile());
            moduleName = moduleFile.getName();
            int extensionSeparator = moduleName.lastIndexOf('.');
            if (extensionSeparator != -1)
            {
                moduleName = moduleName.substring(0, extensionSeparator);
            }
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
