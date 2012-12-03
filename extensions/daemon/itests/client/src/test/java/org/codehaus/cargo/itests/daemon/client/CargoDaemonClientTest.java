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
package org.codehaus.cargo.itests.daemon.client;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import junit.framework.TestCase;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;

import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.tools.daemon.DaemonClient;
import org.codehaus.cargo.tools.daemon.DaemonStart;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

public class CargoDaemonClientTest extends TestCase
{

    private Logger logger = new SimpleLogger();

    private static final int TOTAL_TESTS = 1;

    private static final long TIMEOUT = 60 * 1000;

    private static int TESTS_RUN = 0;

    private static Thread DAEMON_THREAD = null;

    private static URL DAEMON_URL = null;

    private static ContainerFactory CONTAINER_FACTORY = null;

    private static ConfigurationFactory CONFIGURATION_FACTORY = null;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        boolean startDaemon;
        synchronized (this.getClass())
        {
            startDaemon = CargoDaemonClientTest.TESTS_RUN == 0;
            CargoDaemonClientTest.TESTS_RUN++;
        }

        if (startDaemon)
        {
            CargoDaemonClientTest.DAEMON_THREAD = new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        File daemonFile = new File(System.getProperty("artifacts.dir"),
                            "cargo-daemon-webapp.jar");
                        JarFile daemonJar = new JarFile(daemonFile);
                        String daemonClassName =
                            daemonJar.getManifest().getMainAttributes().getValue("Main-Class");
                        URL[] daemonURLs = new URL[] { daemonFile.toURI().toURL() };
                        ClassLoader daemonClassLoader = new URLClassLoader(daemonURLs);
                        Class daemonClass = daemonClassLoader.loadClass(daemonClassName);
                        Method daemonMain = daemonClass.getMethod("main", String[].class);
                        String[] daemonArguments =
                            new String[] { "-p", System.getProperty("daemon.port") };
                        daemonMain.invoke(null, (Object) daemonArguments);
                    }
                    catch (Exception e)
                    {
                        CargoDaemonClientTest.this.logger.warn("Cannot start daemon: " + e,
                            CargoDaemonClientTest.class.getName());
                    }
                }
            };
            CargoDaemonClientTest.DAEMON_THREAD.start();
            CargoDaemonClientTest.CONTAINER_FACTORY = new DefaultContainerFactory();
            CargoDaemonClientTest.CONFIGURATION_FACTORY = new DefaultConfigurationFactory();
        }

        synchronized (this.getClass())
        {
            if (CargoDaemonClientTest.DAEMON_URL == null)
            {
                CargoDaemonClientTest.DAEMON_URL =
                    new URL("http://localhost:" + System.getProperty("daemon.port") + "/");
            }
        }

        DeployableMonitor daemonMonitor =
            new URLDeployableMonitor(CargoDaemonClientTest.DAEMON_URL);
        DeployerWatchdog daemonWatchdog = new DeployerWatchdog(daemonMonitor);
        daemonWatchdog.watchForAvailability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();

        boolean stopDaemon;
        synchronized (this.getClass())
        {
            stopDaemon = CargoDaemonClientTest.TESTS_RUN == CargoDaemonClientTest.TOTAL_TESTS;
        }

        if (stopDaemon)
        {
            CargoDaemonClientTest.DAEMON_THREAD.interrupt();
            CargoDaemonClientTest.DAEMON_THREAD = null;
            System.gc();
        }
    }

    public void testStartStopContainer() throws Exception
    {
        File jetty7x = new File(System.getProperty("artifacts.dir"), "jetty7x.zip");
        assertTrue("File " + jetty7x + " is missing", jetty7x.isFile());

        File configurationDirectory = new File(System.getProperty("daemon.home"));
        assertTrue("Directory " + configurationDirectory + " does not exist",
            configurationDirectory.isDirectory());
        configurationDirectory = new File(configurationDirectory, "configuration");
        assertFalse("Directory " + configurationDirectory + " already exists",
            configurationDirectory.isDirectory());

        Configuration configuration =
            CargoDaemonClientTest.CONFIGURATION_FACTORY.createConfiguration("jetty7x",
                ContainerType.INSTALLED, ConfigurationType.STANDALONE,
                    configurationDirectory.getAbsolutePath());
        InstalledLocalContainer container = (InstalledLocalContainer)
            CargoDaemonClientTest.CONTAINER_FACTORY.createContainer("jetty7x",
            ContainerType.INSTALLED, configuration);
        configuration.setProperty(ServletPropertySet.PORT, System.getProperty("servlet.port"));
        configuration.setProperty(GeneralPropertySet.RMI_PORT, System.getProperty("rmi.port"));

        DaemonClient client = new DaemonClient(CargoDaemonClientTest.DAEMON_URL);

        DaemonStart start = new DaemonStart();
        start.setContainer(container);
        start.setHandleId("test1");
        start.setInstallerZipFile(jetty7x.getAbsolutePath());
        client.start(start);

        DeployableMonitor daemonMonitor = new URLDeployableMonitor(new URL(
            "http://localhost:" + System.getProperty("servlet.port") + "/cargocpc/index.html"),
                CargoDaemonClientTest.TIMEOUT);
        DeployerWatchdog daemonWatchdog = new DeployerWatchdog(daemonMonitor);
        daemonWatchdog.watchForAvailability();

        client.stop("test1");
        daemonWatchdog.watchForUnavailability();
    }

}
