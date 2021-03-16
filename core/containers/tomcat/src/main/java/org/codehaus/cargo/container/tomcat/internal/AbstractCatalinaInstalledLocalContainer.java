/*
 * ========================================================================
 *
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2021 Ali Tokmen.
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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.File;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Base support for Catalina based installed local containers.
 */
public abstract class AbstractCatalinaInstalledLocalContainer extends
    AbstractInstalledLocalContainer
{
    /**
     * Capability of the Tomcat/Catalina container.
     */
    private ContainerCapability capability = new ServletContainerCapability();

    /**
     * Parsed version of the container.
     */
    private String version;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public AbstractCatalinaInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * Returns the version of the Tomcat installation.
     * 
     * @param defaultVersion default version to use if we cannot find out the exact Tomcat version
     * @return The Tomcat version, or <code>null</code> if the version number could not be retrieved
     */
    protected synchronized String getVersion(String defaultVersion)
    {
        String version = this.version;

        if (version == null)
        {
            try (JarFile catalinaJar = new JarFile(new File(getHome(), "server/lib/catalina.jar")))
            {
                // Unfortunately, there's no safe way to find out the version of a Catalina
                // installation, so we need to try multiple paths here

                // Tomcat 4.1.0 and later includes a ServerInfo.properties
                // resource in catalina.jar that contains the version number. If
                // that resource doesn't exist, we're on Tomcat 4.0.x
                ZipEntry entry =
                    catalinaJar.getEntry("org/apache/catalina/util/ServerInfo.properties");
                if (entry != null)
                {
                    Properties props = new Properties();
                    props.load(catalinaJar.getInputStream(entry));
                    String serverInfo = props.getProperty("server.info");
                    int slashPos = serverInfo.indexOf('/');
                    if (slashPos > 0)
                    {
                        version = serverInfo.substring(slashPos + 1);
                    }
                }
                else
                {
                    version = "4.0.x";
                }
            }
            catch (Exception e)
            {
                version = defaultVersion;
                getLogger().debug(
                    "Failed to find Tomcat version, base error [" + e.getMessage() + "]",
                    this.getClass().getName());
            }

            getLogger().debug("Parsed Tomcat version = [" + version + "]",
                this.getClass().getName());

            this.version = version;
        }

        return version;
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
        // Invoke the server main class
        invokeContainer("start", java);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStop(JvmLauncher java) throws Exception
    {
        // invoke the main class
        invokeContainer("stop", java);
    }

    /**
     * Configures the extra classpath if and only if:
     * <ul>
     * <li>We are not on an {@link AbstractCatalinaStandaloneLocalConfiguration}<br>or</li>
     * <li>We are on an {@link AbstractCatalinaStandaloneLocalConfiguration} and specify a third
     * party JVM logger (see <a href="https://codehaus-cargo.atlassian.net/browse/CARGO-1556">
     * CARGO-1556</a>).
     * </ul>
     * {@inheritDoc}
     */
    @Override
    protected void addExtraClasspath(JvmLauncher java)
    {
        if (getConfiguration() instanceof AbstractCatalinaStandaloneLocalConfiguration)
        {
            String jvmArgs = getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
            if (jvmArgs != null && jvmArgs.contains("java.util.logging.manager"))
            {
                // CARGO-1556: If a dedicated logging manager is defined on the JVM level, most
                // likely the associated classes are in the extra classpath configuration. Hence,
                // add these JARs to the start/stop JVM classpath instead of common/lib.
                super.addExtraClasspath(java);
            }
            else
            {
                // If we are in a AbstractCatalinaStandaloneLocalConfiguration, by default the JARs
                // in the extra classpath are copied over to common/lib for the Tomcat bootstrap to
                // load them. Hence, do not add the extra classpath to the JVM.
            }
        }
        else
        {
            super.addExtraClasspath(java);
        }
    }

    /**
     * Checks all ports except for {@link GeneralPropertySet#RMI_PORT}, to avoid bug <a
     * href="https://codehaus-cargo.atlassian.net/browse/CARGO-1337">CARGO-1337</a>. {@inheritDoc}
     */
    @Override
    protected void waitForPortShutdown(int port, int connectTimeout, long deadline)
        throws InterruptedException
    {
        int rmiPort = 0;
        try
        {
            rmiPort = Integer.parseInt(
                getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));
        }
        catch (Throwable ignored)
        {
            // Ignored
        }
        if (port != rmiPort)
        {
            super.waitForPortShutdown(port, connectTimeout, deadline);
        }
    }

    /**
     * Invokes the container bootstrap class to start or stop the container, depending on the value
     * of the provided argument.
     * 
     * @param action Either 'start' or 'stop'
     * @param java the prepared Ant Java command that will be executed
     * 
     * @throws Exception in case of container invocation error
     */
    protected void invokeContainer(String action, JvmLauncher java) throws Exception
    {
        String base = getFileHandler().getAbsolutePath(getConfiguration().getHome());
        java.setSystemProperty("catalina.home", getFileHandler().getAbsolutePath(getHome()));
        java.setSystemProperty("catalina.base", base);

        // CARGO-1220: Allow users to override some system properties
        String jvmArgs = getConfiguration().getPropertyValue(GeneralPropertySet.JVMARGS);
        if (jvmArgs == null || !jvmArgs.contains("java.io.tmpdir"))
        {
            java.setSystemProperty("java.io.tmpdir",
                getFileHandler().append(base, "temp"));
        }
        if (jvmArgs == null || !jvmArgs.contains("java.util.logging.manager"))
        {
            java.setSystemProperty("java.util.logging.manager",
                "org.apache.juli.ClassLoaderLogManager");
        }
        if (jvmArgs == null || !jvmArgs.contains("java.util.logging.config.file"))
        {
            java.setSystemProperty("java.util.logging.config.file",
                getFileHandler().append(base, "conf/logging.properties"));
        }

        java.addClasspathEntries(new File(getHome(), "bin/bootstrap.jar"));
        addToolsJarToClasspath(java);
        java.setMainClass("org.apache.catalina.startup.Bootstrap");
        java.addAppArguments(action);
        java.start();
    }

}
