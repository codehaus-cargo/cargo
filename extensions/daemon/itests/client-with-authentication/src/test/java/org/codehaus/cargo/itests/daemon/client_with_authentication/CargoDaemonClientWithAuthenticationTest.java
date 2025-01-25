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
package org.codehaus.cargo.itests.daemon.client_with_authentication;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.jar.JarFile;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.tools.daemon.DaemonClient;
import org.codehaus.cargo.tools.daemon.DaemonException;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

/**
 * Tests of Codehaus Cargo Daemon using the Java client with authentication.
 */
public class CargoDaemonClientWithAuthenticationTest
{

    /**
     * Total number of tests (used to kill the Daemon when finished).
     */
    private static final int TOTAL_TESTS = 3;

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
     * Exception starting the Cargo Daemon.
     */
    private Exception daemonStartException = null;

    /**
     * Logger.
     */
    private Logger logger = new SimpleLogger();

    /**
     * Start the Cargo Daemon.
     * @throws Exception If anything goes wrong.
     */
    @BeforeEach
    protected void setUp() throws Exception
    {
        boolean startDaemon;
        synchronized (this.getClass())
        {
            startDaemon = CargoDaemonClientWithAuthenticationTest.testsRun == 0;
            CargoDaemonClientWithAuthenticationTest.testsRun++;
        }

        if (startDaemon)
        {
            new Thread()
            {
                @Override
                public void run()
                {
                    File daemonFile =
                        new File(System.getProperty("artifacts.dir"), "cargo-daemon-webapp.jar");
                    File passwordFile =
                        new File(System.getProperty("artifacts.dir"),
                            "cargo-daemon-passwords-sample.properties");
                    System.setProperty("cargo.daemon.passwordFile", passwordFile.getPath());
                    try (JarFile daemonJar = new JarFile(daemonFile);
                        URLClassLoader daemonClassLoader =
                            new URLClassLoader(new URL[] {daemonFile.toURI().toURL()}))
                    {
                        CargoDaemonClientWithAuthenticationTest.daemonClassName =
                            daemonJar.getManifest().getMainAttributes().getValue("Main-Class");
                        Class daemonClass = daemonClassLoader.loadClass(
                            CargoDaemonClientWithAuthenticationTest.daemonClassName);
                        Method daemonMain = daemonClass.getMethod("main", String[].class);
                        String[] daemonArguments =
                            new String[] {"-p", System.getProperty("daemon.port")};
                        daemonMain.invoke(null, (Object) daemonArguments);
                    }
                    catch (Exception e)
                    {
                        CargoDaemonClientWithAuthenticationTest.this.daemonStartException = e;
                        CargoDaemonClientWithAuthenticationTest.this.logger.warn(
                            "Cannot start daemon: " + e,
                                CargoDaemonClientWithAuthenticationTest.class.getName());
                    }
                }
            }
                .start();
        }

        synchronized (this.getClass())
        {
            if (CargoDaemonClientWithAuthenticationTest.daemonUrl == null)
            {
                CargoDaemonClientWithAuthenticationTest.daemonUrl =
                    new URL("http://localhost:" + System.getProperty("daemon.port") + "/");
            }
        }

        DeployableMonitor daemonMonitor =
            new URLDeployableMonitor(CargoDaemonClientWithAuthenticationTest.daemonUrl);
        DeployerWatchdog daemonWatchdog = new DeployerWatchdog(daemonMonitor);
        try
        {
            daemonWatchdog.watchForAvailability();
        }
        catch (Exception e)
        {
            if (daemonStartException != null)
            {
                throw daemonStartException;
            }
            else
            {
                throw e;
            }
        }
    }

    /**
     * Stop the Cargo Daemon.
     */
    @AfterEach
    protected void tearDown()
    {
        boolean stopDaemon;
        synchronized (this.getClass())
        {
            stopDaemon = CargoDaemonClientWithAuthenticationTest.testsRun
                == CargoDaemonClientWithAuthenticationTest.TOTAL_TESTS;
        }

        if (stopDaemon)
        {
            for (Map.Entry<Thread, StackTraceElement[]> thread
                : Thread.getAllStackTraces().entrySet())
            {
                for (StackTraceElement stackTraceElement : thread.getValue())
                {
                    if (stackTraceElement.getClassName().contains(
                        CargoDaemonClientWithAuthenticationTest.daemonClassName))
                    {
                        thread.getKey().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Test the Cargo Daemon client without authentication.
     * @throws Exception If anything fails.
     */
    @Test
    public void testCargoDaemonClientWithoutAuthentication() throws Exception
    {
        DaemonClient client = new DaemonClient(CargoDaemonClientWithAuthenticationTest.daemonUrl);
        try
        {
            client.getHandles();
            Assertions.fail("Unauthenticated connection succeeded");
        }
        catch (DaemonException e)
        {
            Assertions.assertTrue(e.getMessage().startsWith(
                "The username and password you provided are not correct (error 401)"));
        }
    }

    /**
     * Test the Cargo Daemon client with a client with a username only (empty password).
     * @throws Exception If anything fails.
     */
    @Test
    public void testCargoDaemonClientWithUsernameOnly() throws Exception
    {
        DaemonClient client = new DaemonClient(
            CargoDaemonClientWithAuthenticationTest.daemonUrl, "cargo-daemon-user_", null);
        client.getHandles();
    }

    /**
     * Test the Cargo Daemon client with a client with a username and password.
     * @throws Exception If anything fails.
     */
    @Test
    public void testCargoDaemonClientWithUsernameAndPassword() throws Exception
    {
        DaemonClient client = new DaemonClient(
            CargoDaemonClientWithAuthenticationTest.daemonUrl,
                "cargo-daemon-user1", "cargo-password");
        client.getHandles();

        client = new DaemonClient(
            CargoDaemonClientWithAuthenticationTest.daemonUrl,
                "cargo-daemon-user0", "{cargo-password");
        client.getHandles();

        client = new DaemonClient(
            CargoDaemonClientWithAuthenticationTest.daemonUrl,
                "cargo-daemon-user2", "cargo-password");
        client.getHandles();

        client = new DaemonClient(
            CargoDaemonClientWithAuthenticationTest.daemonUrl,
                "cargo-daemon-user3", "cargo-password");
        client.getHandles();
    }

}
