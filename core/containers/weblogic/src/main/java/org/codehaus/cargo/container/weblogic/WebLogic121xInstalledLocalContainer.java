/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2016 Ali Tokmen.
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
package org.codehaus.cargo.container.weblogic;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.internal.util.ComplexPropertyUtils;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.weblogic.internal.AbstractWebLogicInstalledLocalContainer;
import org.codehaus.cargo.container.weblogic.internal.ConsoleUrlWebLogicMonitor;
import org.codehaus.cargo.container.weblogic.internal.WebLogicLocalScriptingContainer;
import org.codehaus.cargo.util.CargoException;

/**
 * Special container support for the Bea WebLogic 12.1.3 application server. Contains WLST support.
 */
public class WebLogic121xInstalledLocalContainer extends
    AbstractWebLogicInstalledLocalContainer implements WebLogicLocalScriptingContainer
{

    /**
     * Unique container id.
     */
    public static final String ID = "weblogic121x";

    /**
     * {@inheritDoc}
     * @see AbstractWebLogicInstalledLocalContainer#AbstractWebLogicInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public WebLogic121xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "WebLogic 12.1.x";
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
    public String getAutoDeployDirectory()
    {
        return "autodeploy";
    }

    /**
     * {@inheritDoc} Also includes checking of the modules directory, which is unique to WebLogic
     * 10.
     */
    @Override
    protected List<String> getBeaHomeDirs()
    {
        List<String> beaHomeDirs = super.getBeaHomeDirs();
        beaHomeDirs.add(getFileHandler().append(getWeblogicHome(), "modules"));
        return beaHomeDirs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<String> getBeaHomeFiles()
    {
        List<String> requiredFiles = new ArrayList<String>();
        requiredFiles.add(getFileHandler().append(getBeaHome(), "inventory/registry.xml"));
        return requiredFiles;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected void executePostStartTasks() throws Exception
    {
        List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();
        WebLogicWlstConfiguration configuration = (WebLogicWlstConfiguration) getConfiguration();

        configurationScript.add(configuration.getConfigurationFactory().readDomainOnlineScript());

        List<User> users = getConfiguration().getUsers();
        Set<String> roles = User.createRoleMap(users).keySet();
        for (String role : roles)
        {
            configurationScript.add(configuration.getConfigurationFactory().
                    createGroupScript(role));
        }

        for (User user : users)
        {
            // WebLogic cannot create user with same name as existing role
            if (!roles.contains(user.getName()))
            {
                configurationScript.add(configuration.getConfigurationFactory().
                        createUserScript(user));
                configurationScript.addAll(configuration.getConfigurationFactory().
                        addUserToGroupsScript(user));
            }
        }

        configurationScript.add(configuration.getConfigurationFactory().updateDomainOnlineScript());

        if (!users.isEmpty())
        {
            getLogger().info("Adding users and groups to WebLogic domain.",
                this.getClass().getName());
            executeScript(configurationScript);
        }

        // Execute online jython scripts
        String scriptPaths = getConfiguration().getPropertyValue(
                WebLogicPropertySet.JYTHON_SCRIPT_ONLINE);
        List<String> scriptPathList = ComplexPropertyUtils.parseProperty(scriptPaths, "|");
        executeScriptFiles(scriptPathList);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void doStop(JvmLauncher java) throws Exception
    {
        List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();
        WebLogicWlstConfiguration configuration = (WebLogicWlstConfiguration) getConfiguration();

        configurationScript.add(configuration.getConfigurationFactory().readDomainOnlineScript());
        configurationScript.add(configuration.getConfigurationFactory().shutdownDomainScript());

        executeScript(configurationScript);
    }

    /**
     * Writes configuration script using WLST.
     *
     * @param configurationScript Script containing WLST configuration to be executed.
     */
    @Override
    public void executeScript(List<ScriptCommand> configurationScript)
    {
        String newLine = System.getProperty("line.separator");
        StringBuffer buffer = new StringBuffer();
        for (ScriptCommand configuration : configurationScript)
        {
            buffer.append(configuration.readScript());
            buffer.append(newLine);
        }

        buffer.append("dumpStack()");

        getLogger().debug("Sending WLST script: " + newLine + buffer.toString(),
            this.getClass().getName());

        try
        {
            // script is stored to *.py file which is added as parameter when invoking WLST
            // configuration class
            File tempFile = File.createTempFile("wlst", ".py");
            tempFile.deleteOnExit();
            getFileHandler().writeTextFile(tempFile.getAbsolutePath(), buffer.toString(), null);

            executeScriptFiles(Arrays.asList(tempFile.getAbsolutePath()));
        }
        catch (Exception e)
        {
            throw new CargoException("Cannot execute WLST script.", e);
        }
    }

    /**
     * Executes scripts using WLST.
     *
     * @param scriptFilePaths List of file paths containing jython scripts.
     */
    @Override
    public void executeScriptFiles(List<String> scriptFilePaths)
    {
        for (String scriptFilePath : scriptFilePaths)
        {
            File scriptFile = new File(scriptFilePath);

            if (scriptFile.exists())
            {
                try
                {
                    JvmLauncher java = createJvmLauncher(false);

                    addWlstArguments(java);

                    java.addAppArgument(scriptFile);
                    int result = java.execute();
                    if (result != 0)
                    {
                        throw new ContainerException("Failure when invoking WLST script,"
                                + " java returned " + result);
                    }
                }
                catch (Exception e)
                {
                    throw new CargoException("Cannot execute WLST script.", e);
                }
            }
            else
            {
                getLogger().warn(String.format("Script file %s doesn't exists.", scriptFilePath),
                            this.getClass().getName());
            }
        }
    }

    /**
     * Adding WLST dependencies and setting main class.
     *
     * @param java Launcher.
     */
    private void addWlstArguments(JvmLauncher java)
    {
        File serverDir = new File(this.getHome(), "server");
        java.addClasspathEntries(new File(serverDir, "lib/weblogic.jar"));
        java.setMainClass("weblogic.WLST");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (waitForStarting)
        {
            waitForStarting(new ConsoleUrlWebLogicMonitor(this));
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }
}
