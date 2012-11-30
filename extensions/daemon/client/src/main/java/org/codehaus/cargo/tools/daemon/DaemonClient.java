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
package org.codehaus.cargo.tools.daemon;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.util.Base64;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Client for the Cargo daemon manager
 *
 * @version $Id$
 */
public class DaemonClient extends LoggedObject
{
    /**
     * The charset to use when decoding Cargo daemon manager responses.
     */
    private static final String MANAGER_CHARSET = "UTF-8";

    /**
     * The full URL of the Cargo daemon manager instance to use.
     */
    private final URL url;

    /**
     * The username to use when authenticating with Cargo daemon manager.
     */
    private final String username;

    /**
     * The password to use when authenticating with Cargo daemon manager.
     */
    private final String password;

    /**
     * The URL encoding charset to use when communicating with Cargo daemon manager.
     */
    private final String charset;

    /**
     * The file handler.
     */
    private final FileHandler fileHandler = new DefaultFileHandler();

    /**
     * The user agent name to use when communicating with Cargo daemon manager.
     */
    private String userAgent;

    /**
     * Creates a Cargo daemon manager wrapper for the specified URL that uses a username of
     * <code>admin</code>, an empty password and ISO-8859-1 URL encoding.
     *
     * @param url the full URL of the Cargo daemon manager instance to use
     */
    public DaemonClient(URL url)
    {
        this(url, "admin");
    }

    /**
     * Creates a Cargo daemon manager wrapper for the specified URL and username that uses an empty
     * password and ISO-8859-1 URL encoding.
     *
     * @param url the full URL of the Cargo daemon manager instance to use
     * @param username the username to use when authenticating with Cargo daemon manager
     */
    public DaemonClient(URL url, String username)
    {
        this(url, username, "");
    }

    /**
     * Creates a Cargo daemon manager wrapper for the specified URL, username and password that uses
     * ISO-8859-1 URL encoding.
     *
     * @param url the full URL of the Cargo daemon manager instance to use
     * @param username the username to use when authenticating with Cargo daemon manager
     * @param password the password to use when authenticating with Cargo daemon manager
     */
    public DaemonClient(URL url, String username, String password)
    {
        this(url, username, password, "ISO-8859-1");
    }

    /**
     * Creates a Cargo daemon manager wrapper for the specified URL, username, password and URL
     * encoding.
     *
     * @param url the full URL of the Cargo daemon manager instance to use
     * @param username the username to use when authenticating with Cargo daemon manager
     * @param password the password to use when authenticating with Cargo daemon manager
     * @param charset the URL encoding charset to use when communicating with Cargo daemon manager
     */
    public DaemonClient(URL url, String username, String password, String charset)
    {
        this.url = url;
        this.username = username;
        this.password = password;
        this.charset = charset;
    }

    /**
     * Gets the full URL of the Cargo daemon manager instance.
     *
     * @return the full URL of the Cargo daemon manager instance
     */
    public URL getURL()
    {
        return this.url;
    }

    /**
     * Gets the username to use when authenticating with Cargo daemon manager.
     *
     * @return the username to use when authenticating with Cargo daemon manager
     */
    public String getUserName()
    {
        return this.username;
    }

    /**
     * Gets the password to use when authenticating with Cargo daemon manager.
     *
     * @return the password to use when authenticating with Cargo daemon manager
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * Gets the URL encoding charset to use when communicating with Cargo daemon manager.
     *
     * @return the URL encoding charset to use when communicating with Cargo daemon manager
     */
    public String getCharset()
    {
        return this.charset;
    }

    /**
     * Gets the user agent name to use when communicating with Cargo daemon manager.
     *
     * @return the user agent name to use when communicating with Cargo daemon manager
     */
    public String getUserAgent()
    {
        return this.userAgent;
    }

    /**
     * Sets the user agent name to use when communicating with Cargo daemon manager.
     *
     * @param userAgent the user agent name to use when communicating with Cargo daemon manager
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * Starts a container specified by the start request.
     *
     * @param start The unique identifier of the container
     * @throws DaemonException if the Cargo daemon manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void start(DaemonStart start) throws DaemonException, IOException
    {
        DaemonParameters parameters = new DaemonParameters();
        InstalledLocalContainer container = start.getContainer();
        String handleId = start.getHandleId();
        String installerZipFile = start.getInstallerZipFile();
        List<Deployable> deployables = start.getDeployables();

        LocalConfiguration configuration = container.getConfiguration();

        parameters.setParameter("handleId", handleId);
        parameters.setParameter("containerId", container.getId());
        parameters.setParameter("configurationType", configuration.getType().toString());
        parameters.setParameter("timeout", String.valueOf(container.getTimeout()));

        if (container.getHome() != null)
        {
            parameters.setParameter("containerHome", container.getHome());
        }
        if (configuration.getHome() != null)
        {
            parameters.setParameter("configurationHome", configuration.getHome());
        }

        if (installerZipFile != null)
        {
            if (!installed(installerZipFile))
            {
                parameters.setFile("installerZipFileData", installerZipFile);
            }

            parameters.setParameter("installerZipFile",
                fileHandler.getName(installerZipFile));
        }

        if (deployables != null)
        {
            parameters.setParameter("deployableFiles",
                setupDeployables(parameters, deployables));
        }

        if (configuration instanceof StandaloneLocalConfiguration)
        {
            parameters.setParameter("configurationFiles",
                setupConfigFiles(parameters, (StandaloneLocalConfiguration) configuration));
        }

        parameters.setParameter("configurationProperties",
            setupConfigurationProperties(configuration));
        parameters.setParameter("containerProperties",
            setupContainerProperties(container));

        parameters.setParameter("containerOutput",
            container.getOutput());

        if (container.isAppend())
        {
            parameters.setParameter("containerAppend", "on");
        }
        else
        {
            parameters.setParameter("containerAppend", "off");
        }

        invoke("start", parameters);
    }

    /**
     * Setup deployables.
     *
     * @param parameters The daemon parameters
     * @param deployables The deployables
     * @return The deployable configuration list
     */
    private String setupDeployables(DaemonParameters parameters, List<Deployable> deployables)
    {
        StringBuilder propertiesJSON = new StringBuilder();
        propertiesJSON.append("[");
        for (int i = 0; i < deployables.size(); i++)
        {
            Deployable deployable = deployables.get(i);

            if (i != 0)
            {
                propertiesJSON.append(",");
            }
            propertiesJSON.append("{");
            if (deployable instanceof WAR)
            {
                WAR war = (WAR) deployable;

                propertiesJSON.append("\"context\":\"" + war.getContext() + "\",");
            }

            propertiesJSON.append("\"type\":\"" + deployable.getType().toString() + "\",");
            propertiesJSON.append("\"filename\":\""
                + fileHandler.getName(deployable.getFile()) + "\"");

            propertiesJSON.append("}");

            parameters.setFile("deployableFileData_" + i, deployable.getFile());
        }
        propertiesJSON.append("]");

        return propertiesJSON.toString();
    }

    /**
     * Setup config files.
     *
     * @param parameters The daemon parameters
     * @param configuration The configuration
     * @return The configuration files list
     */
    private String setupConfigFiles(DaemonParameters parameters,
        StandaloneLocalConfiguration configuration)
    {
        List<FileConfig> fileProperties = configuration.getFileProperties();
        StringBuilder propertiesJSON = new StringBuilder();

        propertiesJSON.append("[");
        for (int i = 0; i < fileProperties.size(); i++)
        {
            FileConfig fileConfig = fileProperties.get(i);

            if (i != 0)
            {
                propertiesJSON.append(",");
            }

            String filename = fileConfig.getToFile();
            String directory = fileConfig.getToDir();
            boolean overwrite = fileConfig.getOverwrite();
            boolean parse = fileConfig.getConfigfile();
            String encoding = fileConfig.getEncoding();

            if (filename == null)
            {
                filename = "";
            }

            if (directory == null)
            {
                directory = "";
            }

            if (encoding == null)
            {
                encoding = "";
            }

            propertiesJSON.append("{");
            propertiesJSON.append("\"filename\":\"" + filename + "\",");
            propertiesJSON.append("\"directory\":\"" + directory + "\",");
            propertiesJSON.append("\"overwrite\":\"" + overwrite + "\",");
            propertiesJSON.append("\"parse\":\"" + parse + "\",");
            propertiesJSON.append("\"encoding\":\"" + encoding + "\",");
            propertiesJSON.append("}");

            parameters.setFile("configurationFileData_" + i, fileConfig.getFile());
        }
        propertiesJSON.append("]");

        return propertiesJSON.toString();
    }

    /**
     * Setup configuration properties.
     *
     * @param configuration The configuration
     * @return The configuration properties list
     */
    private String setupConfigurationProperties(LocalConfiguration configuration)
    {
        StringBuilder propertiesJSON = new StringBuilder();
        Map<String, String> properties = configuration.getProperties();
        int i = 0;

        propertiesJSON.append("{");
        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            if (i != 0)
            {
                propertiesJSON.append(",");
            }

            propertiesJSON.append("\"" + entry.getKey() + "\":\""
                + entry.getValue() + "\"");

            i++;
        }
        propertiesJSON.append("}");

        return propertiesJSON.toString();
    }

    /**
     * Setup container properties.
     *
     * @param container The container
     * @return The container properties list
     */
    private String setupContainerProperties(InstalledLocalContainer container)
    {
        StringBuilder propertiesJSON = new StringBuilder();
        Map<String, String> properties = container.getSystemProperties();
        int i = 0;

        propertiesJSON.append("{");
        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            if (i != 0)
            {
                propertiesJSON.append(",");
            }

            propertiesJSON.append("\"" + entry.getKey() + "\":\""
                + entry.getValue() + "\"");

            i++;
        }
        propertiesJSON.append("}");

        return propertiesJSON.toString();
    }

    /**
     * Asks the daemon if a file is installed.
     * @param file The file to test
     * @return true if file is installed
     * @throws DaemonException If a daemon exception occurs
     * @throws IOException If an I/O error occurs
     */
    private boolean installed(String file) throws DaemonException, IOException
    {
        DaemonParameters parameters = new DaemonParameters();

        parameters.setParameter("file", fileHandler.getName(file));

        String response = invoke("installed", parameters);

        if (response != null)
        {
            response = response.trim();
        }

        return "OK - INSTALLED".equals(response);
    }

    /**
     * Stops the container with the specified handle identifier.
     *
     * @param handleId The unique identifier of the container
     * @throws DaemonException if the Cargo daemon manager request fails
     * @throws IOException if an i/o error occurs
     */
    public void stop(String handleId) throws DaemonException, IOException
    {
        DaemonParameters parameters = new DaemonParameters();

        parameters.setParameter("handleId", handleId);

        invoke("stop", parameters);
    }

    /**
     * Invokes Cargo daemon manager with a specified command and content data.
     *
     * @param path the Cargo daemon manager command to invoke
     * @param parameters an input stream to the content data
     * @return the result of the invoking command, as returned by the Cargo daemon manager
     *         application
     * @throws DaemonException if the Cargo daemon manager request fails
     * @throws IOException if an i/o error occurs
     */
    protected String invoke(String path, DaemonParameters parameters) throws DaemonException,
        IOException
    {
        FormContentType contentType = null;
        UrlEncodedFormWriter urlEncodedFormWriter = null;

        URL invokeURL;
        if (this.url.toString().endsWith("/"))
        {
            invokeURL = new URL(this.url + path);
        }
        else
        {
            invokeURL = new URL(this.url + "/" + path);
        }

        HttpURLConnection connection = (HttpURLConnection) invokeURL.openConnection();
        connection.setAllowUserInteraction(false);
        connection.setDoInput(true);
        connection.setUseCaches(false);

        if (parameters == null)
        {
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
        }
        else
        {
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            if (parameters.isMultipartForm())
            {
                contentType = new MultipartFormContentType();
                // When trying to upload large amount of data the internal connection buffer
                // can become too large and exceed the heap size, leading to a
                // java.lang.OutOfMemoryError.
                // This was fixed in JDK 1.5 by introducing a new setChunkedStreamingMode()
                // method.
                connection.setChunkedStreamingMode(0);
            }
            else
            {
                contentType = new UrlEncodedFormContentType();
                urlEncodedFormWriter = new UrlEncodedFormWriter();

                for (Map.Entry<String, String> entry : parameters.getParameters().entrySet())
                {
                    urlEncodedFormWriter.addField(entry.getKey(), entry.getValue());
                }

                connection.setRequestProperty("Content-Length",
                    String.valueOf(urlEncodedFormWriter.getLength()));
            }
        }

        if (contentType != null)
        {
            connection.setRequestProperty("Content-Type", contentType.getContentType());
        }

        if (this.userAgent != null)
        {
            connection.setRequestProperty("User-Agent", this.userAgent);
        }

        if (this.username != null)
        {
            String authorization = toAuthorization(this.username, this.password);
            connection.setRequestProperty("Authorization", authorization);
        }

        connection.connect();

        if (contentType instanceof MultipartFormContentType)
        {
            MultipartFormWriter writer =
                new MultipartFormWriter((MultipartFormContentType) contentType,
                    connection.getOutputStream());

            for (Map.Entry<String, String> entry : parameters.getParameters().entrySet())
            {
                writer.writeField(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : parameters.getFiles().entrySet())
            {
                writer.writeFile(entry.getKey(), "application/octet-stream",
                    fileHandler.getName(entry.getValue()),
                    fileHandler.getInputStream(entry.getValue()));
            }
            writer.close();
        }
        else if (contentType instanceof UrlEncodedFormContentType)
        {
            urlEncodedFormWriter.write(connection.getOutputStream());
        }

        String response;
        try
        {
            getLogger().info("Trying to read input data", this.getClass().getName());
            response = toString(connection.getInputStream(), MANAGER_CHARSET);
        }
        catch (IOException e)
        {
            if (connection.getResponseCode() == 401)
            {
                throw new DaemonException("The username and password you provided are"
                    + " not correct (error 401)", e);
            }
            else if (connection.getResponseCode() == 403)
            {
                throw new DaemonException("The username you provided is not allowed to "
                    + "use the text-based Cargo daemon manager (error 403)", e);
            }
            else
            {
                throw new DaemonException(connection.getResponseMessage(), e);
            }
        }
        getLogger().info("Response is " + response, this.getClass().getName());

        if (!response.startsWith("OK -"))
        {
            throw new DaemonException(
                "Failed parsing response for " + invokeURL + ". Response was: " + response);
        }

        return response;
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

}
