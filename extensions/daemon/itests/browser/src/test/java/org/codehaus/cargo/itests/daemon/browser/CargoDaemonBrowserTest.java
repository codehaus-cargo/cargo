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
package org.codehaus.cargo.itests.daemon.browser;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.jar.JarFile;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import junit.framework.TestCase;

import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

/**
 * Tests of CARGO Daemon using a virtual browser.
 * 
 */
public class CargoDaemonBrowserTest extends TestCase
{

    /**
     * Timeout while waiting for the Daemon or container to start.
     */
    private static final long TIMEOUT = 60 * 1000;

    /**
     * Total number of tests (used to kill the Daemon when finished).
     */
    private static final int TOTAL_TESTS = 2;

    /**
     * Tests run so far (used to kill the Daemon when finished).
     */
    private static int testsRun = 0;

    /**
     * Class name of the Daemon.
     */
    private static String daemonClassName = null;

    /**
     * Base URL of the Daemon.
     */
    private static URL daemonUrl = null;

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
            startDaemon = CargoDaemonBrowserTest.testsRun == 0;
            CargoDaemonBrowserTest.testsRun++;
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
                        CargoDaemonBrowserTest.daemonClassName =
                            daemonJar.getManifest().getMainAttributes().getValue("Main-Class");
                        URL[] daemonURLs = new URL[] {daemonFile.toURI().toURL()};
                        ClassLoader daemonClassLoader = new URLClassLoader(daemonURLs);
                        Class daemonClass = daemonClassLoader.loadClass(
                            CargoDaemonBrowserTest.daemonClassName);
                        Method daemonMain = daemonClass.getMethod("main", String[].class);
                        String[] daemonArguments =
                            new String[] {"-p", System.getProperty("daemon.port")};
                        daemonMain.invoke(null, (Object) daemonArguments);
                    }
                    catch (Exception e)
                    {
                        CargoDaemonBrowserTest.this.logger.warn("Cannot start daemon: " + e,
                            CargoDaemonBrowserTest.class.getName());
                    }
                }
            }
                .start();
        }

        synchronized (this.getClass())
        {
            if (CargoDaemonBrowserTest.daemonUrl == null)
            {
                CargoDaemonBrowserTest.daemonUrl =
                    new URL("http://localhost:" + System.getProperty("daemon.port") + "/");
            }
        }

        DeployableMonitor daemonMonitor =
            new URLDeployableMonitor(CargoDaemonBrowserTest.daemonUrl);
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
            stopDaemon = CargoDaemonBrowserTest.testsRun == CargoDaemonBrowserTest.TOTAL_TESTS;
        }

        if (stopDaemon)
        {
            for (Map.Entry<Thread, StackTraceElement[]> thread
                : Thread.getAllStackTraces().entrySet())
            {
                for (StackTraceElement stackTraceElement : thread.getValue())
                {
                    if (stackTraceElement.getClassName().contains(
                        CargoDaemonBrowserTest.daemonClassName))
                    {
                        thread.getKey().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Test the Daemon welcome page.
     * @throws Exception If anything fails.
     */
    public void testCargoDaemonWelcomePage() throws Exception
    {
        PingUtils.assertPingTrue("Cargo Daemon not started", "Welcome to Cargo Daemon Web site",
            CargoDaemonBrowserTest.daemonUrl, logger);
    }

    /**
     * Test starting / stopping container.
     * @throws Exception If anything fails.
     */
    public void testStartStopContainer() throws Exception
    {
        WebClient webClient = new WebClient();
        HtmlPage htmlPage = webClient.getPage(CargoDaemonBrowserTest.daemonUrl);

        assertFalse("There should be no running containers",
            htmlPage.asText().contains("started"));
        ((HtmlTextInput) htmlPage.getElementByName("handleId")).setText("test1");

        ((HtmlSelect) htmlPage.getElementByName("containerId"))
            .getOptionByText("jetty7x").setSelected(true);

        File jetty7x = new File(System.getProperty("artifacts.dir"), "jetty7x.zip");
        assertTrue("File " + jetty7x + " is missing", jetty7x.isFile());
        ((HtmlTextInput) htmlPage.getElementByName("installerZipUrl")).setText(
            jetty7x.toURI().toURL().toString());

        File configurationDirectory =
            new File(System.getProperty("daemon.test-configurations.home"));
        assertFalse("Directory " + configurationDirectory + " already exists",
            configurationDirectory.isDirectory());
        ((HtmlTextInput) htmlPage.getElementByName("configurationHome")).setText(
            configurationDirectory.getAbsolutePath());

        htmlPage.getElementById("addConfigurationPropertyButton").click();
        ((HtmlTextInput) htmlPage.getElementById("configurationPropertyKey_0"))
            .setText(ServletPropertySet.PORT);
        ((HtmlTextInput) htmlPage.getElementById("configurationPropertyValue_0"))
            .setText(System.getProperty("servlet.port"));

        htmlPage.getElementById("addConfigurationPropertyButton").click();
        ((HtmlTextInput) htmlPage.getElementById("configurationPropertyKey_1"))
            .setText(GeneralPropertySet.RMI_PORT);
        ((HtmlTextInput) htmlPage.getElementById("configurationPropertyValue_1"))
            .setText(System.getProperty("rmi.port"));

        htmlPage.getElementById("submitButton").click();

        DeployableMonitor daemonMonitor = new URLDeployableMonitor(new URL(
            "http://localhost:" + System.getProperty("servlet.port") + "/cargocpc/index.html"),
                CargoDaemonBrowserTest.TIMEOUT);
        DeployerWatchdog daemonWatchdog = new DeployerWatchdog(daemonMonitor);
        daemonWatchdog.watchForAvailability();

        // htmlPage = (HtmlPage) htmlPage.refresh();
        webClient.closeAllWindows();
        htmlPage = webClient.getPage(CargoDaemonBrowserTest.daemonUrl);
        HtmlElement stopButton = htmlPage.getElementById("stopContainer_test1");
        assertNotNull("Container stop button did not appear. Current content: "
            + htmlPage.asText(), stopButton);
        assertTrue("There should be running containers",
            htmlPage.asText().contains("started"));
        stopButton.click();

        daemonWatchdog.watchForUnavailability();

        // htmlPage = (HtmlPage) htmlPage.refresh();
        webClient.closeAllWindows();
        htmlPage = webClient.getPage(CargoDaemonBrowserTest.daemonUrl);
        assertFalse("There should be no running containers",
            htmlPage.asText().contains("started"));
    }

}
