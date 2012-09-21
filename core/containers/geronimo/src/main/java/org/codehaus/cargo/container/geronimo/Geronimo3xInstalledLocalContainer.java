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

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.geronimo.internal.Geronimo3xContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Geronimo 3.x series container implementation.
 * 
 * @version $Id$
 */
public class Geronimo3xInstalledLocalContainer extends Geronimo1xInstalledLocalContainer
{
    /**
     * Geronimo 3.x series unique id.
     */
    public static final String ID = "geronimo3x";

    /**
     * Capability of the Geronimo Container.
     */
    private ContainerCapability capability = new Geronimo3xContainerCapability();

    /**
     * {@inheritDoc}
     * @see Geronimo1xInstalledLocalContainer#Geronimo1xInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public Geronimo3xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getName()
     */
    @Override
    public String getName()
    {
        return "Geronimo " + getVersion("3.x");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    @Override
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#doStop(JvmLauncher)
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        this.getLogger().debug("Starting container " + getName(), this.getClass().getName());

        prepareJvmLauncher(java);

        java.setSystemProperty("org.apache.geronimo.config.substitution.NamingPort",
            getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));
        java.setSystemProperty("org.apache.geronimo.config.substitution.HTTPPort",
            getConfiguration().getPropertyValue(ServletPropertySet.PORT));
        java.setSystemProperty("org.apache.geronimo.config.substitution.EndPointURI",
            "http://localhost:" + getConfiguration().getPropertyValue(ServletPropertySet.PORT));

        java.setMainClass("org.apache.geronimo.cli.daemon.DaemonCLI");

        java.start();

        waitForCompletion(true);

        // deploy scheduled deployables
        GeronimoInstalledLocalDeployer deployer = new GeronimoInstalledLocalDeployer(this);
        for (Deployable deployable : this.getConfiguration().getDeployables())
        {
            deployer.deploy(deployable);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#doStop(JvmLauncher)
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        this.getLogger().debug("Stopping container " + getName(), this.getClass().getName());

        prepareJvmLauncher(java);

        java.setMainClass("org.apache.geronimo.cli.shutdown.ShutdownCLI");

        java.addAppArguments("--user");
        java.addAppArguments(getConfiguration().getPropertyValue(RemotePropertySet.USERNAME));
        java.addAppArguments("--password");
        java.addAppArguments(getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD));
        java.addAppArguments("--port");
        java.addAppArguments(getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));

        java.start();
    }

    /**
     * Prepare JVM launcher.
     * @param java JVM launcher to prepare.
     */
    public void prepareJvmLauncher(JvmLauncher java)
    {
        java.addClasspathEntries(new File(getHome(), "lib").listFiles());

        java.addJvmArguments("-javaagent:"
            + new File(getHome(), "lib/agent/transformer.jar").getAbsolutePath());

        File javaLib = new File(getJavaHome(), "lib");

        java.setSystemProperty("org.apache.geronimo.home.dir", getHome());
        java.setSystemProperty("org.apache.geronimo.server.dir", getConfiguration().getHome());
        java.setSystemProperty("karaf.home", getHome());
        java.setSystemProperty("karaf.base", getConfiguration().getHome());
        java.setSystemProperty("java.endorsed.dirs", new File(getHome(), "lib/endorsed")
            .getAbsolutePath().replace(File.separatorChar, '/')
            + File.pathSeparatorChar
            + new File(javaLib, "endorsed").getAbsolutePath().replace(File.separatorChar, '/'));
        java.setSystemProperty("java.io.tmpdir", new File(getConfiguration().getHome(),
            "/var/temp").getAbsolutePath().replace(File.separatorChar, '/'));
        java.setSystemProperty("java.util.logging.config.file",
            new File(getConfiguration().getHome(), "etc/java.util.logging.properties")
                .getAbsolutePath().replace(File.separatorChar, '/'));

        java.setSystemProperty(
            "org.apache.xbean.osgi.bundle.util.BundleResourceHelper.searchWiredBundles", "false");
        java.setSystemProperty("karaf.startLocalConsole", "false");
        java.setSystemProperty("karaf.startRemoteShell", "false");
    }

    /**
     * {@inheritDoc}
     * @see Geronimo1xInstalledLocalContainer#getVersion(String)
     */
    @Override
    protected String getVersion(String defaultVersion)
    {
        // TODO get actual version of installed Geronimo server
        return defaultVersion;
    }
}
