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
package org.codehaus.cargo.container.geronimo;

import java.io.File;
import java.util.Iterator;

import org.apache.tools.ant.taskdefs.Java;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.geronimo.internal.GeronimoUtils;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;

/**
 * Geronimo 1.x series container implementation.
 *
 * @version $Id$
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
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Geronimo1xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    public String getName()
    {
        return "Geronimo " + getVersion("1.x");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#doStart(org.apache.tools.ant.taskdefs.Java)
     */
    @Override
    protected void doStart(Java java) throws Exception
    {
        java.setJar(new File(getConfiguration().getHome(), "bin/server.jar"));

        java.addSysproperty(getAntUtils().createSysProperty("org.apache.geronimo.base.dir",
            getConfiguration().getHome()));
        java.addSysproperty(getAntUtils().createSysProperty("java.io.tmpdir",
            new File(getConfiguration().getHome(), "/var/temp").getPath()));

        AntContainerExecutorThread geronimoStarter = new AntContainerExecutorThread(java);
        geronimoStarter.start();

        waitForCompletion(true);

        // deploy scheduled deployables
        GeronimoInstalledLocalDeployer deployer = new GeronimoInstalledLocalDeployer(this, false);
        for (Iterator iterator = this.getConfiguration().getDeployables().iterator(); iterator
            .hasNext();)
        {
            deployer.deploy((Deployable) iterator.next());
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#doStop(org.apache.tools.ant.taskdefs.Java)
     */
    @Override
    protected void doStop(Java java) throws Exception
    {
        java.setJar(new File(getConfiguration().getHome(), "bin/shutdown.jar"));

        java.addSysproperty(getAntUtils().createSysProperty("org.apache.geronimo.base.dir",
            getConfiguration().getHome()));
        java.addSysproperty(getAntUtils().createSysProperty("java.io.tmpdir",
            new File(getConfiguration().getHome(), "/var/temp").getPath()));

        java.createArg().setValue("--user");
        java.createArg().setValue(getConfiguration().getPropertyValue(RemotePropertySet.USERNAME));
        java.createArg().setValue("--password");
        java.createArg().setValue(getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD));
        java.createArg().setValue("--port");
        java.createArg().setValue(getConfiguration().getPropertyValue(
            GeneralPropertySet.RMI_PORT));

        AntContainerExecutorThread geronimoStopper = new AntContainerExecutorThread(java);
        geronimoStopper.start();
    }

    /**
     * Replace default CPC progress monitor by a log progress monitor.
     *
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#waitForCompletion(boolean)
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        boolean exitCondition;

        GeronimoUtils geronimoUtils = new GeronimoUtils();

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
            if ((System.currentTimeMillis() - startTime) > getTimeout())
            {
                setState(State.UNKNOWN);
                String message = "Container failed to start within "
                    + "the timeout period [" + getTimeout()
                    + "]. The Container state is thus unknown.";
                getLogger().info(message, this.getClass().getName());
                throw new ContainerException(message);
            }

            Thread.sleep(1000);

            isStarted = geronimoUtils.isGeronimoStarted(
                getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME),
                getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT),
                getConfiguration().getPropertyValue(RemotePropertySet.USERNAME),
                getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD));

            exitCondition = waitForStarting ? !isStarted : isStarted;

        } while (exitCondition);

        Thread.sleep(5000);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * Parse installed Geronimo version.
     *
     * @return the Geronimo version, or <code>defaultVersion</code> if the version number could not
     *         be determined
     * @param defaultVersion the default version used if the exact Geronimo version can't be
     * determined
     */
    protected final String getVersion(String defaultVersion)
    {
        //TODO get actual version of installed Geronimo server
        return defaultVersion;
    }
}
