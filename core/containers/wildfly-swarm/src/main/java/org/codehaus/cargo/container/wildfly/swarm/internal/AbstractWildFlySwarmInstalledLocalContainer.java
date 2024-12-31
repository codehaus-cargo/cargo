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
package org.codehaus.cargo.container.wildfly.swarm.internal;

import java.io.File;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.wildfly.swarm.WildFlySwarmPropertySet;
import org.codehaus.cargo.container.wildfly.swarm.WildFlySwarmStandaloneLocalConfiguration;

/**
 * WildFly Swarm container common implementation.
 */
public abstract class AbstractWildFlySwarmInstalledLocalContainer extends
    AbstractInstalledLocalContainer
{

    /**
     * Container capability instance.
     */
    private static final ContainerCapability CAPABILITY = new WildFlySwarmContainerCapability();

    /**
     * JVM launcher instance.
     */
    private JvmLauncher swarmJvmLauncher;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    protected AbstractWildFlySwarmInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "WildFly Swarm " + getVersion();
    }

    /**
     * Container version.
     * 
     * @return version string.
     */
    protected abstract String getVersion();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void verify()
    {
        if (getHome() == null)
        {
            throw new ContainerException("You must set the mandatory [home] property");
        }

        final File wildFlySwarmExecutable = new File(getHome());
        if (!wildFlySwarmExecutable.exists() || !wildFlySwarmExecutable.getName().endsWith(".jar"))
        {
            throw new ContainerException("[" + getHome() + "] "
                + "does not point to a valid WildFly Swarm executable.");
        }

        verifyPingURL();
    }

    /**
     * Verifies that the property {@link WildFlySwarmPropertySet#SWARM_APPLICATION_URL} has been
     * defined.
     */
    private void verifyPingURL()
    {
        String pingUrl =
                getConfiguration().getPropertyValue(WildFlySwarmPropertySet.SWARM_APPLICATION_URL);

        if (pingUrl == null || pingUrl.isEmpty())
        {
            throw new ContainerException("Missing mandatory configuration property ["
                + WildFlySwarmPropertySet.SWARM_APPLICATION_URL + "].");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return CAPABILITY;
    }

    @Override
    protected void startInternal() throws Exception
    {
        swarmJvmLauncher = createJvmLauncher(true);
        addMemoryArguments(swarmJvmLauncher);
        doStart(swarmJvmLauncher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopInternal() throws Exception
    {
        doStop(swarmJvmLauncher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        final File swarmExecutable = new File(getHome());
        swarmJvmLauncher.setJarFile(swarmExecutable);
        swarmJvmLauncher.setWorkingDirectory(new File(getFileHandler().getAbsolutePath(
            getConfiguration().getHome())));

        final Configuration configuration = getConfiguration();

        String jvmArgs = configuration.getPropertyValue(GeneralPropertySet.JVMARGS);
        if (jvmArgs == null || !jvmArgs.contains("-Dswarm.http.port="))
        {
            swarmJvmLauncher.addJvmArguments("-Dswarm.http.port="
                + getConfiguration().getPropertyValue(ServletPropertySet.PORT));
        }

        WildFlySwarmStandaloneLocalConfiguration wildFlySwarmConfiguration =
                (WildFlySwarmStandaloneLocalConfiguration) configuration;
        File swarmProjectDescriptor = wildFlySwarmConfiguration.getSwarmProjectDescriptor();
        if (swarmProjectDescriptor != null && swarmProjectDescriptor.exists())
        {
            swarmJvmLauncher.addAppArgumentLine("-s "
                + swarmProjectDescriptor.getAbsolutePath());
        }

        addDeployables();

        getLogger().info("Swarm arg line: " + swarmJvmLauncher.getCommandLine(),
                getClass().getCanonicalName());

        swarmJvmLauncher.start();
    }

    /**
     * Tells whether WildFly Swarm operates in Hollow Swarm mode.
     * @return true if WildFly Swarm operates in Hollow Swarm mode, otherwise false.
     * */
    protected boolean isHollowSwarm()
    {
        String hollowSwarmProperty
            = getConfiguration().getPropertyValue(WildFlySwarmPropertySet.SWARM_HOLLOW_ENABLED);
        return Boolean.parseBoolean(hollowSwarmProperty);
    }

    /**
     * Adds deployments on startup arg line. Only available in the Hollow Swarm mode.
     * */
    protected void addDeployables()
    {
        if (isHollowSwarm() && !getConfiguration().getDeployables().isEmpty())
        {
            getLogger().info("Deploying to Hollow Swarm.", getClass().getCanonicalName());
            for (Deployable deployable : getConfiguration().getDeployables())
            {
                swarmJvmLauncher.addAppArgument(new File(deployable.getFile()));
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        swarmJvmLauncher.kill();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (waitForStarting)
        {
            if (isHollowSwarm())
            {
                getLogger().debug("Running in Hollow Swarm mode - no waiting",
                    getClass().getCanonicalName());
                return;
            }

            waitForStarting(new WildFlySwarmStartupMonitor(this));
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }
}
