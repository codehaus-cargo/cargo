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
package org.codehaus.cargo.container.jrun.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.tools.ant.types.FileSet;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.J2EEContainerCapability;
import org.codehaus.cargo.container.jrun.JRun4xPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

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
     * @see AbstractInstalledLocalContainer#doStart(JvmLauncher)
     */
    @Override
    public void doStart(JvmLauncher java) throws Exception
    {
        doAction(java);

        java.addAppArguments("-start");
        java.addAppArguments(getConfiguration().getPropertyValue(JRun4xPropertySet.SERVER_NAME));

        // Add settings specific to a given container version
        startUpAdditions(java);

        java.start();
    }

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#doStop(JvmLauncher)
     */
    @Override
    public void doStop(JvmLauncher java) throws Exception
    {
        doAction(java);

        java.addAppArguments("-stop");
        java.addAppArguments(getConfiguration().getPropertyValue(JRun4xPropertySet.SERVER_NAME));

        java.start();
    }

    /**
     * Common Ant Java task settings for start and stop actions.
     * 
     * @param java the Ant Java object passed by the Cargo underlying container SPI classes
     */
    private void doAction(JvmLauncher java)
    {
        // Invoke the main class to start the container
        java.setSystemProperty("jrun.home", getConfiguration().getHome());

        java.setMainClass("jrunx.kernel.JRun");

        java.addClasspathEntries(getConfiguration().getHome() + "/lib/jrun.jar");

        FileSet libFileSet = new FileSet();
        libFileSet.setProject(getAntUtils().createProject());
        libFileSet.setDir(new File(getHome() + "/lib"));
        libFileSet.setIncludes("webservices.jar,macromedia_drivers.jar");
        for (String path : libFileSet.getDirectoryScanner().getIncludedFiles())
        {
            java.addClasspathEntries(new File(libFileSet.getDir(), path));
        }
    }

    /**
     * Allow specific version implementations to add custom settings to the Java container that will
     * be started.
     * 
     * @param java the JVM launcher that will start the container
     * @throws FileNotFoundException in case the Tools jar cannot be found
     */
    protected abstract void startUpAdditions(JvmLauncher java)
        throws FileNotFoundException;

    /**
     * @param defaultVersion default version to use if we cannot find out the exact JRun version
     * @return the JRun version found
     */
    protected synchronized String getVersion(String defaultVersion)
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
