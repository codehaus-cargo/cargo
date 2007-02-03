/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.Base64;

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Implementation of {@link HttpURLConnection} using the JDK's
 * {@link java.net.HttpURLConnection} class.
 *
 * @version $Id$
 */
public class JdkHttpURLConnection implements HttpURLConnection
{
    /**
     * {@inheritDoc}
     * @see HttpURLConnection#connect(String, String, String)
     */
    public void connect(String url, String username, String password)
    {
        try
        {
            java.net.HttpURLConnection connection =
                (java.net.HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Authorization",
                getBasicAuthorizationHeader(username, password));

            BufferedReader reader =
                new BufferedReader(new InputStreamReader(connection.getInputStream()));
            reader.readLine();
            reader.close();
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
        StringBuffer buffer = new StringBuffer();
        buffer.append(username).append(':').append(password);
        return "Basic " + new String(Base64.encodeBase64(buffer.toString().getBytes()));
    }
}
