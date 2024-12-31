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
package org.codehaus.cargo.sample.maven3.ping_test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Ping test.
 */
public class PingTest extends TestCase
{

    /**
     * Test the ping.
     * @throws Exception If anything goes wrong.
     */
    public void testPing() throws Exception
    {
        testServlet("/ping-test");
        testServlet("/ping-test-URL-path");
    }

    /**
     * Test deployed servlet.
     * @param urlPath URL path.
     * @throws Exception If anything goes wrong.
     */
    private void testServlet(String urlPath) throws Exception
    {
        final URL url = new URL("http://localhost:" + System.getProperty("http.port")
            + urlPath);

        InputStream responseStream = url.openConnection().getInputStream();
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(responseStream));
        String response = responseReader.readLine();

        assertEquals("Servlet is now ready", response);
    }

}
