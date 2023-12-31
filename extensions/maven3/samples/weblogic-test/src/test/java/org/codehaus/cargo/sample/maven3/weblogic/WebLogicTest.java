/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.sample.maven3.weblogic;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

/**
 * Test WebLogic container.
 */
public class WebLogicTest extends TestCase
{

    /**
     * Logger.
     */
    private Logger logger = new SimpleLogger();

    /**
     * Test changing of classpath with the WebSphere container.
     * @throws Exception If anything fails.
     */
    public void testClasspathWar() throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/classpath-war/test");
        final String expected = "Got class!";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    /**
     * Test deploying a simple EAR.
     * @throws Exception If anything fails.
     */
    public void testSimpleEar() throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/simpleweb");
        final String expected = "Sample page for testing";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    /**
     * Test deploying a simple WAR.
     * @throws Exception If anything fails.
     */
    public void testSimpleWar() throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/simple-war");
        final String expected = "Sample page for testing";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    /**
     * Test verifying datasource.
     * @throws Exception If anything fails.
     */
    public void testDatasource() throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/datasource-war/test");
        final String expected = "Got connection!";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    /**
     * Test verifying JMS.
     * @throws Exception If anything fails.
     */
    public void testJms() throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/jms-queue-war/test");
        final String expected = "Got queue!";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    /**
     * Test verifying user creation and WAR authentication.
     * @throws Exception If anything fails.
     */
    public void testAuthentication() throws Exception
    {
        URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/authentication-war/test");
        final String expected = "Principal name [someone], Is user in \"cargo\" role [true]";

        Map<String, String> requestProperties = new HashMap<String, String>();
        requestProperties.put("Authorization", "Basic "
            + Base64.getEncoder().encodeToString(
                "someone:p@ssw0rd".getBytes(StandardCharsets.UTF_8)));

        PingUtils.assertPingTrue("Failed authentication", expected, url,
                requestProperties, logger);
    }

    /**
     * Test verifying system property configuration.
     * @throws Exception If anything fails.
     */
    public void testSystemProperty() throws Exception
    {
        URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/systemproperty-war"
            + "/test?systemPropertyName=cargo.system.property");
        final String expected = "CargoSystemProp";

        PingUtils.assertPingTrue("System property cargo.system.property not found",
                expected, url, logger);
    }
}
