/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.container.resin.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Common support for all Resin container versions.
 */
public abstract class AbstractResinInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Inclusion filter for all JAR files.
     */
    private static final List<String> ALL_JARS = Arrays.asList("*.jar");

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
     */
    @Override
    public ContainerCapability getCapability()
    {
        return this.capability;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStart(JvmLauncher java) throws Exception
    {
        doAction(java);

        java.addAppArguments("-start");
        java.addAppArguments("-conf");

        File confDir = new File(getConfiguration().getHome(), "conf");
        java.addAppArgument(new File(confDir, getResinConfigurationFileName()));

        // Add settings specific to a given container version
        startUpAdditions(java);

        java.start();
    }

    /**
     * {@inheritDoc}
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

        // Add the JAR for the ResinRun class.
        // Currently, the same JAR also contains all dependencies of ResinRun as well.
        java.addClasspathEntries(getResourceUtils().getResourceLocation(this.getClass(),
            "/" + ResinRun.class.getName().replace('.', '/') + ".class"));

        for (String path : getFileHandler().getChildren(
            getFileHandler().append(getHome(), "lib"),
                AbstractResinInstalledLocalContainer.ALL_JARS))
        {
            java.addClasspathEntries(new File(path));
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
    protected synchronized String getVersion(String defaultVersion)
    {
        String version = this.version;

        if (version == null)
        {
            // TODO: We cannot use try-with-resources here as Resin needs to be Java 6 compatible
            URLClassLoader classloader = null;
            try
            {
                classloader = new URLClassLoader(
                    new URL[] {new File(getHome(), "/lib/resin.jar").toURI().toURL()});

                Class versionClass = classloader.loadClass("com.caucho.Version");
                Field versionField = versionClass.getField("VERSION");
                version = (String) versionField.get(null);

                if (version.startsWith("resin-"))
                {
                    version = version.substring(6);
                }

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
            finally
            {
                try
                {
                    // TODO: We cannot use URLClassLoader.close()
                    //       due to the javac --release 6 constraint
                    // classloader.close();
                }
                catch (Exception ignored)
                {
                    // Ignored
                }
            }
        }
        this.version = version;
        return version;
    }

    /**
     * @return Resin configuration file name.
     */
    protected String getResinConfigurationFileName()
    {
        return "resin.conf";
    }
}
