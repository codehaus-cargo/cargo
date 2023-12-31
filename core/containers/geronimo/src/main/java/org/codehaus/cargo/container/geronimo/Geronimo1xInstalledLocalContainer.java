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
package org.codehaus.cargo.container.geronimo;

import java.io.File;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.geronimo.internal.GeronimoUtils;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Geronimo 1.x series container implementation.
 */
public class Geronimo1xInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Geronimo 1.x series unique id.
     */
    public static final String ID = "geronimo1x";

    /**
     * Capability of the Geronimo Container.
     */
    private ContainerCapability capability = new J2EEContainerCapability();

    /**
     * Geronimo utilities.
     */
    private GeronimoUtils geronimoUtils;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Geronimo1xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
        this.geronimoUtils = new GeronimoUtils(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "Geronimo 1.x";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        this.getLogger().debug("Starting container " + getName(), this.getClass().getName());

        java.setJarFile(new File(getConfiguration().getHome(), "bin/server.jar"));

        java.setSystemProperty("org.apache.geronimo.server.dir", getConfiguration().getHome());
        java.setSystemProperty("java.io.tmpdir",
            new File(getConfiguration().getHome(), "/var/temp").getPath());

        java.start();

        waitForCompletion(true);

        // deploy scheduled deployables
        GeronimoInstalledLocalDeployer deployer = new GeronimoInstalledLocalDeployer(this);
        for (Deployable deployable : this.getConfiguration().getDeployables())
        {
            deployer.redeploy(deployable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        this.getLogger().debug("Stopping container " + getName(), this.getClass().getName());

        java.setJarFile(new File(getConfiguration().getHome(), "bin/shutdown.jar"));

        java.setSystemProperty("org.apache.geronimo.server.dir", getConfiguration().getHome());
        java.setSystemProperty("java.io.tmpdir",
            new File(getConfiguration().getHome(), "/var/temp").getPath());

        java.addAppArguments("--user");
        java.addAppArguments(getConfiguration().getPropertyValue(RemotePropertySet.USERNAME));
        java.addAppArguments("--password");
        java.addAppArguments(getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD));
        java.addAppArguments("--port");
        java.addAppArguments(getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));

        java.start();
    }

    /**
     * Replace default CPC progress monitor by a log progress monitor.
     * 
     * {@inheritDoc}
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        boolean exitCondition;

        getLogger().debug("Checking if Geronimo is started using:"
            + " hostname [" + getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME)
            + "], RMI port [" + getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT)
            + "], username [" + getConfiguration().getPropertyValue(RemotePropertySet.USERNAME)
            + "], password [" + getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD)
            + "]", this.getClass().getName());

        long startTime = System.currentTimeMillis();
        boolean isStarted;
        do
        {
            if (System.currentTimeMillis() - startTime > getTimeout())
            {
                setState(State.UNKNOWN);
                String message = "Container failed to start within "
                    + "the timeout period [" + getTimeout()
                    + "]. The Container state is thus unknown.";
                getLogger().info(message, this.getClass().getName());
                throw new ContainerException(message);
            }

            Thread.sleep(1000);

            isStarted = geronimoUtils.isGeronimoStarted();

            exitCondition = waitForStarting ? !isStarted : isStarted;
        }
        while (exitCondition);

        Thread.sleep(5000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return this.capability;
    }
}
