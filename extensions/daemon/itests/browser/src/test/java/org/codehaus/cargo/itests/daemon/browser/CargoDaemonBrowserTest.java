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

public class CargoDaemonBrowserTest extends TestCase
{

    private Logger logger = new SimpleLogger();

    private static final int TOTAL_TESTS = 2;

    private static final long TIMEOUT = 30 * 1000;

    private static int TESTS_RUN = 0;

    private static Thread DAEMON_THREAD = null;

    private static URL DAEMON_URL = null;

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
            startDaemon = CargoDaemonBrowserTest.TESTS_RUN == 0;
            CargoDaemonBrowserTest.TESTS_RUN++;
        }

        if (startDaemon)
        {
            CargoDaemonBrowserTest.DAEMON_THREAD = new Thread()
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
                        CargoDaemonBrowserTest.this.logger.warn("Cannot start daemon: " + e,
                            CargoDaemonBrowserTest.class.getName());
                    }
                }
            };
            CargoDaemonBrowserTest.DAEMON_THREAD.start();
        }

        synchronized (this.getClass())
        {
            if (CargoDaemonBrowserTest.DAEMON_URL == null)
            {
                CargoDaemonBrowserTest.DAEMON_URL =
                    new URL("http://localhost:" + System.getProperty("daemon.port") + "/");
            }
        }

        DeployableMonitor daemonMonitor =
            new URLDeployableMonitor(CargoDaemonBrowserTest.DAEMON_URL);
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
            stopDaemon = CargoDaemonBrowserTest.TESTS_RUN == CargoDaemonBrowserTest.TOTAL_TESTS;
        }

        if (stopDaemon)
        {
            CargoDaemonBrowserTest.DAEMON_THREAD.interrupt();
            CargoDaemonBrowserTest.DAEMON_THREAD = null;
            System.gc();
        }
    }

    public void testCargoDaemonWelcomePage() throws Exception
    {
        PingUtils.assertPingTrue("Cargo Daemon not started", "Welcome to Cargo Daemon Web site",
            CargoDaemonBrowserTest.DAEMON_URL, logger);
    }

    public void testStartStopContainer() throws Exception
    {
        WebClient webClient = new WebClient();
        HtmlPage htmlPage = webClient.getPage(CargoDaemonBrowserTest.DAEMON_URL);

        assertTrue("There should be no running containers",
            htmlPage.asText().contains("No running containers"));
        ((HtmlTextInput) htmlPage.getElementByName("handleId")).setText("test1");

        ((HtmlSelect) htmlPage.getElementByName("containerId"))
            .getOptionByText("jetty7x").setSelected(true);

        File jetty7x = new File(System.getProperty("artifacts.dir"), "jetty7x.zip");
        assertTrue("File " + jetty7x + " is missing", jetty7x.isFile());
        ((HtmlTextInput) htmlPage.getElementByName("installerZipUrl")).setText(
            jetty7x.toURI().toURL().toString());

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

        HtmlElement stopButton;
        long timeout = System.currentTimeMillis() + CargoDaemonBrowserTest.TIMEOUT;
        for (stopButton = null;
            stopButton == null && System.currentTimeMillis() < timeout;
            stopButton = htmlPage.getElementById("stopContainer_test1"))
        {
            Thread.sleep(1000);
        }
        assertNotNull("Container stop button did not appear. Current content: "
            + htmlPage.asText(), stopButton);
        assertFalse("There should be running containers",
            htmlPage.asText().contains("No running containers"));
        stopButton.click();

        daemonWatchdog.watchForUnavailability();
    }

}
