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
package org.codehaus.cargo.sample.ant.users_test;

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
 * Tests whether User configuration works.
 */
public class UsersTest extends TestCase
{

    /**
     * Logger.
     */
    protected Logger logger = new SimpleLogger();

    /**
     * Test verifying user configuration by cargo property.
     * @throws Exception If anything fails.
     */
    public void testAuthenticationByProperty() throws Exception
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
     * Test verifying user configuration by ANT configuration &lt;user&gt; element.
     * @throws Exception If anything fails.
     */
    public void testAuthenticationByElement() throws Exception
    {
        URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/authentication-war/test");
        final String expected = "Principal name [elementUser], Is user in \"cargo\" role [true]";

        Map<String, String> requestProperties = new HashMap<String, String>();
        requestProperties.put("Authorization", "Basic "
            + Base64.getEncoder().encodeToString(
                "elementUser:pass".getBytes(StandardCharsets.UTF_8)));

        PingUtils.assertPingTrue("Failed authentication", expected, url,
                requestProperties, logger);
    }
}
