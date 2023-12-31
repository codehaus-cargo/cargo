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
package org.codehaus.cargo.container.internal.http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.codehaus.cargo.util.log.LoggedObject;

/**
 * HTTP(S) request class.
 */
public class HttpRequest extends LoggedObject
{
    /**
     * Size of the buffers / chunks used when sending files to the HTTP server.<br>
     * <br>
     * When trying to upload large amount of data the internal connection buffer can become too
     * large and exceed the heap size, leading to an {@link OutOfMemoryError}. This was fixed in
     * JDK 1.5 by introducing {@link HttpURLConnection#setChunkedStreamingMode(int)}.<br>
     * <br>
     * As per <a href="https://codehaus-cargo.atlassian.net/browse/CARGO-1418">CARGO-1418</a>, use
     * a sensible chunk size for fast links.
     */
    protected static final int BUFFER_CHUNK_SIZE = 256 * 1024;

    /**
     * cache of nonce values seen
     */
    private static final NonceCounter NONCE_COUNTER = new NonceCounter();

    /**
     * URL to be called.
     */
    private URL url;

    /**
     * Request timeout.
     */
    private long timeout;

    /**
     * Username to use.
     */
    private String username;

    /**
     * Password to use.
     */
    private String password;

    /**
     * Request header map.
     */
    private Map<String, String> requestProperties;

    /**
     * String request body.
     */
    private String requestBody;

    /**
     * Constructor.
     * 
     * @param url URL to be called.
     */
    public HttpRequest(URL url)
    {
        this(url, 0);
    }

    /**
     * Constructor.
     * 
     * @param url URL to be called.
     * @param timeout Request timeout.
     */
    public HttpRequest(URL url, long timeout)
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
     * @param requestBody String request body to set.
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
        this.username = username;
        this.password = password;
    }

    /**
     * @return Result of HTTP GET call of this connection.
     * @throws IOException If connecting to the server fails.
     */
    public HttpResult get() throws IOException
    {
        return connect("GET", null);
    }

    /**
     * @return Result of HTTP POST call of this connection.
     * @throws IOException If connecting to the server fails.
     */
    public HttpResult post() throws IOException
    {
        return connect("POST", null);
    }

    /**
     * @return Result of HTTP PUT call of this connection.
     * @throws IOException If connecting to the server fails.
     */
    public HttpResult put() throws IOException
    {
        return connect("PUT", null);
    }

    /**
     * @return Result of HTTP DELETE call of this connection.
     * @throws IOException If connecting to the server fails.
     */
    public HttpResult delete() throws IOException
    {
        return connect("DELETE", null);
    }

    /**
     * Connect to server and execute defined HTTP method call.
     * 
     * @param httpMethod HTTP method to be called.
     * @param digestData HTTP Digest authentication data, if available
     * @return the HTTP(S) result containing -1 as response code if no connection could be
     * established
     * @throws IOException If connecting to the server fails.
     */
    private HttpResult connect(String httpMethod, String digestData) throws IOException
    {
        getLogger().debug(
            "Calling [" + url + "] with timeout " + this.timeout, this.getClass().getName());

        HttpURLConnection connection = null;
        try
        {
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

            connection.setAllowUserInteraction(false);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            if (timeout != 0)
            {
                connection.setReadTimeout((int) timeout);
                connection.setConnectTimeout((int) timeout);
            }
            // Do not forcibly close the connection, rather have it kept alive for efficient HTTP
            // resource pooling in case of multiple requests to the same server
            // connection.setRequestProperty("Connection", "close");

            if (digestData != null)
            {
                connection.setRequestProperty("Authorization", digestData);
                getLogger().debug("Set Digest authentication", this.getClass().getName());
            }
            else
            {
                String userInfo = url.getUserInfo();
                if (userInfo != null)
                {
                    userInfo = Base64.getEncoder().encodeToString(
                        userInfo.getBytes(StandardCharsets.UTF_8));
                    connection.setRequestProperty("Authorization", "Basic " + userInfo);

                    getLogger().debug("Set Basic authentication based on URL user information",
                        this.getClass().getName());
                }
                else if (this.username != null && !this.username.isEmpty())
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(username).append(':');
                    if (password != null)
                    {
                        sb.append(password);
                    }
                    userInfo = Base64.getEncoder().encodeToString(
                        sb.toString().getBytes(StandardCharsets.UTF_8));
                    connection.setRequestProperty("Authorization", "Basic " + userInfo);

                    getLogger().debug("Set Basic authentication based on username/password",
                        this.getClass().getName());
                }
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

            getLogger().debug("Sending request and writing to output stream if necessary",
                this.getClass().getName());
            try
            {
                writeOutputStream(connection);
            }
            catch (IOException e)
            {
                // CARGO-1569: When there is a need for Digest authentication, certain versions of
                // WildFly aggressively close the unauthenticated stream before the
                // writeOutputStream method finishes, which in turn causes an IOException
                if (connection.getResponseCode() != 401)
                {
                    throw e;
                }
                String wwwAuthenticate = connection.getHeaderField("WWW-Authenticate");
                if (wwwAuthenticate == null || !wwwAuthenticate.startsWith("Digest "))
                {
                    throw e;
                }
            }

            int responseCode = connection.getResponseCode();
            getLogger().debug("Got response code [" + responseCode + "]",
                this.getClass().getName());

            if (responseCode == 401)
            {
                String wwwAuthenticate = connection.getHeaderField("WWW-Authenticate");
                if (digestData == null && wwwAuthenticate != null
                    && wwwAuthenticate.startsWith("Digest "))
                {
                    getLogger().debug("Server requests Digest authentication",
                        getClass().getName());

                    String realm = extractHeaderComponent(wwwAuthenticate, "realm");
                    String qop = extractHeaderComponent(wwwAuthenticate, "qop");
                    String nonce = extractHeaderComponent(wwwAuthenticate, "nonce");
                    String opaque = extractHeaderComponent(wwwAuthenticate, "opaque");
                    String algorithm = extractHeaderComponent(wwwAuthenticate, "algorithm");

                    if (realm == null)
                    {
                        HttpResult result = new HttpResult();
                        result.setResponseCode(-1);
                        result.setResponseMessage(
                            "The server requested a Digest authentication but the realm is not "
                                + "provided");
                        return result;
                    }
                    else if (nonce == null)
                    {
                        HttpResult result = new HttpResult();
                        result.setResponseCode(-1);
                        result.setResponseMessage(
                            "The server requested a Digest authentication but the nonce is not "
                                + "provided");
                        return result;
                    }
                    if (qop != null && !"auth".equals(qop))
                    {
                        HttpResult result = new HttpResult();
                        result.setResponseCode(-1);
                        result.setResponseMessage(
                            "The server requested a Digest authentication but the qop is set to ["
                                + qop + "] instead of [auth]");
                        return result;
                    }
                    if (algorithm == null)
                    {
                        algorithm = "MD5";
                    }
                    MessageDigest digest = MessageDigest.getInstance(algorithm);

                    String ha1 = this.username + ":" + realm + ":" + this.password;
                    byte[] hash = digest.digest(ha1.getBytes(StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    for (byte hashByte : hash)
                    {
                        sb.append(String.format("%02x", hashByte));
                    }
                    ha1 = sb.toString();

                    String uri;
                    String uriPath = url.getPath();
                    String uriQuery = url.getQuery();
                    if (uriQuery != null)
                    {
                        uri = uriPath + "?" + uriQuery;
                    }
                    else
                    {
                        uri = uriPath;
                    }

                    String ha2 = httpMethod;
                    ha2 += ":" + uri;
                    hash = digest.digest(ha2.getBytes(StandardCharsets.UTF_8));
                    sb = new StringBuilder();
                    for (byte hashByte : hash)
                    {
                        sb.append(String.format("%02x", hashByte));
                    }
                    ha2 = sb.toString();

                    String nc = NONCE_COUNTER.count(nonce);

                    String cnonce =
                        String.format("%08x", (long) (Math.random() * 4294967295.0));
                    cnonce = cnonce.substring(cnonce.length() - 8);

                    String ha3;
                    if (qop != null)
                    {
                        ha3 = ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2;
                    }
                    else
                    {
                        ha3 = ha1 + ":" + nonce + ":" + ha2;
                    }
                    hash = digest.digest(ha3.getBytes(StandardCharsets.UTF_8));
                    sb = new StringBuilder();
                    for (byte hashByte : hash)
                    {
                        sb.append(String.format("%02x", hashByte));
                    }
                    ha3 = sb.toString();

                    wwwAuthenticate = "Digest username=\"" + this.username + "\", "
                        + "realm=\"" + realm + "\", "
                        + "nonce=\"" + nonce + "\", "
                        + "uri=\"" + uri + "\", "
                        + "algorithm=" + algorithm + ", "
                        + "nc=" + nc + ", "
                        + "cnonce=\"" + cnonce + "\", "
                        + "response=\"" + ha3 + "\"";
                    if (qop != null)
                    {
                        wwwAuthenticate += ", qop=\"" + qop + "\"";
                    }
                    if (opaque != null)
                    {
                        wwwAuthenticate += ", opaque=\"" + opaque + "\"";
                    }

                    getLogger().debug("Digest authentication with ha=" + ha1 + ", ha2=" + ha2,
                        getClass().getName());

                    return connect(httpMethod, wwwAuthenticate);
                }
                else
                {
                    getLogger().debug("Server requests [" + wwwAuthenticate + "] authentication",
                        getClass().getName());
                }
            }

            HttpResult result = new HttpResult();
            result.setResponseCode(responseCode);
            result.setResponseMessage(connection.getResponseMessage());
            result.setResponseBody(readFully(connection));
            return result;
        }
        catch (KeyManagementException | NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }
        finally
        {
            try
            {
                if (connection != null)
                {
                    connection.disconnect();
                }
            }
            catch (Throwable ignored)
            {
                // Ignored
            }
        }
    }

    /**
     * If necessary, writes to the output stream of the HTTP URL connection. By default, the
     * {@link #requestBody} is written as output.
     * 
     * @param connection the HTTP URL connection to read from
     * @exception IOException if an error happens during the connection establishment or write
     */
    protected void writeOutputStream(HttpURLConnection connection) throws IOException
    {
        if (this.requestBody != null)
        {
            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream();
                BufferedWriter httpRequestBodyWriter =
                    new BufferedWriter(new OutputStreamWriter(outputStream)))
            {
                httpRequestBodyWriter.write(this.requestBody);
            }
        }
        else
        {
            connection.connect();
        }
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
            Charset charset = extractCharset(connection.getContentType());

            // try getting data from the input stream for successful response,
            // otherwise from the error stream
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300)
            {
                InputStream in = connection.getInputStream();
                if (in != null)
                {
                    responseBody = readStreamData(in, charset);
                }
            }
            else
            {
                InputStream in = connection.getErrorStream();
                if (in != null)
                {
                    responseBody = readStreamData(in, charset);
                }
            }
        }

        return responseBody;
    }

    /**
     * Extract charset from <code>Content-Type</code> header.
     * 
     * @param contentType <code>Content-Type</code> header.
     * @return Character set extracted, UTF-8 if no valid charset found.
     */
    protected static Charset extractCharset(String contentType)
    {
        Charset charset = StandardCharsets.UTF_8;
        if (contentType != null)
        {
            int charsetStart = contentType.indexOf("; charset=");
            if (charsetStart > 0)
            {
                try
                {
                    charset = Charset.forName(contentType.substring(charsetStart + 10));
                }
                catch (Exception ignored)
                {
                    // Ignore parsing charset
                }
            }
        }
        return charset;
    }

    /**
     * @param stream the stream from which to read data from
     * @param charset the charset to use when constructing the string
     * @return the stream data
     * @throws IOException in case of error
     */
    private String readStreamData(InputStream stream, Charset charset) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(stream, charset);

        StringBuilder sb = new StringBuilder();
        char[] chars = new char[1024];
        int n;
        while ((n = reader.read(chars, 0, chars.length)) != -1)
        {
            sb.append(chars, 0, n);
        }

        // See: https://codehaus-cargo.atlassian.net/browse/CARGO-1342
        String response = sb.toString().replaceAll("\\r\\n?", "\n");
        if (response.startsWith("HTTP/"))
        {
            int httpHeaderBodySeparation = response.indexOf("\n\n");
            if (httpHeaderBodySeparation != -1)
            {
                String splitResponse = response.substring(httpHeaderBodySeparation + 2);
                httpHeaderBodySeparation = splitResponse.indexOf('\n');
                if (httpHeaderBodySeparation != -1)
                {
                    response = splitResponse.substring(httpHeaderBodySeparation + 1);
                }
            }
        }

        return response;
    }

    /**
     * Extract a component of a header.
     * 
     * @param header header to extract from
     * @param component component to extract
     * @return Extracted component, null if component doesn't exist in header
     */
    protected static String extractHeaderComponent(String header, String component)
    {
        String fullComponent = component + "=\"";
        int fullComponentLength = fullComponent.length();
        int index1 = header.indexOf(fullComponent);
        if (index1 == -1)
        {
            return null;
        }
        int index2 = header.indexOf('"', index1 + fullComponentLength);
        if (index2 == -1)
        {
            return null;
        }
        return header.substring(index1 + fullComponentLength, index2);
    }

    /**
     * NonceCounter provides a 1,000 item LRU cache counting
     * the number of times a nonce has been seen.
     */
    private static class NonceCounter
    {
        /**
         * LRU cache size limit.
         */
        private final int maxLruCacheSizeLimit = 1000;

        /**
         * Map holds the nonce values and their counts
         */
        private Map<String, Integer> nonces;

        /**
         * Nonce counter.
         */
        public NonceCounter()
        {
            nonces = new LinkedHashMap<String, Integer>(maxLruCacheSizeLimit + 1, .75F, true)
            {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public boolean removeEldestEntry(Map.Entry<String, Integer> eldest)
                {
                    return size() > maxLruCacheSizeLimit;
                }
            };
        }

        /**
         * Count returns a hexadecimal string counting the number
         * of times nonce has been seen.  The first value returned
         * for a nonce is 00000001.
         * 
         * @param nonce the nonce value to count
         * @return formatted nonce value
         */
        public synchronized String count(String nonce)
        {
            Integer count = nonces.get(nonce);
            if (count == null)
            {
                count = 1;
            }
            else
            {
                count = count + 1;
            }

            nonces.put(nonce, count);

            return String.format("%08x", count);
        }
    }

    /**
     * A TrustManager that does not validate certificate chains.
     */
    private static class PermissiveTrustManager implements X509TrustManager
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
            // Method purposefully left empty
        }

        /**
         * {@inheritDoc}
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
         * String)
         */
        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
        {
            // Method purposefully left empty
        }
    }

    /**
     * A HostnameVerifier that does not care whether the name on the certificate matches the
     * hostname.
     */
    private static class PermissiveHostnameVerifier implements HostnameVerifier
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
