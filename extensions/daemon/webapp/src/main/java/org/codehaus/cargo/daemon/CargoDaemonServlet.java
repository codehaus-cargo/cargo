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
package org.codehaus.cargo.daemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.installer.Installer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.daemon.file.FileManager;
import org.codehaus.cargo.daemon.properties.PropertyTable;
import org.codehaus.cargo.daemon.request.StartRequest;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.codehaus.cargo.uberjar.Uberjar;
import org.codehaus.cargo.util.XmlReplacement;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * Cargo daemon servlet.
 */
public class CargoDaemonServlet extends HttpServlet implements Runnable
{
    /**
     * The charset to use when decoding Cargo daemon responses.
     */
    private static final String DAEMON_SERVLET_CHARSET = StandardCharsets.UTF_8.name();

    /**
     * The periodic amount of milliseconds between checking if containers are still alive.
     */
    private static final int AUTOSTARTTIMEOUT = 20;

    /**
     * The initial amount of milliseconds between checking if containers are still alive.
     */
    private static final int INITIALAUTOSTARTTIMEOUT = 3;

    /**
     * Serial version UUID.
     */
    private static final long serialVersionUID = 3514721195204610896L;

    /**
     * Container factory.
     */
    private static final ContainerFactory CONTAINER_FACTORY = new DefaultContainerFactory();

    /**
     * Configuration factory.
     */
    private static final ConfigurationFactory CONFIGURATION_FACTORY =
        new DefaultConfigurationFactory();

    /**
     * Deployable factory.
     */
    private static final DeployableFactory DEPLOYABLE_FACTORY = new DefaultDeployableFactory();

    /**
     * Daemon version.
     */
    private String daemonVersion;

    /**
     * List of deployables supported by the daemon, stored as a JSON.
     */
    private String deployableTypes;

    /**
     * File manager for the daemon.
     */
    private final FileManager fileManager = new FileManager();

    /**
     * Map of handles to deployed containers.
     */
    private volatile HandleDatabase handles = null;

    /**
     * Used for running background tasks.
     */
    private ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(1);

    /**
     * Read the index page.
     * 
     * @return Index page.
     * @throws Exception If exception happens
     */
    private String generateIndexPage() throws Exception
    {
        Map<String, String> replacements = new HashMap<String, String>();

        Map<String, Set<ContainerType>> containerIds = CONTAINER_FACTORY.getContainerIds();
        SortedMap<String, String> sortedContainerIds = new TreeMap<String, String>();
        for (String containerId : containerIds.keySet())
        {
            try
            {
                Configuration configuration = CONFIGURATION_FACTORY.createConfiguration(
                    containerId, ContainerType.INSTALLED, ConfigurationType.STANDALONE);
                InstalledLocalContainer container = (InstalledLocalContainer)
                    CONTAINER_FACTORY.createContainer(
                        containerId, ContainerType.INSTALLED, configuration);

                String[] containerNameAndVersion =
                    container.getName().toLowerCase(Locale.ENGLISH).split("\\s");
                if (containerNameAndVersion.length > 1)
                {
                    if (containerNameAndVersion[1].charAt(0) >= '0'
                        && containerNameAndVersion[1].charAt(0) <= '9')
                    {
                        Double containerVersion = Double.parseDouble(
                            containerNameAndVersion[1].replace(".x", ""));
                        if (containerVersion < 10.0)
                        {
                            sortedContainerIds.put(
                                containerNameAndVersion[0] + '0' + containerVersion, containerId);
                        }
                        else
                        {
                            sortedContainerIds.put(
                                containerNameAndVersion[0] + containerVersion, containerId);
                        }
                    }
                    else
                    {
                        sortedContainerIds.put(
                            containerNameAndVersion[0] + containerNameAndVersion[1], containerId);
                    }
                }
                else
                {
                    sortedContainerIds.put(containerNameAndVersion[0], containerId);
                }
            }
            catch (ContainerException e)
            {
                // That container doesn't have an installed standalone configuration
                continue;
            }
        }

        replacements.put("daemonVersion", this.daemonVersion);
        replacements.put("deployableTypes", this.deployableTypes);
        replacements.put("containerIds",
            JSONArray.toJSONString(new ArrayList<String>(sortedContainerIds.values())));
        replacements.put("handles", JSONValue.toJSONString(getHandleDetails()));

        StringBuilder indexPageBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            this.getServletContext().getResourceAsStream("/index.html"), StandardCharsets.UTF_8)))
        {
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                for (Map.Entry<String, String> replacement : replacements.entrySet())
                {
                    line = line.replace('@' + replacement.getKey() + '@', replacement.getValue());
                }
                indexPageBuilder.append(line);
                indexPageBuilder.append("\r\n");
            }
        }
        return indexPageBuilder.toString();
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        try (InputStream manifestMf =
                this.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"))
        {
            if (manifestMf != null)
            {
                Properties prop = new Properties();
                prop.load(manifestMf);
                this.daemonVersion = prop.getProperty("Implementation-Version");
            }
        }
        catch (IOException e)
        {
            // Ignore
        }
        if (this.daemonVersion == null)
        {
            this.daemonVersion = this.getClass().getPackage().getImplementationVersion();
        }
        if (this.daemonVersion == null)
        {
            this.daemonVersion = Uberjar.class.getPackage().getImplementationVersion();
        }
        if (this.daemonVersion == null)
        {
            this.daemonVersion = "unknown";
        }

        List<String> deployableTypes = new ArrayList<String>();

        deployableTypes.add(DeployableType.AOP.toString());
        deployableTypes.add(DeployableType.BUNDLE.toString());
        deployableTypes.add(DeployableType.EAR.toString());
        deployableTypes.add(DeployableType.EJB.toString());
        deployableTypes.add(DeployableType.FILE.toString());
        deployableTypes.add(DeployableType.HAR.toString());
        deployableTypes.add(DeployableType.RAR.toString());
        deployableTypes.add(DeployableType.SAR.toString());
        deployableTypes.add(DeployableType.WAR.toString());

        this.deployableTypes = JSONArray.toJSONString(deployableTypes);

        // Try loading the handle database files
        try
        {
            handles = fileManager.loadHandleDatabase();
        }
        catch (IOException e)
        {
            // Ignore, we'll try again later
        }

        // Start background task for restarting webapps
        scheduledExecutor.scheduleAtFixedRate(
            this, INITIALAUTOSTARTTIMEOUT, AUTOSTARTTIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public void destroy()
    {
        scheduledExecutor.shutdown();
        super.destroy();
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String servletPath = request.getServletPath();
        servletPath = servletPath.substring(servletPath.lastIndexOf('/') + 1);

        switch (servletPath)
        {
            case "start":
                // The "start" method is used to either:
                //   1) Create a new handleId (i.e. a new container and configuration) and start it
                //   2) Replace an existing handleId with the provided configuration and start it
                //   3) Load an existing handleId with the existing configuration and start it

                StartRequest startRequest = null;
                boolean previouslyExistingStartRequest = false;
                try
                {
                    String handleId = request.getParameter("handleId");
                    String containerId = request.getParameter("containerId");
                    if (handleId != null && containerId == null)
                    {
                        Handle handle = handles.get(handleId);
                        if (handle != null)
                        {
                            // Use case: Load existing handleId with the existing configuration
                            previouslyExistingStartRequest = true;

                            startRequest = new StartRequest();
                            startRequest.setParameters(handle.getProperties());
                        }
                    }
                    if (startRequest == null)
                    {
                        // Use case: Create / replace existing handleId with provided configuration
                        startRequest = new StartRequest().parse(request);
                        startRequest.setSave(true);
                    }

                    startContainer(startRequest);

                    response.setContentType("text/plain");
                    response.setCharacterEncoding(CargoDaemonServlet.DAEMON_SERVLET_CHARSET);
                    response.getWriter().println("OK - STARTED");
                }
                catch (Throwable e)
                {
                    getServletContext().log("Cannot start server", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                }
                finally
                {
                    if (!previouslyExistingStartRequest && startRequest != null)
                    {
                        startRequest.cleanup();
                    }
                }
                break;

            case "stop":
                try
                {
                    boolean delete = Boolean.parseBoolean(request.getParameter("deleteContainer"));
                    String handleId = request.getParameter("handleId");

                    Handle handle = handles.get(handleId);

                    if (handle != null)
                    {
                        synchronized (handle)
                        {
                            InstalledLocalContainer container = handle.getContainer();

                            if (delete)
                            {
                                handles.remove(handleId);
                                fileManager.saveHandleDatabase(handles);
                            }

                            if (container != null)
                            {
                                container.stop();
                            }

                            handle.setForceStop(true);
                        }
                    }

                    response.setContentType("text/plain");
                    response.setCharacterEncoding(CargoDaemonServlet.DAEMON_SERVLET_CHARSET);
                    response.getWriter().println("OK - STOPPED");
                }
                catch (Throwable e)
                {
                    getServletContext().log("Cannot stop server", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                }
                break;

            case "viewlog":
            case "viewcargolog":
                try
                {
                    String handleId = request.getParameter("handleId");
                    Long offset = getLong(request.getParameter("offset"));
                    Handle handle = handles.get(handleId);
                    String logFilePath = null;
                    long filesize;
                    long pos = 0;

                    if (handle == null)
                    {
                        filesize = 0;
                    }
                    else
                    {
                        if ("viewlog".equals(servletPath))
                        {
                            logFilePath = handle.getContainerOutputPath();
                        }
                        else if ("viewcargolog".equals(servletPath))
                        {
                            logFilePath = handle.getContainerLogPath();
                        }
                        filesize = fileManager.getFileSize(logFilePath);
                    }

                    response.setContentType("text/plain");
                    response.setCharacterEncoding(CargoDaemonServlet.DAEMON_SERVLET_CHARSET);
                    response.setHeader("X-Text-Size", String.valueOf(filesize));

                    ServletOutputStream outputStream = response.getOutputStream();

                    // For some browsers, there needs to be at least 1024 bytes sent before
                    // something is displayed.
                    // So, we respond with a nice log header to make sure we reach this limit.
                    if (offset == null)
                    {
                        outputLogPageHeader(outputStream);
                        fileManager.copyHeader(
                            getClass().getClassLoader().getResourceAsStream(
                                "org/codehaus/cargo/daemon/logheader.txt"), outputStream);

                        outputStream.println("DATE " + new Date());
                        outputStream.println("");

                        outputStream.flush();
                    }

                    if (filesize > 0)
                    {
                        if (offset == null)
                        {
                            // For logs larger than 1MB, only start at the last 1MB
                            // if no offset is specified
                            if (filesize > 1048576)
                            {
                                pos = filesize - 1048576;
                            }
                        }
                        else
                        {
                            pos = offset;
                        }

                        fileManager.copy(logFilePath, outputStream, pos, filesize - pos);
                    }

                    if (offset == null)
                    {
                        outputLogPageFooter(outputStream, handleId, servletPath, filesize);
                    }
                }
                catch (Throwable e)
                {
                    getServletContext().log("Cannot view log for server", e);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
                }
                break;

            case "installed":
                String file = request.getParameter("file");
                response.setContentType("text/plain");
                response.setCharacterEncoding(CargoDaemonServlet.DAEMON_SERVLET_CHARSET);
                if (fileManager.existsFile(null, file))
                {
                    response.getWriter().println("OK - INSTALLED");
                }
                else
                {
                    response.getWriter().println("OK - NOTEXIST");
                }
                break;

            case "getHandles":
                response.setContentType("application/json");
                response.setCharacterEncoding(CargoDaemonServlet.DAEMON_SERVLET_CHARSET);
                response.getWriter().println(JSONValue.toJSONString(getHandleDetails()));
                break;

            case "index.html":
                String indexPage;
                try
                {
                    indexPage = generateIndexPage();
                }
                catch (Exception e)
                {
                    getServletContext().log("Cannot read index page", e);
                    throw new ServletException(e);
                }
                response.setContentType("text/html");
                response.setCharacterEncoding(CargoDaemonServlet.DAEMON_SERVLET_CHARSET);
                response.getWriter().print(indexPage);
                break;

            default:
                throw new ServletException("Unknown servlet path: " + servletPath);
        }
    }

    /**
     * Starts the container.
     * 
     * @param request Contains the information needed to start a container
     * @throws Throwable If exception happens.
     */
    private void startContainer(StartRequest request) throws Throwable
    {
        String handleId = request.getParameter("handleId", true);
        String containerId = request.getParameter("containerId", true);
        String containerHome = request.getParameter("containerHome", false);
        String installerZipUrl = request.getParameter("installerZipUrl", false);
        String installerZipFile = request.getParameter("installerZipFile", false);
        String configurationHome = request.getParameter("configurationHome", false);
        String configurationType = request.getParameter("configurationType", true);
        String containerOutputFile = request.getParameter("containerOutput", false);
        String containerLogFile = request.getParameter("containerLogFile", false);
        String containerLogLevel = request.getParameter("containerLogLevel", false);
        boolean containerAppend = "on".equals(request.getParameter("containerAppend", false));
        String autostart = request.getParameter("autostart", false);
        String timeout = request.getParameter("timeout", false);
        PropertyTable containerProperties = request.getProperties("containerProperties", false);
        PropertyTable configurationProperties =
            request.getProperties("configurationProperties", false);
        List<PropertyTable> configurationFileProperties =
            request.getPropertiesList("configurationFileProperties", false);
        List<String> configurationFiles = request.getStringList("configurationFiles", false);
        List<PropertyTable> deployableFiles = request.getPropertiesList("deployableFiles", false);
        List<PropertyTable> xmlReplacements = request.getPropertiesList("xmlReplacements", false);
        InputStream installerZipInputStream = request.getFile("installerZipFileData", false);
        List<String> extraFiles = request.getStringList("extraFiles", false);
        List<String> sharedFiles = request.getStringList("sharedFiles", false);
        List<String> extraClasspath = request.getStringList("extraClasspath", false);
        List<String> sharedClasspath = request.getStringList("sharedClasspath", false);
        List<String> additionalClasspath = request.getStringList("additionalClasspath", false);

        Handle handle;
        InstalledLocalContainer previousContainer = null;

        synchronized (handles)
        {
            handle = handles.get(handleId);

            if (handle == null)
            {
                handle = new Handle();
                handle.setId(handleId);
                handle.setForceStop(false);
                handles.put(handleId, handle);
            }
            else
            {
                previousContainer = handle.getContainer();
            }
        }

        synchronized (handle)
        {
            if (configurationHome == null || configurationHome.isEmpty())
            {
                configurationHome = fileManager.getConfigurationDirectory(handleId);
            }

            ConfigurationType parsedConfigurationType =
                ConfigurationType.toType(configurationType);
            LocalConfiguration configuration =
                (LocalConfiguration) CONFIGURATION_FACTORY.createConfiguration(containerId,
                    ContainerType.INSTALLED, parsedConfigurationType, configurationHome);

            // CARGO-1198: If we are saving a new container, delete the old workspace directory
            if (request.isSave())
            {
                fileManager.deleteWorkspaceDirectory(handleId);
            }

            for (Map.Entry<String, String> configurationProperty
                : configurationProperties.entrySet())
            {
                configuration.setProperty(
                    configurationProperty.getKey(), configurationProperty.getValue());
            }

            if (configuration instanceof StandaloneLocalConfiguration)
            {
                StandaloneLocalConfiguration standaloneConfiguration =
                    (StandaloneLocalConfiguration) configuration;
                for (PropertyTable xmlReplacement : xmlReplacements)
                {
                    String file = xmlReplacement.get("file", true);
                    String xpathExpression = xmlReplacement.get("xpathExpression", true);
                    String attributeName = xmlReplacement.get("attributeName", false);
                    if (attributeName != null && attributeName.isEmpty())
                    {
                        attributeName = null;
                    }
                    String value = xmlReplacement.get("value", true);

                    final XmlReplacement.ReplacementBehavior replacementBehavior;
                    String replacementBehaviorString =
                            xmlReplacement.get("replacementBehavior", false);
                    if (replacementBehaviorString == null)
                    {
                        replacementBehavior = XmlReplacement.ReplacementBehavior.THROW_EXCEPTION;
                    }
                    else
                    {
                        replacementBehavior = XmlReplacement.ReplacementBehavior.valueOf(
                                replacementBehaviorString);
                    }

                    XmlReplacement xmlReplacementObject = new XmlReplacement(
                        file, xpathExpression, attributeName, replacementBehavior,
                        value);
                    standaloneConfiguration.addXmlReplacement(xmlReplacementObject);
                }
            }

            InstalledLocalContainer container =
                (InstalledLocalContainer) CONTAINER_FACTORY.createContainer(containerId,
                    ContainerType.INSTALLED, configuration);

            additionalClasspath = setupAdditionalClasspath(additionalClasspath, handleId);

            if (timeout != null && !timeout.isEmpty())
            {
                container.setTimeout(Long.parseLong(timeout));
            }

            container.setHome(containerHome);
            container.setSystemProperties(containerProperties);

            if (containerLogFile == null || containerLogFile.isEmpty())
            {
                containerLogFile = "cargo.log";
            }
            containerLogFile = fileManager.getLogFile(handleId, containerLogFile);
            Logger logger = new FileLogger(containerLogFile, containerAppend);

            if (containerLogLevel != null && !containerLogLevel.isEmpty())
            {
                logger.setLevel(LogLevel.toLevel(containerLogLevel));
            }
            container.setLogger(logger);

            if (containerOutputFile == null || containerOutputFile.isEmpty())
            {
                containerOutputFile = "container.log";
            }
            containerOutputFile = fileManager.getLogFile(handleId, containerOutputFile);

            container.setOutput(containerOutputFile);
            container.setAppend(containerAppend);

            if (installerZipFile != null && installerZipInputStream != null)
            {
                fileManager.saveFile(installerZipFile, installerZipInputStream);
            }

            if (installerZipUrl != null || installerZipFile != null)
            {
                containerHome = installContainer(installerZipUrl, installerZipFile);
            }

            if (containerHome != null)
            {
                container.setHome(containerHome);
            }

            if (request.isSave())
            {
                saveConfigurationFiles(configurationFiles, handleId, request);
            }

            setupConfigurationFiles(handleId, configuration, configurationFileProperties, request);
            setupDeployableFiles(handleId, containerId, deployableFiles, configuration, request);
            if (container instanceof InstalledLocalContainer)
            {
                if (request.isSave())
                {
                    saveExtraFiles(extraFiles, handleId, request);
                    saveSharedFiles(sharedFiles, handleId, request);
                }
                setupExtraClasspath((InstalledLocalContainer) container, extraClasspath, handleId);
                setupSharedClasspath((InstalledLocalContainer) container, sharedClasspath,
                    handleId);
            }

            handle.setConfiguration(configuration);
            handle.setContainer(container);

            handle.setContainerOutputPath(containerOutputFile);
            handle.setContainerLogPath(containerLogFile);

            if (request.isSave())
            {
                handle.setAutostart("on".equals(autostart) || "true".equals(autostart));
                handle.addProperties(request.getParameters());

                fileManager.saveHandleDatabase(handles);
            }

            if (previousContainer != null)
            {
                try
                {
                    previousContainer.stop();

                    // Wait 5 seconds to allow sockets to close after forced kill
                    Thread.sleep(5000);
                }
                catch (Throwable ignored)
                {
                    // Ignored
                }
            }

            try
            {
                container.start();
            }
            catch (Throwable t)
            {
                try
                {
                    // Start failed, make sure container is stopped.
                    container.stop();
                }
                catch (Throwable ignored)
                {
                    // Ignored
                }

                throw t;
            }
        }
    }

    /**
     * Setup additional classpath.
     * 
     * @param additionalClasspath The additional classpath for the container.
     * @param handleId The handle id.
     * @return The resolved additional classpath.
     */
    private List<String> setupAdditionalClasspath(List<String> additionalClasspath,
        String handleId)
    {
        if (additionalClasspath == null || additionalClasspath.size() == 0)
        {
            return null;
        }

        List<String> result = new ArrayList<String>();

        for (String classpath : additionalClasspath)
        {
            result.add(fileManager.resolveConfigurationPath(handleId, classpath));
        }

        return result;
    }

    /**
     * Save configuration files to the workspace.
     * 
     * @param configurationFiles The configuration files.
     * @param handleId The handle id.
     * @param request The initial start request.
     * @throws IOException if error happens
     */
    private void saveConfigurationFiles(List<String> configurationFiles, String handleId,
        StartRequest request) throws IOException
    {
        int i = 0;

        if (configurationFiles == null || configurationFiles.size() == 0)
        {
            return;
        }

        for (String filename : configurationFiles)
        {
            InputStream inputStream = request.getFile("configurationFileData_" + i, true);
            fileManager.saveFile(handleId, filename, inputStream);
            i++;
        }
    }

    /**
     * Save shared classpath files to the workspace.
     * 
     * @param sharedFiles The shared classpath files.
     * @param handleId The handle id.
     * @param request The initial start request.
     * @throws IOException if error happens
     */
    private void saveSharedFiles(List<String> sharedFiles, String handleId, StartRequest request)
        throws IOException
    {
        int i = 0;

        if (sharedFiles == null || sharedFiles.size() == 0)
        {
            return;
        }

        for (String filename : sharedFiles)
        {
            InputStream inputStream = request.getFile("sharedFileData_" + i, true);
            fileManager.saveFile(handleId, filename, inputStream);
            i++;
        }
    }

    /**
     * Save extra classpath files to the workspace.
     * 
     * @param extraFiles The extra classpath files.
     * @param handleId The handle id.
     * @param request The initial start request.
     * @throws IOException if error happens
     */
    private void saveExtraFiles(List<String> extraFiles, String handleId, StartRequest request)
        throws IOException
    {
        int i = 0;

        if (extraFiles == null || extraFiles.size() == 0)
        {
            return;
        }

        for (String filename : extraFiles)
        {
            InputStream inputStream = request.getFile("extraFileData_" + i, true);
            fileManager.saveFile(handleId, filename, inputStream);
            i++;
        }
    }

    /**
     * Setup shared classpath.
     * 
     * @param container The container to start.
     * @param sharedClasspaths The shared classpaths.
     * @param handleId The handle id.
     */
    private void setupSharedClasspath(InstalledLocalContainer container,
        List<String> sharedClasspaths, String handleId)
    {
        if (sharedClasspaths == null || sharedClasspaths.size() == 0)
        {
            return;
        }

        String[] sharedClasspathsArray = new String[sharedClasspaths.size()];

        for (int i = 0; i < sharedClasspathsArray.length; i++)
        {
            sharedClasspathsArray[i] =
                fileManager.resolveWorkspacePath(handleId, sharedClasspaths.get(i));
        }

        container.setSharedClasspath(sharedClasspathsArray);
    }

    /**
     * Setup extra classpath.
     * 
     * @param container The container to start.
     * @param extraClasspaths The extra classpaths.
     * @param handleId The handle id.
     */
    private void setupExtraClasspath(InstalledLocalContainer container,
        List<String> extraClasspaths, String handleId)
    {
        if (extraClasspaths == null || extraClasspaths.size() == 0)
        {
            return;
        }

        String[] extraClasspathsArray = new String[extraClasspaths.size()];

        for (int i = 0; i < extraClasspathsArray.length; i++)
        {
            extraClasspathsArray[i] =
                fileManager.resolveWorkspacePath(handleId, extraClasspaths.get(i));
        }

        container.setExtraClasspath(extraClasspathsArray);
    }

    /**
     * Setup the configuration files.
     * 
     * @param handleId Unique handle identifier of the container.
     * @param configuration Reference to the cargo configuration.
     * @param configurationFiles List of properties for configuration files.
     * @param request The start request for a container.
     */
    private void setupConfigurationFiles(String handleId, LocalConfiguration configuration,
        List<PropertyTable> configurationFiles, StartRequest request)
    {
        for (PropertyTable properties : configurationFiles)
        {
            FileConfig fileConfig = new FileConfig();

            String file = properties.get("file", true);
            String toFile = properties.get("tofile", false);
            String toDirectory = properties.get("todir", false);
            String encoding = properties.get("encoding", false);
            boolean overwrite = properties.getBoolean("overwrite");
            boolean filter = properties.getBoolean("filter");

            fileConfig.setConfigfile(filter);
            fileConfig.setOverwrite(overwrite);

            if (encoding != null && !encoding.isEmpty())
            {
                fileConfig.setEncoding(encoding);
            }

            if (toFile != null && !toFile.isEmpty())
            {
                fileConfig.setToFile(toFile);
            }

            if (toDirectory != null && !toDirectory.isEmpty())
            {
                fileConfig.setToDir(toDirectory);
            }

            fileConfig.setFile(fileManager.resolveWorkspacePath(handleId, file));

            configuration.setFileProperty(fileConfig);
        }
    }

    /**
     * Setup the deployable files.
     * 
     * @param handleId Unique handle identifier of the container.
     * @param containerId The container identifier.
     * @param deployableFiles List of properties for deployable files.
     * @param configuration Reference to the configuration.
     * @param request The start request of the container.
     * @throws IOException if error happens
     */
    private void setupDeployableFiles(String handleId, String containerId,
        List<PropertyTable> deployableFiles, LocalConfiguration configuration,
        StartRequest request) throws IOException
    {
        int i = 0;

        for (PropertyTable properties : deployableFiles)
        {
            DeployableType deployableType = DeployableType.toType(properties.get("type"));
            String filename = properties.get("filename", true);

            String location;
            InputStream deployableFileData = request.getFile("deployableFileData_" + i, false);
            if (deployableFileData != null)
            {
                location = fileManager.saveFile(handleId, filename, deployableFileData);
            }
            else if (fileManager.existsFile(handleId, filename))
            {
                // for a restart request, reuse existing deployable files
                location = fileManager.resolveWorkspacePath(handleId, filename);
            }
            else
            {
                throw new CargoDaemonException("File parameter deployableFileData_" + i
                    + " for file \"" + filename + "\" on handle id " + handleId + " is required.");
            }

            Deployable deployable =
                DEPLOYABLE_FACTORY.createDeployable(containerId, location, deployableType);

            if (deployable instanceof WAR)
            {
                WAR war = (WAR) deployable;

                String context = properties.get("context", false);

                if (context != null && !context.isEmpty())
                {
                    war.setContext(context);
                }
            }

            configuration.addDeployable(deployable);

            i++;
        }
    }

    /**
     * Install the container based on a zip file or an URL to a zip file.
     * 
     * @param url The URL to a zip file
     * @param file Or the path to zip file
     * @return the container home path
     */
    private String installContainer(String url, String file)
    {
        try
        {
            Installer installer;
            URL installURL;

            if (file != null)
            {
                installURL = new URL(fileManager.getFileURL(file));
            }
            else
            {
                installURL = new URL(url);
            }

            installer =
                new ZipURLInstaller(installURL,
                    fileManager.getInstallDirectory(),
                    fileManager.getInstallDirectory());
            installer.install();

            return installer.getHome();
        }
        catch (MalformedURLException e)
        {
            throw new CargoDaemonException("Malformed URL " + e);
        }
    }

    /**
     * @return Details of current handles.
     */
    private Map<String, String> getHandleDetails()
    {
        Map<String, String> result = new TreeMap<String, String>();

        for (Map.Entry<String, Handle> entry : this.handles.entrySet())
        {
            result.put(entry.getKey(), entry.getValue().getContainerStatus().toString());
        }
        return result;
    }

    /**
     * Converts text to long if possible, otherwise returns 0
     * 
     * @param text The text to convert.
     * @return The converted long
     */
    private Long getLong(String text)
    {
        try
        {
            return Long.valueOf(text);
        }
        catch (Throwable e)
        {
            return null;
        }
    }

    /**
     * Background task to autostart containers if they are stopped.
     */
    @Override
    public void run()
    {
        for (Map.Entry<String, Handle> entry : this.handles.entrySet())
        {
            Handle handle = entry.getValue();

            if (handle == null)
            {
                continue;
            }

            synchronized (handle)
            {
                if (handle.isAutostart() && handle.getContainerStatus().isStopped()
                    && !handle.isForceStop())
                {
                    StartRequest startRequest = new StartRequest();

                    startRequest.setParameters(handle.getProperties());
                    try
                    {
                        startContainer(startRequest);
                    }
                    catch (Throwable e)
                    {
                        // Ignore
                    }
                }
            }
        }
    }


    /**
     * Prints the log page header to the servlet output stream.
     * 
     * @param outputStream The output stream
     * @throws Exception in case of error
     */
    private void outputLogPageHeader(ServletOutputStream outputStream) throws Exception
    {
        outputStream.print(""
                + "<!doctype html>\n"
                + "<html lang=\"en-US\">\n"
                + "  <head>\n"
                + "     <meta charset=\"utf-8\">\n"
                + "     <style>\n"
                + "       pre {\n"
                + "         margin: 0px;\n"
                + "       }\n"
                + "     </style>\n"
                + "    <title>Cargo Live Log Viewer</title>"
                + "  </head>\n"
                + "  <body>\n"
                + "  <div id=\"logText\"><pre>");
    }

    /**
     * Prints the log page footer to the servlet output stream.
     * 
     * @param outputStream The output stream
     * @param handleId The handle id
     * @param pageId The page id (ie, viewlog or viewcargolog)
     * @param pos The last offset in the log file
     * @throws Exception in case of error
     */
    private void outputLogPageFooter(ServletOutputStream outputStream, String handleId,
            String pageId, long pos) throws Exception
    {
        outputStream.print("</pre></div>\n"
                + "  <img alt=\"activity indicator\" src=\"spinner.gif\">\n"
                + "\n"
                + "    <script>\n"
                + "//<![CDATA[\n"
                + "\n"
                + "      var logText = document.getElementById('logText')\n"
                + "      var offset = " + pos + ";\n"
                + "      var handleId = \"" + handleId + "\";\n"
                + "      // See http://www.howtocreate.co.uk/tutorials/javascript/browserwindow\n"
                + "      function getViewportHeight()\n"
                + "      {\n"
                + "        if (typeof( window.innerWidth ) == 'number') {\n"
                + "          // Non-IE\n"
                + "          return window.innerHeight;\n"
                + "        }\n"
                + "        else if (document.documentElement && ( "
                + "document.documentElement.clientWidth "
                + "|| document.documentElement.clientHeight ))\n"
                + "        {\n"
                + "          // IE 6+ in 'standards compliant mode'\n"
                + "          return document.documentElement.clientHeight;\n"
                + "        }\n"
                + "        else if (document.body && ( document.body.clientWidth "
                + "|| document.body.clientHeight ))\n"
                + "        {\n"
                + "          // IE 4 compatible\n"
                + "          return document.body.clientHeight;\n"
                + "        }\n"
                + "        return null;\n"
                + "      }\n"
                + "      function getCurrentHeight(scrollDiv)\n"
                + "      {\n"
                + "        if (scrollDiv.scrollHeight > 0)\n"
                + "        {\n"
                + "           return scrollDiv.scrollHeight;\n"
                + "        }\n"
                + "        else if (scrollDiv.offsetHeight > 0)\n"
                + "        {\n"
                + "           return scrollDiv.offsetHeight;\n"
                + "        }\n"
                + "        return null;\n"
                + "      }\n"
                + "      function shouldAutoscroll(scrollDiv)\n"
                + "      {\n"
                + "         var bottomThreshold = 25;\n"
                + "         var currentHeight = getCurrentHeight(scrollDiv);\n"
                + "         var height = getViewportHeight();\n"
                + "         var scrollPos = Math.max(scrollDiv.scrollTop, "
                + "document.documentElement.scrollTop, "
                + "document.body.scrollTop);\n"
                + "         var diff = currentHeight - scrollPos - height;\n"
                + "         return diff < bottomThreshold;\n"
                + "      }\n"
                + "      function scrollToBottom(scrollDiv)\n"
                + "      {\n"
                + "        var currentHeight = getCurrentHeight(scrollDiv);\n"
                + "        if (document.documentElement)\n"
                + "        {\n"
                + "           document.documentElement.scrollTop = currentHeight;\n"
                + "        }\n"
                + "        if (document.body)\n"
                + "        {\n"
                + "           document.body.scrollTop = currentHeight;\n"
                + "        }\n"
                + "        scrollDiv.scrollTop = currentHeight;\n"
                + "      }\n"
                + "      var xmlHttpRequest = false;\n"
                + "      if (window.XMLHttpRequest)\n"
                + "      {\n"
                + "        xmlHttpRequest = new XMLHttpRequest();\n"
                + "      }\n"
                + "      else if (window.ActiveXObject)\n"
                + "      {\n"
                + "        xmlHttpRequest = new ActiveXObject(\"Microsoft.XMLHTTP\");\n"
                + "      }\n"
                + "      function getMoreLog()\n"
                + "      {\n"
                + "        if (isNaN(offset))\n"
                + "        {\n"
                + "          offset = 0;\n"
                + "        }\n"
                + "        else\n"
                + "        {\n"
                + "          var currentDate = new Date();\n"
                + "          xmlHttpRequest.open(\"GET\", \"./" + pageId
                + "?handleId=\" + handleId + \"&offset=\" + offset + \"&now=\" + currentDate, "
                + "false);\n"
                + "          try\n"
                + "          {\n"
                + "            xmlHttpRequest.send();\n"
                + "            var response = xmlHttpRequest.responseText;\n"
                + "            if (response.length != 0)\n"
                + "            {\n"
                + "               var pre = document.createElement('pre');\n"
                + "               var doscroll = shouldAutoscroll(logText);\n"
                + "               pre.innerHTML = response;\n"
                + "               logText.appendChild(pre);\n"
                + "               if (doscroll) scrollToBottom(logText);\n"
                + "            }\n"
                + "            offset = parseInt(xmlHttpRequest.getResponseHeader"
                + "('X-Text-Size'));\n"
                + "          }\n"
                + "          catch (ignored)\n"
                + "          {\n"
                + "            // Ignored\n"
                + "          }\n"
                + "          setTimeout(getMoreLog, 1000);\n"
                + "        }\n"
                + "      }\n"
                + "      if (xmlHttpRequest)\n"
                + "      {\n"
                + "        setTimeout(getMoreLog, 1000);\n"
                + "      }\n"
                + "//]]>\n"
                + "    </script>\n"
                + "  </body>\n"
                + "</html>\n");
    }
}
