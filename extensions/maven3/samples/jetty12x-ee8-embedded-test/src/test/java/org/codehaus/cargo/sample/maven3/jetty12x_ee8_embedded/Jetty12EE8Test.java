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
package org.codehaus.cargo.sample.maven3.jetty12x_ee8_embedded;

import java.net.URL;

import org.junit.jupiter.api.Test;

import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.SimpleLogger;

/**
 * Test EE8 on a Jetty 12.x container.
 */
public class Jetty12EE8Test
{

    /**
     * Logger.
     */
    private Logger logger = new SimpleLogger();

    /**
     * Test EE8 on a Jetty 12.x embedded container.
     * @throws Exception If anything fails.
     */
    @Test
    public void testEe8() throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + "/simple-war/version.jsp");
        final String expected = "Servlet version: 4.0";

        PingUtils.assertPingTrue(url.getPath() + " shows wrong EE version", expected, url, logger);
    }

}
