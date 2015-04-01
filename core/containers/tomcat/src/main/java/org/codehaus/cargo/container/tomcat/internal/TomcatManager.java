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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.StringTokenizer;

import org.codehaus.cargo.util.Base64;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * A Tomcat manager webapp invocation wrapper.
 * 
 */
public class TomcatManager extends LoggedObject
{
    /**
     * The charset to use when decoding Tomcat manager responses.
     */
    private static final String MANAGER_CHARSET = "UTF-8";

    /**
     * The full URL of the Tomcat manager instance to use.
     */
    private URL url;

    /**
     * The username to use when authenticating with Tomcat manager.
     */
    private String username;

    /**
     * The password to use when authenticating with Tomcat manager.
     */
    private String password;

    /**
     * The URL encoding charset to use when communicating with Tomcat manager.
     */
    private String charset;

    /**
     * The user agent name to use when communicating with Tomcat manager.
     */
    private String userAgent;

    /**
     * MD5 message digest.
     */
    private MessageDigest md5;

    /**
     * Creates a Tomcat manager wrapper for the specified URL that uses a username of
     * <code>admin</code>, an empty password and ISO-8859-1 URL encoding.
     * 
     * @param url the full URL of the Tomcat manager instance to use
     */
    public TomcatManager(URL url)
    {
        this(url, "admin");
    }

    /**
     * Creates a Tomcat manager wrapper for the specified URL and username that uses an empty
     * password and ISO-8859-1 URL encoding.
     * 
     * @param url the full URL of the Tomcat manager instance to use
     * @param username the username to use when authenticating with Tomcat manager
     */
    public TomcatManager(URL url, String username)
    {
        this(url, username, "");
    }

    /**
     * Creates a Tomcat manager wrapper for the specified URL, username and password that uses
     * ISO-8859-1 URL encoding.
     * 
     * @param url the full URL of the Tomcat manager instance to use
     * @param username the username to use when authenticating with Tomcat manager
     * @param password the password to use when authenticating with Tomcat manager
     */
    public TomcatManager(URL url, String username, String password)
    {
        this(url, username, password, "ISO-8859-1");
    }

    /**
     * Creates a Tomcat manager wrapper for the specified URL, username, password and URL encoding.
     * 
     * @param url the full URL of the Tomcat manager instance to use
     * @param username the username to use when authenticating with Tomcat manager
     * @param password the password to use when authenticating with Tomcat manager
     * @param charset the URL encoding charset to use when communicating with Tomcat manager
     */
    public TomcatManager(URL url, String username, String password, String charset)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        this.charset = charset;

        try
        {
            md5 = MessageDigest.getInstance("md5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CargoException(
                "Cannot get the MD5 digest for generating the Digest authentication headers", e);
        }
    }

    /**
     * Gets the full URL of the Tomcat manager instance.
     * 
     * @return the full URL of the Tomcat manager instance
     */
    public URL getURL()
    {
        return this.url;
    }

    /**
     * Gets the username to use when authenticating with Tomcat manager.
     * 
     * @return the username to use when authenticating with Tomcat manager
     */
    public String getUserName()
    {
        return this.username;
    }

    /**
     * Gets the password to use when authenticating with Tomcat manager.
     * 
     * @return the password to use when authenticating with Tomcat manager
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * Gets the URL encoding charset to use when communicating with Tomcat manager.
     * 
     * @return the URL encoding charset to use when communicating with Tomcat manager
     */
    public String getCharset()
    {
        return this.charset;
    }

    /**
     * Gets the user agent name to use when communicating with Tomcat manager.
     * 
     * @return the user agent name to use when communicating with Tomcat manager
     */
    public String getUserAgent()
    {
        return this.userAgent;
    }

    /**
     * Sets the user agent name to use when communicating with Tomcat manager.
     * 
     * @param userAgent the user agent name to use when communicating with Tomcat manager
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * Deploys the specified WAR as a URL to the specified context path.
     * 
     * @param path the webapp context path to deploy to
     * @param war the URL of the WAR to deploy
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deploy(String path, URL war) throws TomcatManagerException, IOException
    {
        deploy(path, war, false);
    }

    /**
     * Deploys the specified WAR as a URL to the specified context path, optionally undeploying the
     * webapp if it already exists.
     * 
     * @param path the webapp context path to deploy to
     * @param war the URL of the WAR to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deploy(String path, URL war, boolean update) throws TomcatManagerException,
        IOException
    {
        deploy(path, war, update, null);
    }

    /**
     * Deploys the specified WAR as a URL to the specified context path, optionally undeploying the
     * webapp if it already exists and using the specified tag name.
     * 
     * @param path the webapp context path to deploy to
     * @param war the URL of the WAR to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @param tag the tag name to use
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deploy(String path, URL war, boolean update, String tag)
        throws TomcatManagerException, IOException
    {
        deployImpl(path, null, null, war, null, update, tag);
    }

    /**
     * Deploys the specified WAR as a HTTP PUT to the specified context path.
     * 
     * @param path the webapp context path to deploy to
     * @param war an input stream to the WAR to deploy
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deploy(String path, InputStream war) throws TomcatManagerException, IOException
    {
        deploy(path, war, false);
    }

    /**
     * Deploys the specified WAR as a HTTP PUT to the specified context path, optionally undeploying
     * the webapp if it already exists.
     * 
     * @param path the webapp context path to deploy to
     * @param war an input stream to the WAR to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deploy(String path, InputStream war, boolean update)
        throws TomcatManagerException, IOException
    {
        deploy(path, war, update, null);
    }

    /**
     * Deploys the specified WAR as a HTTP PUT to the specified context path, optionally undeploying
     * the webapp if it already exists and using the specified tag name.
     * 
     * @param path the webapp context path to deploy to
     * @param war an input stream to the WAR to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @param tag the tag name to use
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deploy(String path, InputStream war, boolean update, String tag)
        throws TomcatManagerException, IOException
    {
        deployImpl(path, null, null, null, war, update, tag);
    }

    /**
     * Deploys the specified WAR as a HTTP PUT to the specified context path, optionally undeploying
     * the webapp if it already exists and using the specified tag name.
     * 
     * @param path the webapp context path to deploy to
     * @param version the webapp version
     * @param war an input stream to the WAR to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @param tag the tag name to use
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deploy(String path, String version, InputStream war, boolean update, String tag)
        throws TomcatManagerException, IOException
    {
        deployImpl(path, version, null, null, war, update, tag);
    }

    /**
     * Deploys the specified context XML configuration to the specified context path.
     * 
     * @param path the webapp context path to deploy to
     * @param config the URL of the context XML configuration to deploy
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deployContext(String path, URL config) throws TomcatManagerException, IOException
    {
        deployContext(path, config, false);
    }

    /**
     * Deploys the specified context XML configuration to the specified context path, optionally
     * undeploying the webapp if it already exists.
     * 
     * @param path the webapp context path to deploy to
     * @param config the URL of the context XML configuration to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deployContext(String path, URL config, boolean update)
        throws TomcatManagerException, IOException
    {
        deployContext(path, config, update, null);
    }

    /**
     * Deploys the specified context XML configuration to the specified context path, optionally
     * undeploying the webapp if it already exists and using the specified tag name.
     * 
     * @param path the webapp context path to deploy to
     * @param config the URL of the context XML configuration to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @param tag the tag name to use
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deployContext(String path, URL config, boolean update, String tag)
        throws TomcatManagerException, IOException
    {
        deployContext(path, config, null, update, tag);
    }

    /**
     * Deploys the specified context XML configuration and WAR as a URL to the specified context
     * path.
     * 
     * @param path the webapp context path to deploy to
     * @param config the URL of the context XML configuration to deploy
     * @param war the URL of the WAR to deploy
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deployContext(String path, URL config, URL war) throws TomcatManagerException,
        IOException
    {
        deployContext(path, config, war, false);
    }

    /**
     * Deploys the specified context XML configuration and WAR as a URL to the specified context
     * path, optionally undeploying the webapp if it already exists.
     * 
     * @param path the webapp context path to deploy to
     * @param config the URL of the context XML configuration to deploy
     * @param war the URL of the WAR to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deployContext(String path, URL config, URL war, boolean update)
        throws TomcatManagerException, IOException
    {
        deployContext(path, config, war, update, null);
    }

    /**
     * Deploys the specified context XML configuration and WAR as a URL to the specified context
     * path, optionally undeploying the webapp if it already exists and using the specified tag
     * name.
     * 
     * @param path the webapp context path to deploy to
     * @param config the URL of the context XML configuration to deploy
     * @param war the URL of the WAR to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @param tag the tag name to use
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void deployContext(String path, URL config, URL war, boolean update, String tag)
        throws TomcatManagerException, IOException
    {
        deployImpl(path, null, config, war, null, update, tag);
    }

    /**
     * Undeploys the webapp at the specified context path.
     * 
     * @param path the webapp context path to undeploy
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void undeploy(String path) throws TomcatManagerException, IOException
    {
        undeploy(path, null);
    }

    /**
     * Undeploys the webapp at the specified context path.
     * 
     * @param path the webapp context path to undeploy
     * @param version the version of the webapp context path to undeploy
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void undeploy(String path, String version) throws TomcatManagerException, IOException
    {
        StringBuilder buffer = new StringBuilder("/undeploy");
        buffer.append("?path=").append(URLEncoder.encode(path, this.charset));
        if (version != null)
        {
            buffer.append("&version=").append(URLEncoder.encode(version, this.charset));
        }
        invoke(buffer.toString());
    }

    /**
     * Removes the webapp at the specified context path.
     * 
     * @param path the webapp context path to remove
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void remove(String path) throws TomcatManagerException, IOException
    {
        invoke("/remove?path=" + URLEncoder.encode(path, this.charset));
    }

    /**
     * Reloads the webapp at the specified context path.
     * 
     * @param path the webapp context path to reload
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void reload(String path) throws TomcatManagerException, IOException
    {
        invoke("/reload?path=" + URLEncoder.encode(path, this.charset));
    }

    /**
     * Starts the webapp at the specified context path.
     * 
     * @param path the webapp context path to start
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void start(String path) throws TomcatManagerException, IOException
    {
        invoke("/start?path=" + URLEncoder.encode(path, this.charset));
    }

    /**
     * Stops the webapp at the specified context path.
     * 
     * @param path the webapp context path to stop
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void stop(String path) throws TomcatManagerException, IOException
    {
        invoke("/stop?path=" + URLEncoder.encode(path, this.charset));
    }

    /**
     * Invokes Tomcat manager with the specified command.
     * 
     * @param path the Tomcat manager command to invoke
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    protected void invoke(String path) throws TomcatManagerException, IOException
    {
        invoke(path, null, null);
    }

    /**
     * Invokes Tomcat manager with the specified command and content data.
     * 
     * @param path the Tomcat manager command to invoke
     * @param data an input stream to the content data
     * @param digestData HTTP Digest authentication data, if available
     * @return the result of the invoking command, as returned by the Tomcat Manager application
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    protected String invoke(String path, InputStream data, String digestData) throws
        TomcatManagerException, IOException
    {
        // TODO: This method should be refactored so that it can be unit testable.

        getLogger().debug("Invoking Tomcat manager using path [" + path + "]",
            getClass().getName());

        URL invokeURL = new URL(this.url + path);
        HttpURLConnection connection = (HttpURLConnection) invokeURL.openConnection();
        connection.setAllowUserInteraction(false);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        if (data == null)
        {
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
        }
        else
        {
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            // When trying to upload large amount of data the internal connection buffer can become
            // too large and exceed the heap size, leading to a java.lang.OutOfMemoryError.
            // This was fixed in JDK 1.5 by introducing a new setChunkedStreamingMode() method.
            connection.setChunkedStreamingMode(0);
        }

        if (this.userAgent != null)
        {
            connection.setRequestProperty("User-Agent", this.userAgent);
        }

        if (digestData != null)
        {
            connection.setRequestProperty("Authorization", digestData);
        }
        else if (this.username != null)
        {
            String authorization = toAuthorization(this.username, this.password);
            connection.setRequestProperty("Authorization", authorization);
        }

        connection.connect();

        if (data != null)
        {
            pipe(data, connection.getOutputStream());
        }

        String response;
        try
        {
            response = toString(connection.getInputStream(), MANAGER_CHARSET);
        }
        catch (IOException e)
        {
            if (connection.getResponseCode() == 401)
            {
                String wwwAuthenticate = connection.getHeaderField("WWW-Authenticate");
                if (digestData == null && wwwAuthenticate != null
                    && wwwAuthenticate.startsWith("Digest "))
                {
                    getLogger().debug(
                        "Response code is 401 and server requests Digest authentication",
                        getClass().getName());

                    String realm = extractHeaderComponent(wwwAuthenticate, "realm");
                    String qop = extractHeaderComponent(wwwAuthenticate, "qop");
                    String nonce = extractHeaderComponent(wwwAuthenticate, "nonce");
                    String opaque = extractHeaderComponent(wwwAuthenticate, "opaque");
                    String algorithm = extractHeaderComponent(wwwAuthenticate, "algorithm");

                    if (realm == null || nonce == null)
                    {
                        throw new TomcatManagerException(
                            "The username and password you provided are not correct (error 401), "
                                + "the server requested a Digest authentication but "
                                    + "realm or nonce are not provided", e);
                    }
                    if (qop != null && !"auth".equals(qop))
                    {
                        throw new TomcatManagerException(
                            "The username and password you provided are not correct (error 401), "
                                + "the server requested a Digest authentication but qop is set to "
                                    + qop, e);
                    }
                    if (algorithm != null && !"MD5".equals(algorithm))
                    {
                        throw new TomcatManagerException(
                            "The username and password you provided are not correct (error 401), "
                                + "the server requested a Digest authentication but "
                                    + "algorithm is set to " + algorithm, e);
                    }

                    String ha1 = this.username + ":" + realm + ":" + this.password;
                    byte[] hash = md5.digest(ha1.getBytes("UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    for (byte hashByte : hash)
                    {
                        sb.append(String.format("%02x", hashByte));
                    }
                    ha1 = sb.toString();

                    String uri = invokeURL.getPath();
                    String ha2;
                    if (data == null)
                    {
                        ha2 = "GET";
                    }
                    else
                    {
                        ha2 = "PUT";
                    }
                    ha2 += ":" + uri;
                    hash = md5.digest(ha2.getBytes("UTF-8"));
                    sb = new StringBuilder();
                    for (byte hashByte : hash)
                    {
                        sb.append(String.format("%02x", hashByte));
                    }
                    ha2 = sb.toString();

                    String nc = String.format("%08x", System.currentTimeMillis());
                    nc = nc.substring(nc.length() - 8);
                    String cnonce = String.format("%08x", (long) (Math.random() * 4294967295.0));
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
                    hash = md5.digest(ha3.getBytes("UTF-8"));
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
                        + "algorithm=MD5, "
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

                    getLogger().debug("Digest authentication with ha=" + ha1 + ", ha2=" + ha2
                        + " and full header " + wwwAuthenticate, getClass().getName());

                    return invoke(path, data, wwwAuthenticate);
                }
                else
                {
                    throw new TomcatManagerException(
                        "The username and password you provided are not correct (error 401)", e);
                }
            }
            else if (connection.getResponseCode() == 403)
            {
                throw new TomcatManagerException("The username you provided is not allowed to "
                    + "use the text-based Tomcat Manager (error 403)", e);
            }
            else
            {
                throw e;
            }
        }

        if (!response.startsWith("OK -"))
        {
            throw new TomcatManagerException(response);
        }

        return response;
    }

    /**
     * Deploys the specified WAR.
     * 
     * @param path the webapp context path to deploy to
     * @param version the webapp version
     * @param config the URL of the context XML configuration to deploy, or null for none
     * @param war the URL of the WAR to deploy, or null to use <code>data</code>
     * @param data an input stream to the WAR to deploy, or null to use <code>war</code>
     * @param update whether to first undeploy the webapp if it already exists
     * @param tag the tag name to use
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    private void deployImpl(String path, String version, URL config, URL war, InputStream data,
        boolean update, String tag) throws TomcatManagerException, IOException
    {
        StringBuilder buffer = new StringBuilder("/deploy");
        buffer.append("?path=").append(URLEncoder.encode(path, this.charset));
        if (version != null)
        {
            buffer.append("&version=").append(URLEncoder.encode(version, this.charset));
        }
        if (config != null)
        {
            buffer.append("&config=").append(URLEncoder.encode(config.toString(), this.charset));
        }
        if (war != null)
        {
            buffer.append("&war=").append(URLEncoder.encode(war.toString(), this.charset));
        }
        if (update)
        {
            buffer.append("&update=true");
        }
        if (tag != null)
        {
            buffer.append("&tag=").append(URLEncoder.encode(tag, this.charset));
        }

        invoke(buffer.toString(), data, null);
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
        int index2 = header.indexOf("\"", index1 + fullComponentLength);
        if (index2 == -1)
        {
            return null;
        }
        return header.substring(index1 + fullComponentLength, index2);
    }

    /**
     * Gets the HTTP Basic Authorization header value for the supplied username and password.
     * 
     * @param username the username to use for authentication
     * @param password the password to use for authentication
     * @return the HTTP Basic Authorization header value
     */
    private static String toAuthorization(String username, String password)
    {
        StringBuilder buffer = new StringBuilder();
        buffer.append(username).append(':');
        if (password != null)
        {
            buffer.append(password);
        }
        return "Basic " + new String(Base64.encodeBase64(buffer.toString().getBytes()));
    }

    /**
     * Reads all the data from the specified input stream and writes it to the specified output
     * stream. Both streams are also closed.
     * 
     * @param in the input stream to read from
     * @param out the output stream to write to
     * @throws IOException if an i/o error occurs
     */
    private void pipe(InputStream in, OutputStream out) throws IOException
    {
        BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
        int n;
        byte[] bytes = new byte[1024 * 4];
        while ((n = in.read(bytes)) != -1)
        {
            bufferedOut.write(bytes, 0, n);
        }
        bufferedOut.flush();
        bufferedOut.close();
        in.close();
    }

    /**
     * Gets the data from the specified input stream as a string using the specified charset.
     * 
     * @param in the input stream to read from
     * @param charset the charset to use when constructing the string
     * @return a string representation of the data read from the input stream
     * @throws IOException if an i/o error occurs
     */
    private String toString(InputStream in, String charset) throws IOException
    {
        InputStreamReader reader = new InputStreamReader(in, charset);

        StringBuilder buffer = new StringBuilder();
        char[] chars = new char[1024];
        int n;
        while ((n = reader.read(chars, 0, chars.length)) != -1)
        {
            buffer.append(chars, 0, n);
        }

        return buffer.toString();
    }

    /**
     * List currently deployed webapps.
     * 
     * @return a string representing the result of invoked command
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public String list() throws IOException, TomcatManagerException
    {
        return invoke("/list", null, null);
    }

    /**
     * Return the status of the webapp at the specified context path.
     * 
     * @param path the webapp context path to get status
     * @return the current status of the webapp in the running container
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if an i/o error occurs
     */
    public TomcatDeployableStatus getStatus(String path) throws IOException, TomcatManagerException
    {
        StringTokenizer records = new StringTokenizer(list(), "\n");
        while (records.hasMoreTokens())
        {
            String record = records.nextToken();
            StringTokenizer words = new StringTokenizer(record, ":");
            while (words.hasMoreTokens())
            {
                String str = words.nextToken();
                if (path.equals(str))
                {
                    String token = words.nextToken();
                    return TomcatDeployableStatus.toStatus(token);
                }
            }
        }
        return TomcatDeployableStatus.NOT_FOUND;
    }
}
