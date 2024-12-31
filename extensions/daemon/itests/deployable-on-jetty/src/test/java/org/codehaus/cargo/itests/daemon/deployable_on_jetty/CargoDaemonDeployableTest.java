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
package org.codehaus.cargo.itests.daemon.deployable_on_jetty;

import java.io.File;
import java.net.URL;

import org.htmlunit.WebClient;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.htmlunit.html.HtmlTextInput;

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
 * Tests of Codehaus Cargo Daemon started on Jetty using the Java client.
 */
public class CargoDaemonDeployableTest extends TestCase
{

    /**
     * Timeout while waiting for the container to start.
     */
    private static final long TIMEOUT = 60 * 1000;

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

        synchronized (this.getClass())
        {
            if (CargoDaemonDeployableTest.daemonUrl == null)
            {
                CargoDaemonDeployableTest.daemonUrl =
                    new URL("http://localhost:" + System.getProperty("daemon.port")
                        + "/cargo-daemon-webapp/");
            }
        }

        DeployableMonitor daemonMonitor =
            new URLDeployableMonitor(CargoDaemonDeployableTest.daemonUrl);
        DeployerWatchdog daemonWatchdog = new DeployerWatchdog(daemonMonitor);
        daemonWatchdog.watchForAvailability();
    }

    /**
     * Test the Daemon welcome page.
     * @throws Exception If anything fails.
     */
    public void testCargoDaemonWelcomePage() throws Exception
    {
        PingUtils.assertPingTrue("Cargo Daemon not started",
            "Welcome to the Cargo Daemon Web site", CargoDaemonDeployableTest.daemonUrl, logger);
    }

    /**
     * Test starting / stopping container.
     * @throws Exception If anything fails.
     */
    public void testStartStopContainer() throws Exception
    {
        WebClient webClient = new WebClient();
        HtmlPage htmlPage = webClient.getPage(CargoDaemonDeployableTest.daemonUrl);

        assertFalse("There should be no running containers",
            htmlPage.asNormalizedText().contains("started"));

        final long timeout = System.currentTimeMillis() + CargoDaemonDeployableTest.TIMEOUT;
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
        htmlPage = webClient.getPage(CargoDaemonDeployableTest.daemonUrl);
        ((HtmlTextInput) htmlPage.getElementByName("handleId")).setText("test-on-jetty");

        ((HtmlSelect) htmlPage.getElementByName("containerId"))
            .getOptionByText("jetty9x").setSelected(true);

        File jetty9x = new File(System.getProperty("artifacts.dir"), "jetty9x.zip");
        assertTrue("File " + jetty9x + " is missing", jetty9x.isFile());
        ((HtmlTextInput) htmlPage.getElementByName("installerZipUrl")).setText(
            jetty9x.toURI().toURL().toString());

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
                CargoDaemonDeployableTest.TIMEOUT);
        DeployerWatchdog daemonWatchdog = new DeployerWatchdog(daemonMonitor);
        daemonWatchdog.watchForAvailability();

        webClient.reset();
        htmlPage = webClient.getPage(CargoDaemonDeployableTest.daemonUrl);
        DomElement stopButton = htmlPage.getElementById("stopContainer_test-on-jetty");
        assertNotNull("Container stop button did not appear. Current content: "
            + htmlPage.asNormalizedText(), stopButton);
        assertTrue("There should be running containers",
            htmlPage.asNormalizedText().contains("started"));
        stopButton.click();

        daemonWatchdog.watchForUnavailability();

        webClient.reset();
        htmlPage = webClient.getPage(CargoDaemonDeployableTest.daemonUrl);
        assertFalse("There should be no running containers",
            htmlPage.asNormalizedText().contains("started"));
    }
}
