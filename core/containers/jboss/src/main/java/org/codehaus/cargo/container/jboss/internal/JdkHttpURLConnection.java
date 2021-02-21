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
package org.codehaus.cargo.container.jboss.internal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.codehaus.cargo.container.ContainerException;

/**
 * Implementation of {@link HttpURLConnection} using the JDK's {@link java.net.HttpURLConnection}
 * class.
 */
public class JdkHttpURLConnection implements HttpURLConnection
{
    /**
     * Socket read timeout, in milliseconds
     */
    private int timeout;

    /**
     * Sets the timeout to a specified value, in milliseconds.
     * @param timeout Timeout value (in milliseconds).
     * @see java.net.HttpURLConnection#setReadTimeout(int)
     */
    @Override
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(String url, String username, String password)
    {
        try
        {
            java.net.HttpURLConnection connection =
                (java.net.HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Authorization",
                getBasicAuthorizationHeader(username, password));
            if (timeout > 0)
            {
                connection.setReadTimeout(timeout);
            }

            try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(connection.getInputStream())))
            {
                reader.readLine();
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to deploy to [" + url + "]", e);
        }
    }

    /**
     * @param username the username to connect with
     * @param password the password to connect with
     * @return the HTTP Basic Authorization header value for the supplied username and password.
     */
    private String getBasicAuthorizationHeader(String username, String password)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(username).append(':').append(password);
        return "Basic "
            + Base64.getEncoder().encodeToString(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
}
