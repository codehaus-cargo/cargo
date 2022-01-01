/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.container.weblogic.internal;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.spi.AbstractRemoteContainer;
import org.codehaus.cargo.container.spi.jvm.DefaultJvmLauncherFactory;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherFactory;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherRequest;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Basic support for the WebLogic remote application server.
 */
public abstract class AbstractWebLogicRemoteContainer extends AbstractRemoteContainer implements
    WebLogicRemoteScriptingContainer
{

    /**
     * Capability of the WebLogic container.
     */
    private ContainerCapability capability = new J2EEContainerCapability();

    /**
     * JVM launcher factory.
     */
    private JvmLauncherFactory jvmLauncherFactory;

    /**
     * {@inheritDoc}
     * @see AbstractRemoteContainer#AbstractRemoteContainer(org.codehaus.cargo.container.configuration.RuntimeConfiguration)
     */
    public AbstractWebLogicRemoteContainer(RuntimeConfiguration configuration)
    {
        super(configuration);
        this.jvmLauncherFactory = new DefaultJvmLauncherFactory();
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
     * Writes configuration script using WLST.
     * 
     * @param configurationScript Script containing WLST configuration to be executed.
     */
    @Override
    public void executeScript(List<ScriptCommand> configurationScript)
    {
        String newLine = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder();
        for (ScriptCommand configuration : configurationScript)
        {
            sb.append(configuration.readScript());
            sb.append(newLine);
        }

        sb.append("dumpStack()");

        getLogger().debug("Sending WLST script: " + newLine + sb.toString(),
            this.getClass().getName());

        try
        {
            // script is stored to *.py file which is added as parameter when invoking WLST
            // configuration class
            File tempFile = File.createTempFile("wlst", ".py");
            tempFile.deleteOnExit();
            getFileHandler().writeTextFile(tempFile.getAbsolutePath(), sb.toString(), null);

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
                    JvmLauncherRequest request = new JvmLauncherRequest(false, this);
                    JvmLauncher java = jvmLauncherFactory.createJvmLauncher(request);

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
        File serverDir = new File(getWeblogicHome(), "server");
        java.addClasspathEntries(new File(serverDir, "lib/weblogic.jar"));
        java.setMainClass("weblogic.WLST");
    }

    /**
     * @return FileHandler.
     */
    private FileHandler getFileHandler()
    {
        return new DefaultFileHandler();
    }

    /**
     * @return WebLogic home directory.
     */
    public String getWeblogicHome()
    {
        String localWebLogicHome = getConfiguration().
                getPropertyValue(WebLogicPropertySet.LOCAL_WEBLOGIC_HOME);

        if (localWebLogicHome != null && !localWebLogicHome.isEmpty())
        {
            return localWebLogicHome;
        }

        throw new CargoException("Property " + WebLogicPropertySet.LOCAL_WEBLOGIC_HOME
                + " is not set. It is required for remote deployment of deployables.");
    }
}
