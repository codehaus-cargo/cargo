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
package org.codehaus.cargo.tools.daemon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.internal.http.FormContentType;
import org.codehaus.cargo.container.internal.http.HttpFormRequest;
import org.codehaus.cargo.container.internal.http.HttpRequest;
import org.codehaus.cargo.container.internal.http.HttpResult;
import org.codehaus.cargo.container.internal.http.MultipartFormContentType;
import org.codehaus.cargo.container.internal.http.UrlEncodedFormContentType;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.XmlReplacement;
import org.codehaus.cargo.util.log.LoggedObject;
import org.codehaus.cargo.util.log.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Client for the Cargo daemon
 */
public class DaemonClient extends LoggedObject
{
    /**
     * The full URL of the Cargo daemon instance to use.
     */
    private final URL url;

    /**
     * The username to use when authenticating with Cargo daemon.
     */
    private final String username;

    /**
     * The password to use when authenticating with Cargo daemon.
     */
    private final String password;

    /**
     * The file handler.
     */
    private final FileHandler fileHandler = new DefaultFileHandler();

    /**
     * The user agent name to use when communicating with Cargo daemon.
     */
    private String userAgent;

    /**
     * Creates a Cargo daemon wrapper for the specified URL which has public access (no username
     * nor password required).
     * 
     * @param url the full URL of the Cargo daemon instance to use
     */
    public DaemonClient(URL url)
    {
        this(url, null, null);
    }

    /**
     * Creates a Cargo daemon wrapper for the specified URL, username and password that uses
     * UTF-8 URL encoding.
     * 
     * @param url the full URL of the Cargo daemon instance to use
     * @param username the username to use when authenticating with Cargo daemon
     * @param password the password to use when authenticating with Cargo daemon
     */
    public DaemonClient(URL url, String username, String password)
    {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Overriden in order to set the logger on ancillary components.
     * {@inheritDoc}
     * 
     * @param logger the logger to set and set in the ancillary objects
     */
    @Override
    public void setLogger(Logger logger)
    {
        super.setLogger(logger);
        this.fileHandler.setLogger(logger);
    }

    /**
     * Gets the full URL of the Cargo daemon instance.
     * 
     * @return the full URL of the Cargo daemon instance
     */
    public URL getURL()
    {
        return this.url;
    }

    /**
     * Gets the username to use when authenticating with Cargo daemon.
     * 
     * @return the username to use when authenticating with Cargo daemon
     */
    public String getUserName()
    {
        return this.username;
    }

    /**
     * Gets the password to use when authenticating with Cargo daemon.
     * 
     * @return the password to use when authenticating with Cargo daemon
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * Gets the user agent name to use when communicating with Cargo daemon.
     * 
     * @return the user agent name to use when communicating with Cargo daemon
     */
    public String getUserAgent()
    {
        return this.userAgent;
    }

    /**
     * Sets the user agent name to use when communicating with Cargo daemon.
     * 
     * @param userAgent the user agent name to use when communicating with Cargo daemon
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * Starts a container specified by the start request.
     * 
     * @param start The unique identifier of the container
     * @throws DaemonException if the Cargo daemon request fails
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
            parameters.setParameter("xmlReplacements", setupXmlReplacements(container));

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
        boolean first = true;
        for (String additionalClasspathEntry : additionalClasspathEntries)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                classpathJSON.append(",");
            }

            classpathJSON.append("\"");
            classpathJSON.append(additionalClasspathEntry);
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
        if (list == null || list.size() == 0)
        {
            return;
        }

        StringBuilder listJSON = new StringBuilder();
        listJSON.append("[");
        boolean first = true;
        for (String item : list)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                listJSON.append(",");
            }

            listJSON.append("\"" + item + "\"");
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

        for (FileConfig fileConfig : fileConfigs)
        {
            String file = fileConfig.getFile();
            String relativePath = fileHandler.getName(file);
            resolveFile(files, file, relativePath);
        }

        StringBuilder propertiesJSON = new StringBuilder();

        propertiesJSON.append("[");
        boolean first = true;
        for (FileConfig fileConfig : fileConfigs)
        {
            if (first)
            {
                first = false;
            }
            else
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
                propertiesJSON.append("\"tofile\":\"" + escapeJson(toFile) + "\",");
            }

            if (toDirectory != null)
            {
                propertiesJSON.append("\"todir\":\"" + escapeJson(toDirectory) + "\",");
            }
            propertiesJSON.append("\"overwrite\":\"" + overwrite + "\",");
            propertiesJSON.append("\"filter\":\"" + filter + "\",");
            if (encoding != null)
            {
                propertiesJSON.append("\"encoding\":\"" + escapeJson(encoding) + "\",");
            }
            propertiesJSON.append("\"file\":\"" + escapeJson(file) + "\"");
            propertiesJSON.append("}");
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
        Set<String> properties = configuration.getProperties();

        propertiesJSON.append("{");
        boolean first = true;
        for (String property : properties)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                propertiesJSON.append(",");
            }

            propertiesJSON.append("\"" + escapeJson(property) + "\":\""
                + escapeJson(configuration.getPropertyValue(property)) + "\"");
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

        propertiesJSON.append("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            if (first)
            {
                first = false;
            }
            else
            {
                propertiesJSON.append(",");
            }

            propertiesJSON.append("\"" + escapeJson(entry.getKey()) + "\":\""
                + escapeJson(entry.getValue()) + "\"");
        }
        propertiesJSON.append("}");

        return propertiesJSON.toString();
    }

    /**
     * Setup container XML replacements.
     * 
     * @param container The container
     * @return The container XML replacements list
     */
    private String setupXmlReplacements(InstalledLocalContainer container)
    {
        StringBuilder propertiesJSON = new StringBuilder();

        if (container.getConfiguration() instanceof StandaloneLocalConfiguration)
        {
            StandaloneLocalConfiguration configuration =
                (StandaloneLocalConfiguration) container.getConfiguration();

            propertiesJSON.append("[");
            boolean first = true;
            for (XmlReplacement xmlReplacement : configuration.getXmlReplacements())
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    propertiesJSON.append(",");
                }

                propertiesJSON.append(
                    "{\"attributeName\":\""
                        + escapeJson(xmlReplacement.getAttributeName()) + "\","
                    + "\"file\":\"" + escapeJson(xmlReplacement.getFile()) + "\","
                    +  "\"value\":\"" + escapeJson(xmlReplacement.getValue()) + "\","
                    + "\"xpathExpression\":\""
                        + escapeJson(xmlReplacement.getXpathExpression()) + "\","
                    + "\"replacementBehavior\":\""
                        + xmlReplacement.getReplacementBehavior() + "\"}");
            }
            propertiesJSON.append("]");
        }

        return propertiesJSON.toString();
    }

    /**
     * Escapes the JSON string.
     * 
     * @param string String to escape.
     * @return Escaped string.
     */
    private String escapeJson(String string)
    {
        if (string != null)
        {
            return string.replace("\\", "\\\\").replace("\"", "\\\"");
        }
        else
        {
            return "";
        }
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
     * @throws DaemonException if the Cargo daemon request fails
     * @throws IOException if an i/o error occurs
     */
    public void stop(String handleId) throws DaemonException, IOException
    {
        DaemonParameters parameters = new DaemonParameters();

        parameters.setParameter("handleId", handleId);

        invoke("stop", parameters);
    }

    /**
     * Get details of the handles in the Cargo Daemon.
     * 
     * @return Handle details, where the key is the handle identifier and the value the status
     * @throws DaemonException if the Cargo daemon request fails
     * @throws IOException if an i/o error occurs
     */
    public Map<String, String> getHandles() throws DaemonException, IOException
    {
        JSONObject handles = (JSONObject) JSONValue.parse(invoke("getHandles", null));
        if (handles == null)
        {
            return Collections.emptyMap();
        }
        else
        {
            Map<String, String> result = new HashMap<String, String>(handles.size());
            for (Map.Entry<Object, Object> handle
                : (Set<Map.Entry<Object, Object>>) handles.entrySet())
            {
                result.put(handle.getKey().toString(), handle.getValue().toString());
            }
            return result;
        }
    }

    /**
     * Invokes Cargo daemon with a specified command and content data.
     * 
     * @param path the Cargo daemon command to invoke
     * @param parameters an input stream to the content data
     * @return the result of the invoking command, as returned by the Cargo daemon
     *         application
     * @throws DaemonException if the Cargo daemon request fails
     * @throws IOException if an i/o error occurs
     */
    protected String invoke(String path, DaemonParameters parameters) throws DaemonException,
        IOException
    {
        URL invokeURL;
        if (this.url.toString().endsWith("/"))
        {
            invokeURL = new URL(this.url + path);
        }
        else
        {
            invokeURL = new URL(this.url + "/" + path);
        }

        HttpResult result;
        if (parameters == null)
        {
            HttpRequest request = new HttpRequest(invokeURL);
            request.setAuthentication(this.username, this.password);
            if (this.userAgent != null)
            {
                request.addRequestProperty("User-Agent", this.userAgent);
            }
            request.setLogger(this.getLogger());
            result = request.get();
        }
        else
        {
            FormContentType contentType = null;
            if (parameters.isMultipartForm())
            {
                MultipartFormContentType multipartFormContentType = new MultipartFormContentType();
                contentType = multipartFormContentType;

                for (Map.Entry<String, String> entry : parameters.getFiles().entrySet())
                {
                    multipartFormContentType.setFormFile(
                        entry.getKey(), new File(entry.getValue()));
                }
            }
            else
            {
                contentType = new UrlEncodedFormContentType();
            }
            for (Map.Entry<String, String> entry : parameters.getParameters().entrySet())
            {
                contentType.setFormContent(entry.getKey(), entry.getValue());
            }

            HttpRequest request = new HttpFormRequest(invokeURL, contentType);
            request.setAuthentication(this.username, this.password);
            if (this.userAgent != null)
            {
                request.addRequestProperty("User-Agent", this.userAgent);
            }
            request.setLogger(this.getLogger());
            result = request.post();
        }

        if (!result.isSuccessful())
        {
            if (result.getResponseCode() == 401)
            {
                throw new DaemonException("The username and password you provided are"
                    + " not correct (error 401): "
                        + extractErrorMessage(result.getResponseBody()));
            }
            else if (result.getResponseCode() == 403)
            {
                throw new DaemonException("The username you provided is not allowed to "
                    + "use the text-based Cargo daemon (error 403): "
                        + extractErrorMessage(result.getResponseBody()));
            }
            else
            {
                throw new DaemonException("Failed to call Cargo Daemon via URL [" + invokeURL
                    + "], response code: " + result.getResponseCode()
                        + ", response message: " + result.getResponseMessage()
                            + ", response body: " + extractErrorMessage(result.getResponseBody()));
            }
        }
        else
        {
            String response = result.getResponseBody();
            if (response == null || !response.startsWith("OK -") && !response.startsWith("{"))
            {
                throw new DaemonException("Failed parsing response for " + invokeURL
                    + ". Response was: " + extractErrorMessage(response));
            }

            return response;
        }
    }

    /**
     * Extract the error message from the Daemon error servlet response.
     * @param response Daemon error servlet response.
     * @return Extracted error message.
     */
    private String extractErrorMessage(String response)
    {
        if (response != null)
        {
            final String scriptEndString = "</script>";
            int scriptEnd = response.indexOf(scriptEndString);
            if (scriptEnd != -1)
            {
                return response.substring(scriptEnd + scriptEndString.length()).trim();
            }
        }

        return response;
    }
}
