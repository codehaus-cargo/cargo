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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
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

        String handleId = start.getHandleId();
        parameters.setParameter("handleId", handleId);

        InstalledLocalContainer container = start.getContainer();
        if (container != null)
        {
            boolean autostart = start.isAutostart();
            String installerZipFile = start.getInstallerZipFile();
            String logFile = start.getLogFile();
            List<Deployable> deployables = start.getDeployables();

            LocalConfiguration configuration = container.getConfiguration();

            parameters.setParameter("autostart", String.valueOf(autostart));
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

                parameters.setParameter("installerZipFile", fileHandler.getName(installerZipFile));
            }

            if (deployables != null)
            {
                parameters.setParameter("deployableFiles",
                    setupDeployables(parameters, deployables));
            }

            setupConfigFiles(parameters, configuration);

            if (container instanceof InstalledLocalContainer)
            {
                setupExtraClasspath(parameters, (InstalledLocalContainer) container);
                setupSharedClasspath(parameters, (InstalledLocalContainer) container);
            }

            setupAdditionalClasspath(parameters, start.getAdditionalClasspathEntries());

            parameters.setParameter("configurationProperties",
                setupConfigurationProperties(configuration));
            parameters.setParameter("containerProperties", setupContainerProperties(container));

            parameters.setParameter("containerOutput", container.getOutput());

            if (logFile != null)
            {
                parameters.setParameter("containerLogFile", logFile);
            }

            parameters.setParameter("containerLogLevel",
                container.getLogger().getLevel().toString());

            if (container.isAppend())
            {
                parameters.setParameter("containerAppend", "on");
            }
            else
            {
                parameters.setParameter("containerAppend", "off");
            }
        }

        invoke("start", parameters);
    }

    /**
     * Setup the additional classpath for the container.
     * 
     * @param parameters The daemon parameters.
     * @param additionalClasspathEntries The additional classpath entries.
     */
    private void setupAdditionalClasspath(DaemonParameters parameters,
        List<String> additionalClasspathEntries)
    {
        if (additionalClasspathEntries == null || additionalClasspathEntries.size() == 0)
        {
            return;
        }

        StringBuilder classpathJSON = new StringBuilder();
        classpathJSON.append("[");

        for (int i = 0; i < additionalClasspathEntries.size(); i++)
        {
            String additionalClasspath = additionalClasspathEntries.get(i);

            if (i != 0)
            {
                classpathJSON.append(",");
            }

            classpathJSON.append("\"");
            classpathJSON.append(additionalClasspath);
            classpathJSON.append("\"");
        }

        classpathJSON.append("]");

        parameters.setParameter("additionalClasspath", classpathJSON.toString());
    }

    /**
     * Resolves all files it finds into a map, with relative paths as the key and absolute path as
     * the value.
     * 
     * @param classpaths The list of classpaths to use.
     * @param files The files map to add all found files to.
     * @param paths The initial paths.
     * @param prefix The relative path prefix.
     */
    private void resolveFiles(List<String> classpaths, Map<String, String> files, String[] paths,
        String prefix)
    {
        for (String path : paths)
        {
            String relativePath = fileHandler.getName(path);

            if (prefix != null)
            {
                relativePath = fileHandler.append(prefix, relativePath);
            }

            if (classpaths != null)
            {
                classpaths.add(relativePath);
            }

            if (fileHandler.isDirectory(path))
            {
                String[] children = fileHandler.getChildren(path);

                if (children != null && children.length != 0)
                {
                    resolveFiles(null, files, children, relativePath);
                }
            }
            else
            {
                files.put(relativePath, path);
            }
        }
    }

    /**
     * Resolves a file/directory path into a map of file paths which specifies each file of the
     * directory, or the file itself.
     * 
     * @param files The map to put each resolved file in.
     * @param file The absolute file path to resolve.
     * @param relativePath The relative path of the file.
     */
    private void resolveFile(Map<String, String> files, String file, String relativePath)
    {
        if (fileHandler.isDirectory(file))
        {
            String[] children = fileHandler.getChildren(file);

            if (children != null && children.length != 0)
            {
                for (String child : children)
                {
                    resolveFile(files, child,
                        fileHandler.append(relativePath, fileHandler.getName(child)));
                }
            }

        }
        else
        {
            files.put(relativePath, file);
        }
    }

    /**
     * Adds a list of string parameter to the daemon parameters.
     * 
     * @param parameters The daemon parameters.
     * @param parameterName The parameter name to add.
     * @param list The list of strings to add as value.
     */
    private void addListParameter(DaemonParameters parameters, String parameterName,
        List<String> list)
    {
        int i = 0;

        if (list == null || list.size() == 0)
        {
            return;
        }

        StringBuilder listJSON = new StringBuilder();
        listJSON.append("[");

        for (String item : list)
        {
            if (i != 0)
            {
                listJSON.append(",");
            }

            listJSON.append("\"" + item + "\"");
            i++;
        }

        listJSON.append("]");

        parameters.setParameter(parameterName, listJSON.toString());
    }

    /**
     * Adds a list of file parameters to the daemon parameters.
     * 
     * @param parameters The daemon parameters.
     * @param parameterName The parameter name to add.
     * @param filePrefix The file prefix to use when adding.
     * @param files The map of files, with as key the relative path, and the absolute path as value.
     */
    private void addFilesParameter(DaemonParameters parameters, String parameterName,
        String filePrefix, Map<String, String> files)
    {
        StringBuilder propertiesJSON = new StringBuilder();
        propertiesJSON.append("[");

        int fileId = 0;
        for (Map.Entry<String, String> entry : files.entrySet())
        {
            String relativePath = entry.getKey();
            String absolutePath = entry.getValue();

            if (fileId != 0)
            {
                propertiesJSON.append(",");
            }

            propertiesJSON.append("\"" + relativePath + "\"");

            parameters.setFile(filePrefix + fileId, absolutePath);
            fileId++;
        }

        propertiesJSON.append("]");

        parameters.setParameter(parameterName, propertiesJSON.toString());
    }

    /**
     * Setup extra classpath.
     * 
     * @param parameters The daemon parameters
     * @param container The container to deploy
     */
    private void setupExtraClasspath(DaemonParameters parameters,
        InstalledLocalContainer container)
    {
        String[] extraClasspaths = container.getExtraClasspath();

        if (extraClasspaths == null || extraClasspaths.length == 0)
        {
            return;
        }

        Map<String, String> files = new HashMap<String, String>();
        List<String> relativeClasspaths = new ArrayList<String>();

        resolveFiles(relativeClasspaths, files, extraClasspaths, null);

        addFilesParameter(parameters, "extraFiles", "extraFileData_", files);
        addListParameter(parameters, "extraClasspath", relativeClasspaths);
    }

    /**
     * Setup shared classpath.
     * 
     * @param parameters The daemon parameters
     * @param container The container to deploy
     */
    private void setupSharedClasspath(DaemonParameters parameters,
        InstalledLocalContainer container)
    {
        String[] sharedClasspaths = container.getSharedClasspath();

        if (sharedClasspaths == null || sharedClasspaths.length == 0)
        {
            return;
        }

        Map<String, String> files = new HashMap<String, String>();
        List<String> relativeClasspaths = new ArrayList<String>();

        resolveFiles(relativeClasspaths, files, sharedClasspaths, null);

        addFilesParameter(parameters, "sharedFiles", "sharedFileData_", files);
        addListParameter(parameters, "sharedClasspath", relativeClasspaths);
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
            propertiesJSON.append("\"filename\":\"" + fileHandler.getName(deployable.getFile())
                + "\"");

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
     */
    private void setupConfigFiles(DaemonParameters parameters, LocalConfiguration configuration)
    {
        List<FileConfig> fileConfigs = configuration.getFileProperties();

        if (fileConfigs == null || fileConfigs.size() == 0)
        {
            return;
        }

        Map<String, String> files = new HashMap<String, String>();
        int i = 0;

        for (FileConfig fileConfig : fileConfigs)
        {
            String file = fileConfig.getFile();

            String relativePath = fileHandler.getName(file);

            resolveFile(files, file, relativePath);
        }


        StringBuilder propertiesJSON = new StringBuilder();

        propertiesJSON.append("[");
        for (FileConfig fileConfig : fileConfigs)
        {
            if (i != 0)
            {
                propertiesJSON.append(",");
            }

            String file = fileHandler.getName(fileConfig.getFile());
            String toFile = fileConfig.getToFile();
            String toDirectory = fileConfig.getToDir();
            boolean overwrite = fileConfig.getOverwrite();
            boolean filter = fileConfig.getConfigfile();
            String encoding = fileConfig.getEncoding();

            propertiesJSON.append("{");
            if (toFile != null)
            {
                propertiesJSON.append("\"tofile\":\"" + toFile + "\",");
            }

            if (toDirectory != null)
            {
                propertiesJSON.append("\"todir\":\"" + toDirectory + "\",");
            }
            propertiesJSON.append("\"overwrite\":\"" + overwrite + "\",");
            propertiesJSON.append("\"filter\":\"" + filter + "\",");
            if (encoding != null)
            {
                propertiesJSON.append("\"encoding\":\"" + encoding + "\",");
            }
            propertiesJSON.append("\"file\":\"" + file + "\"");
            propertiesJSON.append("}");

            i++;
        }
        propertiesJSON.append("]");

        parameters.setParameter("configurationFileProperties", propertiesJSON.toString());
        addFilesParameter(parameters, "configurationFiles", "configurationFileData_", files);
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

            propertiesJSON.append("\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"");

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

            propertiesJSON.append("\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"");

            i++;
        }
        propertiesJSON.append("}");

        return propertiesJSON.toString();
    }

    /**
     * Asks the daemon if a file is installed.
     * 
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
            String scriptEndString = "</script>";
            int scriptEnd = response.indexOf(scriptEndString);
            if (scriptEnd != -1)
            {
                response = response.substring(scriptEnd + scriptEndString.length()).trim();
            }

            throw new DaemonException("Failed parsing response for " + invokeURL
                + ". Response was: " + response);
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
