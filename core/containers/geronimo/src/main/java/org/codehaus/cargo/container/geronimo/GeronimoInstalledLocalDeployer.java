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
import java.util.jar.JarFile;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Bundle;
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

/**
 * A Geronimo deploytool-based deployer to perform deployment to a local container.
 * 
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
     * @see AbstractInstalledLocalDeployer#AbstractInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
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
     * @throws IOException If the files in the extra classpath cannot be read
     */
    public void deployExtraClasspath(String[] extraClasspath) throws IOException
    {
        for (String extraClasspathElement : extraClasspath)
        {
            File extraClasspathElementFile = new File(extraClasspathElement);
            JarFile jarFile = new JarFile(extraClasspathElementFile);

            extraClasspathElement = extraClasspathElementFile.getName();

            String extension = extraClasspathElement.substring(
                extraClasspathElement.lastIndexOf('.') + 1);
            String artifact =
                jarFile.getManifest().getMainAttributes().getValue("Bundle-SymbolicName");
            if (artifact == null)
            {
                artifact = extraClasspathElement.substring(
                    0, extraClasspathElement.lastIndexOf('.'));
            }
            String version =
                jarFile.getManifest().getMainAttributes().getValue("Bundle-Version");
            if (version == null)
            {
                if (artifact.indexOf('-') == -1)
                {
                    version = "1.0";
                }
                else
                {
                    version = artifact.substring(artifact.lastIndexOf('-') + 1);
                    artifact = artifact.substring(0, artifact.lastIndexOf('-'));
                }
            }

            File target = new File(getInstalledContainer().getConfiguration().getHome(),
                "var/temp/" + artifact + "-" + version + "." + extension);
            getFileHandler().copyFile(
                extraClasspathElementFile.getAbsolutePath(), target.getAbsolutePath());

            JvmLauncher java = createAdminDeployerJava("install-library");
            java.addAppArguments("--groupId", "org.codehaus.cargo.classpath");
            java.addAppArgument(target);

            try
            {
                int retval = java.execute();
                if (retval != 0)
                {
                    getLogger().warn("Failed to add extra classpath element ["
                        + extraClasspathElement + "]", this.getClass().getName());
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

        try
        {
            int retval = java.execute();
            if (retval != 0)
            {
                throw new ContainerException("Failed to deploy [" + deployable + "]");
            }
        }
        catch (JvmLauncherException e)
        {
            throw new ContainerException("Failed to deploy [" + deployable + "]", e);
        }
    }

    /**
     * Deploys a RAR plan.
     * @param id Plan identifier (used for undeploying previous instance if required).
     * @param planFile XML file of plan.
     */
    public void deployRar(String id, File planFile)
    {
        File tranql = new File(getInstalledContainer().getHome(),
            "repository/org/tranql/tranql-connector-ra");
        File[] tranqlFiles = tranql.listFiles();
        if (tranqlFiles == null || tranqlFiles.length == 0)
        {
            throw new ContainerException("Directory " + tranql + " does not exist or is empty");
        }
        tranql = tranqlFiles[0];
        tranqlFiles = tranql.listFiles();
        if (tranqlFiles == null || tranqlFiles.length == 0)
        {
            throw new ContainerException("Directory " + tranql + " does not exist or is empty");
        }

        // First, attempt to undeploy
        JvmLauncher java = createAdminDeployerJava("undeploy");
        java.addAppArguments(id);
        java.execute();

        java = createAdminDeployerJava("deploy");
        java.addAppArgument(planFile);
        java.addAppArgument(tranqlFiles[0]);

        try
        {
            int retval = java.execute();
            if (retval != 0)
            {
                throw new ContainerException("Failed to deploy [" + planFile + "]");
            }
        }
        catch (JvmLauncherException e)
        {
            throw new ContainerException("Failed to deploy [" + planFile + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#start(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void start(Deployable deployable)
    {
        String deployableId = getModuleId(deployable);

        JvmLauncher java = createAdminDeployerJava("start");
        java.addAppArguments(deployableId);
        try
        {
            int retval = java.execute();
            if (retval != 0)
            {
                throw new ContainerException("Failed to start [" + deployable + "]");
            }
        }
        catch (JvmLauncherException e)
        {
            throw new ContainerException("Failed to start [" + deployable + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#stop(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void stop(Deployable deployable)
    {
        String deployableId = getModuleId(deployable);

        JvmLauncher java = createAdminDeployerJava("stop");
        java.addAppArguments(deployableId);

        try
        {
            int retval = java.execute();
            if (retval != 0)
            {
                throw new ContainerException("Failed to stop [" + deployable + "]");
            }
        }
        catch (JvmLauncherException e)
        {
            throw new ContainerException("Failed to stop [" + deployable + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        String deployableId = getModuleId(deployable);

        String command;
        if (deployable.getType() == DeployableType.BUNDLE)
        {
            command = "uninstall-bundle";
        }
        else
        {
            command = "undeploy";
        }

        JvmLauncher java = createAdminDeployerJava(command);
        java.addAppArguments(deployableId);

        try
        {
            int retval = java.execute();
            if (retval != 0)
            {
                throw new ContainerException("Failed to undeploy [" + deployable + "]");
            }
        }
        catch (JvmLauncherException e)
        {
            throw new ContainerException("Failed to undeploy [" + deployable + "]", e);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        if (deployable.getType() == DeployableType.BUNDLE)
        {
            super.redeploy(deployable);
        }
        else
        {
            JvmLauncher java = createAdminDeployerJava("redeploy");
            addPathArgument(java, deployable);
            String deployableId = getModuleId(deployable);
            java.addAppArguments(deployableId);

            try
            {
                int retval = java.execute();
                if (retval != 0)
                {
                    // Redeploy failed, attempt normal deployment
                    deploy(deployable);
                }
            }
            catch (JvmLauncherException e)
            {
                throw new ContainerException("Failed to redeploy [" + deployable + "]", e);
            }
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
                    throw new ContainerException("Cannot write deployment plan", e);
                }

                java.addAppArgument(toFile);
            }
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

        if (deployable.getType() == DeployableType.BUNDLE)
        {
            String deployableName = new File(deployable.getFile()).getName();
            try
            {
                moduleId = Long.toString(
                    new GeronimoUtils(getInstalledContainer().getConfiguration())
                       .getBundleId((Bundle) deployable));
            }
            catch (Exception e)
            {
                throw new ContainerException(
                    "Cannot get bundle ID for deployable " + deployableName, e);
            }
        }
        else
        {
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
        }

        getLogger().debug("Computed module id [" + moduleId + "] for deployable ["
            + deployable + "]", this.getClass().getName());

        return moduleId;
    }
}
