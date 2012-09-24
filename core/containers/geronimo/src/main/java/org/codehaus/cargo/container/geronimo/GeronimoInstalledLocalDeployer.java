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
package org.codehaus.cargo.container.geronimo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.geronimo.deployable.GeronimoDeployable;
import org.codehaus.cargo.container.geronimo.internal.GeronimoUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherException;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherRequest;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.CargoException;

/**
 * A Geronimo deploytool-based deployer to perform deployment to a local container.
 * 
 * @version $Id$
 */
public class GeronimoInstalledLocalDeployer extends AbstractInstalledLocalDeployer
{
    /**
     * Ant utility class.
     */
    private AntUtils antUtils;

    /**
     * Geronimo utilities.
     */
    private GeronimoUtils geronimoUtils;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalDeployer#AbstractInstalledLocalDeployer(org.codehaus.cargo.container.InstalledLocalContainer)
     */
    public GeronimoInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
        this.antUtils = new AntUtils();
        this.geronimoUtils = new GeronimoUtils(container.getConfiguration());
    }

    /**
     * @return the Ant utility class
     */
    protected final AntUtils getAntUtils()
    {
        return this.antUtils;
    }

    /**
     * @return the same value as {@link #getContainer()} but more type-safe.
     */
    protected InstalledLocalContainer getInstalledContainer()
    {
        return (InstalledLocalContainer) super.getContainer();
    }

    /**
     * Deploys extra classpath elements to the Geronimo classpath.
     * @param extraClasspath Classpath elements to deploy
     */
    public void deployExtraClasspath(String[] extraClasspath)
    {
        for (String extraClasspathElement : extraClasspath)
        {
            File extraClasspathElementFile = new File(extraClasspathElement);
            if (extraClasspathElementFile.getName().indexOf('-') == -1)
            {
                String name = extraClasspathElementFile.getName()
                    .substring(0, extraClasspathElementFile.getName().lastIndexOf('.'));
                extraClasspathElementFile = new File(
                    getFileHandler().createUniqueTmpDirectory(), name + "-1.0.jar");
                getFileHandler().copyFile(
                    extraClasspathElement, extraClasspathElementFile.getAbsolutePath());
            }
            JvmLauncher java = createAdminDeployerJava("install-library");
            java.addAppArguments("--groupId", "org.codehaus.cargo.classpath");
            java.addAppArgument(extraClasspathElementFile);

            try
            {
                int retval = java.execute();
                if (retval != 0)
                {
                    throw new ContainerException("Failed to add extra classpath element ["
                        + extraClasspathElement + "]");
                }
            }
            catch (JvmLauncherException e)
            {
                throw new ContainerException("Failed to add extra classpath element ["
                    + extraClasspathElement + "]", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        JvmLauncher java;
        if (deployable.getType() == DeployableType.BUNDLE)
        {
            java = createAdminDeployerJava("install-bundle");
            java.addAppArguments("--start");
        }
        else
        {
            java = createAdminDeployerJava("deploy");
        }
        addPathArgument(java, deployable);
        String deployableId = getModuleId(deployable);

        try
        {
            int retval = java.execute();
            if (retval != 0)
            {
                throw new ContainerException("Failed to deploy [" + deployableId + "]");
            }
        }
        catch (JvmLauncherException e)
        {
            throw new ContainerException("Failed to deploy [" + deployableId + "]", e);
        }
    }

    /**
     * Distribute a deployable to a running or offline Geronimo server. The deployable is not
     * automatically started.
     * 
     * @param deployable the deployable being installed
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    protected void distribute(Deployable deployable)
    {
        JvmLauncher java = createDeployerJava("distribute");
        addPathArgument(java, deployable);
        String deployableId = getModuleId(deployable);

        try
        {
            int retval = java.execute();
            if (retval != 0)
            {
                throw new ContainerException("Failed to distribute [" + deployableId + "]");
            }
        }
        catch (JvmLauncherException e)
        {
            throw new ContainerException("Failed to distribute [" + deployableId + "]", e);
        }

    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#start(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void start(Deployable deployable)
    {
        start(getModuleId(deployable));
    }

    /**
     * Starts a deployable with the given ID.
     * 
     * @param deployableId the ID of the deployable being started
     * @see org.codehaus.cargo.container.deployer.Deployer#start(org.codehaus.cargo.container.deployable.Deployable)
     */
    protected void start(String deployableId)
    {
        if (deployableId == null)
        {
            throw new ContainerException("Failed: deployable ID cannot be null.");
        }
        else
        {
            JvmLauncher java = createAdminDeployerJava("start");
            java.addAppArguments(deployableId);
            try
            {
                int retval = java.execute();
                if (retval != 0)
                {
                    throw new ContainerException("Failed to start [" + deployableId + "]");
                }
            }
            catch (JvmLauncherException e)
            {
                throw new ContainerException("Failed to start [" + deployableId + "]", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#stop(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void stop(Deployable deployable)
    {
        stop(getModuleId(deployable));
    }

    /**
     * Stops a deployable with the given ID.
     * 
     * @param deployableId the ID of the deployable being stopped
     * @see org.codehaus.cargo.container.deployer.Deployer#stop(org.codehaus.cargo.container.deployable.Deployable)
     */
    protected void stop(String deployableId)
    {
        if (deployableId == null)
        {
            throw new ContainerException("Failed: deployable ID cannot be null.");
        }
        else
        {
            JvmLauncher java = createAdminDeployerJava("stop");
            java.addAppArguments(deployableId);

            try
            {
                int retval = java.execute();
                if (retval != 0)
                {
                    throw new ContainerException("Failed to stop [" + deployableId + "]");
                }
            }
            catch (JvmLauncherException e)
            {
                throw new ContainerException("Failed to stop [" + deployableId + "]", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        String command;
        if (deployable.getType() == DeployableType.BUNDLE)
        {
            command = "uninstall-bundle";
        }
        else
        {
            command = "undeploy";
        }
        undeploy(getModuleId(deployable), command);
    }

    /**
     * Undeploy a deployable with the given ID.
     * 
     * @param deployableId the ID of the deployable being undeployed
     * @param command Command name
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    protected void undeploy(String deployableId, String command)
    {
        if (deployableId == null)
        {
            throw new ContainerException("Failed: deployable ID cannot be null.");
        }
        else
        {
            JvmLauncher java = createAdminDeployerJava("undeploy");
            java.addAppArguments(deployableId);

            try
            {
                int retval = java.execute();
                if (retval != 0)
                {
                    throw new ContainerException("Failed to undeploy [" + deployableId + "]");
                }
            }
            catch (JvmLauncherException e)
            {
                throw new ContainerException("Failed to undeploy [" + deployableId + "]", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        JvmLauncher java = createAdminDeployerJava("redeploy");
        addPathArgument(java, deployable);
        String deployableId = getModuleId(deployable);
        addModuleIdArgument(java, deployableId);

        try
        {
            int retval = java.execute();
            if (retval != 0)
            {
                throw new ContainerException("Failed to redeploy [" + deployableId + "]");
            }
        }
        catch (JvmLauncherException e)
        {
            throw new ContainerException("Failed to redeploy [" + deployableId + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(org.codehaus.cargo.container.deployable.Deployable, org.codehaus.cargo.container.deployer.DeployableMonitor)
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
     * Create a preinitialized instance of the JVM launcher to be used for managing Geronimo
     * deployables.
     * 
     * @return The created task instance
     */
    private JvmLauncher createJava()
    {
        JvmLauncherRequest request = new JvmLauncherRequest(false, this);
        JvmLauncher java =
            getInstalledContainer().getJvmLauncherFactory().createJvmLauncher(request);

        return java;
    }

    /**
     * Create an instance of the JVM launcher preinitialized to invoke the deploy tool jar with the
     * specified command.
     * 
     * @param action the deployer action to take
     * @return The created task instance
     */
    private JvmLauncher createDeployerJava(String action)
    {
        JvmLauncher java = createJava();

        if (getContainer() instanceof Geronimo3xInstalledLocalContainer)
        {
            ((Geronimo3xInstalledLocalContainer) getContainer()).prepareJvmLauncher(java);
            java.setMainClass("org.apache.geronimo.cli.deployer.DeployerCLI");
        }
        else
        {
            java.setJarFile(new File(getInstalledContainer().getHome(), "bin/deployer.jar"));
        }

        java.addAppArguments(action);

        return java;
    }

    /**
     * Create an instance of the JVM launcher preinitialized to invoke the deploy tool jar with the
     * specified command and admin/manager user auth options.
     * 
     * @param action the deployer action to take
     * @return The created task instance
     */
    private JvmLauncher createAdminDeployerJava(String action)
    {
        JvmLauncher java = createJava();
        java.addAppArguments("--user");
        java.addAppArguments(getContainer().getConfiguration().getPropertyValue(
            RemotePropertySet.USERNAME));
        java.addAppArguments("--password");
        java.addAppArguments(getContainer().getConfiguration().getPropertyValue(
            RemotePropertySet.PASSWORD));
        if (geronimoUtils.isGeronimoStarted())
        {
            java.addAppArguments("--host");
            java.addAppArguments(getContainer().getConfiguration().getPropertyValue(
                GeneralPropertySet.HOSTNAME));
            java.addAppArguments("--port");
            java.addAppArguments(getContainer().getConfiguration().getPropertyValue(
                GeneralPropertySet.RMI_PORT));
        }
        else
        {
            java.addAppArguments("--offline");
        }

        if (getContainer() instanceof Geronimo3xInstalledLocalContainer)
        {
            ((Geronimo3xInstalledLocalContainer) getContainer()).prepareJvmLauncher(java);
            java.setMainClass("org.apache.geronimo.cli.deployer.DeployerCLI");
        }
        else
        {
            java.setJarFile(new File(getInstalledContainer().getHome(), "bin/deployer.jar"));
        }
        java.addAppArguments(action);

        return java;
    }

    /**
     * Add deployable path and plan arguments to the deployer Ant Java task.
     * @param java the JVM launcher
     * @param deployable the target deployable
     */
    private void addPathArgument(JvmLauncher java, Deployable deployable)
    {
        String deployableFile = deployable.getFile();

        if (deployable instanceof WAR && ((WAR) deployable).isExpanded())
        {
            throw new ContainerException(
                "The Apache Geronimo container does not support expanded WARs");
        }

        // add deployable path
        java.addAppArguments(deployableFile);

        // add deployable plan
        if (deployable instanceof GeronimoDeployable)
        {
            GeronimoDeployable geronimoDeployable = (GeronimoDeployable) deployable;
            String plan =
                geronimoDeployable.getPlan((InstalledLocalContainer) this.getContainer());
            if (plan != null)
            {
                File toFile = new File(getContainer().getConfiguration().getHome(), "var/temp/"
                    + new File(deployableFile).getName() + ".xml");
                try
                {
                    FileWriter writer = new FileWriter(toFile);
                    writer.write(plan);
                    writer.close();
                }
                catch (IOException e)
                {
                    throw new CargoException("Cannot write deployment plan", e);
                }

                java.addAppArgument(toFile);
            }
        }
    }

    /**
     * Add moduleId argument to the deployer Ant Java task.
     * @param java the JVM launcher
     * @param moduleId the deployable ID
     */
    private void addModuleIdArgument(JvmLauncher java, String moduleId)
    {
        if (moduleId != null)
        {
            java.addAppArguments(moduleId);
        }
    }

    /**
     * Returns the moduleId for the specified deployable.
     * @param deployable the target deployable
     * @return the moduleId to use for the specified deployable
     */
    private String getModuleId(Deployable deployable)
    {
        String moduleId = null;

        String archiveFile = deployable.getFile();

        if (getFileHandler().exists(archiveFile))
        {
            moduleId = new File(archiveFile).getName();
            int lastDot = moduleId.lastIndexOf('.');
            if (lastDot != -1)
            {
                moduleId = moduleId.substring(0, lastDot);
            }
        }

        getLogger().debug("Computed module id [" + moduleId + "] for deployable ["
            + deployable.getFile() + "]", this.getClass().getName());

        return moduleId;
    }
}
