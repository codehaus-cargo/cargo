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
package org.codehaus.cargo.sample.maven3.glassfish6xRemoteDeploy;

import java.net.URL;

import org.junit.jupiter.api.Test;

import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

/**
 * Test remote deployment on a GlassFish 6.x container.
 */
public class GlassFish6xRemoteDeployTest
{

    /**
     * Logger.
     */
    private Logger logger = new SimpleLogger();

    /**
     * Test the CARGO ping component.
     * @throws Exception If anything fails.
     */
    @Test
    public void testCargo() throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/cargocpc/");
        final String expected = "Cargo Ping Component";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

    /**
     * Test the simple WAR (JSP).
     * @throws Exception If anything fails.
     */
    @Test
    public void testSimpleWarJsp() throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/simple-war/index.jsp");
        final String expected = "Sample page for testing";

        PingUtils.assertPingTrue(url.getPath() + " not started", expected, url, logger);
    }

}
