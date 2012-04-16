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
package org.codehaus.cargo.sample.maven2.runMojo;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.maven.cli.MavenCli;

import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

public class RunMojoTest extends TestCase
{

    private Logger logger = new SimpleLogger();

    private File projectDirectory;

    private File output;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        File target = new File(System.getProperty("target"));
        this.projectDirectory = new File(target, "classes").getAbsoluteFile();

        this.output = new File(target, "output.log");
    }

    public void testRunMojo() throws Exception
    {
        final PrintStream outputStream = new PrintStream(output);

        String portOption = "-Dcargo.samples.servlet.port=" + System.getProperty("http.port");
        final String[] options = new String[] { portOption, "-o", "-X", "clean", "cargo:run" };

        new Thread(new Runnable() {
            public void run() {
                MavenCli maven2 = new MavenCli();
                maven2.doMain(options , projectDirectory.getPath(), outputStream, outputStream);
            }
        }).start();

        waitForRunMojoStart();
    }

    public void testCargo() throws Exception
    {
        waitForRunMojoStart();

        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/cargocpc/");
        final String expected = "Cargo Ping Component";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    public void testSimpleWarJsp() throws Exception
    {
        waitForRunMojoStart();

        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/simple-war-" + System.getProperty("cargo.resources.version") + "/index.jsp");
        final String expected = "Sample page for testing";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    private void waitForRunMojoStart() throws Exception
    {
        long timeout = 60 * 1000 + System.currentTimeMillis();
        while (System.currentTimeMillis() < timeout)
        {
            String outputString = FileUtils.readFileToString(output);
            if (outputString.contains("Press Ctrl-C to stop the container..."))
            {
                return;
            }
            else if (outputString.contains("BUILD FAILURE"))
            {
                fail("There has been a BUILD FAILURE. Please check file " + output);
                return;
            }

            Thread.sleep(1000);
        }

        fail("The file " + output + " did not have the Ctrl-C message after 60 seconds");
    }

}
