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
package org.codehaus.cargo.container.internal.http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.codehaus.cargo.util.log.LoggedObject;

/**
 * HTTP(S) connection class.
 */
public class HttpConnection extends LoggedObject
{
    /**
     * URL to be called.
     */
    private URL url;

    /**
     * Request timeout.
     */
    private long timeout;

    /**
     * Request header map.
     */
    private Map<String, String> requestProperties;

    /**
     * Custom request body writer - for composed request body.
     */
    private HttpRequestBodyWriter requestBodyWriter;

    /**
     * Simple String request body.
     */
    private String requestBody;

    /**
     * Constructor.
     * 
     * @param url URL to be called.
     */
    public HttpConnection(URL url)
    {
        this(url, 0L);
    }

    /**
     * Constructor.
     * 
     * @param url URL to be called.
     * @param timeout Request timeout.
     */
    public HttpConnection(URL url, long timeout)
    {
        this.url = url;
        this.timeout = timeout;
        this.requestProperties = new HashMap<String, String>();
    }

    /**
     * @param propertyName Name of request header property.
     * @param propertyValue Value of request header property.
     */
    public void addRequestProperty(String propertyName, String propertyValue)
    {
        this.requestProperties.put(propertyName, propertyValue);
    }

    /**
     * @param requestBody Set custom request body writer.
     */
    public void setRequestBody(HttpRequestBodyWriter requestBody)
    {
        this.requestBodyWriter = requestBody;
    }

    /**
     * @param requestBody Set simple String request body.
     */
    public void setRequestBody(String requestBody)
    {
        this.requestBody = requestBody;
    }

    /**
     * Set HTTP authentication.
     * 
     * @param username User name.
     * @param password Password.
     */
    public void setAuthentication(final String username, final String password)
    {
        Authenticator.setDefault(new Authenticator()
        {
            @Override
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        });
    }

    /**
     * @return Result of HTTP GET call of this connection.
     */
    public HttpResult get()
    {
        return connect("GET");
    }

    /**
     * @return Result of HTTP POST call of this connection.
     */
    public HttpResult post()
    {
        return connect("POST");
    }

    /**
     * @return Result of HTTP PUT call of this connection.
     */
    public HttpResult put()
    {
        return connect("PUT");
    }

    /**
     * @return Result of HTTP DELETE call of this connection.
     */
    public HttpResult delete()
    {
        return connect("DELETE");
    }

    /**
     * Connect to server and execute defined HTTP method call.
     * 
     * @param httpMethod HTTP method to be called.
     * @return the HTTP(S) result containing -1 as response code if no connection could be
     * established
     */
    private HttpResult connect(String httpMethod)
    {
        HttpResult result = new HttpResult();
        try
        {
            HttpURLConnection connection;
            if (url.getProtocol().equalsIgnoreCase("https"))
            {
                TrustManager[] trustAll = {new PermissiveTrustManager()};
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAll, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                connection = (HttpsURLConnection) url.openConnection();

                HostnameVerifier verifyAll = new PermissiveHostnameVerifier();
                ((HttpsURLConnection) connection).setHostnameVerifier(verifyAll);
            }
            else
            {
                connection = (HttpURLConnection) url.openConnection();
            }

            connection.setRequestMethod(httpMethod);

            String userInfo = url.getUserInfo();
            if (userInfo != null)
            {
                userInfo =
                    Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
                connection.setRequestProperty("Authorization", "Basic " + userInfo);
            }

            connection.setRequestProperty("Connection", "close");
            if (timeout != 0)
            {
                connection.setReadTimeout((int) timeout);
                connection.setConnectTimeout((int) timeout);
            }

            // Add optional request properties specified by the caller
            if (requestProperties != null)
            {
                for (Map.Entry<String, String> requestProperty : requestProperties.entrySet())
                {
                    String key = requestProperty.getKey();
                    String value = requestProperty.getValue();

                    connection.setRequestProperty(key, value);

                    getLogger().debug("Added property [" + key + "] = [" + value + "]",
                        this.getClass().getName());
                }
            }

            // Add request body if specified
            if (requestBody != null)
            {
                connection.setDoOutput(true);
                try (OutputStream outputStream = connection.getOutputStream();
                    BufferedWriter httpRequestBodyWriter =
                        new BufferedWriter(new OutputStreamWriter(outputStream)))
                {
                    httpRequestBodyWriter.write(requestBody);
                }
            }
            else if (requestBodyWriter != null)
            {
                connection.setDoOutput(true);
                try (OutputStream outputStream = connection.getOutputStream())
                {
                    requestBodyWriter.writeToOutputStream(outputStream);
                }
            }

            connection.connect();
            result.setResponseCode(connection.getResponseCode());
            result.setResponseMessage(connection.getResponseMessage());
            result.setResponseBody(readFully(connection));

            connection.disconnect();
        }
        catch (KeyManagementException | IOException | NoSuchAlgorithmException e)
        {
            result.setResponseCode(-1);
            result.setResponseMessage(e.toString());
        }

        getLogger().debug("Called [" + url + "], result = [" + result.getResponseCode() + "]",
            this.getClass().getName());

        return result;
    }

    /**
     * Fully reads the input stream from the passed HTTP URL connection to prevent (harmless)
     * server-side exception.
     * 
     * @param connection the HTTP URL connection to read from
     * @exception IOException if an error happens during the read
     * @return the HTTP connection response body
     */
    private String readFully(HttpURLConnection connection) throws IOException
    {
        String responseBody = "";

        // Only read if there is data to read ... The problem is that not all servers return a
        // content-length header. If there is no header getContentLength() returns -1. It seems to
        // work and it seems that all servers that return no content-length header also do not
        // block on read() operations!
        if (connection.getContentLength() != 0)
        {
            // try getting data from the input stream for successful response,
            // otherwise from the error stream
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300)
            {
                InputStream in = connection.getInputStream();
                if (in != null)
                {
                    responseBody = readStreamData(in);
                }
            }
            else
            {
                InputStream in = connection.getErrorStream();
                if (in != null)
                {
                    responseBody = readStreamData(in);
                }
            }
        }

        return responseBody;
    }

    /**
     * @param stream the stream from which to read data from
     * @return the stream data
     * @throws IOException in case of error
     */
    private String readStreamData(InputStream stream) throws IOException
    {
        StringBuilder body = new StringBuilder();
        byte[] buf = new byte[256];

        // Make sure we read all the data in the stream
        int n;
        while ((n = stream.read(buf)) != -1)
        {
            body.append(new String(buf, 0, n));
        }

        return body.toString();
    }

    /**
     * A TrustManager that does not validate certificate chains.
     */
    private class PermissiveTrustManager implements X509TrustManager
    {
        /**
         * {@inheritDoc}
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
         * String)
         */
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
        {
        }

        /**
         * {@inheritDoc}
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
         * String)
         */
        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
        {
        }
    }

    /**
     * A HostnameVerifier that does not care whether the name on the certificate matches the
     * hostname.
     */
    private class PermissiveHostnameVerifier implements HostnameVerifier
    {
        /**
         * {@inheritDoc}
         * @see HostnameVerifier#verify
         */
        @Override
        public boolean verify(String hostname, SSLSession session)
        {
            return true;
        }
    }
}
