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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.codehaus.cargo.container.internal.http.HttpRequest;
import org.codehaus.cargo.container.internal.http.HttpResult;
import org.codehaus.cargo.container.internal.http.HttpFileRequest;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * A Tomcat manager webapp invocation wrapper.
 */
public class TomcatManager extends LoggedObject
{
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
     * The URL encoding charset to use when communicating with Tomcat manager.<br>
     * <br>
     * <b>TODO</b>: {@link URLEncoder#encode(java.lang.String, java.nio.charset.Charset)}
     * was introduced in Java 10, switch the below type to {@link Charset} when Codehaus Cargo is
     * on Java 10+.
     */
    private String charset;

    /**
     * The user agent name to use when communicating with Tomcat manager.
     */
    private String userAgent;

    /**
     * Operation timeout when communicating with Tomcat manager
     */
    private int timeout = 0;

    /**
     * Creates a Tomcat manager wrapper for the specified URL, username and password that uses
     * UTF-8 URL encoding.
     * 
     * @param url the full URL of the Tomcat manager instance to use
     * @param username the username to use when authenticating with Tomcat manager
     * @param password the password to use when authenticating with Tomcat manager
     */
    public TomcatManager(URL url, String username, String password)
    {
        this(url, username, password, StandardCharsets.UTF_8);
    }

    /**
     * Creates a Tomcat manager wrapper for the specified URL, username, password and URL encoding.
     * 
     * @param url the full URL of the Tomcat manager instance to use
     * @param username the username to use when authenticating with Tomcat manager
     * @param password the password to use when authenticating with Tomcat manager
     * @param charset the URL encoding charset to use when communicating with Tomcat manager
     */
    public TomcatManager(URL url, String username, String password, Charset charset)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        this.charset = charset.name();
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
    public Charset getCharset()
    {
        return Charset.forName(this.charset);
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @param war the WAR file to deploy
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
     */
    public void deploy(String path, File war) throws TomcatManagerException, IOException
    {
        deploy(path, war, false);
    }

    /**
     * Deploys the specified WAR as a HTTP PUT to the specified context path, optionally undeploying
     * the webapp if it already exists.
     * 
     * @param path the webapp context path to deploy to
     * @param war the WAR file to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
     */
    public void deploy(String path, File war, boolean update)
        throws TomcatManagerException, IOException
    {
        deploy(path, war, update, null);
    }

    /**
     * Deploys the specified WAR as a HTTP PUT to the specified context path, optionally undeploying
     * the webapp if it already exists and using the specified tag name.
     * 
     * @param path the webapp context path to deploy to
     * @param war the WAR file to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @param tag the tag name to use
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
     */
    public void deploy(String path, File war, boolean update, String tag)
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
     * @param war the WAR file to deploy
     * @param update whether to first undeploy the webapp if it already exists
     * @param tag the tag name to use
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
     */
    public void deploy(String path, String version, File war, boolean update, String tag)
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
     */
    public void undeploy(String path, String version) throws TomcatManagerException, IOException
    {
        StringBuilder sb = new StringBuilder("/undeploy");
        sb.append("?path=").append(URLEncoder.encode(path, this.charset));
        if (version != null)
        {
            sb.append("&version=").append(URLEncoder.encode(version, this.charset));
        }
        invoke(sb.toString());
    }

    /**
     * Removes the webapp at the specified context path.
     * 
     * @param path the webapp context path to remove
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
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
     * @throws IOException if connecting to the server fails
     */
    protected void invoke(String path) throws TomcatManagerException, IOException
    {
        invoke(path, null);
    }

    /**
     * Invokes Tomcat manager with the specified command and content data.
     * 
     * @param path the Tomcat manager command to invoke
     * @param fileData the file to stream as content data, if needed
     * @return the result of the invoking command, as returned by the Tomcat Manager application
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
     */
    protected String invoke(String path, File fileData) throws TomcatManagerException, IOException
    {
        getLogger().debug("Invoking Tomcat manager using path [" + path + "]",
            getClass().getName());

        HttpResult response;
        URL invokeURL = new URL(this.url + path);
        if (fileData == null)
        {
            getLogger().debug("Performing GET request", getClass().getName());
            HttpRequest request = new HttpRequest(invokeURL, this.timeout);
            request.setLogger(this.getLogger());
            request.setAuthentication(username, password);
            if (this.userAgent != null)
            {
                request.addRequestProperty("User-Agent", this.userAgent);
            }
            response = request.get();
        }
        else
        {
            getLogger().debug("Performing PUT request", getClass().getName());
            HttpFileRequest request = new HttpFileRequest(invokeURL, fileData, this.timeout);
            request.setLogger(this.getLogger());
            request.setAuthentication(username, password);
            if (this.userAgent != null)
            {
                request.addRequestProperty("User-Agent", this.userAgent);
            }
            response = request.put();
        }
        if (!response.isSuccessful())
        {
            throw new TomcatManagerException("HTTP request failed, response code: "
                + response.getResponseCode() + ", response message: "
                    + response.getResponseMessage() + ", response body: "
                        + response.getResponseBody());
        }
        else
        {
            String responseBody = response.getResponseBody();
            if (responseBody == null || !responseBody.startsWith("OK -"))
            {
                throw new TomcatManagerException("The Tomcat Manager responded \"" + responseBody
                    + "\" instead of the expected \"OK\" message");
            }
            return responseBody;
        }
    }

    /**
     * Deploys the specified WAR.
     * 
     * @param path the webapp context path to deploy to
     * @param version the webapp version
     * @param config the URL of the context XML configuration to deploy, or null for none
     * @param war the URL of the WAR to deploy, or null to use <code>file</code>
     * @param file the WAR file to deploy, or null to use <code>war</code>
     * @param update whether to first undeploy the webapp if it already exists
     * @param tag the tag name to use
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
     */
    private void deployImpl(String path, String version, URL config, URL war, File file,
        boolean update, String tag) throws TomcatManagerException, IOException
    {
        StringBuilder sb = new StringBuilder("/deploy");
        sb.append("?path=").append(URLEncoder.encode(path, this.charset));
        if (version != null)
        {
            sb.append("&version=").append(URLEncoder.encode(version, this.charset));
        }
        if (config != null)
        {
            sb.append("&config=").append(URLEncoder.encode(config.toString(), this.charset));
        }
        if (war != null)
        {
            sb.append("&war=").append(URLEncoder.encode(war.toString(), this.charset));
        }
        if (update)
        {
            sb.append("&update=true");
        }
        if (tag != null)
        {
            sb.append("&tag=").append(URLEncoder.encode(tag, this.charset));
        }

        invoke(sb.toString(), file);
    }

    /**
     * List currently deployed webapps.
     * 
     * @return a string representing the result of invoked command
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
     */
    public String list() throws IOException, TomcatManagerException
    {
        return invoke("/list", null);
    }

    /**
     * Return the status of the webapp at the specified context path.
     * 
     * @param path the webapp context path to get status
     * @return the current status of the webapp in the running container
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
     */
    public TomcatDeployableStatus getStatus(String path) throws IOException, TomcatManagerException
    {
        return getStatus(path, null);
    }

    /**
     * Return the status of the webapp at the specified context path and version.
     * 
     * @param path the webapp context path to get status
     * @param version the version of the webapp context path to get status
     * @return the current status of the webapp in the running container
     * @throws TomcatManagerException if the Tomcat manager request fails
     * @throws IOException if connecting to the server fails
     */
    public TomcatDeployableStatus getStatus(String path, String version) throws IOException,
        TomcatManagerException
    {
        String versionIdentifier;
        if (version != null)
        {
            versionIdentifier = "##" + version;
        }
        else
        {
            versionIdentifier = null;
        }

        StringTokenizer records = new StringTokenizer(list(), "\n");
        while (records.hasMoreTokens())
        {
            String record = records.nextToken();
            StringTokenizer words = new StringTokenizer(record, ":");
            while (words.hasMoreTokens())
            {
                String str = words.nextToken();
                // CARGO-1563: If the path is set to /ROOT, Tomcat will actually deploy on /
                if (path.equals(str) || path.equalsIgnoreCase("/ROOT") && str.equals("/"))
                {
                    String status = words.nextToken();
                    if (versionIdentifier != null)
                    {
                        // Number of active sessions (ignored)
                        str = words.nextToken();
                        try
                        {
                            str = words.nextToken();
                            if (str.endsWith(versionIdentifier))
                            {
                                return TomcatDeployableStatus.toStatus(status);
                            }
                        }
                        catch (NoSuchElementException ignored)
                        {
                            // Tomcat Manager 6.x and earlier didn't have versions,
                            // ignore version matches and return best match
                            return TomcatDeployableStatus.toStatus(status);
                        }
                    }
                    else
                    {
                        return TomcatDeployableStatus.toStatus(status);
                    }
                }
            }
        }
        return TomcatDeployableStatus.NOT_FOUND;
    }

    /**
     * Operation timeout when communicating with Tomcat manager
     * 
     * @param timeout in milliseconds; max is Integer.MAX_VALUE
     */
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }
}
