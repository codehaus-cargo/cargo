/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.container.spi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.container.internal.util.JdkUtils;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.jvm.DefaultJvmLauncherFactory;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherFactory;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherRequest;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.Logger;

/**
 * Default container implementation that all local installed container implementations must extend.
 */
public abstract class AbstractInstalledLocalContainer extends AbstractLocalContainer implements
    InstalledLocalContainer
{
    /**
     * Regular expression matcher to capture a quoted <code>-classpath</code> or <code>-cp</code>
     * argument
     */
    private static Pattern classpathQuotedPattern =
        Pattern.compile("-(classpath|cp)\\s+\"([^\"]*)\"");

    /**
     * Regular expression matcher to capture non-quoted <code>-classpath</code> or
     * <code>-cp</code> argument
     */
    private static Pattern classpathPattern =
        Pattern.compile("-(classpath|cp)\\s+([^\\s+\"]*)\\s+");

    /**
     * Regular expression matcher to capture non-quoted <code>-classpath</code> or <code>-cp</code>
     * argument as the final argument
     */
    private static Pattern classpathFinalPattern =
        Pattern.compile("-(classpath|cp)\\s+([^\\s+\"]*)");

    /**
     * List of system properties to set in the container JVM.
     */
    private Map<String, String> systemProperties;

    /**
     * Additional classpath entries for the classpath that will be used to start the containers.
     */
    private List<String> extraClasspath;

    /**
     * Additional classpath entries for the classpath that will be shared by the container
     * applications.
     */
    private List<String> sharedClasspath;

    /**
     * The container home installation directory.
     */
    private String home;

    /**
     * HTTP utility class.
     */
    private HttpUtils httpUtils;

    /**
     * Resource utility class.
     */
    private ResourceUtils resourceUtils;

    /**
     * JVM launcher factory.
     */
    private JvmLauncherFactory jvmLauncherFactory;

    /**
     * JVM launcher that started the container.
     */
    private JvmLauncher jvmStartLauncher;

    /**
     * Major JVM version
     */
    private int jvmMajorVersion = -1;

    /**
     * Default constructor.
     * 
     * @param configuration the configuration to associate to this container. It can be changed
     * later on by calling {@link #setConfiguration(LocalConfiguration)}
     */
    public AbstractInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);

        this.resourceUtils = new ResourceUtils();
        this.httpUtils = new HttpUtils();
        this.jvmLauncherFactory = new DefaultJvmLauncherFactory();
        extraClasspath = new ArrayList<String>();
        sharedClasspath = new ArrayList<String>();
        systemProperties = new HashMap<String, String>();
    }

    /**
     * Overriden in order to set the logger on ancillary components.
     * 
     * @param logger the logger to set and set in the ancillary objects
     */
    @Override
    public void setLogger(Logger logger)
    {
        super.setLogger(logger);
        this.resourceUtils.setLogger(logger);
        this.httpUtils.setLogger(logger);
    }

    /**
     * @return the HTTP utility class
     */
    protected final HttpUtils getHttpUtils()
    {
        return this.httpUtils;
    }

    /**
     * @return the Resource utility class
     */
    protected final ResourceUtils getResourceUtils()
    {
        return this.resourceUtils;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHome(String home)
    {
        this.home = home;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSystemProperties(Map<String, String> properties)
    {
        Map<String, String> props = new HashMap<String, String>();
        props.putAll(properties);

        this.systemProperties = props;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getSystemProperties()
    {
        return this.systemProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExtraClasspath(String[] classpath)
    {
        this.extraClasspath.clear();
        this.extraClasspath.addAll(Arrays.asList(classpath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getExtraClasspath()
    {
        return this.extraClasspath.toArray(new String[this.extraClasspath.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSharedClasspath(String[] classpath)
    {
        this.sharedClasspath.clear();
        this.sharedClasspath.addAll(Arrays.asList(classpath));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSharedClasspath()
    {
        return this.sharedClasspath.toArray(new String[this.sharedClasspath.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHome()
    {
        return this.home;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JvmLauncherFactory getJvmLauncherFactory()
    {
        return jvmLauncherFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setJvmLauncherFactory(JvmLauncherFactory jvmLauncherFactory)
    {
        this.jvmLauncherFactory = jvmLauncherFactory;
    }

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#start()} that all
     * containers extending this class must implement.
     * 
     * @param java the predefined JVM launcher to use to start the container
     * @throws Exception if any error is raised during the container start
     */
    protected abstract void doStart(JvmLauncher java) throws Exception;

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#stop()} that all
     * containers extending this class must implement.
     * 
     * @param java the predefined JVM launcher to use to stop the container
     * @throws Exception if any error is raised during the container stop
     */
    protected abstract void doStop(JvmLauncher java) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startInternal() throws Exception
    {
        jvmStartLauncher = createJvmLauncher(true);
        // Due to defect in org.apache.tools.ant.taskdefs.Java#setAppend we
        // can't call setAppendOutput if we want to spawn the process. If the
        // output isn't null we will have already disabled process spawning
        if (getOutput() != null)
        {
            jvmStartLauncher.setAppendOutput(isAppend());
        }
        addMemoryArguments(jvmStartLauncher);
        doStart(jvmStartLauncher);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void stopInternal() throws Exception
    {
        doStop(createJvmLauncher(false));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void forceStopInternal()
    {
        if (jvmStartLauncher != null)
        {
            jvmStartLauncher.kill();
        }
    }

    /**
     * Creates a preinitialized instance of a JVM launcher to be used for starting, stopping and
     * controlling the container.
     * 
     * @param server {@code true} to launch a server process, {@code false} to launch a
     * client/utility process.
     * @return The created JVM launcher, never {@code null}.
     */
    protected JvmLauncher createJvmLauncher(boolean server)
    {
        boolean spawned = Boolean.parseBoolean(getConfiguration().getPropertyValue(
                GeneralPropertySet.SPAWN_PROCESS));

        if (jvmMajorVersion == -1)
        {
            JvmLauncherRequest request = new JvmLauncherRequest(false, this);
            JvmLauncher java = jvmLauncherFactory.createJvmLauncher(request);
            setJvmToLaunchContainerIn(java);

            // Read the real JVM version
            try
            {
                File jvmVersionFile = File.createTempFile("cargo-jvm-version-", ".txt");
                java.setOutputFile(jvmVersionFile);
                java.setAppendOutput(true);
                java.setMainClass("-version");

                // CARGO-1595: Wait for the process to complete (which is what java.execute does),
                //             in case the command returns a lot of text back and takes time
                java.execute();

                // CARGO-1586: Read all the lines of the output (not just the first line)
                StringBuilder javaVersionOutput = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(jvmVersionFile)))
                {
                    for (String line = br.readLine(); line != null; line = br.readLine())
                    {
                        if (line.startsWith("java version \"")
                            || line.startsWith("openjdk version \""))
                        {
                            jvmMajorVersion = JdkUtils.parseMajorJavaVersion(
                                line.substring(line.indexOf('"') + 1));
                            break;
                        }
                        javaVersionOutput.append(line);
                        javaVersionOutput.append(FileHandler.NEW_LINE);
                    }

                    if (jvmMajorVersion == -1)
                    {
                        throw new IOException(
                            "Can't read JVM version from output: " + javaVersionOutput);
                    }
                }
                finally
                {
                    jvmVersionFile.delete();
                }
            }
            catch (IOException e)
            {
                throw new CargoException(
                    "Cannot read JVM version, please check that the provided execution ["
                        + java.getCommandLine() + "] is valid", e);
            }
        }

        JvmLauncherRequest request = new JvmLauncherRequest(server, this, spawned);

        JvmLauncher java = jvmLauncherFactory.createJvmLauncher(request);

        // Most container configurations assume that the container would be started from the same
        // working directory as the configuration; so set this here.
        java.setWorkingDirectory(
            new File(getFileHandler().getAbsolutePath(getConfiguration().getHome())));

        if (getOutput() == null)
        {
            // CARGO-1596: If no output file was set, then output the Java process via the logger
            java.setOutputLogger(getLogger(), this.getClass().getName());
        }
        else
        {
            File outputFile = new File(getOutput());

            // Ensure that directories where the output file will go are created
            getFileHandler().mkdirs(outputFile.getAbsoluteFile().getParent());

            // CARGO-520: Set append to "true" by default
            java.setOutputFile(outputFile);
            java.setAppendOutput(true);
        }

        setJvmToLaunchContainerIn(java);

        // Add extra container classpath entries specified by the user.
        addExtraClasspath(java);

        // Add system properties for the container JVM
        addSystemProperties(java);

        // Add runtime arguments if present
        addRuntimeArgs(java);

        // Add JVM args if defined
        addJvmArgs(java, server);

        if (server)
        {
            addStartJvmArgs(java);
        }

        // Add spawn options if defined
        addSpawn(java);

        // Make the Java command timeout match the container timeout
        java.setTimeout(getTimeout());

        return java;
    }

    /**
     * Gets the Java home directory to use for this container.
     * 
     * @return The Java home directory to use, never {@code null}.
     */
    protected String getJavaHome()
    {
        String javaHome = getConfiguration().getPropertyValue(GeneralPropertySet.JAVA_HOME);
        if (javaHome == null)
        {
            javaHome = System.getProperty("java.home");
        }
        return javaHome;
    }

    /**
     * Determines which java virtual machine will run the container.
     * 
     * @param java the java command that will start the container
     */
    public void setJvmToLaunchContainerIn(JvmLauncher java)
    {
        String javaHome = getJavaHome();
        if (javaHome != null)
        {
            String binDir = getFileHandler().append(javaHome, "bin");
            String javaPath = getFileHandler().append(binDir, "java");
            if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).startsWith("windows"))
            {
                javaPath += ".exe";
            }
            java.setJvm(javaPath);
        }
    }

    /**
     * Add system properties to the Ant java command used to start the container.
     * 
     * @param java the java command that will start the container
     */
    private void addSystemProperties(JvmLauncher java)
    {
        for (Map.Entry<String, String> systemProperty : getSystemProperties().entrySet())
        {
            java.setSystemProperty(systemProperty.getKey(), systemProperty.getValue());
        }
    }

    /**
     * Adds the tools.jar to the classpath, except for Mac OSX and Java 9 or above - As these JVMs
     * do not need the tools.jar.
     * 
     * @param java the JVM launcher to which to add the tools.jar
     * @exception FileNotFoundException in case the tools.jar file cannot be found
     */
    protected final void addToolsJarToClasspath(JvmLauncher java) throws FileNotFoundException
    {
        // On OSX, the tools.jar classes are included in the classes.jar so there is no need to
        // include any tools.jar file to the classpath. On Java 9, there is no more tools.jar.
        if (!JdkUtils.isOSX() && jvmMajorVersion < 9)
        {
            java.addClasspathEntries(JdkUtils.getToolsJar(getJavaHome()));
        }
    }

    /**
     * Add extra container classpath entries specified by the user.
     * 
     * @param java the java command used to start/stop the container
     */
    protected void addExtraClasspath(JvmLauncher java)
    {
        for (String extraClasspathItem : extraClasspath)
        {
            java.addClasspathEntries(extraClasspathItem);

            getLogger().debug("Adding [" + extraClasspathItem + "] to execution classpath",
                this.getClass().getName());
        }
    }

    /**
     * Add command line arguments to the java command.
     * @param java The java command
     */
    protected void addRuntimeArgs(JvmLauncher java)
    {
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
    }

    /**
     * Add the @link{GeneralPropertySet#JVMARGS} arguments to the java command.
     * @param java The java command
     * @param server Whether the command is for the server (as opposed to a JVM used for
     * deployments or other non-server actions)
     */
    private void addJvmArgs(JvmLauncher java, boolean server)
    {
        String jvmargs = getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
        String startJmvmargs = null;
        if (server)
        {
            startJmvmargs = getConfiguration().getPropertyValue(GeneralPropertySet.START_JVMARGS);
        }
        if (jvmargs != null)
        {
            // Replace new lines and tabs, so that Maven or Ant plugins can
            // specify multiline JVM arguments in their XML files
            jvmargs = jvmargs.replace('\n', ' ');
            jvmargs = jvmargs.replace('\r', ' ');
            jvmargs = jvmargs.replace('\t', ' ');

            if (jvmargs == null || !jvmargs.contains("-Xms"))
            {
                java.addJvmArguments("-Xms128m");
            }
            if (jvmargs == null || !jvmargs.contains("-Xmx"))
            {
                java.addJvmArguments("-Xmx512m");
            }

            // CARGO-1294: Warning when starting containers on Java 8
            if (jvmMajorVersion >= 8)
            {
                jvmargs.replaceAll("\\s*-XX:PermSize\\d+\\w\\s*", " ");
                jvmargs.replaceAll("\\s*-XX:MaxPermSize\\d+\\w\\s*", " ");
            }
            else
            {
                if (jvmargs == null || !jvmargs.contains("-XX:PermSize"))
                {
                    java.addJvmArguments("-XX:PermSize=48m");
                }
                if (jvmargs == null || !jvmargs.contains("-XX:MaxPermSize"))
                {
                    java.addJvmArguments("-XX:MaxPermSize=128m");
                }
            }

            if (startJmvmargs != null)
            {
                // CARGO-1535: If in server mode and the START_JVMARGS has memory-related settings,
                // then remove them from the JVMARGS
                if (startJmvmargs.contains("-Xms"))
                {
                    jvmargs.replaceAll("\\s*-Xms\\d+\\w\\s*", " ");
                }
                if (startJmvmargs.contains("-Xmx"))
                {
                    jvmargs.replaceAll("\\s*-Xmx\\d+\\w\\s*", " ");
                }
                if (startJmvmargs.contains("-XX:PermSize"))
                {
                    jvmargs.replaceAll("\\s*-XX:PermSize\\d+\\w\\s*", " ");
                }
                if (startJmvmargs.contains("-XX:MaxPermSize"))
                {
                    jvmargs.replaceAll("\\s*-XX:MaxPermSize\\d+\\w\\s*", " ");
                }
            }

            // CARGO-1556: Allow setting the JVM classpath using a -classpath or -cp argument set
            // as GeneralPropertySet.JVMARGS
            jvmargs = addJvmClasspathArguments(java, jvmargs);
            java.addJvmArgumentLine(jvmargs);
        }
    }

    /**
     * Add the @link{GeneralPropertySet#START_JVMARGS} arguments to the java command.
     * @param java The java command
     */
    private void addStartJvmArgs(JvmLauncher java)
    {
        String startJmvmargs =
            getConfiguration().getPropertyValue(GeneralPropertySet.START_JVMARGS);
        if (startJmvmargs != null)
        {
            // Replace new lines and tabs, so that Maven or Ant plugins can
            // specify multiline JVM arguments in their XML files
            startJmvmargs = startJmvmargs.replace('\n', ' ');
            startJmvmargs = startJmvmargs.replace('\r', ' ');
            startJmvmargs = startJmvmargs.replace('\t', ' ');

            // CARGO-1294: Warning when starting containers on Java 8
            if (jvmMajorVersion >= 8)
            {
                startJmvmargs.replaceAll("\\s*-XX:PermSize\\d+\\w\\s*", " ");
                startJmvmargs.replaceAll("\\s*-XX:MaxPermSize\\d+\\w\\s*", " ");
            }

            // CARGO-1556: Allow setting the JVM classpath using a -classpath or -cp argument set
            // as GeneralPropertySet.START_JVMARGS
            startJmvmargs = addJvmClasspathArguments(java, startJmvmargs);

            java.addJvmArgumentLine(startJmvmargs);
        }
    }

    /**
     * Converts the <code>-classpath</code> or <code>-cp</code> JVM arguments into classpath
     * entries.
     * 
     * @param java the predefined JVM launcher on which to add classpath entries
     * @param jvmArgs JVM arguments line
     * @return JVM arguments line with <code>-classpath</code> and <code>-cp</code> entries removed
     */
    private String addJvmClasspathArguments(JvmLauncher java, String jvmArgs)
    {
        String jvmargs = jvmArgs;
        if (jvmargs.contains("-classpath") || jvmargs.contains("-cp"))
        {
            String classpath = null;
            Matcher classpathQuotedMatcher =
                AbstractInstalledLocalContainer.classpathQuotedPattern.matcher(jvmargs);
            if (classpathQuotedMatcher.find())
            {
                classpath = classpathQuotedMatcher.group(2);
                jvmargs = classpathQuotedMatcher.replaceAll(" ");
            }
            else
            {
                Matcher classpathMatcher =
                    AbstractInstalledLocalContainer.classpathPattern.matcher(jvmargs);
                if (classpathMatcher.find())
                {
                    classpath = classpathMatcher.group(2);
                    jvmargs = classpathMatcher.replaceAll(" ");
                }
                else
                {
                    Matcher classpathFinalMatcher =
                        AbstractInstalledLocalContainer.classpathFinalPattern.matcher(jvmargs);
                    if (classpathFinalMatcher.find())
                    {
                        classpath = classpathFinalMatcher.group(2);
                        jvmargs = classpathFinalMatcher.replaceAll("");
                    }
                }
            }
            if (classpath == null)
            {
                throw new ContainerException(
                    "The JVM arguments contains a classpath entry but none of the classpath "
                        + "matchers matched");
            }
            else
            {
                getLogger().debug("Adding [" + classpath + "] to execution classpath",
                    this.getClass().getName());
                java.addClasspathEntries(classpath.split(File.pathSeparator));
            }
        }
        return jvmargs;
    }

    /**
     * Adds the JVM memory arguments.
     * 
     * @param java the predefined JVM launcher on which to add memory-related arguments
     */
    protected void addMemoryArguments(JvmLauncher java)
    {
        // If the jvmArgs don't already contain memory settings add the default
        String jvmArgs = getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
        String startJvmargs =
            getConfiguration().getPropertyValue(GeneralPropertySet.START_JVMARGS);
        if (startJvmargs != null)
        {
            if (jvmArgs == null)
            {
                jvmArgs = startJvmargs;
            }
            else
            {
                jvmArgs += " " + startJvmargs;
            }
        }
        if (jvmArgs == null || !jvmArgs.contains("-Xms"))
        {
            java.addJvmArguments("-Xms128m");
        }
        if (jvmArgs == null || !jvmArgs.contains("-Xmx"))
        {
            java.addJvmArguments("-Xmx512m");
        }

        // CARGO-1294: Warning when starting containers on Java 8
        if (jvmMajorVersion < 8)
        {
            if (jvmArgs == null || !jvmArgs.contains("-XX:PermSize"))
            {
                java.addJvmArguments("-XX:PermSize=48m");
            }
            if (jvmArgs == null || !jvmArgs.contains("-XX:MaxPermSize"))
            {
                java.addJvmArguments("-XX:MaxPermSize=128m");
            }
        }
    }

    /**
     * Add option of spawn if property exists
     * 
     * @param java the predefined JVM launcher which will spawn
     */
    private void addSpawn(JvmLauncher java)
    {
        boolean spawnProcess = Boolean.parseBoolean(getConfiguration().getPropertyValue(
            GeneralPropertySet.SPAWN_PROCESS));
        if (spawnProcess)
        {
            if (getOutput() == null)
            {
                java.setSpawn(spawnProcess);
            }
            else
            {
                getLogger().warn("Process cannot be spawned unless output is null",
                    this.getClass().getName());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void verify()
    {
        super.verify();
        verifyHome();
    }

    /**
     * Verify that the home property has been set.
     */
    private void verifyHome()
    {
        if (getHome() == null)
        {
            throw new ContainerException("You must set the mandatory [home] property");
        }

        if (!getFileHandler().isDirectory(getHome()))
        {
            throw new ContainerException("[" + getHome() + "] is not a directory. It must point "
                + "to the container home directory.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerType getType()
    {
        return ContainerType.INSTALLED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addExtraClasspath(String location)
    {
        ifPresentAddPathToList(location, extraClasspath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSharedClasspath(String location)
    {
        ifPresentAddPathToList(location, sharedClasspath);
    }

    /**
     * adds the location to the list, if the file exists.
     * 
     * @param location path to add to the list
     * @param list where to append this path
     */
    public void ifPresentAddPathToList(String location, List<String> list)
    {
        if (location == null || !this.getFileHandler().exists(location)
            || this.getFileHandler().isDirectory(location))
        {
            throw new IllegalArgumentException("Invalid file path: " + location);
        }
        list.add(location);
    }
}
