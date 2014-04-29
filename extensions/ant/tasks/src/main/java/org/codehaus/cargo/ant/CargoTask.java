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
package org.codehaus.cargo.ant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Environment.Variable;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.container.spi.util.ContainerUtils;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.generic.deployer.DeployerFactory;
import org.codehaus.cargo.tools.daemon.DaemonClient;
import org.codehaus.cargo.tools.daemon.DaemonPropertySet;
import org.codehaus.cargo.tools.daemon.DaemonStart;
import org.codehaus.cargo.util.log.AntLogger;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;

/**
 * Ant task wrapper around the Cargo API to start/stop containers. This task is generic and can be
 * used with any container implementation that implements the {@link Container} interface (either
 * the implementations provided by Cargo or your own ones).
 * 
 * @version $Id$
 */
public class CargoTask extends Task
{
    /**
     * Represents a configure container action.
     * @see #setAction(String)
     */
    private static final String ACTION_CONFIGURE = "configure";

    /**
     * Represents a start container action.
     * @see #setAction(String)
     */
    private static final String ACTION_START = "start";

    /**
     * Represents a restart container action.
     * @see #setAction(String)
     */
    private static final String ACTION_RESTART = "restart";

    /**
     * Represents a run container action.
     * @see #setAction(String)
     */
    private static final String ACTION_RUN = "run";

    /**
     * Represents a stop container action.
     * @see #setAction(String)
     */
    private static final String ACTION_STOP = "stop";

    /**
     * The prefix for all daemon actions.
     * @see #setAction(String)
     */
    private static final String ACTIONS_DAEMON_PREFIX = "daemon-";

    /**
     * Represents a start container action via the Cargo Daemon.
     * @see #setAction(String)
     */
    private static final String ACTION_DAEMON_START =
        CargoTask.ACTIONS_DAEMON_PREFIX + CargoTask.ACTION_START;

    /**
     * Represents a stop container action via the Cargo Daemon.
     * @see #setAction(String)
     */
    private static final String ACTION_DAEMON_STOP =
        CargoTask.ACTIONS_DAEMON_PREFIX + CargoTask.ACTION_STOP;

    /**
     * All actions for local containers.
     * @see #setAction(String)
     */
    private static final List<String> LOCAL_ACTIONS = Arrays.asList(new String[] {
        ACTION_START, ACTION_RESTART, ACTION_RUN, ACTION_STOP, ACTION_CONFIGURE,
        ACTION_DAEMON_START, ACTION_DAEMON_STOP
    });

    /**
     * Represents a deploy to container action.
     * @see #setAction(String)
     */
    private static final String ACTION_DEPLOY = "deploy";

    /**
     * Represents an undeploy from container action.
     * @see #setAction(String)
     */
    private static final String ACTION_UNDEPLOY = "undeploy";

    /**
     * Represents a redeploy to container action.
     * @see #setAction(String)
     */
    private static final String ACTION_REDEPLOY = "redeploy";

    /**
     * All actions for deployers (local or remote).
     * @see #setAction(String)
     */
    private static final List<String> DEPLOYER_ACTIONS = Arrays.asList(new String[] {
        ACTION_DEPLOY, ACTION_UNDEPLOY, ACTION_REDEPLOY
    });

    /**
     * The action that will be executed by this task.
     * @see #setAction(String)
     */
    private String action;

    /**
     * If specified, the task will create an Ant property pointing to the container instance created
     * by the task.
     */
    private String id;

    /**
     * If specified, allows to reuse a previously created container instance.
     */
    private Reference refid;

    /**
     * An id representing the container (unique per container type).
     */
    private String containerId;

    /**
     * The container's type (e.g. "installed", "embedded" and "remote").
     */
    private ContainerType containerType = ContainerType.INSTALLED;

    /**
     * List of system properties to set in the container JVM.
     */
    private Map<String, String> systemProperties = new HashMap<String, String>();

    /**
     * System properties loaded from file.
     */
    private File systemPropertiesFile;

    /**
     * Additional classpath entries for the classpath that will be used to start the containers.
     */
    private Path extraClasspath;

    /**
     * Classpath entries for the classpath that will be shared the applications deployed in a
     * container.
     */
    private Path sharedClasspath;

    /**
     * The file to which output of the container should be written. If not specified the output will
     * be logged in Cargo's log file.
     */
    private String output;

    /**
     * Cargo log file. If no file is specified the output will be logged on stdout.
     */
    private File log;

    /**
     * Logging level. Valid values are "warn", "info" and "debug". A logging level of warn only logs
     * warnings. A level of info logs both warnings and info messages. A level of debug logs all 3
     * levels of messages.
     */
    private LogLevel logLevel;

    /**
     * Whether output of the container should be appended to an existing file, or the existing file
     * should be truncated.
     */
    private boolean append;

    /**
     * The container home.
     */
    private String home;

    /**
     * Timeout for starting/stopping the container.
     */
    private long timeout;

    /**
     * ZipURLInstaller configuration (if defined by the user).
     */
    private ZipURLInstallerElement zipURLInstallerElement;

    /**
     * Configuration (if defined by the user). If not specified we'll use a default standalone
     * configuration.
     */
    private ConfigurationElement configurationElement;

    /**
     * Daemon configuration (if defined by the user).
     */
    private DaemonElement daemonElement;

    /**
     * Factory to create container instances from a container id.
     */
    private ContainerFactory containerFactory = new DefaultContainerFactory();

    /**
     * Factory to create deployer instances from a container id.
     */
    private DeployerFactory deployerFactory = new DefaultDeployerFactory();

    /**
     * Custom container implementation class to use.
     */
    private Class containerClass;

    /**
     * The container instance created by the {@link #execute()} call.
     */
    private Container container;

    /**
     * the logger to use to log outputs from the execution of the Ant task.
     */
    private Logger logger;

    /**
     * @param containerClass the container implementation class to use
     */
    public void setClass(Class containerClass)
    {
        this.containerClass = containerClass;
    }

    /**
     * {@inheritDoc}
     * @see #setClass(Class)
     */
    protected final Class getContainerClass()
    {
        return this.containerClass;
    }

    /**
     * Sets the action to execute ("start", "stop", etc.).
     * 
     * @param action the action that will be executed by this task
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    /**
     * @param containerId the container id
     */
    public void setContainerId(String containerId)
    {
        this.containerId = containerId;
    }

    /**
     * @param type the container's type (e.g. "installed", "embedded" or "remote")
     */
    public void setType(ContainerType type)
    {
        this.containerType = type;
    }

    /**
     * Sets the home dir.
     * @param home home dir to set.
     */
    public void setHome(String home)
    {
        this.home = home;
    }

    /**
     * @param id the Ant property name that will contain the container instance reference
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @param refid the Ant property name that contains an existing container instance reference
     */
    public void setRefId(Reference refid)
    {
        this.refid = refid;
    }

    /**
     * @return the configured {@link ZipURLInstallerElement} element
     */
    public ZipURLInstallerElement createZipURLInstaller()
    {
        if (getZipURLInstaller() == null)
        {
            this.zipURLInstallerElement = new ZipURLInstallerElement();
        }

        return this.zipURLInstallerElement;
    }

    /**
     * @return the configured {@link ConfigurationElement} element
     */
    public ConfigurationElement createConfiguration()
    {
        if (getConfiguration() == null)
        {
            this.configurationElement = new ConfigurationElement();
        }

        return this.configurationElement;
    }

    /**
     * @return the configured {@link DaemonElement} element
     */
    public DaemonElement createDaemon()
    {
        if (getDaemon() == null)
        {
            this.daemonElement = new DaemonElement();
        }

        return this.daemonElement;
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#setOutput(String)
     */
    public void setOutput(String output)
    {
        this.output = output;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.util.log.LoggedObject#setLogger(Logger)
     */
    public void setLog(File log)
    {
        this.log = log;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.util.internal.log.AbstractLogger#setLevel(org.codehaus.cargo.util.log.LogLevel)
     */
    public void setLogLevel(String logLevel)
    {
        this.logLevel = LogLevel.toLevel(logLevel);
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#setAppend(boolean)
     */
    public void setAppend(boolean isAppend)
    {
        this.append = isAppend;
    }

    /**
     * {@inheritDoc}
     * @see LocalContainer#setTimeout(long)
     */
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    /**
     * Adds extra classpath that will be used for starting the container.
     * 
     * @return reference to the classpath
     */
    public Path createExtraClasspath()
    {
        if (getExtraClasspath() == null)
        {
            this.extraClasspath = new Path(getProject());
        }

        return this.extraClasspath.createPath();
    }

    /**
     * Adds shared classpath that will be shared by container applications.
     * 
     * @return reference to the classpath
     */
    public Path createSharedClasspath()
    {
        if (getSharedClasspath() == null)
        {
            this.sharedClasspath = new Path(getProject());
        }

        return this.sharedClasspath.createPath();
    }

    /**
     * Adds a system property that will be set up in the executing container VM.
     * 
     * @param property the system property to add
     */
    public void addSysproperty(Environment.Variable property)
    {
        getSystemProperties().put(property.getKey(), property.getValue());
    }

    /**
     * @param systemPropertiesFile System properties loaded from file.
     */
    public void setSystemPropertiesFile(File systemPropertiesFile)
    {
        this.systemPropertiesFile = systemPropertiesFile;
    }

    /**
     * Adds a set of properties that will be used as system properties in the executing container
     * VM.
     * 
     * Note: When we switch to Ant 1.6 we will be able to replace this by Ant 1.6 PropertySet
     * 
     * @param propertySet Ant element defining the property set
     */
    public void addConfiguredSyspropertyset(PropertySet propertySet)
    {
        ResourceBundle bundle = propertySet.readProperties();
        Enumeration keys = bundle.getKeys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            Variable var = new Variable();
            var.setKey(key);
            var.setValue(bundle.getString(key));
            addSysproperty(var);
        }
    }

    /**
     * Called by Ant when the Variable object has been properly initialized.
     * 
     * @param property the system property to set
     */
    public void addConfiguredSysproperty(Environment.Variable property)
    {
        addSysproperty(property);
    }

    /**
     * Create the Cargo logger that will be used for logging all messages. If the user has specified
     * a log file we create a File logger. If no file has been specified we use an Ant logger by
     * default to log to the Ant logging subsystem.
     */
    private void createCargoLogger()
    {
        if (getLog() != null)
        {
            this.logger = new FileLogger(getLog(), true);
        }
        else
        {
            // Use an Ant logger adapter to log to Ant
            this.logger = new AntLogger(getProject());
        }

        if (getLogLevel() != null)
        {
            this.logger.setLevel(getLogLevel());
        }
        else
        {
            this.logger.setLevel(LogLevel.INFO);
        }
    }

    /**
     * @see Task#execute()
     */
    @Override
    public void execute()
    {
        this.container = makeContainer();

        // Verify that the task is correctly set up.
        verify();

        // Setup all attributes and nested elements
        setupLogger();

        if (getContainer().getType().isLocal())
        {
            setupOutput();
            setupTimeout();

            if (getContainer().getType() == ContainerType.INSTALLED)
            {
                setupHome();
                setupExtraClasspath();
                setupSharedClasspath();
                setupSystemProperties();
            }
        }

        // Save the reference id if specified
        if (getId() != null)
        {
            getProject().addReference(getId(), getContainer());
        }

        executeActions();
    }

    /**
     * Execute the action specified by the user.
     */
    protected void executeActions()
    {
        if (getAction() == null)
        {
            // Nothing to execute...
            return;
        }

        if (LOCAL_ACTIONS.contains(getAction()))
        {
            final LocalContainer localContainer = (LocalContainer) getContainer();

            if (getAction().startsWith(ACTIONS_DAEMON_PREFIX))
            {
                if (daemonElement == null)
                {
                    throw new BuildException("Daemon configuration is empty.");
                }

                String daemonURLString = daemonElement.getProperty(DaemonPropertySet.URL);
                String daemonUsername = daemonElement.getProperty(DaemonPropertySet.USERNAME);
                String daemonPassword = daemonElement.getProperty(DaemonPropertySet.PASSWORD);
                String daemonHandleId = daemonElement.getProperty(DaemonPropertySet.HANDLE);
                boolean daemonAutostartContainer =
                    Boolean.valueOf(daemonElement.getProperty(DaemonPropertySet.AUTOSTART));

                if (daemonURLString == null || daemonURLString.length() == 0)
                {
                    throw new BuildException("Missing daemon URL property.");
                }
                URL daemonURL;
                try
                {
                    daemonURL = new URL(daemonURLString);
                }
                catch (MalformedURLException e)
                {
                    throw new BuildException("Invalid daemon URL: " + e, e);
                }

                if (daemonHandleId == null || daemonHandleId.length() == 0)
                {
                    throw new BuildException("Missing daemon handle id property.");
                }

                DaemonClient daemon;
                if (daemonUsername != null && daemonUsername.length() > 0
                    && daemonPassword != null && daemonPassword.length() > 0)
                {
                    daemon = new DaemonClient(daemonURL, daemonUsername, daemonPassword);
                }
                else if (daemonUsername != null && daemonUsername.length() > 0)
                {
                    daemon = new DaemonClient(daemonURL, daemonUsername);
                }
                else
                {
                    daemon = new DaemonClient(daemonURL);
                }

                if (ACTION_DAEMON_START.equalsIgnoreCase(getAction()))
                {
                    final InstalledLocalContainer installedLocalContainer =
                        (InstalledLocalContainer) localContainer;

                    DaemonStart start = new DaemonStart();
                    start.setAdditionalClasspathEntries(daemonElement.getClasspaths());
                    start.setAutostart(daemonAutostartContainer);
                    start.setContainer(installedLocalContainer);
                    start.setDeployables(localContainer.getConfiguration().getDeployables());
                    start.setHandleId(daemonHandleId);
                    if (getZipURLInstaller() != null)
                    {
                        ZipURLInstaller installer = getZipURLInstaller().createInstaller();
                        installer.setLogger(getContainer().getLogger());
                        if (!installer.isAlreadyDownloaded())
                        {
                            installer.download();
                        }
                        start.setInstallerZipFile(
                            zipURLInstallerElement.createInstaller().getDownloadFile());
                    }
                    if (getLog() != null)
                    {
                        start.setLogFile(getLog().getName());
                    }
                    try
                    {
                        daemon.start(start);
                    }
                    catch (Exception e)
                    {
                        throw new BuildException("Cannot start the container via Daemon: " + e, e);
                    }
                }
                else if (ACTION_DAEMON_START.equalsIgnoreCase(getAction()))
                {
                    try
                    {
                        daemon.stop(daemonHandleId);
                    }
                    catch (Exception e)
                    {
                        throw new BuildException("Cannot stop the container via Daemon: " + e, e);
                    }
                }
                else
                {
                    throw new BuildException("Unknown daemon action [" + getAction()
                        + "] for local container");
                }
            }
            else if (ACTION_START.equalsIgnoreCase(getAction()))
            {
                localContainer.start();
            }
            else if (ACTION_RESTART.equalsIgnoreCase(getAction()))
            {
                localContainer.restart();
            }
            else if (ACTION_RUN.equalsIgnoreCase(getAction()))
            {
                // When Ctrl-C is pressed, stop the container
                Runtime.getRuntime().addShutdownHook(new Thread()
                {
                    @Override
                    public void run()
                    {
                        try 
                        {
                            if (org.codehaus.cargo.container.State.STARTED
                                == localContainer.getState()
                                ||
                                org.codehaus.cargo.container.State.STARTING
                                == localContainer.getState())
                            {
                                localContainer.stop();
                            }
                        }
                        catch (Exception e)
                        {
                            CargoTask.this.log(
                                "Failed stopping the container", e, Project.MSG_WARN);
                        }
                    }
                });

                localContainer.start();

                log("Press Ctrl-C to stop the container...");
                ContainerUtils.waitTillContainerIsStopped(getContainer());
            }
            else if (ACTION_STOP.equalsIgnoreCase(getAction()))
            {
                localContainer.stop();
            }
            else if (ACTION_CONFIGURE.equalsIgnoreCase(getAction()))
            {
                localContainer.getConfiguration().configure(localContainer);
            }
            else
            {
                throw new BuildException("Unknown action [" + getAction()
                    + "] for local container");
            }
        }
        else
        {
            Deployer deployer = deployerFactory.createDeployer(getContainer());
            deployer.setLogger(getLogger());

            for (DeployableElement deployableElement : getConfiguration().getDeployables())
            {
                if (ACTION_UNDEPLOY.equalsIgnoreCase(getAction())
                    && deployableElement.getFile() == null)
                {
                    // CARGO-1173: The "file" attribute is optional for the undeploy action
                    deployableElement.setFile(".");
                }

                Deployable deployable = deployableElement.createDeployable(getContainerId());
                DeployableMonitor deployableMonitor = deployableElement.createDeployableMonitor();

                if (ACTION_DEPLOY.equalsIgnoreCase(getAction()))
                {
                    if (deployableMonitor == null)
                    {
                        deployer.deploy(deployable);
                    }
                    else
                    {
                        deployer.deploy(deployable, deployableMonitor);
                    }
                }
                else if (ACTION_UNDEPLOY.equalsIgnoreCase(getAction()))
                {
                    if (deployableMonitor == null)
                    {
                        deployer.undeploy(deployable);
                    }
                    else
                    {
                        deployer.undeploy(deployable, deployableMonitor);
                    }
                }
                else if (ACTION_REDEPLOY.equalsIgnoreCase(getAction()))
                {
                    if (deployableMonitor == null)
                    {
                        deployer.redeploy(deployable);
                    }
                    else
                    {
                        deployer.redeploy(deployable, deployableMonitor);
                    }
                }
                else
                {
                    throw new BuildException("Unknown action [" + getAction()
                        + "] for deployer");
                }
            }
        }
    }

    /**
     * Set up a logger for the container.
     */
    protected void setupLogger()
    {
        getContainer().setLogger(getLogger());

        if (getContainer().getType().isLocal())
        {
            ((LocalContainer) getContainer()).getConfiguration().setLogger(getLogger());
        }
        else
        {
            ((RemoteContainer) getContainer()).getConfiguration().setLogger(getLogger());
        }
    }

    /**
     * Set up an output file containing container's console output if defined.
     */
    protected void setupOutput()
    {
        if (getOutput() != null)
        {
            ((LocalContainer) getContainer()).setOutput(getOutput());
            ((LocalContainer) getContainer()).setAppend(isAppend());
        }
    }

    /**
     * Set up a timeout if defined.
     */
    protected void setupTimeout()
    {
        if (getTimeout() != 0)
        {
            if (getTimeout() != ((LocalContainer) getContainer()).getTimeout())
            {
                ((LocalContainer) getContainer()).setTimeout(getTimeout());
            }
        }
    }

    /**
     * Set up a home dir (possibly using a ZipURLInstaller).
     */
    protected void setupHome()
    {
        if (getHome() != null)
        {
            ((InstalledLocalContainer) getContainer()).setHome(getHome());
        }
        else if (getZipURLInstaller() != null)
        {
            ZipURLInstaller installer = getZipURLInstaller().createInstaller();
            installer.setLogger(getContainer().getLogger());
            installer.install();
            ((InstalledLocalContainer) getContainer()).setHome(installer.getHome());
        }
    }

    /**
     * Set up extra classpaths if defined.
     */
    protected void setupExtraClasspath()
    {
        if (getExtraClasspath() != null)
        {
            ((InstalledLocalContainer) getContainer()).setExtraClasspath(
                getExtraClasspath().list());
        }
    }

    /**
     * Set up shared classpath if defined.
     */
    protected void setupSharedClasspath()
    {
        if (getSharedClasspath() != null)
        {
            ((InstalledLocalContainer) getContainer()).setSharedClasspath(
                getSharedClasspath().list());
        }
    }

    /**
     * Set up system properties if defined.
     */
    protected void setupSystemProperties()
    {
        Map<String, String> systemProperties = new HashMap<String, String>();

        // Set container properties loaded from file (if any)
        if (getSystemPropertiesFile() != null)
        {
            Properties properties = new Properties();
            try
            {
                InputStream inputStream = new FileInputStream(getSystemPropertiesFile());
                try
                {
                    properties.load(new BufferedInputStream(inputStream));
                }
                finally
                {
                    inputStream.close();
                }
                for (Enumeration<?> propertyNames = properties.propertyNames();
                    propertyNames.hasMoreElements();)
                {
                    String propertyName = (String) propertyNames.nextElement();
                    String propertyValue = properties.getProperty(propertyName);
                    systemProperties.put(propertyName, propertyValue);
                }
            }
            catch (IOException e)
            {
                throw new BuildException("System property file ["
                    + getSystemPropertiesFile() + "] cannot be loaded", e);
            }
        }

        if (!getSystemProperties().isEmpty())
        {
            for (Map.Entry<String, String> systemProperty : getSystemProperties().entrySet())
            {
                systemProperties.put(systemProperty.getKey(), systemProperty.getValue());
            }
        }

        ((InstalledLocalContainer) getContainer()).setSystemProperties(systemProperties);
    }

    /**
     * {@inheritDoc}
     * @see #addSysproperty(Environment.Variable)
     */
    protected final Map<String, String> getSystemProperties()
    {
        return this.systemProperties;
    }

    /**
     * @return System properties loaded from file.
     */
    protected File getSystemPropertiesFile()
    {
        return systemPropertiesFile;
    }

    /**
     * {@inheritDoc}
     * @see #createZipURLInstaller()
     */
    protected final ZipURLInstallerElement getZipURLInstaller()
    {
        return this.zipURLInstallerElement;
    }

    /**
     * {@inheritDoc}
     * @see #createConfiguration()
     */
    protected final ConfigurationElement getConfiguration()
    {
        return this.configurationElement;
    }

    /**
     * {@inheritDoc}
     * @see #createDaemon()
     */
    protected final DaemonElement getDaemon()
    {
        return this.daemonElement;
    }

    /**
     * {@inheritDoc}
     * @see #createExtraClasspath()
     */
    protected final Path getExtraClasspath()
    {
        return this.extraClasspath;
    }

    /**
     * {@inheritDoc}
     * @see #createSharedClasspath()
     */
    protected final Path getSharedClasspath()
    {
        return this.sharedClasspath;
    }

    /**
     * {@inheritDoc}
     * @see #getTimeout()
     */
    protected final long getTimeout()
    {
        return this.timeout;
    }

    /**
     * {@inheritDoc}
     * @see #setOutput(String)
     */
    protected final String getOutput()
    {
        return this.output;
    }

    /**
     * {@inheritDoc}
     * @see #setLog(File)
     */
    protected final File getLog()
    {
        return this.log;
    }

    /**
     * {@inheritDoc}
     * @see #setLogLevel(String)
     */
    protected LogLevel getLogLevel()
    {
        return this.logLevel;
    }

    /**
     * {@inheritDoc}
     * @see #setHome(String)
     */
    protected final String getHome()
    {
        return this.home;
    }

    /**
     * {@inheritDoc}
     * @see #setAppend(boolean)
     */
    protected final boolean isAppend()
    {
        return this.append;
    }

    /**
     * @return the action to execute ("start" or "stop")
     */
    protected final String getAction()
    {
        return this.action;
    }

    /**
     * @return the container instance to start/stop
     */
    protected Container makeContainer()
    {
        Container container;
        if (getRefid() != null)
        {
            Object o = getContainerReference();
            if (!(o instanceof Container))
            {
                throw new BuildException("The [refid] attribute must point to a container "
                    + "reference, it is pointing to a [" + o.getClass().getName() + "] object");
            }
            container = (Container) o;

            // Use the logger defined in the container as the main Cargo logger
            this.logger = container.getLogger();
        }
        else
        {
            if (getConfiguration() == null)
            {
                throw new BuildException("Missing mandatory [configuration] element.");
            }

            if (getHome() != null)
            {
                setHome(calculateAbsoluteDirectory("container home", getHome()));
            }
            if (getZipURLInstaller() != null
                && getZipURLInstaller().getDownloadDir() != null)
            {
                getZipURLInstaller().setDownloadDir(
                    calculateAbsoluteDirectory("zip URL installer download",
                        getZipURLInstaller().getDownloadDir()));
            }
            if (getZipURLInstaller() != null
                && getZipURLInstaller().getExtractDir() != null)
            {
                getZipURLInstaller().setExtractDir(
                    calculateAbsoluteDirectory("zip URL installer extract",
                        getZipURLInstaller().getExtractDir()));
            }
            if (getConfiguration().getHome() != null)
            {
                getConfiguration().setHome(calculateAbsoluteDirectory("configuration home",
                    getConfiguration().getHome()));
            }

            // If the user has registered a custom container class, register it against the
            // container factory.
            if (getContainerClass() != null)
            {
                this.containerFactory.registerContainer(this.containerId, this.containerType,
                    getContainerClass());
            }

            container = this.containerFactory.createContainer(this.containerId, this.containerType,
                getConfiguration().createConfiguration(this.containerId, this.containerType));

            createCargoLogger();
        }

        return container;
    }

    /**
     * @return the container object refId
     */
    private Object getContainerReference()
    {
        Object o;
        try
        {
            o = getRefid().getReferencedObject(getProject());
        }
        catch (BuildException e)
        {
            throw new BuildException("The [" + getRefid().getRefId() + "] reference does not "
                + "exist. You must first define a Cargo container reference.", e);
        }
        return o;
    }

    /**
     * {@inheritDoc}
     * @see #setContainerId(String)
     */
    protected String getContainerId()
    {
        return this.containerId;
    }

    /**
     * {@inheritDoc}
     * @see #setId(String)
     */
    protected String getId()
    {
        return this.id;
    }

    /**
     * {@inheritDoc}
     * @see #setRefId(Reference)
     */
    protected Reference getRefid()
    {
        return this.refid;
    }

    /**
     * Checks if the task is correctly initialized and that the container is ready to be used.
     */
    private void verify()
    {
        if (getId() != null && getRefid() != null)
        {
            throw new BuildException("You must use either [id] or [refid] but not both");
        }

        if (getContainerId() == null && getRefid() == null)
        {
            throw new BuildException("You must specify a [containerId] attribute or use a [refid] "
                + "attribute");
        }

        if (getId() == null && getAction() == null)
        {
            throw new BuildException("You must specify an [action] attribute with values "
                + LOCAL_ACTIONS + " (for local containers) or " + DEPLOYER_ACTIONS
                + " (for local or remote container deployments)");
        }

        if (getId() == null)
        {
            if (!LOCAL_ACTIONS.contains(getAction()) && !DEPLOYER_ACTIONS.contains(getAction()))
            {
                throw new BuildException("Unknown action: " + DEPLOYER_ACTIONS);
            }

            if (!getContainer().getType().isLocal() && !DEPLOYER_ACTIONS.contains(getAction()))
            {
                throw new BuildException("Valid actions for remote containers are: "
                    + DEPLOYER_ACTIONS);
            }
        }

        if (getHome() == null && getZipURLInstaller() == null
            && getContainer().getType() == ContainerType.INSTALLED
            && ((InstalledLocalContainer) getContainer()).getHome() == null)
        {
            throw new BuildException("You must specify either a [home] attribute pointing"
                + " to the location where the " + getContainer().getName()
                + " is installed, or a nested [zipurlinstaller] element");
        }
    }

    /**
     * @param containerFactory the new container factory to use
     */
    public void setContainerFactory(ContainerFactory containerFactory)
    {
        this.containerFactory = containerFactory;
    }

    /**
     * @return the container instance created after the execution of {@link #execute()}
     */
    protected Container getContainer()
    {
        return this.container;
    }

    /**
     * @return the logger to use to log outputs from the execution of the Ant task
     */
    protected Logger getLogger()
    {
        return this.logger;
    }

    /**
     * Calculate the absolute directory for any given path. This method will also emit a warning if
     * the given path is not absolute.
     * @param type Directory type, for example <code>container home</code>.
     * @param directory Directory path.
     * @return Absolute directory path.
     */
    private String calculateAbsoluteDirectory(String type, String directory)
    {
        File directoryFile = new File(directory);
        if (!directoryFile.isAbsolute())
        {
            String absoluteDirectory = directoryFile.getAbsolutePath();
            if (getLogger() != null)
            {
                getLogger().warn("The provided " + type + " directory [" + directory
                    + "] is not an absolute directory. Replacing it with its absolute directory "
                        + "counterpart, i.e. [" + absoluteDirectory + "] To avoid this message in "
                            + "the future, you can also use the ${basedir} variable in your "
                                + "paths.", this.getClass().getName());
            }
            return absoluteDirectory;
        }
        else
        {
            return directory;
        }
    }
}
