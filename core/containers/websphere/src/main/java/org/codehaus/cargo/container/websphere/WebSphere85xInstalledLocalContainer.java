/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2017 Ali Tokmen.
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
package org.codehaus.cargo.container.websphere;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.ScriptingCapableContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.internal.util.ComplexPropertyUtils;
import org.codehaus.cargo.container.internal.util.JdkUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.startup.ContainerMonitor;
import org.codehaus.cargo.container.websphere.internal.ConsoleUrlWebSphereMonitor;
import org.codehaus.cargo.container.websphere.internal.ProcessExecutor;
import org.codehaus.cargo.container.websphere.util.ByteUnit;
import org.codehaus.cargo.container.websphere.util.JvmArguments;
import org.codehaus.cargo.util.CargoException;

/**
 * WebSphere 8.5.x container implementation.
 */
public class WebSphere85xInstalledLocalContainer extends AbstractInstalledLocalContainer
    implements ScriptingCapableContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "websphere85x";

    /**
     * Container name (human-readable name).
     */
    private static final String NAME = "WebSphere 8.5.x";

    /**
     * Windows command suffix.
     */
    private static final String WINDOWS_SUFFIX = ".bat";

    /**
     * Linux command suffix.
     */
    private static final String LINUX_SUFFIX = ".sh";

    /**
     * Capabilities.
     */
    private ContainerCapability capability = new J2EEContainerCapability();

    /**
     * Process executor.
     */
    private ProcessExecutor processExecutor;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public WebSphere85xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#start()} to all
     * container extending this class must implement.
     * 
     * @param java the predefined JVM launcher to use to start the container
     * @throws Exception if any error is raised during the container start
     */
    @Override
    public void doStart(JvmLauncher java) throws Exception
    {
        getLogger().info("Starting WebSphere.", this.getClass().getName());
        List<String> arguments = new ArrayList<String>();
        arguments.add(getConfiguration().getPropertyValue(WebSpherePropertySet.SERVER));
        arguments.add("-profileName");
        arguments.add(getConfiguration().getPropertyValue(WebSpherePropertySet.PROFILE));
        runStartServerCommand(arguments.toArray(new String[arguments.size()]));

        // Register shutdown hook for case if Cargo java process terminates prematurely.
        // Needed because WebSphere runs in separate process.
        boolean spawnProcess = Boolean.parseBoolean(getConfiguration()
                .getPropertyValue(GeneralPropertySet.SPAWN_PROCESS));
        if (!spawnProcess)
        {
            final LocalContainer localContainer = this;
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
                        throw new CargoException("Failed stopping the container.", e);
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected void executePostStartTasks() throws Exception
    {
        List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();
        WebSphereConfiguration configuration = (WebSphereConfiguration) getConfiguration();

        // add users and groups
        List<User> users = getConfiguration().getUsers();
        if (!users.isEmpty())
        {
            getLogger().info("Adding users and groups to WebSphere domain.",
                    this.getClass().getName());

            for (User user : users)
            {
                configurationScript.addAll(configuration.getFactory().createUserScript(user));
            }
            configurationScript.add(configuration.getFactory().saveSyncScript());
        }

        String onlineDeploymentValue = getConfiguration().getPropertyValue(
                WebSpherePropertySet.ONLINE_DEPLOYMENT);
        boolean onlineDeployment = Boolean.parseBoolean(onlineDeploymentValue);
        if (onlineDeployment)
        {
            getLogger().info("Adding deployments to WebSphere domain.",
                    this.getClass().getName());

            // deploy deployables
            List<String> extraLibraries = Arrays.asList(getExtraClasspath());
            for (Deployable deployable : getConfiguration().getDeployables())
            {
                configurationScript.addAll(configuration.getFactory()
                        .deployDeployableScript(deployable, extraLibraries));
            }

            configurationScript.add(configuration.getFactory().saveSyncScript());

            // start deployables
            for (Deployable deployable : getConfiguration().getDeployables())
            {
                configurationScript.add(configuration.getFactory()
                        .startDeployableScript(deployable));
            }

            configurationScript.add(configuration.getFactory().saveSyncScript());
        }

        if (!configurationScript.isEmpty())
        {
            executeScript(configurationScript);
        }

        // Execute online jython scripts
        String scriptPaths = getConfiguration().getPropertyValue(
                WebSpherePropertySet.JYTHON_SCRIPT_ONLINE);
        List<String> scriptPathList = ComplexPropertyUtils.parseProperty(scriptPaths, "|");
        executeScriptFiles(scriptPathList);
    }

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#stop()} to all container
     * extending this class must implement.
     * 
     * @param java the predefined JVM launcher to use to stop the container
     * @throws Exception if any error is raised during the container stop
     */
    @Override
    public void doStop(JvmLauncher java) throws Exception
    {
        getLogger().info("Stopping WebSphere.", this.getClass().getName());
        List<String> arguments = new ArrayList<String>();
        arguments.add(getConfiguration().getPropertyValue(WebSpherePropertySet.SERVER));
        arguments.add("-profileName");
        arguments.add(getConfiguration().getPropertyValue(WebSpherePropertySet.PROFILE));
        arguments.add("-username");
        arguments.add(getConfiguration().getPropertyValue(WebSpherePropertySet.ADMIN_USERNAME));
        arguments.add("-password");
        arguments.add(getConfiguration().getPropertyValue(WebSpherePropertySet.ADMIN_PASSWORD));
        runStopServerCommand(arguments.toArray(new String[arguments.size()]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeScript(List<ScriptCommand> configurationScript)
    {
        try
        {
            // import wsadminlib library
            File wsadminlibFile = File.createTempFile("wsadminlib-", ".py");
            wsadminlibFile.deleteOnExit();
            getResourceUtils().copyResource(AbstractLocalConfiguration.RESOURCE_PATH
                    + getId() + "/wsadminlib.py",
                    wsadminlibFile, new FilterChain(), null);
            configurationScript.add(0, ((WebSphereConfiguration) getConfiguration()).
                    getFactory().importWsadminlibScript(wsadminlibFile.getAbsolutePath()));

            // build jython script
            String newLine = System.getProperty("line.separator");
            StringBuffer buffer = new StringBuffer();
            for (ScriptCommand configuration : configurationScript)
            {
                buffer.append(configuration.readScript());
            }

            getLogger().debug("Sending jython script: " + newLine + buffer.toString(),
                this.getClass().getName());

            // script is stored to *.py file
            File tempFile = File.createTempFile("jython", ".py");
            tempFile.deleteOnExit();
            getFileHandler().writeTextFile(tempFile.getAbsolutePath(), buffer.toString(), null);

            executeScriptFiles(Arrays.asList(tempFile.getAbsolutePath()));
        }
        catch (Exception e)
        {
            throw new CargoException("Cannot execute jython script.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeScriptFiles(List<String> scriptFilePaths)
    {
        String jvmArgs = getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
        JvmArguments parsedArguments = JvmArguments.parseArguments(jvmArgs);

        for (String scriptFilePath : scriptFilePaths)
        {
            File scriptFile = new File(scriptFilePath);

            if (scriptFile.exists())
            {
                List<String> arguments = new ArrayList<String>();
                arguments.add("-lang");
                arguments.add("jython");
                arguments.add("-profileName");
                arguments.add(getConfiguration().getPropertyValue(WebSpherePropertySet.PROFILE));
                arguments.add("-f");
                arguments.add(scriptFile.getAbsolutePath());

                // Need to set JVM heap size to be able to process large deployables
                arguments.add("-javaoption -Xms"
                        + Long.toString(parsedArguments.getInitialHeap(ByteUnit.MEGABYTES)) + "m");
                arguments.add("-javaoption -Xmx"
                        + Long.toString(parsedArguments.getMaxHeap(ByteUnit.MEGABYTES)) + "m");

                ContainerMonitor monitor = new ConsoleUrlWebSphereMonitor(this);
                if (!monitor.isRunning())
                {
                    arguments.add("-conntype");
                    arguments.add("NONE");
                }
                else
                {
                    arguments.add("-conntype");
                    arguments.add("SOAP");
                    arguments.add("-user");
                    arguments.add(getConfiguration().
                            getPropertyValue(WebSpherePropertySet.ADMIN_USERNAME));
                    arguments.add("-password");
                    arguments.add(getConfiguration().
                            getPropertyValue(WebSpherePropertySet.ADMIN_PASSWORD));
                }
                runWebSphereCommand("wsadmin", arguments.toArray(new String[arguments.size()]));
            }
            else
            {
                getLogger().warn(String.format("Script file %s doesn't exists.", scriptFilePath),
                            this.getClass().getName());
            }
        }
    }

    /**
     * Run a manageprofile command.
     * @param arguments Arguments.
     */
    public void runManageProfileCommand(String... arguments)
    {
        runWebSphereCommand("manageprofiles", arguments);
    }

    /**
     * Run a start server command.
     * @param arguments Arguments.
     */
    public void runStartServerCommand(String... arguments)
    {
        runWebSphereCommand("startServer", arguments);
    }

    /**
     * Run a stop server command.
     * @param arguments Arguments.
     */
    public void runStopServerCommand(String... arguments)
    {
        runWebSphereCommand("stopServer", arguments);
    }

    /**
     * Run one of WebSphere commands.
     * @param wsCommand Command name.
     * @param arguments Arguments.
     */
    private void runWebSphereCommand(String wsCommand, String... arguments)
    {
        StringBuffer command = new StringBuffer();
        command.append(getHome());
        command.append(File.separator);
        command.append("bin");
        command.append(File.separator);
        command.append(wsCommand);

        if (JdkUtils.isWindows())
        {
            command.append(WINDOWS_SUFFIX);
        }
        else
        {
            command.append(LINUX_SUFFIX);
        }

        for (String argument : arguments)
        {
            command.append(" ");
            command.append(argument);
        }

        getLogger().debug("Executing command: " + command.toString(),
                this.getClass().getName());

        getProcessExecutor().executeAndWait(command.toString());
    }

    /**
     * @return Initialized process executor.
     */
    private ProcessExecutor getProcessExecutor()
    {
        if (processExecutor == null)
        {
            processExecutor = new ProcessExecutor(getLogger());
        }
        return processExecutor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (waitForStarting)
        {
            waitForStarting(new ConsoleUrlWebSphereMonitor(this));
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }
}
