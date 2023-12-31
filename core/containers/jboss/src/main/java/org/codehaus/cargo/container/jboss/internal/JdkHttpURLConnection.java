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
package org.codehaus.cargo.container.jboss.internal;

import java.io.IOException;
import java.net.URL;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.internal.http.HttpRequest;
import org.codehaus.cargo.container.internal.http.HttpResult;
import org.codehaus.cargo.util.log.Logger;

/**
 * Perform a HTTP GET to a URL. We have this as a separate class so we can mock it in our tests.
 */
public class JdkHttpURLConnection
{
    /**
     * Connect to given URL as an HTTP <code>GET</code> request.
     * @param url URL to connect to.
     * @param username Username (optional, can be <code>null</code>)
     * @param password Password (optional, can be <code>null</code>)
     * @param timeout Connection timeout (optional, can be <code>0</code>)
     * @param logger Codehaus Cargo logger to use
     * @throws IOException If connecting to the JBoss server fails
     */
    public void connect(String url, String username, String password, int timeout, Logger logger)
        throws IOException
    {
        HttpResult response;
        HttpRequest request = new HttpRequest(new URL(url), timeout);
        request.setLogger(logger);
        request.setAuthentication(username, password);
        response = request.get();
        if (!response.isSuccessful())
        {
            throw new ContainerException("Failed to deploy to [" + url + "], response code: "
                + response.getResponseCode() + ", response message: "
                    + response.getResponseMessage() + ", response body: "
                        + response.getResponseBody());
        }
    }
}
