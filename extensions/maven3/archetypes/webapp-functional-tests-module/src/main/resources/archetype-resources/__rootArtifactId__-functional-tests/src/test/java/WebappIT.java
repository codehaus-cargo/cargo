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
package ${package};

import java.net.URL;
import java.net.HttpURLConnection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the Web application, by checking that the index page returns a code 200.
 */
public class WebappIT
{
    /**
     * Base URL used in tests, includes in particular the container's port number.
     */
    private static String baseUrl;

    /**
     * Initialize tests by saving the base URL used in tests using the container's port number.
     */
    @BeforeAll
    public static void initializeTest()
    {
        String port = System.getProperty("servlet.port");
        WebappIT.baseUrl = "http://localhost:" + port + "/${rootArtifactId}-webapp";
    }

    /**
     * Call the index page on the container.
     * @throws Exception if anything goes wrong.
     */
    @Test
    public void callIndexPage() throws Exception
    {
        URL url = new URL(WebappIT.baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        Assertions.assertEquals(200, connection.getResponseCode());
    }
}
