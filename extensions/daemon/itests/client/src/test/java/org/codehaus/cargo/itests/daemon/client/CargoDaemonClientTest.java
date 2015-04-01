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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import junit.framework.TestCase;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;

import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.tools.daemon.DaemonClient;
import org.codehaus.cargo.tools.daemon.DaemonStart;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

/**
 * Tests of CARGO Daemon using the Java client.
 * 
 */
public class CargoDaemonClientTest extends TestCase
{

    /**
     * Total number of tests (used to kill the Daemon when finished).
     */
    private static final int TOTAL_TESTS = 1;

    /**
     * Tests run so far (used to kill the Daemon when finished).
     */
    private static int testsRun = 0;

    /**
     * Timeout while waiting for the Daemon or container to start.
     */
    private static final long TIMEOUT = 60 * 1000;

    /**
     * Class name of the Daemon.
     */
    private static String daemonClassName = null;

    /**
     * Base URL of the Daemon.
     */
    private static URL daemonUrl = null;

    /**
     * Container factory.
     */
    private static ContainerFactory containerFactory = null;

    /**
     * Configuration factory.
     */
    private static ConfigurationFactory configurationFactory = null;

    /**
     * Logger.
     */
    private Logger logger = new SimpleLogger();

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
            startDaemon = CargoDaemonClientTest.testsRun == 0;
            CargoDaemonClientTest.testsRun++;
        }

        if (startDaemon)
        {
            new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        File daemonFile = new File(System.getProperty("artifacts.dir"),
                            "cargo-daemon-webapp.jar");
                        JarFile daemonJar = new JarFile(daemonFile);
                        CargoDaemonClientTest.daemonClassName =
                            daemonJar.getManifest().getMainAttributes().getValue("Main-Class");
                        URL[] daemonURLs = new URL[] {daemonFile.toURI().toURL()};
                        ClassLoader daemonClassLoader = new URLClassLoader(daemonURLs);
                        Class daemonClass = daemonClassLoader.loadClass(
                            CargoDaemonClientTest.daemonClassName);
                        Method daemonMain = daemonClass.getMethod("main", String[].class);
                        String[] daemonArguments =
                            new String[] {"-p", System.getProperty("daemon.port")};
                        daemonMain.invoke(null, (Object) daemonArguments);
                    }
                    catch (Exception e)
                    {
                        CargoDaemonClientTest.this.logger.warn("Cannot start daemon: " + e,
                            CargoDaemonClientTest.class.getName());
                    }
                }
            }
                .start();
            CargoDaemonClientTest.containerFactory = new DefaultContainerFactory();
            CargoDaemonClientTest.configurationFactory = new DefaultConfigurationFactory();
        }

        synchronized (this.getClass())
        {
            if (CargoDaemonClientTest.daemonUrl == null)
            {
                CargoDaemonClientTest.daemonUrl =
                    new URL("http://localhost:" + System.getProperty("daemon.port") + "/");
            }
        }

        DeployableMonitor daemonMonitor =
            new URLDeployableMonitor(CargoDaemonClientTest.daemonUrl);
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
            stopDaemon = CargoDaemonClientTest.testsRun == CargoDaemonClientTest.TOTAL_TESTS;
        }

        if (stopDaemon)
        {
            for (Map.Entry<Thread, StackTraceElement[]> thread
                : Thread.getAllStackTraces().entrySet())
            {
                for (StackTraceElement stackTraceElement : thread.getValue())
                {
                    if (stackTraceElement.getClassName().contains(
                        CargoDaemonClientTest.daemonClassName))
                    {
                        thread.getKey().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Test starting, stopping and restarting container.
     * @throws Exception If anything fails.
     */
    public void testStartStopRestartContainer() throws Exception
    {
        File jetty7x = new File(System.getProperty("artifacts.dir"), "jetty7x.zip");
        assertTrue("File " + jetty7x + " is missing", jetty7x.isFile());

        File configurationDirectory =
            new File(System.getProperty("daemon.test-configurations.home"));
        assertFalse("Directory " + configurationDirectory + " already exists",
            configurationDirectory.isDirectory());

        StandaloneLocalConfiguration configuration = (StandaloneLocalConfiguration)
            CargoDaemonClientTest.configurationFactory.createConfiguration("jetty7x",
                ContainerType.INSTALLED, ConfigurationType.STANDALONE,
                    configurationDirectory.getAbsolutePath());
        InstalledLocalContainer container = (InstalledLocalContainer)
            CargoDaemonClientTest.containerFactory.createContainer("jetty7x",
                ContainerType.INSTALLED, configuration);
        container.getSystemProperties().put("systemPropertyName", "testProperty");
        configuration.setProperty(ServletPropertySet.PORT, System.getProperty("servlet.port"));
        configuration.setProperty(GeneralPropertySet.RMI_PORT, System.getProperty("rmi.port"));
        configuration.addXmlReplacement("etc/webdefault.xml", "//web-app/description",
            "Testing XML replacements via the CARGO Daemon");
        DeployableFactory deployableFactory = new DefaultDeployableFactory();
        List<Deployable> deployables = new ArrayList<Deployable>();
        deployables.add(deployableFactory.createDeployable("jetty7x",
            new File(System.getProperty("artifacts.dir"), "simple-war.war").getAbsolutePath(),
                DeployableType.WAR));
        deployables.add(deployableFactory.createDeployable("jetty7x",
            new File(System.getProperty("artifacts.dir"),
                "systemproperty-war.war").getAbsolutePath(), DeployableType.WAR));

        DaemonClient client = new DaemonClient(CargoDaemonClientTest.daemonUrl);

        DaemonStart start = new DaemonStart();
        start.setContainer(container);
        start.setDeployables(deployables);
        start.setHandleId("test1");
        start.setInstallerZipFile(jetty7x.getAbsolutePath());
        client.start(start);

        DeployableMonitor cargoCpcMonitor = new URLDeployableMonitor(new URL(
            "http://localhost:" + System.getProperty("servlet.port") + "/cargocpc/index.html"),
                CargoDaemonClientTest.TIMEOUT);
        DeployableMonitor simpleWarMonitor = new URLDeployableMonitor(new URL(
            "http://localhost:" + System.getProperty("servlet.port") + "/simple-war/index.jsp"),
                CargoDaemonClientTest.TIMEOUT);
        DeployableMonitor systemPropertyWarMonitor = new URLDeployableMonitor(new URL(
            "http://localhost:" + System.getProperty("servlet.port")
                + "/systemproperty-war/test?systemPropertyName=testProperty"),
                    CargoDaemonClientTest.TIMEOUT);
        DeployerWatchdog cargoCpcWatchdog = new DeployerWatchdog(cargoCpcMonitor);
        cargoCpcWatchdog.watchForAvailability();
        DeployerWatchdog simpleWarWatchdog = new DeployerWatchdog(simpleWarMonitor);
        simpleWarWatchdog.watchForAvailability();
        DeployerWatchdog systemPropertyWarWatchdog =
            new DeployerWatchdog(systemPropertyWarMonitor);
        systemPropertyWarWatchdog.watchForAvailability();

        client.stop("test1");
        cargoCpcWatchdog.watchForUnavailability();

        // CARGO-1262: Try restart
        start = new DaemonStart();
        start.setHandleId("test1");
        client.start(start);
        cargoCpcWatchdog.watchForAvailability();
        simpleWarWatchdog.watchForAvailability();
        systemPropertyWarWatchdog.watchForAvailability();

        client.stop("test1");
        cargoCpcWatchdog.watchForUnavailability();

        // Check if the XML replacements worked properly
        String webdefaultXml = new DefaultFileHandler().readTextFile(
            configurationDirectory.getAbsolutePath() + "/etc/webdefault.xml", "UTF-8");
        assertTrue(webdefaultXml.contains("Testing XML replacements via the CARGO Daemon"));
    }

}
