/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.internal.util;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.codehaus.cargo.container.internal.http.HttpRequest;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Set of common HTTP(S) utility methods.
 */
public class HttpUtils extends LoggedObject
{
    /**
     * Storage class for the HTTP ping result.
     */
    public static class HttpResult
    {
        /**
         * The HTTP connection response code (eg 200).
         */
        public int responseCode;

        /**
         * The HTTP connection response message (eg "Ok").
         */
        public String responseMessage;

        /**
         * The HTTP connection response body.
         */
        public String responseBody;
    }

    /**
     * @param pingURL the URL to ping
     * @return true if the URL can be ping or false otherwise
     */
    public boolean ping(URL pingURL)
    {
        return isAvailable(testConnectivity(pingURL, null, 0L));
    }

    /**
     * Ping a URL and store the detailed result in a {@link HttpResult} object.
     * 
     * @param pingURL the URL to ping
     * @param result the detailed ping result
     * @return true if the URL can be ping or false otherwise
     */
    public boolean ping(URL pingURL, HttpResult result)
    {
        return ping(pingURL, null, result);
    }

    /**
     * Ping a URL and store the detailed result in a {@link HttpResult} object.
     * 
     * @param pingURL the URL to ping
     * @param result the detailed ping result
     * @param timeout the timeout to wait for, 0 if waiting to infinity
     * @return true if the URL can be ping or false otherwise
     */
    public boolean ping(URL pingURL, HttpResult result, long timeout)
    {
        return ping(pingURL, null, result, timeout);
    }

    /**
     * Ping a URL and store the detailed result in a {@link HttpResult} object.
     * 
     * @param pingURL the URL to ping
     * @param requestProperties optional request properties to add to the connection (can be null)
     * @param result the detailed ping result
     * @return true if the URL can be ping or false otherwise
     */
    public boolean ping(URL pingURL, Map<String, String> requestProperties, HttpResult result)
    {
        return ping(pingURL, requestProperties, result, 0L);
    }

    /**
     * Ping a URL and store the detailed result in a {@link HttpResult} object.
     * 
     * @param pingURL the URL to ping
     * @param requestProperties optional request properties to add to the connection (can be null)
     * @param result the detailed ping result
     * @param timeout the timeout to wait for, 0 if waiting to infinity
     * @return true if the URL can be ping or false otherwise
     */
    public boolean ping(URL pingURL, Map<String, String> requestProperties, HttpResult result,
        long timeout)
    {
        HttpResult responseResult = testConnectivity(pingURL, requestProperties, timeout);
        result.responseBody = responseResult.responseBody;
        result.responseCode = responseResult.responseCode;
        result.responseMessage = responseResult.responseMessage;

        return isAvailable(responseResult);
    }

    /**
     * Tests whether we are able to connect to the HTTP(S) server identified by the specified URL.
     * 
     * @param url the URL to check
     * @param requestProperties optional request properties to add to the connection (can be null)
     * @param timeout the timeout in ms, 0 for infinity
     * @return the HTTP(S) result containing -1 as response code if no connection could be
     * established
     */
    private HttpResult testConnectivity(URL url, Map<String, String> requestProperties,
        long timeout)
    {
        HttpRequest connection = new HttpRequest(url, timeout);
        connection.setLogger(getLogger());

        if (requestProperties != null)
        {
            for (Map.Entry<String, String> requestProperty : requestProperties.entrySet())
            {
                String key = requestProperty.getKey();
                String value = requestProperty.getValue();

                connection.addRequestProperty(key, value);
            }
        }

        HttpResult responseResult = new HttpResult();
        try
        {
            org.codehaus.cargo.container.internal.http.HttpResult httpResult = connection.get();
            responseResult.responseBody = httpResult.getResponseBody();
            responseResult.responseCode = httpResult.getResponseCode();
            responseResult.responseMessage = httpResult.getResponseMessage();
        }
        catch (IOException e)
        {
            responseResult.responseCode = -1;
            responseResult.responseMessage = e.toString();
        }

        return responseResult;
    }

    /**
     * Tests whether an HTTP(S) return code corresponds to a valid connection to the test URL or
     * not. Success is 2xx (successful), 3xx (redirection), 401 (unauthorized) or 403 (forbidden).
     * 
     * @param responseResult the detailed HTTP ping result
     * @return <code>true</code> if the test URL could be called without error, <code>false</code>
     * otherwise
     */
    private boolean isAvailable(HttpResult responseResult)
    {
        boolean result;
        if (responseResult.responseCode >= 200 && responseResult.responseCode < 400
            || responseResult.responseCode == 401 || responseResult.responseCode == 403)
        {
            result = true;
        }
        else
        {
            result = false;
        }
        return result;
    }
}
