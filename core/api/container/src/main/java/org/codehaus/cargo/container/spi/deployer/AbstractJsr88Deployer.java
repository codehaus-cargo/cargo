/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
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
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * JSR-88 remote deployer.
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
     * Timeout.
     */
    private long timeout;

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
        this.fileHandler.setLogger(this.getLogger());

        // Set a timeout in order to avoid CARGO-1299
        String timeout = configuration.getPropertyValue(RemotePropertySet.TIMEOUT);
        this.timeout = Long.parseLong(timeout);
    }

    /**
     * {@inheritDoc}
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
            if (war.getContext().isEmpty())
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
        }
        else if (deployable.getType() == DeployableType.EAR)
        {
            EAR ear = (EAR) deployable;
            String localFileName = ear.getName() + ".ear";
            tempDirectory = new File(fileHandler.createUniqueTmpDirectory());
            deployableFile = new File(tempDirectory, localFileName);
            fileHandler.copyFile(deployable.getFile(), deployableFile.getAbsolutePath());
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
                this.filterTargets(deploymentManager.getTargets()), deployableFile, null);
            this.waitForProgressObject("Distributing", progressObject);

            progressObject = deploymentManager.start(progressObject.getResultTargetModuleIDs());
            this.waitForProgressObject("Starting", progressObject);
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
        this.waitForProgressObject("Stopping", progressObject);

        progressObject = deploymentManager.undeploy(targetModules);
        this.waitForProgressObject("Undeploying", progressObject);
    }

    /**
     * {@inheritDoc}
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
            this.waitForProgressObject("Stopping", progressObject);

            progressObject = deploymentManager.undeploy(targetModules);
            this.waitForProgressObject("Undeploying", progressObject);
        }

        this.deploy(deployable);
    }

    /**
     * {@inheritDoc}
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
        this.waitForProgressObject("Starting", progressObject);
    }

    /**
     * {@inheritDoc}
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
        this.waitForProgressObject("Stopping", progressObject);
    }

    /**
     * Waits for a progress object.
     * 
     * @param reason Reason for wait (Start, Stop, etc.), used for error messages.
     * @param progressObject Progress object.
     * @throws CargoException If timeout or deployment fails.
     */
    private void waitForProgressObject(String reason, ProgressObject progressObject)
        throws CargoException
    {
        long timeout = System.currentTimeMillis() + this.timeout;
        while (System.currentTimeMillis() < timeout)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                throw new CargoException("Thread.sleep failed", e);
            }

            DeploymentStatus status = progressObject.getDeploymentStatus();
            if (status.isCompleted())
            {
                return;
            }
            else if (status.isFailed())
            {
                throw new CargoException(reason + " failed: " + status.getMessage());
            }
        }

        throw new CargoException(reason + " timed out after " + this.timeout + " milliseconds");
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
                "Cannot locate the JSR-88 deployer class " + deploymentFactoryClassName + "\nMake "
                    + "sure the target server's librarires are in Codehaus Cargo's classpath.\n"
                        + "Read more on: https://codehaus-cargo.github.io/cargo/JSR88.html", e);
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
            if (war.getContext().isEmpty())
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
            throw new IllegalArgumentException("Codehaus Cargo deployable type "
                + deployable.getType() + " has no JSR-88 match and cannot be remotely deployed.");
        }

        TargetModuleID[] modules = deploymentManager.getAvailableModules(moduleType,
            deploymentManager.getTargets());

        StringBuilder sb = new StringBuilder();
        sb.append("Cannot find the module \"");
        sb.append(moduleName);
        sb.append("\". Available modules:");

        List<TargetModuleID> targetModules = new ArrayList<>();

        for (TargetModuleID module : modules)
        {
            String moduleId = module.getModuleID();

            if (moduleName.equals(moduleId))
            {
                targetModules.add(module);
            }
            else
            {
                sb.append("\n\t- ");
                sb.append(moduleId);
            }
        }

        if (targetModules.isEmpty())
        {
            throw new CargoException(sb.toString());
        }

        return filterTargetModuleIDs(targetModules);
    }

    /**
     * @return The run time configuration.
     */
    protected RuntimeConfiguration getRuntimeConfiguration()
    {
        return this.configuration;
    }

    /**
     * @param targets List with all available targets for this container instance.
     * @return Let implementations filter targets for deploy.
     */
    protected Target[] filterTargets(Target[] targets)
    {
        return targets;
    }

    /**
     * @param targetModuleIDs List with all available target module IDs for the target module.
     * @return Let implementations filter target module IDs for deploy.
     */
    protected TargetModuleID[] filterTargetModuleIDs(List<TargetModuleID> targetModuleIDs)
    {
        return targetModuleIDs.toArray(new TargetModuleID[targetModuleIDs.size()]);
    }
}
