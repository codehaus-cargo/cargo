/*
* Copyright 2016 IBM Corp.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
 */
package org.codehaus.cargo.container.liberty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.liberty.internal.LibertyInstall;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;

/**
 * This starts a WebSphere Liberty server
 */
public class LibertyInstalledLocalContainer extends AbstractInstalledLocalContainer
{

    /**
     * Unique container id
     */
    private static final String ID = "liberty";

    /**
     * Container name (human-readable name)
     */
    private static final String NAME = "WebSphere Liberty";

    /**
     * Capabilities
     */
    private ContainerCapability capability = new J2EEContainerCapability();

    /**
     * Creates an installed local connector for WebSphere Liberty.
     * 
     * @param configuration the configuration
     */
    public LibertyInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * @return the configuration capability for WebSphere Liberty
     */
    @Override
    public ContainerCapability getCapability()
    {
        return capability;
    }

    /**
     * @return the id of the container
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * @return the name of the container
     */
    @Override
    public String getName()
    {
        return NAME;
    }

    /**
     * Start the container.
     * 
     * @param java the java configuration. This is ignored by WebSphere Liberty.
     * @throws Exception if something goes wrong
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        String spawn = getConfiguration().getPropertyValue(GeneralPropertySet.SPAWN_PROCESS);
        String command = "run";
        if ("true".equals(spawn))
        {
            command = "start";
        }

        String jvmArgs = getConfiguration().getPropertyValue(GeneralPropertySet.START_JVMARGS);

        runCommand(new LibertyInstall(this), command, env(jvmArgs));
    }

    /**
     * Create a man of environment variables using the passed in jvmArgs.
     * 
     * @param inJvmArgs the <code>jvmargs</code> to use, or <code>null</code> if the configured
     * JVM arguments are to be used.
     * @return the map of environment variables to use.
     */
    private Map<String, String> env(String inJvmArgs)
    {
        String jvmArgs = inJvmArgs;
        LocalConfiguration config = getConfiguration();
        Map<String, String> env = new HashMap<String, String>();

        if (jvmArgs == null)
        {
            jvmArgs = config.getPropertyValue(GeneralPropertySet.JVMARGS);
        }

        if (jvmArgs != null)
        {
            env.put("JVM_ARGS", jvmArgs);
        }

        String javaHome = config.getPropertyValue(GeneralPropertySet.JAVA_HOME);
        if (javaHome == null)
        {
            javaHome = System.getProperty("java.home");
        }

        env.put("JAVA_HOME", javaHome);

        return env;
    }

    /**
     * Stop the container
     * 
     * @param java the java configuration. This is ignored by WebSphere Liberty.
     * @throws Exception if something goes wrong
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        runCommand(new LibertyInstall(this), "stop", env(null));
    }

    /**
     * Run the specified server command on WebSphere Liberty waiting for it to complete.
     * 
     * @param install The liberty install.
     * @param command The server command to invoke.
     * @param env environment variables
     * @throws Exception If anything goes wrong.
     */
    private void runCommand(LibertyInstall install, String command, Map<String, String> env)
        throws Exception
    {
        Process p = install.runCommand(command, env);
        if (!"run".equals(command))
        {
            if (p.waitFor(getTimeout(), TimeUnit.MILLISECONDS))
            {
                int retVal = p.exitValue();
                if (retVal != 0)
                {
                    throw new CargoException(
                        "WebSphere Liberty command " + command + " failed, return code " + retVal);
                }
            }
            else
            {
                p.destroyForcibly();
                throw new CargoException(
                    "WebSphere Liberty command " + command
                        + " did not complete after " + getTimeout() + " milliseconds");
            }
        }
    }
}
