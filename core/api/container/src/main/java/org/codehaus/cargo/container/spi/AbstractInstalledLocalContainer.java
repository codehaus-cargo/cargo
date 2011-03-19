/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.container.spi;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.taskdefs.condition.Os;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.container.internal.util.JdkUtils;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.SSHPropertySet;
import org.codehaus.cargo.container.spi.jvm.DefaultJvmLauncherFactory;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherFactory;
import org.codehaus.cargo.container.spi.jvm.JvmLauncherRequest;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.log.Logger;

/**
 * Default container implementation that all local installed container implementations must extend.
 * 
 * @version $Id$
 */
public abstract class AbstractInstalledLocalContainer extends AbstractLocalContainer implements
    InstalledLocalContainer
{
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
     * JDK utility class.
     */
    private JdkUtils jdkUtils;

    /**
     * Ant utility class.
     */
    private AntUtils antUtils;

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
     * Default constructor.
     * 
     * @param configuration the configuration to associate to this container. It can be changed
     * later on by calling {@link #setConfiguration(LocalConfiguration)}
     */
    public AbstractInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);

        this.jdkUtils = new JdkUtils();
        this.antUtils = new AntUtils();
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
     * @see org.codehaus.cargo.util.log.Loggable#setLogger(org.codehaus.cargo.util.log.Logger)
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
     * @return the JDK utility class
     */
    protected final JdkUtils getJdkUtils()
    {
        return this.jdkUtils;
    }

    /**
     * @return the Ant utility class
     */
    protected final AntUtils getAntUtils()
    {
        return this.antUtils;
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
     * 
     * @see InstalledLocalContainer#setHome(String)
     */
    public final void setHome(String home)
    {
        this.home = home;
    }

    /**
     * {@inheritDoc}
     * 
     * @see InstalledLocalContainer#setSystemProperties
     */
    public void setSystemProperties(Map<String, String> properties)
    {
        Map<String, String> props = new HashMap<String, String>();
        props.putAll(properties);

        this.systemProperties = props;
    }

    /**
     * {@inheritDoc}
     * 
     * @see InstalledLocalContainer#getSystemProperties()
     */
    public Map<String, String> getSystemProperties()
    {
        return this.systemProperties;
    }

    /**
     * {@inheritDoc}
     * 
     * @see InstalledLocalContainer#setExtraClasspath(String[])
     */
    public void setExtraClasspath(String[] classpath)
    {
        this.extraClasspath.clear();
        this.extraClasspath.addAll(Arrays.asList(classpath));
    }

    /**
     * {@inheritDoc}
     * 
     * @see InstalledLocalContainer#getExtraClasspath()
     */
    public String[] getExtraClasspath()
    {
        return this.extraClasspath.toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     * 
     * @see InstalledLocalContainer#getHome()
     */
    public void setSharedClasspath(String[] classpath)
    {
        this.sharedClasspath.clear();
        this.sharedClasspath.addAll(Arrays.asList(classpath));
    }

    /**
     * {@inheritDoc}
     * 
     * @see InstalledLocalContainer#getSharedClasspath()
     */
    public String[] getSharedClasspath()
    {
        return this.sharedClasspath.toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     * 
     * @see InstalledLocalContainer#getHome()
     */
    public final String getHome()
    {
        return this.home;
    }

    /**
     * {@inheritDoc}
     */
    public JvmLauncherFactory getJvmLauncherFactory()
    {
        return jvmLauncherFactory;
    }

    /**
     * {@inheritDoc}
     */
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
     * 
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#startInternal()
     */
    @Override
    protected final void startInternal() throws Exception
    {
        JvmLauncher java = createJvmLauncher(true);
        addMemoryArguments(java);
        doStart(java);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#stopInternal()
     */
    @Override
    protected final void stopInternal() throws Exception
    {
        doStop(createJvmLauncher(false));
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
        boolean ssh = getConfiguration().getPropertyValue(SSHPropertySet.HOST) != null;

        JvmLauncherRequest request = new JvmLauncherRequest(server, this, ssh);

        JvmLauncher java = jvmLauncherFactory.createJvmLauncher(request);

        if (ssh)
        {
            addSshProperties(java);
        }

        // If the user has not specified any output file then the process's output will be logged
        // to the Ant logging subsystem which will in turn go to the Cargo's logging subsystem as
        // we're configuring Ant with our own custom build listener (see below).
        if (getOutput() != null)
        {
            File outputFile = new File(getOutput());

            // Ensure that directories where the output file will go are created
            getFileHandler().mkdirs(outputFile.getAbsoluteFile().getParent());

            java.setOutputFile(outputFile);
            java.setAppendOutput(isAppend());
        }

        setJvmToLaunchContainerIn(java);

        // Add extra container classpath entries specified by the user.
        addExtraClasspath(java);

        // Add system properties for the container JVM
        addSystemProperties(java);

        // Add runtime arguments if present
        addRuntimeArgs(java);

        // Add JVM args if defined
        addJvmArgs(java);

        return java;
    }

    /**
     * Adds in parameters necessary to identify this as a cargo-launched container.
     * 
     * @param java the java command that will start the container
     */
    private void addSshProperties(JvmLauncher java)
    {
        // setup working directory
        java.setWorkingDirectory(new File(getFileHandler().getAbsolutePath(
            getConfiguration().getHome())));

        if (getConfiguration().getDeployables() != null)
        {
            for (Deployable toDeploy : getConfiguration().getDeployables())
            {
                java.setSystemProperty(
                    "sshjava.shift." + getFileHandler().getAbsolutePath(toDeploy.getFile()),
                    "deployables/" + new File(toDeploy.getFile()).getName());
            }
        }

        if (getHome() != null)
        {
            java.setSystemProperty("sshjava.shift." + getFileHandler().getAbsolutePath(getHome()),
                "containers/" + getId());
        }

        Properties properties = new Properties();
        properties.put("sshjava.username", SSHPropertySet.USERNAME);
        properties.put("sshjava.host", SSHPropertySet.HOST);
        properties.put("sshjava.password", SSHPropertySet.PASSWORD);
        properties.put("sshjava.keyfile", SSHPropertySet.KEYFILE);
        properties.put("sshjava.remotebase", SSHPropertySet.REMOTEBASE);

        for (Map.Entry<?, ?> entry : properties.entrySet())
        {
            java.setSystemProperty(entry.getKey().toString(),
                getConfiguration().getPropertyValue(entry.getValue().toString()));
        }
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
    protected void setJvmToLaunchContainerIn(JvmLauncher java)
    {
        String javaHome = getJavaHome();
        if (javaHome != null)
        {
            String binDir = getFileHandler().append(javaHome, "bin");
            String javaPath = getFileHandler().append(binDir, "java");
            if (Os.isFamily("windows"))
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
     * Adds the tools.jar to the classpath, except for Mac OSX as it is not needed.
     * 
     * @param java the JVM launcher to which to add the tools.jar
     * @exception FileNotFoundException in case the tools.jar file cannot be found
     */
    protected final void addToolsJarToClasspath(JvmLauncher java) throws FileNotFoundException
    {
        // On OSX, the tools.jar classes are included in the classes.jar so there is no need to
        // include any tools.jar file to the cp.
        if (!getJdkUtils().isOSX())
        {
            java.addClasspathEntries(getJdkUtils().getToolsJar(getJavaHome()));
        }
    }

    /**
     * Add extra container classpath entries specified by the user.
     * 
     * @param java the java command used to start/stop the container
     */
    private void addExtraClasspath(JvmLauncher java)
    {
        if (extraClasspath.size() > 0)
        {
            for (String extraClasspathItem : extraClasspath)
            {
                java.addClasspathEntries(extraClasspathItem);

                getLogger().debug("Adding [" + extraClasspathItem + "] to execution classpath",
                    this.getClass().getName());
            }
        }
    }

    /**
     * Add command line arguments to the java command.
     * @param java The java command
     */
    private void addRuntimeArgs(JvmLauncher java)
    {
        String runtimeArgs = getConfiguration().getPropertyValue(GeneralPropertySet.RUNTIME_ARGS);
        if (runtimeArgs != null)
        {
            String[] arguments = runtimeArgs.split(" ");
            java.addAppArguments(arguments);
        }
    }

    /**
     * Add the @link{GeneralPropertySet#JVMARGS} arguments to the java command.
     * @param java The java command
     */
    private void addJvmArgs(JvmLauncher java)
    {
        String jvmargs = getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
        if (jvmargs != null)
        {
            // Replace new lines and tabs, so that Maven or ANT plugins can
            // specify multiline JVM arguments in their XML files
            jvmargs = jvmargs.replace('\n', ' ');
            jvmargs = jvmargs.replace('\r', ' ');
            jvmargs = jvmargs.replace('\t', ' ');
            java.addJvmArgumentLine(jvmargs);
        }
    }

    /**
     * Adds the JVM memory arguments.
     * 
     * @param java the predefined JVM launcher on which to add memory-related arguments
     */
    protected void addMemoryArguments(JvmLauncher java)
    {
        // if the jvmArgs don't alread contain memory settings add the default
        String jvmArgs = getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
        if (jvmArgs == null || !jvmArgs.contains("-Xms"))
        {
            java.addJvmArguments("-Xms128m");
        }
        if (jvmArgs == null || !jvmArgs.contains("-Xmx"))
        {
            java.addJvmArguments("-Xmx512m");
        }
        if (jvmArgs == null || !jvmArgs.contains("-XX:PermSize"))
        {
            java.addJvmArguments("-XX:PermSize=48m");
        }
        if (jvmArgs == null || !jvmArgs.contains("-XX:MaxPermSize"))
        {
            java.addJvmArguments("-XX:MaxPermSize=128m");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#verify()
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
     * 
     * @see org.codehaus.cargo.container.Container#getType()
     */
    public ContainerType getType()
    {
        return ContainerType.INSTALLED;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.SpawnedContainer#addExtraClasspath()
     */
    public void addExtraClasspath(String location)
    {
        ifPresentAddPathToList(location, extraClasspath);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.SpawnedContainer#addSharedClasspath()
     */
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
        if (location == null || !this.getFileHandler().exists(location))
        {
            throw new IllegalArgumentException("Invalid file path: " + location);
        }
        list.add(location);
    }
}
