/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import org.apache.commons.vfs.util.Os;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.util.AntBuildListener;
import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.container.internal.util.JdkUtils;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.log.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.File;

/**
 * Default container implementation that all local installed container implementations must extend.
 * 
 * @version $Id$
 */
public abstract class AbstractInstalledLocalContainer
    extends AbstractLocalContainer implements InstalledLocalContainer
{
    /**
     * List of system properties to set in the container JVM. 
     */
    private Map systemProperties;

    /**
     * Additional classpath entries for the classpath that will be used to 
     * start the containers.
     */
    private String[] extraClasspath;    

    /**
     * Additional classpath entries for the classpath that will be shared by
     * the container applications.
     */
    private String[] sharedClasspath;

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
     * File utility class.
     */
    private FileHandler fileHandler;
    
    /**
     * Default constructor.
     * @param configuration the configuration to associate to this container. It can be changed
     *        later on by calling {@link #setConfiguration(LocalConfiguration)}
     */
    public AbstractInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);

        this.jdkUtils = new JdkUtils();
        this.fileHandler = new DefaultFileHandler();
        this.antUtils = new AntUtils();
        this.resourceUtils = new ResourceUtils();
        this.httpUtils = new HttpUtils();
    }

    /**
     * Overriden in order to set the logger on ancillary components.
     * @param logger the logger to set and set in the ancillary objects
     * @see org.codehaus.cargo.util.log.Loggable#setLogger(org.codehaus.cargo.util.log.Logger)
     */
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
     * @return the Cargo file utility class
     */
    public FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the Cargo file utility class to use. This method is useful for unit
     *        testing with Mock objects as it can be passed a test file handler that doesn't perform
     *        any real file action.
     */
    public void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }
    
    /**
     * {@inheritDoc}
     * @see InstalledLocalContainer#setHome(String) 
     */
    public final void setHome(String home)
    {
        this.home = home;
    }

    /**
     * {@inheritDoc}
     * @see InstalledLocalContainer#setSystemProperties
     */
    public void setSystemProperties(Map properties)
    {
        Map props = new HashMap();
        props.putAll(properties);
        
        this.systemProperties = props;
    }

    /**
     * {@inheritDoc}
     * @see InstalledLocalContainer#getSystemProperties()
     */
    public Map getSystemProperties()
    {
        return this.systemProperties;
    }

    /**
     * {@inheritDoc}
     * @see InstalledLocalContainer#setExtraClasspath(String[])
     */
    public void setExtraClasspath(String[] classpath)
    {
        this.extraClasspath = classpath;
    }

    /**
     * {@inheritDoc}
     * @see InstalledLocalContainer#getExtraClasspath()
     */
    public String[] getExtraClasspath()
    {
        return this.extraClasspath;
    }   

    /**
     * {@inheritDoc}
     * @see InstalledLocalContainer#getHome()
     */
    public void setSharedClasspath(String[] classpath)
    {
        this.sharedClasspath = classpath;
    }

    /**
     * {@inheritDoc}
     * @see InstalledLocalContainer#getSharedClasspath()
     */
    public String[] getSharedClasspath()
    {
        return this.sharedClasspath;
    }

    /**
     * {@inheritDoc}
     * @see InstalledLocalContainer#getHome()
     */
    public final String getHome()
    {
        return this.home;
    }

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#start()} that all
     * containers extending this class must implement.
     *
     * @param java the predefined Ant {@link org.apache.tools.ant.taskdefs.Java} command to use to
     *             start the container
     * @throws Exception if any error is raised during the container start
     */
    protected abstract void doStart(Java java) throws Exception;

    /**
     * Implementation of {@link org.codehaus.cargo.container.LocalContainer#stop()} that all
     * containers extending this class must implement.
     *
     * @param java the predefined Ant {@link Java} command to use to stop the container
     * @throws Exception if any error is raised during the container stop
     */
    protected abstract void doStop(Java java) throws Exception;

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#startInternal()
     */
    protected final void startInternal() throws Exception
    {
        doStart(createJavaTask());
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#stopInternal()
     */
    protected final void stopInternal() throws Exception
    {
        doStop(createJavaTask());
    }

    /**
     * Creates a preinitialized instance of the Ant Java task to be used for starting and shutting
     * down the container.
     *
     * @return The created task instance
     */
    private Java createJavaTask()
    {
        Java java = (Java) getAntUtils().createAntTask("java");
        java.setFork(true);

        // If the user has not specified any output file then the process's output will be logged
        // to the Ant logging subsystem which will in turn go to the Cargo's logging subsystem as
        // we're configuring Ant with our own custom build listener (see below).
        if (getOutput() != null)
        {
            java.setOutput(new File(getOutput()));
            java.setAppend(isAppend());
        }

        // Add a build listener to the Ant project so that we can catch what the Java task logs
        java.getProject().addBuildListener(
            new AntBuildListener(getLogger(), this.getClass().getName()));

        setJvmToLaunchContainerIn(java); 

        // Add extra container classpath entries specified by the user.
        addExtraClasspath(java);
       
        // Add system properties for the container JVM
        addSystemProperties(java);

        // Add JVM args if defined
        String jvmargs = getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS); 
        if (jvmargs != null)
        {
            java.createJvmarg().setLine(jvmargs);
        }
        
        return java;
    }

    /**
     * Determines which java virtual machine will run the container.
     * 
     * @param java the java command that will start the container
     */
    protected void setJvmToLaunchContainerIn(Java java)
    {
        String javaHome = getConfiguration().getPropertyValue(GeneralPropertySet.JAVA_HOME);
        if (javaHome != null)
        {
            String binDir = getFileHandler().append(javaHome, "bin");
            String javaPath = getFileHandler().append(binDir, "java");
            if (Os.isFamily(Os.OS_FAMILY_WINDOWS))
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
    private void addSystemProperties(Java java)
    {
        if (getSystemProperties() != null)
        {
            Iterator keys = getSystemProperties().keySet().iterator();
            while (keys.hasNext())
            {
                String key = (String) keys.next();
    
                java.addSysproperty(getAntUtils().createSysProperty(key, 
                    (String) getSystemProperties().get(key)));
            }
        }
    }
    
    /**
     * Adds the tools.jar to the classpath, except for Mac OSX as it is not
     * needed.
     * 
     * @param classpath the classpath object to which to add the tools.jar
     * @exception FileNotFoundException in case the tools.jar file cannot be
     *            found
     */
    protected final void addToolsJarToClasspath(Path classpath) throws FileNotFoundException
    {
        // On OSX, the tools.jar classes are included in the classes.jar so there is no need to 
        // include any tools.jar file to the cp.
        if (!getJdkUtils().isOSX())
        {    
            String javaHome = getConfiguration().getPropertyValue(GeneralPropertySet.JAVA_HOME);
            if (javaHome == null)
            {
                classpath.createPathElement().setLocation(getJdkUtils().getToolsJar());
            }  
            else 
            {
                String libDir = getFileHandler().append(javaHome, "lib");
                classpath.setLocation(new File(libDir, "tools.jar"));
            }
            
        }
    }
    
    /**
     * Add extra container classpath entries specified by the user.
     * 
     * @param javaCommand the java command used to start/stop the container
     */
    private void addExtraClasspath(Java javaCommand)
    {
        Path classpath = javaCommand.createClasspath();
        if (getExtraClasspath() != null)
        {
            Path path = new Path(getAntUtils().createProject());

            for (int i = 0; i < getExtraClasspath().length; i++)
            {
                Path pathElement = new Path(getAntUtils().createProject(), getExtraClasspath()[i]);
                path.addExisting(pathElement);

                getLogger().debug("Adding [" + pathElement + "] to execution classpath",
                    this.getClass().getName());
            }
            
            classpath.addExisting(path);
        }        
    }    

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#verify()
     */
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
     * @see org.codehaus.cargo.container.Container#getType()
     */
    public ContainerType getType()
    {
        return ContainerType.INSTALLED;
    }
}
