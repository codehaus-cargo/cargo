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
package org.codehaus.cargo.sample.maven2.remoteDeploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.maven.cli.MavenCli;

import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

/**
 * Test remote deployment.
 * 
 * @version $Id$
 */
public class RemoteDeployTest extends TestCase
{

    /**
     * Whether the initialization is complete.
     */
    private static boolean initialized = false;

    /**
     * Logger.
     */
    private Logger logger = new SimpleLogger();

    /**
     * Project directory.
     */
    private File projectDirectory;

    /**
     * Project output file.
     */
    private File output;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        File target = new File(System.getProperty("target"));
        this.projectDirectory = new File(target, "classes").getAbsoluteFile();

        this.output = new File(target, "output.log");

        synchronized (RemoteDeployTest.class)
        {
            if (!RemoteDeployTest.initialized)
            {
                final PrintStream outputStream = new PrintStream(output);

                String portOption = "-Dcargo.samples.servlet.port="
                    + System.getProperty("http.port");
                final String[] options =
                    new String[] {portOption, "-o", "-X", "clean", "cargo:deploy"};

                new Thread(new Runnable()
                {
                    public void run()
                    {
                        MavenCli maven2 = new MavenCli();
                        maven2.doMain(
                            options , projectDirectory.getPath(), outputStream, outputStream);
                    }
                }).start();

                RemoteDeployTest.initialized = true;
            }
        }
    }

    /**
     * Test remote deployment.
     * @throws Exception If anything fails.
     */
    public void testRemoteDeploy() throws Exception
    {
        waitForRemoteDeployStart();
    }

    /**
     * Test the CARGO ping component.
     * @throws Exception If anything fails.
     */
    public void testCargo() throws Exception
    {
        waitForRemoteDeployStart();

        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/cargocpc/");
        final String expected = "Cargo Ping Component";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    /**
     * Test the simple WAR (JSP).
     * @throws Exception If anything fails.
     */
    public void testSimpleWarJsp() throws Exception
    {
        waitForRemoteDeployStart();

        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/simple-war/index.jsp");
        final String expected = "Sample page for testing";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    /**
     * Wait for remote deployment to start.
     * @throws Exception If anything fails.
     */
    private void waitForRemoteDeployStart() throws Exception
    {
        String outputString = null;
        long timeout = 90 * 1000 + System.currentTimeMillis();
        while (System.currentTimeMillis() < timeout)
        {
            try
            {
                outputString = FileUtils.readFileToString(output);
            }
            catch (FileNotFoundException e)
            {
                outputString = e.toString();
            }

            if (outputString.contains("BUILD SUCCESS"))
            {
                return;
            }
            else if (outputString.contains("BUILD FAILURE"))
            {
                fail("There has been a BUILD FAILURE. Please check file " + output);
                return;
            }

            Thread.sleep(1000);
            System.gc();
        }

        fail("The file " + output + " did not have the BUILD SUCCESS message after 90 seconds. "
            + "Current content: \n\n" + outputString);
    }

}
