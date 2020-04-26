/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2020 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ScriptingCapableContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.jboss.internal.JBoss7xContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.startup.CombinedContainerMonitor;
import org.codehaus.cargo.container.startup.ContainerMonitor;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFlyCliConfigurationFactory;
import org.codehaus.cargo.util.CargoException;

/**
 * WildFly container implementation.
 */
public abstract class AbstractWildFlyInstalledLocalContainer extends AbstractInstalledLocalContainer
    implements ScriptingCapableContainer
{
    /**
     * Capability of the WildFly container.
     */
    private static final ContainerCapability CAPABILITY = new JBoss7xContainerCapability();

    /**
     * WildFly version.
     */
    protected String version;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    public AbstractWildFlyInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        setProperties(java);
        java.addJvmArgumentLine("-D[Standalone]");

        java.setJarFile(new File(getHome(), "jboss-modules.jar"));

        String modules = getConfiguration().getPropertyValue(
            JBossPropertySet.ALTERNATIVE_MODULES_DIR);
        if (!new File(modules).isAbsolute())
        {
            modules = getFileHandler().append(getHome(), modules);
        }

        java.addAppArguments(
            "-mp", modules,
            "org.jboss.as.standalone",
            "--server-config="
                + getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml");

        String runtimeArgs = getConfiguration().getPropertyValue(GeneralPropertySet.RUNTIME_ARGS);
        if (runtimeArgs != null)
        {
            // Replace new lines and tabs, so that Maven or ANT plugins can
            // specify multiline runtime arguments in their XML files
            runtimeArgs = runtimeArgs.replace('\n', ' ');
            runtimeArgs = runtimeArgs.replace('\r', ' ');
            runtimeArgs = runtimeArgs.replace('\t', ' ');
            java.addAppArgumentLine(runtimeArgs);
        }

        java.start();
    }

    /**
     * Set the properties on the JVM launcher.
     * @param java JVM launcher to set the properties on.
     */
    protected void setProperties(JvmLauncher java)
    {
        java.setSystemProperty("org.jboss.boot.log.file",
            getConfiguration().getHome() + "/log/boot.log");
        try
        {
            java.setSystemProperty("logging.configuration",
                new File(getConfiguration().getHome() + "/configuration/logging.properties")
                    .toURI().toURL().toString());
        }
        catch (MalformedURLException e)
        {
            throw new CargoException("Cannot create logging file URL." , e);
        }
        java.setEnvironmentVariable("JBOSS_HOME", getHome());
        java.setSystemProperty("jboss.home.dir", getHome());
        java.setSystemProperty("jboss.server.base.dir", getConfiguration().getHome());

        // CARGO-1111: To allow JBoss 7.x and onwards to be accessed from remote machines, the
        // system property jboss.bind.address must be set.
        final Map<String, String> systemProperties = getSystemProperties();
        if (!systemProperties.containsKey("jboss.bind.address"))
        {
            String hostname = getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME);
            if ("localhost".equals(hostname))
            {
                hostname = "0.0.0.0";
            }

            java.setSystemProperty("jboss.bind.address", hostname);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        List<ScriptCommand> configurationScript = new ArrayList<ScriptCommand>();

        WildFlyConfiguration configuration = (WildFlyConfiguration) getConfiguration();
        WildFlyCliConfigurationFactory factory = configuration.getConfigurationFactory();
        configurationScript.add(factory.connectToServerScript());
        configurationScript.add(factory.shutdownServerScript());

        executeScript(configurationScript);
    }

    /**
     * Writes CLI configuration script.
     * 
     * @param configurationScript Script containing CLI configuration to be executed.
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

        getLogger().debug("Sending CLI script: " + newLine + buffer.toString(),
            this.getClass().getName());

        try
        {
            // script is stored to *.cli file which is added as parameter when invoking CLI
            // executor
            File tempFile = File.createTempFile("wildfly-", ".cli");
            tempFile.deleteOnExit();
            getFileHandler().writeTextFile(tempFile.getAbsolutePath(), buffer.toString(), null);

            executeScriptFiles(Arrays.asList(tempFile.getAbsolutePath()));
        }
        catch (IOException e)
        {
            throw new CargoException("Cannot create temporary CLI script file.", e);
        }
    }

    /**
     * Executes CLI scripts.
     * 
     * @param scriptFilePaths List of file paths containing CLI scripts.
     */
    @Override
    public void executeScriptFiles(List<String> scriptFilePaths)
    {
        for (String scriptFilePath : scriptFilePaths)
        {
            File scriptFile = new File(scriptFilePath);

            if (scriptFile.isAbsolute() && !scriptFile.exists())
            {
                getLogger().warn(String.format("Script file %s doesn't exists.", scriptFilePath),
                        this.getClass().getName());
            }
            else
            {
                JvmLauncher java = createJvmLauncher(false);

                addCliArguments(java);
                setProperties(java);

                java.addAppArguments("--file=" + scriptFile);
                int result = java.execute();
                if (result != 0)
                {
                    throw new ContainerException("Failure when invoking CLI script,"
                            + " java returned " + result);
                }
            }
        }
    }

    /**
     * Adding WildFLy CLI dependencies and setting main class.
     * 
     * @param java Launcher.
     */
    private void addCliArguments(JvmLauncher java)
    {
        java.setJarFile(new File(getHome(), "jboss-modules.jar"));

        String modules = getConfiguration().getPropertyValue(
            JBossPropertySet.ALTERNATIVE_MODULES_DIR);
        if (!new File(modules).isAbsolute())
        {
            modules = getFileHandler().append(getHome(), modules);
        }

        java.addAppArguments(
                "-mp", modules,
                "org.jboss.as.cli");
    }

    /**
     * Parse installed WildFly version.
     * 
     * @param defaultVersion the version used if the exact WildFly version can't be determined
     * @return the WildFly version, or <code>defaultVersion</code> if the version number could not
     * be determined.
     */
    protected synchronized String getVersion(String defaultVersion)
    {
        String version = this.version;

        if (version == null)
        {
            try
            {
                File configAdminFile = null;

                File configAdminDirectory = getConfigAdminDirectory();

                if (configAdminDirectory.isDirectory())
                {
                    File[] contents = configAdminDirectory.listFiles();
                    for (File content : contents)
                    {
                        if (content.getName().endsWith(".jar"))
                        {
                            if (configAdminFile != null)
                            {
                                throw new IllegalStateException("The directory "
                                    + configAdminDirectory + " contains more than one JAR.");
                            }
                            configAdminFile = content;
                        }
                    }
                    if (configAdminFile == null)
                    {
                        throw new IllegalStateException("The directory " + configAdminDirectory
                            + " does not contain any JAR files.");
                    }
                }
                else
                {
                    throw new IllegalArgumentException(configAdminDirectory
                        + " is not a directory.");
                }

                try (JarFile jarFile = new JarFile(configAdminFile))
                {
                    version = jarFile.getManifest().getMainAttributes().getValue("Bundle-Version");
                    if (version == null)
                    {
                        version = jarFile.getManifest().getMainAttributes().getValue(
                            "Implementation-Version");
                    }
                }

                if (version == null)
                {
                    getLogger().debug("Couldn't find Bundle-Version in the MANIFEST of "
                        + configAdminFile, this.getClass().getName());
                }
                else
                {
                    getLogger().info("Parsed WildFly version = [" + version + "]",
                        this.getClass().getName());
                }
            }
            catch (Exception e)
            {
                getLogger().debug(
                    "Failed to find WildFly version, base error [" + e.getMessage() + "]",
                    this.getClass().getName());
            }

            if (version == null)
            {
                version = defaultVersion;
            }
            this.version = version;
        }

        return version;
    }

    /**
     * @return Config admin directory.
     */
    protected File getConfigAdminDirectory()
    {
        return new File(getHome(), "modules/system/layers/base/org/jboss/as/system-jmx/main");
    }

    /**
     * {@inheritDoc}. As WildFly needs to have the runtime AFTER the arguments passed to the main
     * class, we need to set the associated argument line when starting container (and not when
     * initializing the JVM launcher).
     */
    @Override
    protected void addRuntimeArgs(JvmLauncher java)
    {
        // Nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (waitForStarting)
        {
            ContainerMonitor first = new ManagementUrlWildFlyMonitor(this);
            ContainerMonitor second = new CLIWildFlyMonitor(this);
            waitForStarting(new CombinedContainerMonitor(this, first, second));
        }
        else
        {
            super.waitForCompletion(waitForStarting);
        }
    }
}
