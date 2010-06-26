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
package org.codehaus.cargo.container.jrun.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.AntContainerExecutorThread;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.jrun.JRun4xPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;

/**
 * Common support for all JRun container versions.
 * 
 * @version $Id$
 */
public abstract class AbstractJRunInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Parsed version of the container.
     */
    private String version;

    /**
     * Capability of the JRun container.
     */
    private ContainerCapability capability = new J2EEContainerCapability();

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    public AbstractJRunInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getCapability()
     */
    public ContainerCapability getCapability()
    {
        return this.capability;
    }
    
    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStart(Java)
     */
    @Override
    public void doStart(Java java) throws Exception
    {
        Path classpath = doAction(java);
        
        java.createArg().setValue("-start");
        java.createArg().setValue(getConfiguration().getPropertyValue(
                JRun4xPropertySet.SERVER_NAME));

        // Add settings specific to a given container version
        startUpAdditions(java, classpath);

        AntContainerExecutorThread jrunRunner = new AntContainerExecutorThread(java);
        jrunRunner.start();
    }
    
    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStop(Java)
     */
    @Override
    public void doStop(Java java) throws Exception
    {
        doAction(java);

        java.createArg().setValue("-stop");
        java.createArg().setValue(getConfiguration().getPropertyValue(
                JRun4xPropertySet.SERVER_NAME));

        AntContainerExecutorThread jrunRunner = new AntContainerExecutorThread(java);
        jrunRunner.start();
    }

    /**
     * Common Ant Java task settings for start and stop actions.
     *
     * @param java the Ant Java object passed by the Cargo underlying container SPI classes
     * @return the classpath set (this is required as strangely there's no way to query the Ant
     *         Java object for the classapth after it's set)
     */
    private Path doAction(Java java)
    {
        // Invoke the main class to start the container
        java.addSysproperty(getAntUtils().createSysProperty("jrun.home", 
            getConfiguration().getHome()));
        
        java.setClassname("jrunx.kernel.JRun");

        Path classPath = java.createClasspath();   
        classPath.setPath(getConfiguration().getHome() + "/lib/jrun.jar");
        
        FileSet libFileSet = new FileSet();
        libFileSet.setDir(new File(getHome() + "/lib"));
        libFileSet.setIncludes("webservices.jar,macromedia_drivers.jar");
        classPath.addFileset(libFileSet);
        
        return classPath;
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractLocalContainer#waitForCompletion(boolean)
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        super.waitForCompletion(waitForStarting);

        if (!waitForStarting)
        {
            // JRun stop is not synchronous, therefore sleep a bit after the
            // CARGO ping component has stopped in order to allow some time for
            // the server to stop completely
            Thread.sleep(10000);
        }
    }
    
    /**
     * Allow specific version implementations to add custom settings to the 
     * Java container that will be started.
     * 
     * @param javaContainer the Ant Java object that will start the container
     * @param classpath the classpath that will be used to start the 
     *        container
     * @throws FileNotFoundException in case the Tools jar cannot be found
     */
    protected abstract void startUpAdditions(Java javaContainer, Path classpath) 
        throws FileNotFoundException;

    /**
     * @param defaultVersion default version to use if we cannot find out the exact JRun version
     * @return the JRun version found
     */
    protected String getVersion(String defaultVersion)
    {
        String version = this.version;
        
        if (version == null)
        {
            try
            {
                JarFile jRunJar = new JarFile(new File(getHome(), "/lib/jrun.jar"));
                ZipEntry entry = jRunJar.getEntry("jrunx/kernel/resource.properties");
                if (entry != null)
                {
                    Properties props = new Properties();
                    props.load(jRunJar.getInputStream(entry));
                    version = props.getProperty("jrun.version");
                }
                else
                {
                    version = "4.x";
                }
            }
            catch (Exception e)
            {
                getLogger().debug("Failed to get JRun version, Error = [" + e.getMessage()
                    + "]. Using generic version [" + defaultVersion + "]", 
                    this.getClass().getName());
                version = defaultVersion;
            }
        }
        this.version = version;
        return version;
    }
}
