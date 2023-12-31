/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.jboss;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ScriptingCapableContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.container.jboss.internal.JBoss7xContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.util.ContainerUtils;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;

/**
 * JBoss 7.x series container implementation.
 */
public class JBoss7xInstalledLocalContainer extends AbstractInstalledLocalContainer
    implements ScriptingCapableContainer
{
    /**
     * JBoss 7.x series unique id.
     */
    public static final String ID = "jboss7x";

    /**
     * Capability of the JBoss container.
     */
    private static final ContainerCapability CAPABILITY = new JBoss7xContainerCapability();

    /**
     * JBoss version.
     */
    protected String version;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    public JBoss7xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
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
        return "JBoss " + getVersion("7.x");
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
     * Parse installed JBoss version.
     * 
     * @return the JBoss version, or <code>defaultVersion</code> if the version number could not be
     * determined
     * @param defaultVersion the default version used if the exact JBoss version can't be determined
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
                    getLogger().info("Parsed JBoss version = [" + version + "]",
                        this.getClass().getName());
                }
            }
            catch (Exception e)
            {
                getLogger().debug(
                    "Failed to find JBoss version, base error [" + e.getMessage() + "]",
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
        return new File(getHome(), "bundles/org/jboss/as/osgi/configadmin/main");
    }

    /**
     * {@inheritDoc}. As JBoss 7.x needs to have the runtime AFTER the arguments passed to the main
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
    protected void doStart(JvmLauncher java) throws Exception
    {
        copyExtraClasspathJars();

        setProperties(java);

        java.setJarFile(new File(getHome(), "jboss-modules.jar"));

        String modules = getConfiguration().getPropertyValue(
            JBossPropertySet.ALTERNATIVE_MODULES_DIR);
        if (!new File(modules).isAbsolute())
        {
            modules = getFileHandler().append(getHome(), modules);
        }

        java.addAppArguments(
            "-mp", modules,
            "-logmodule", "org.jboss.logmanager",
            "-jaxpmodule", "javax.xml.jaxp-provider",
            "org.jboss.as.standalone",
            "--server-config="
                + getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml");

        String runtimeArgs = getConfiguration().getPropertyValue(GeneralPropertySet.RUNTIME_ARGS);
        if (runtimeArgs != null)
        {
            // Replace new lines and tabs, so that Maven or Ant plugins can
            // specify multiline runtime arguments in their XML files
            runtimeArgs = runtimeArgs.replace('\n', ' ');
            runtimeArgs = runtimeArgs.replace('\r', ' ');
            runtimeArgs = runtimeArgs.replace('\t', ' ');
            java.addAppArgumentLine(runtimeArgs);
        }

        java.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executePostStartTasks() throws Exception
    {
        Map<String, String> properties = getConfiguration().getProperties();

        // Execute online CLI scripts
        for (Map.Entry<String, String> property : properties.entrySet())
        {
            String propertyName = property.getKey();
            if (propertyName.startsWith(JBossPropertySet.CLI_ONLINE_SCRIPT))
            {
                String scriptPath = property.getValue();
                executeScriptFiles(Arrays.asList(scriptPath));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        String host =
            getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME);
        String port =
            getConfiguration().getPropertyValue(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT);

        java.setJarFile(new File(getHome(), "jboss-modules.jar"));

        java.addAppArguments(
            "-mp", getHome() + "/modules",
            "-logmodule", "org.jboss.logmanager",
            "org.jboss.as.cli",
            "--connect", "--controller=" + host + ":" + port,
            "command=:shutdown");

        String username = getConfiguration().getPropertyValue(RemotePropertySet.USERNAME);

        if (username != null && !username.trim().isEmpty())
        {
            String password =
                getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD);

            java.addAppArguments("--user=" + username, "--password=" + password);
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
            throw new CargoException("Cannot create logging file URL.", e);
        }
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
     * Cope extra classpath JARs.
     * @throws IOException If creating the JARs' <code>module.xml</code> fails.
     */
    protected void copyExtraClasspathJars() throws IOException
    {
        List<String> dependencies = new ArrayList<String>();
        dependencies.add("javax.api");
        dependencies.add("javax.transaction.api");

        // Create JARs for modules
        Set<String> classpath = new TreeSet<String>();
        if (this.getExtraClasspath() != null && this.getExtraClasspath().length != 0)
        {
            for (String classpathElement : this.getExtraClasspath())
            {
                classpath.add(classpathElement);
            }
        }
        if (this.getSharedClasspath() != null && this.getSharedClasspath().length != 0)
        {
            for (String classpathElement : this.getSharedClasspath())
            {
                classpath.add(classpathElement);
            }
        }
        for (String classpathElement : classpath)
        {
            String moduleName = getFileHandler().getName(classpathElement);
            // Strip extension from JAR file to get module name
            moduleName = moduleName.substring(0, moduleName.lastIndexOf('.'));
            // CARGO-1091: JBoss expects subdirectories when the module name contains dots.
            //             Replace all dots with minus to keep a version separator.
            moduleName = moduleName.replace('.', '-');
            String folder = this.getHome()
                + "/modules/org/codehaus/cargo/classpath/" + moduleName + "/main";
            getFileHandler().mkdirs(folder);

            StringBuilder dependenciesXml = new StringBuilder();
            for (String dependency : dependencies)
            {
                dependenciesXml.append("\n    <module name=\"" + dependency + "\"/>");
            }
            dependencies.add("org.codehaus.cargo.classpath." + moduleName);

            Map<String, String> replacements = new HashMap<String, String>(2);
            replacements.put("moduleName", moduleName);
            replacements.put("dependencies", dependenciesXml.toString());

            getFileHandler().copyFile(classpathElement,
                getFileHandler().append(folder, moduleName + ".jar"));
            getResourceUtils().copyResource(
                AbstractLocalConfiguration.RESOURCE_PATH + "jboss-module/jboss-module.xml",
                    getFileHandler().append(folder, "module.xml"),
                        getFileHandler(), replacements, StandardCharsets.UTF_8);
        }
    }

    /**
     * Writes CLI configuration script.
     * 
     * @param configurationScript Script containing CLI configuration to be executed.
     */
    @Override
    public void executeScript(List<ScriptCommand> configurationScript)
    {
        String newLine = FileHandler.NEW_LINE;
        StringBuilder sb = new StringBuilder();

        for (ScriptCommand configuration : configurationScript)
        {
            sb.append(configuration.readScript());
            sb.append(newLine);
        }

        getLogger().debug("Sending CLI script: " + newLine + sb.toString(),
            this.getClass().getName());

        try
        {
            // script is stored to *.cli file which is added as parameter when invoking CLI
            // executor
            File tempFile = File.createTempFile("jboss-", ".cli");
            tempFile.deleteOnExit();
            getFileHandler().writeTextFile(tempFile.getAbsolutePath(), sb.toString(), null);

            executeScriptFiles(Arrays.asList(tempFile.getAbsolutePath()));

            tempFile.delete();
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
                File scriptOutput = new File(scriptFile + ".output");
                scriptOutput.deleteOnExit();
                try
                {
                    java.setOutputFile(scriptOutput);
                    java.setAppendOutput(false);

                    addCliArguments(java);
                    setProperties(java);

                    java.addAppArguments("--file=" + scriptFile);
                    int result = java.execute();
                    if (result != 0)
                    {
                        throw new ContainerException(
                            "Failure when invoking CLI script: java returned " + result);
                    }
                }
                catch (RuntimeException e)
                {
                    StringBuilder message = new StringBuilder(e.getMessage());
                    try
                    {
                        String detail = getFileHandler().readTextFile(
                            scriptOutput.getPath(), StandardCharsets.UTF_8);
                        message.append(", detailed message: ");
                        message.append(detail);
                    }
                    catch (Exception ignored)
                    {
                        // If reading the detailed message failed, throw the initial exception
                        throw e;
                    }
                    throw new ContainerException(message.toString());
                }
                finally
                {
                    scriptOutput.delete();
                }
            }
        }
    }

    /**
     * Adding JBoss CLI dependencies and setting main class.
     * 
     * @param java Launcher.
     */
    private void addCliArguments(JvmLauncher java)
    {
        String host =
                getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME);
        String port =
            getConfiguration().getPropertyValue(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT);

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

        if (isOnline())
        {
            java.addAppArguments("--connect", "--controller=" + host + ":" + port);
        }
    }

    /**
     * @return True if WildFly is started and has cargocpc deployed.
     */
    public boolean isOnline()
    {
        HttpUtils httpUtils = new HttpUtils();
        return httpUtils.ping(ContainerUtils.getCPCURL(getConfiguration()));
    }
}
