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
package org.codehaus.cargo.itests.daemon.browser;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.jar.JarFile;

import org.htmlunit.WebClient;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.htmlunit.html.HtmlTextInput;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

/**
 * Tests of Codehaus Cargo Daemon using a virtual browser.
 */
public class CargoDaemonBrowserTest
{

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
     * Exception starting the Cargo Daemon.
     */
    private static Exception daemonStartException = null;

    /**
     * Logger.
     */
    private static Logger logger = new SimpleLogger();

    /**
     * Start the standalone Cargo Daemon.
     * @throws Exception If anything goes wrong.
     */
    @BeforeAll
    protected static void setUp() throws Exception
    {
        new Thread()
        {
            @Override
            public void run()
            {
                File daemonFile =
                    new File(System.getProperty("artifacts.dir"), "cargo-daemon-webapp.jar");
                try (JarFile daemonJar = new JarFile(daemonFile);
                    URLClassLoader daemonClassLoader =
                        new URLClassLoader(new URL[] {daemonFile.toURI().toURL()}))
                {
                    CargoDaemonBrowserTest.daemonClassName =
                        daemonJar.getManifest().getMainAttributes().getValue("Main-Class");
                    Class daemonClass = daemonClassLoader.loadClass(
                        CargoDaemonBrowserTest.daemonClassName);
                    Method daemonMain = daemonClass.getMethod("main", String[].class);
                    String[] daemonArguments =
                        new String[] {"-p", System.getProperty("daemon.port")};
                    daemonMain.invoke(null, (Object) daemonArguments);
                }
                catch (Exception e)
                {
                    CargoDaemonBrowserTest.daemonStartException = e;
                    CargoDaemonBrowserTest.logger.warn("Cannot start daemon: " + e,
                        CargoDaemonBrowserTest.class.getName());
                }
            }
        }
            .start();

        if (CargoDaemonBrowserTest.daemonUrl == null)
        {
            CargoDaemonBrowserTest.daemonUrl =
                new URL("http://localhost:" + System.getProperty("daemon.port") + "/");
        }

        DeployableMonitor daemonMonitor =
            new URLDeployableMonitor(CargoDaemonBrowserTest.daemonUrl);
        DeployerWatchdog daemonWatchdog = new DeployerWatchdog(daemonMonitor);
        try
        {
            daemonWatchdog.watchForAvailability();
        }
        catch (Exception e)
        {
            if (CargoDaemonBrowserTest.daemonStartException != null)
            {
                throw CargoDaemonBrowserTest.daemonStartException;
            }
            else
            {
                throw e;
            }
        }
    }

    /**
     * Kills the standalone Cargo Daemon.
     * @throws Exception If anything goes wrong.
     */
    @AfterAll
    protected static void tearDown() throws Exception
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

    /**
     * Test starting / stopping container.
     * @throws Exception If anything fails.
     */
    @Test
    public void testStartStopContainer() throws Exception
    {
        WebClient webClient = new WebClient();
        HtmlPage htmlPage = webClient.getPage(CargoDaemonBrowserTest.daemonUrl);

        Assertions.assertTrue(
            htmlPage.asNormalizedText().contains("Welcome to the Cargo Daemon Web site"),
                "The Cargo Daemon home page should have loaded");
        Assertions.assertFalse(htmlPage.asNormalizedText().contains("started"),
            "There should be no running containers");

        final long timeout = System.currentTimeMillis() + CargoDaemonBrowserTest.TIMEOUT;
        boolean foundContainerToStop = true;
        while (foundContainerToStop && System.currentTimeMillis() < timeout)
        {
            foundContainerToStop = false;
            for (DomElement handle : htmlPage.getElementsByName("handleId"))
            {
                String handleId = handle.getAttribute("value");
                if (handleId != null && !handleId.isEmpty())
                {
                    DomElement deleteButton =
                        htmlPage.getElementById("deleteContainer_" + handleId);
                    if (deleteButton != null)
                    {
                        foundContainerToStop = true;
                        deleteButton.click();
                        Thread.sleep(1000);
                        break;
                    }
                }
            }
        }

        webClient.reset();
        htmlPage = webClient.getPage(CargoDaemonBrowserTest.daemonUrl);
        ((HtmlTextInput) htmlPage.getElementByName("handleId")).setText("test-tjws");

        ((HtmlSelect) htmlPage.getElementByName("containerId"))
            .getOptionByText("jetty9x").setSelected(true);

        File jetty9x = new File(System.getProperty("artifacts.dir"), "jetty9x.zip");
        Assertions.assertTrue(jetty9x.isFile(), "File " + jetty9x + " is missing");
        ((HtmlTextInput) htmlPage.getElementByName("installerZipUrl")).setText(
            jetty9x.toURI().toURL().toString());

        File configurationDirectory =
            new File(System.getProperty("daemon.test-configurations.home"));
        Assertions.assertFalse(configurationDirectory.isDirectory(),
            "Directory " + configurationDirectory + " already exists");
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

        DeployableMonitor containerMonitor = new URLDeployableMonitor(new URL(
            "http://localhost:" + System.getProperty("servlet.port") + "/cargocpc/index.html"),
                CargoDaemonBrowserTest.TIMEOUT);
        DeployerWatchdog containerWatchdog = new DeployerWatchdog(containerMonitor);
        containerWatchdog.watchForAvailability();

        webClient.reset();
        htmlPage = webClient.getPage(CargoDaemonBrowserTest.daemonUrl);
        DomElement stopButton = htmlPage.getElementById("stopContainer_test-tjws");
        Assertions.assertNotNull(stopButton, "Container stop button did not appear. "
            + "Current content: " + htmlPage.asNormalizedText());
        Assertions.assertTrue(htmlPage.asNormalizedText().contains("started"),
            "There should be running containers");
        stopButton.click();

        containerWatchdog.watchForUnavailability();

        webClient.reset();
        htmlPage = webClient.getPage(CargoDaemonBrowserTest.daemonUrl);
        Assertions.assertFalse(htmlPage.asNormalizedText().contains("started"),
            "There should be no running containers");
    }
}
