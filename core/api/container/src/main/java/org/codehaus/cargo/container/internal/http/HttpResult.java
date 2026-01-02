/*
 * ========================================================================
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
package org.codehaus.cargo.container.internal.http;

/**
 * Set of common HTTP(S) utility methods.
 */
public class HttpResult
{
    /**
     * The HTTP connection response code (eg 200).
     */
    private int responseCode;

    /**
     * The HTTP connection response message (eg "Ok").
     */
    private String responseMessage;

    /**
     * The HTTP connection response body.
     */
    private String responseBody;

    /**
     * @return Response body of HTTP response.
     */
    public String getResponseBody()
    {
        return responseBody;
    }

    /**
     * @param responseBody Response body of HTTP response.
     */
    public void setResponseBody(String responseBody)
    {
        this.responseBody = responseBody;
    }

    /**
     * @return HTTP response code.
     */
    public int getResponseCode()
    {
        return responseCode;
    }

    /**
     * @param responseCode HTTP response code.
     */
    public void setResponseCode(int responseCode)
    {
        this.responseCode = responseCode;
    }

    /**
     * @return HTTP response message.
     */
    public String getResponseMessage()
    {
        return responseMessage;
    }

    /**
     * @param responseMessage HTTP response message.
     */
    public void setResponseMessage(String responseMessage)
    {
        this.responseMessage = responseMessage;
    }

    /**
     * @return True if HTTP call is successful - its response code is 2xx.
     */
    public boolean isSuccessful()
    {
        if (responseCode >= 200 && responseCode < 300)
        {
            return true;
        }
        return false;
    }
}
