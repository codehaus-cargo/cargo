/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import junit.framework.Assert;

import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.util.log.Logger;

/**
 * Utilities to ping URLs. Useful for the tests.
 * 
 * @version $Id$
 */
public class PingUtils extends Assert
{
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
        boolean success = httpUtils.ping(pingURL, requestProperties, result);

        String text = message + ". Failed to ping [" + pingURL.toString() + "], ";
        if (result.responseCode == -1)
        {
            text = text + "Cannot connect to the URL";
        }
        else
        {
            text = text + "Reason = [" + result.responseMessage + "], Body = ["
                + result.responseBody + "], Code = [" + result.responseCode + "]";
        }

        if (expectTrue)
        {
            assertTrue(text, success);
        }
        else
        {
            assertFalse(text, success);
        }

        if (expectedContent != null)
        {
            String content = result.responseBody;
            assertNotNull("result.responseBody is null", content);
            assertTrue(content + " does not contain " + expectedContent,
                content.contains(expectedContent));
        }
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
