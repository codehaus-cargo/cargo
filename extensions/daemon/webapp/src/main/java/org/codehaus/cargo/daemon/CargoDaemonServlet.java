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
package org.codehaus.cargo.daemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.State;
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
import org.codehaus.cargo.daemon.jvm.DaemonJvmLauncherFactory;
import org.codehaus.cargo.daemon.properties.PropertyTable;
import org.codehaus.cargo.daemon.request.StartRequest;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployable.DeployableFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * Cargo daemon servlet.
 * 
 * @version $Id$
 */
public class CargoDaemonServlet extends HttpServlet implements Runnable
{

    /**
     * The amount of milliseconds to wait between immediately stopping and restarting a container.
     */
    private static final int STOPSTARTTIMEOUT = 500;

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
     * Default index page.
     */
    private String indexPage;

    /**
     * Constructor of the cargo daemon servlet.
     */
    public CargoDaemonServlet()
    {
        // Start background task for restarting webapps.
        scheduledExecutor.scheduleAtFixedRate(this, INITIALAUTOSTARTTIMEOUT, AUTOSTARTTIMEOUT,
            TimeUnit.SECONDS);

        try
        {
            loadHandleDatabase();
        }
        catch (IOException e)
        {
            // Ignore, we'll try again later
        }
    }

    /**
     * Loads the handle database from disk.
     * 
     * @throws IOException if error occurs
     */
    private synchronized void loadHandleDatabase() throws IOException
    {
        if (handles == null)
        {
            handles = fileManager.loadHandleDatabase();
        }
    }

    /**
     * Read the index page.
     * 
     * @throws Exception If exception happens
     */
    private void readIndexPage() throws Exception
    {
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

        Map<String, String> replacements = new HashMap<String, String>();

        replacements.put("containerIds", JSONArray
            .toJSONString(new ArrayList<String>(new TreeSet<String>(CONTAINER_FACTORY
                .getContainerIds().keySet()))));
        replacements.put("deployableTypes", JSONArray.toJSONString(deployableTypes));

        replacements.put("handles", JSONValue.toJSONString(getHandleDetails()));

        StringBuilder indexPageBuilder = new StringBuilder();

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(this.getServletContext()
                .getResourceAsStream("/index.html")));
        try
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
        finally
        {
            reader.close();
            reader = null;
            System.gc();
        }

        this.indexPage = indexPageBuilder.toString();
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        String servletPath = request.getServletPath();
        servletPath = servletPath.substring(servletPath.lastIndexOf('/') + 1);

        if ("start".equals(servletPath))
        {

            StartRequest startRequest = null;
            try
            {
                startRequest = new StartRequest().parse(request);
                startRequest.setSave(true);

                startContainer(startRequest);

                response.setContentType("text/plain");
                response.getWriter().println("OK - STARTED");
            }
            catch (Throwable e)
            {
                getServletContext().log("Cannot start server", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            }
            finally
            {
                if (startRequest != null)
                {
                    startRequest.cleanup();
                }
            }
        }
        else if ("restart".equals(servletPath))
        {
            try
            {
                String handleId = request.getParameter("handleId");
                StartRequest startRequest = new StartRequest();

                Handle handle = handles.get(handleId);
                startRequest.setParameters(handle.getProperties());
                startContainer(startRequest);

                response.setContentType("text/plain");
                response.getWriter().println("OK - STARTED");
            }
            catch (Throwable e)
            {
                getServletContext().log("Cannot start server", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            }
        }
        else if ("stop".equals(servletPath))
        {
            try
            {
                boolean delete = Boolean.valueOf(request.getParameter("deleteContainer"));
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
                response.getWriter().println("OK - STOPPED");
            }
            catch (Throwable e)
            {
                getServletContext().log("Cannot stop server", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            }
        }
        else if ("viewlog".equals(servletPath))
        {
            try
            {
                String handleId = request.getParameter("handleId");
                Handle handle = handles.get(handleId);

                if (handle == null)
                {
                    throw new CargoDaemonException("Handle id " + handleId + " not found.");
                }

                String logFilePath = handle.getLogPath();

                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
                ServletOutputStream outputStream = response.getOutputStream();

                // For some browsers, there needs to be atleast 1024 bytes sent before something is
                // displayed.
                // So, we respond with a nice log header to make sure we reach this limit.
                fileManager.copyHeader(
                    getClass().getClassLoader().getResourceAsStream(
                        "org/codehaus/cargo/daemon/logheader.txt"), outputStream);

                outputStream.println("DATE " + new Date());
                outputStream.println("");

                outputStream.flush();

                if (logFilePath == null || logFilePath.length() == 0)
                {
                    outputStream.println("");
                }
                else
                {
                    fileManager.copyContinuous(logFilePath, outputStream);
                }
            }
            catch (Throwable e)
            {
                getServletContext().log("Cannot view log for server", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            }
        }
        else if ("installed".equals(servletPath))
        {
            String file = request.getParameter("file");

            response.setContentType("text/plain");
            if (fileManager.existsFile(file))
            {
                response.getWriter().println("OK - INSTALLED");
            }
            else
            {
                response.getWriter().println("OK - NOTEXIST");
            }
        }
        else if ("getHandles".equals(servletPath))
        {
            response.setContentType("text/plain");
            response.getWriter().println(JSONValue.toJSONString(getHandleDetails()));
        }
        else if ("index.html".equals(servletPath))
        {
            try
            {
                readIndexPage();
            }
            catch (Exception e)
            {
                getServletContext().log("Cannot read index page", e);
                throw new ServletException(e);
            }
            response.getWriter().print(this.indexPage);
        }
        else
        {
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
        String containerOutput = request.getParameter("containerOutput", false);
        String containerAppend = request.getParameter("containerAppend", false);
        String autostart = request.getParameter("autostart", false);
        String timeout = request.getParameter("timeout", false);
        PropertyTable containerProperties = request.getProperties("containerProperties", false);
        PropertyTable configurationProperties =
            request.getProperties("configurationProperties", false);
        List<PropertyTable> configurationFileProperties =
            request.getPropertiesList("configurationFileProperties", false);
        List<String> configurationFiles = request.getStringList("configurationFiles", false);
        List<PropertyTable> deployableFiles = request.getPropertiesList("deployableFiles", false);
        InputStream installerZipInputStream = request.getFile("installerZipFileData", false);
        List<String> extraFiles = request.getStringList("extraFiles", false);
        List<String> sharedFiles = request.getStringList("sharedFiles", false);
        List<String> extraClasspath = request.getStringList("extraClasspath", false);
        List<String> sharedClasspath = request.getStringList("sharedClasspath", false);
        List<String> additionalClasspath = request.getStringList("additionalClasspath", false);

        Handle handle = handles.get(handleId);

        if (handle == null)
        {
            handle = new Handle();
            Handle previousHandle = handles.putIfAbsent(handleId, handle);

            if (previousHandle != null)
            {
                handle = previousHandle;
            }

            handle.setId(handleId);
            handle.setForceStop(false);
        }

        synchronized (handle)
        {
            InstalledLocalContainer container = handle.getContainer();

            if (container != null)
            {
                container.stop();

                // Wait for a small moment
                Thread.sleep(STOPSTARTTIMEOUT);
            }

            if (configurationHome == null || configurationHome.length() == 0)
            {
                configurationHome = fileManager.getConfigurationDirectory(handleId);
            }

            ConfigurationType parsedConfigurationType =
                ConfigurationType.toType(configurationType);
            LocalConfiguration configuration =
                (LocalConfiguration) CONFIGURATION_FACTORY.createConfiguration(containerId,
                    ContainerType.INSTALLED, parsedConfigurationType, configurationHome);

            configuration.getProperties().putAll(configurationProperties);

            container =
                (InstalledLocalContainer) CONTAINER_FACTORY.createContainer(containerId,
                    ContainerType.INSTALLED, configuration);

            additionalClasspath = setupAdditionalClasspath(additionalClasspath, handleId);

            container.setJvmLauncherFactory(new DaemonJvmLauncherFactory(additionalClasspath));

            if (timeout != null && timeout.length() > 0)
            {
                container.setTimeout(Long.parseLong(timeout));
            }

            container.setHome(containerHome);
            container.setSystemProperties(containerProperties);
            if (containerOutput != null && containerOutput.length() > 0)
            {
                container.setOutput(fileManager.getLogFile(handleId, containerOutput));
                container.setAppend("on".equals(containerAppend));
            }
            else
            {
                container.setOutput(fileManager.getLogFile(handleId, "cargo.log"));
                container.setAppend(false);
            }

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

            if (configuration instanceof StandaloneLocalConfiguration)
            {
                if (request.isSave())
                {
                    saveConfigurationFiles(configurationFiles, handleId, request);
                }

                setupConfigurationFiles(handleId, (StandaloneLocalConfiguration) configuration,
                    configurationFileProperties, request);
                setupDeployableFiles(handleId, containerId, deployableFiles, configuration,
                    request);
            }
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

            try
            {
                container.start();
            }
            catch (Throwable t)
            {
                try
                {
                    // Make sure container is stopped.
                    container.stop();
                }
                catch (Throwable ignored)
                {
                    // Ignored
                }

                throw t;
            }

            handle.setConfiguration(configuration);
            handle.setContainer(container);

            handle.setLogPath(container.getOutput());

            if (request.isSave())
            {
                handle.setAutostart(Boolean.valueOf(autostart));
                handle.addProperties(request.getParameters());

                fileManager.saveHandleDatabase(handles);
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
     */
    private void saveConfigurationFiles(List<String> configurationFiles, String handleId,
        StartRequest request)
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
     */
    private void saveSharedFiles(List<String> sharedFiles, String handleId, StartRequest request)
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
     */
    private void saveExtraFiles(List<String> extraFiles, String handleId, StartRequest request)
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
    private void setupConfigurationFiles(String handleId,
        StandaloneLocalConfiguration configuration, List<PropertyTable> configurationFiles,
        StartRequest request)
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

            if (encoding != null && encoding.length() != 0)
            {
                fileConfig.setEncoding(encoding);
            }

            if (toFile != null && toFile.length() != 0)
            {
                fileConfig.setToFile(toFile);
            }
            
            if (toDirectory != null && toDirectory.length() != 0)
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
     */
    private void setupDeployableFiles(String handleId, String containerId,
        List<PropertyTable> deployableFiles, LocalConfiguration configuration,
        StartRequest request)
    {
        int i = 0;

        for (PropertyTable properties : deployableFiles)
        {
            DeployableType deployableType = DeployableType.toType(properties.get("type"));
            String filename = properties.get("filename", true);

            String location =
                fileManager.saveFile(handleId, filename,
                    request.getFile("deployableFileData_" + i, false));

            Deployable deployable =
                DEPLOYABLE_FACTORY.createDeployable(containerId, location, deployableType);

            if (deployable instanceof WAR)
            {
                WAR war = (WAR) deployable;

                String context = properties.get("context", false);

                if (context != null && context.length() > 0)
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
     * Background task to autostart containers if they are stopped.
     */
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
                if (handle.isAutostart() && handle.getContainerStatus() == State.STOPPED
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
}
