package org.codehaus.cargo.container.liberty.local;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.liberty.internal.LibertyInstall;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * This starts a Liberty server
 */
public class LibertyInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /** Unique container id */
    private static final String ID = "liberty";

    /** Container name (human-readable name) */
    private static final String NAME = "WebSphere Liberty";

    /** Capabilities */
    private ContainerCapability capability = new J2EEContainerCapability();

    /**
     * Creates an installed local connector for Liberty.
     * @param configuration the configuration
     */
    public LibertyInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * @return the configuration capability for Liberty
     */
    public ContainerCapability getCapability()
    {
        return capability;
    }

    /**
     * @return the id of the container
     */
    public String getId()
    {
        return ID;
    }

    /**
     * @return the name of the container
     */
    public String getName()
    {
        return NAME;
    }

    /**
     * Start the container.
     * @param java the java configuration. This is ignored by Liberty
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
     * @param inJvmArgs the jvmargs to use, or null if the configured jvm args are to be used.
     * @return the map of envrionment variables to use.
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
     * @param java the java configuration. This is ignored by Liberty.
     * @throws Exception if something goes wrong
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        runCommand(new LibertyInstall(this), "stop", env(null));
    }

    /**
     * Run the specified server command on Liberty waiting for it to complete.
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
        if (!!!"run".equals(command))
        {
            int retVal = p.waitFor();
            if (retVal != 0)
            {
                throw new Exception("Liberty failed to start with return code " + retVal);
            }
        }
        else
        {
            // TODO do something to find out when it is done
        }
    }
}
