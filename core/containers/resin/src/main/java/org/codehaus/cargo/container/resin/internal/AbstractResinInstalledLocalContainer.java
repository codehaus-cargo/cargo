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
package org.codehaus.cargo.container.resin.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.tools.ant.types.FileSet;
import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.container.spi.util.DefaultServerRun;
import org.codehaus.cargo.util.CargoException;

/**
 * Common support for all Resin container versions.
 * 
 * @version $Id$
 */
public abstract class AbstractResinInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Parsed version of the container.
     */
    private String version;

    /**
     * Capability of the Resin container.
     */
    private ContainerCapability capability = new ServletContainerCapability();

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    public AbstractResinInstalledLocalContainer(LocalConfiguration configuration)
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
        java.addAppArguments("-conf");

        File confDir = new File(getConfiguration().getHome(), "conf");
        java.addAppArgument(new File(confDir, "resin.conf"));

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

        java.start();
    }

    /**
     * Common Ant Java task settings for start and stop actions.
     * 
     * @param java the JVM launcher passed by the Cargo underlying container SPI classes
     */
    private void doAction(JvmLauncher java)
    {
        // Invoke the main class to start the container
        java.setSystemProperty("resin.home", getConfiguration().getHome());

        // As Resin has not feature to stop it, we're using a ResinRun class that keeps a reference
        // to the running Resin server and which creates a listener socket so that it can then
        // stop Resin when it receives the signal to do so.
        java.setMainClass(ResinRun.class.getName());

        // However this ResinRun class depends on classes found in other Cargo jars (namely, in
        // Core Util and Core Container) so we also need to include those jars in the container
        // execution classpath.
        java.addClasspathEntries(getResourceUtils().getResourceLocation("/"
            + ResinRun.class.getName().replace('.', '/') + ".class"));
        java.addClasspathEntries(getResourceUtils().getResourceLocation("/"
            + DefaultServerRun.class.getName().replace('.', '/') + ".class"));
        java.addClasspathEntries(getResourceUtils().getResourceLocation("/"
            + CargoException.class.getName().replace('.', '/') + ".class"));

        FileSet fileSet = new FileSet();
        fileSet.setProject(getAntUtils().createProject());
        fileSet.setDir(new File(getHome()));
        fileSet.createInclude().setName("lib/*.jar");
        for (String path : fileSet.getDirectoryScanner().getIncludedFiles())
        {
            java.addClasspathEntries(new File(fileSet.getDir(), path));
        }
    }

    /**
     * Allow specific version implementations to add custom settings to the Java container that will
     * be started.
     * 
     * @param javaContainer the JVM launcher that will start the container
     * @throws FileNotFoundException in case the Tools jar cannot be found
     */
    protected abstract void startUpAdditions(JvmLauncher javaContainer)
        throws FileNotFoundException;

    /**
     * @param defaultVersion default version to use if we cannot find out the exact Resin version
     * @return the Resin version found
     */
    protected String getVersion(String defaultVersion)
    {
        String version = this.version;

        if (version == null)
        {
            try
            {
                URLClassLoader classloader = new URLClassLoader(
                    new URL[] {new File(getHome(), "/lib/resin.jar").toURI().toURL()});
                version = new ResinUtil().getResinVersion(classloader);
                getLogger().info("Found Resin version [" + version + "]",
                    this.getClass().getName());
            }
            catch (Exception e)
            {
                getLogger().debug("Failed to get Resin version, Error = [" + e.getMessage()
                    + "]. Using generic version [" + defaultVersion + "]",
                    this.getClass().getName());
                version = defaultVersion;
            }
        }
        this.version = version;
        return version;
    }
}
