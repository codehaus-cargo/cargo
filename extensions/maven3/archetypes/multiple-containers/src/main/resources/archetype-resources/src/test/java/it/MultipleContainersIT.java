/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2021 Ali Tokmen.
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
package ${package}.it;

import java.net.URL;
import java.net.HttpURLConnection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultipleContainersIT
{
    private String baseUrl;
    private String baseUrl2;

    @Before
    public void initializeTest() throws Exception
    {
        String port = System.getProperty("servlet.port");
        this.baseUrl = "http://localhost:" + port + "/${artifactId}";
        port = System.getProperty("servlet2.port");
        this.baseUrl2 = "http://localhost:" + port + "/${artifactId}";
    }

    @Test
    public void callIndexPage() throws Exception
    {
        URL url = new URL(this.baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        Assert.assertEquals(200, connection.getResponseCode());
    }

    @Test
    public void callIndexPageOnSecondContainer() throws Exception
    {
        URL url = new URL(this.baseUrl2);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        Assert.assertEquals(200, connection.getResponseCode());
    }
}
