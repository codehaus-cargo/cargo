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
package org.codehaus.cargo.sample.java;

import java.net.URL;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.log.Logger;

/**
 * Utilities to ping URLs. Useful for the tests.
 */
public final class PingUtils
{
    /**
     * Timeout (in milliseconds)
     */
    private static final int TIMEOUT = 20000;

    /**
     * Utility classes should not have a public or default constructor.
     */
    private PingUtils()
    {
        // Empty
    }

    /**
     * Ping a container and check the result.
     * @param message Error message.
     * @param expectedContent Expected content.
     * @param pingURL Ping URL.
     * @param requestProperties Properties for the request.
     * @param expectTrue <code>true</code> if expecting the container to respond with a correct
     * content, <code>false</code> if you expect the container to return an error code.
     * @param errorLogger Logger used to log errors.
     */
    public static void assertPing(String message, String expectedContent, URL pingURL,
        Map<String, String> requestProperties, boolean expectTrue, Logger errorLogger)
    {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.setLogger(errorLogger);
        HttpUtils.HttpResult result = new HttpUtils.HttpResult();

        boolean success = false;
        if (expectTrue)
        {
            success = pingSuccessWithTimeout(httpUtils, pingURL, requestProperties, result);
        }
        else
        {
            success = httpUtils.ping(pingURL, requestProperties, result, PingUtils.TIMEOUT);
        }

        StringBuilder text = new StringBuilder(message);
        text.append(". Failed to ping [" + pingURL.toString() + "], ");
        if (requestProperties != null && !requestProperties.isEmpty())
        {
            text.append("requestProperties = " + requestProperties + ", ");
        }
        if (result.responseCode == -1)
        {
            text.append("Cannot connect to the URL");
        }
        else
        {
            text.append("Reason = [" + result.responseMessage + "], Body = ["
                + result.responseBody + "], Code = [" + result.responseCode + "]");
        }

        if (expectTrue)
        {
            Assertions.assertTrue(success, text.toString());
        }
        else
        {
            Assertions.assertFalse(success, text.toString());
        }

        if (expectedContent != null)
        {
            String content = result.responseBody;
            Assertions.assertNotNull(content, "result.responseBody is null");
            Assertions.assertTrue(content.contains(expectedContent),
                content + " does not contain " + expectedContent);
        }
    }

    /**
     * Ping a container and check the result.
     * 
     * @param httpUtils HTTP utils.
     * @param pingUrl Ping URL.
     * @param requestProperties Properties for the request.
     * @param result Result of last request.
     * @return <code>true</code> if container responded with a correct content
     */
    private static boolean pingSuccessWithTimeout(HttpUtils httpUtils, URL pingUrl,
        Map<String, String> requestProperties, HttpUtils.HttpResult result)
    {
        boolean success = false;

        // Some containers have delay between their startup and deploying of deployables.
        // This construct will apply timeout for call response for cases when deployable
        // isn't deployed immediately.
        long timeout = System.currentTimeMillis() + PingUtils.TIMEOUT;
        do
        {
            success = httpUtils.ping(pingUrl, requestProperties, result, PingUtils.TIMEOUT);
            if (success)
            {
                break;
            }

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                throw new CargoException("Cannot ping container", e);
            }
        }
        while (System.currentTimeMillis() < timeout);

        return success;
    }

    /**
     * Ping a container and expect it to return a given content.
     * @param message Error message.
     * @param expectedContent Expected content.
     * @param pingURL Ping URL.
     * @param requestProperties Properties for the request.
     * @param errorLogger Logger used to log errors.
     */
    public static void assertPingTrue(String message, String expectedContent, URL pingURL,
        Map<String, String> requestProperties, Logger errorLogger)
    {
        assertPing(message, expectedContent, pingURL, requestProperties, true, errorLogger);
    }

    /**
     * Ping a container and expect it to return a given content.
     * @param message Error message.
     * @param expectedContent Expected content.
     * @param pingURL Ping URL.
     * @param errorLogger Logger used to log errors.
     */
    public static void assertPingTrue(String message, String expectedContent, URL pingURL,
        Logger errorLogger)
    {
        assertPing(message, expectedContent, pingURL, null, true, errorLogger);
    }

    /**
     * Ping a container and expect it to return an error.
     * @param message Error message.
     * @param expectedContent Expected content.
     * @param pingURL Ping URL.
     * @param errorLogger Logger used to log errors.
     */
    public static void assertPingFalse(String message, String expectedContent, URL pingURL,
        Logger errorLogger)
    {
        assertPing(message, expectedContent, pingURL, null, false, errorLogger);
    }

    /**
     * Ping a container and expect it to return any content.
     * @param message Error message.
     * @param pingURL Ping URL.
     * @param errorLogger Logger used to log errors.
     */
    public static void assertPingTrue(String message, URL pingURL, Logger errorLogger)
    {
        assertPingTrue(message, null, pingURL, errorLogger);
    }

    /**
     * Ping a container and expect it to return an error.
     * @param message Error message.
     * @param pingURL Ping URL.
     * @param errorLogger Logger used to log errors.
     */
    public static void assertPingFalse(String message, URL pingURL, Logger errorLogger)
    {
        assertPingFalse(message, null, pingURL, errorLogger);
    }
}
