/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file was
 * originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.container.jboss.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.codehaus.cargo.container.ContainerCapability;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Abstract class for JBoss container family.
 */
public abstract class AbstractJBossInstalledLocalContainer extends
    AbstractInstalledLocalContainer implements JBossInstalledLocalContainer
{
    /**
     * Capability of the JBoss container.
     */
    private static final ContainerCapability CAPABILITY = new JBossContainerCapability();

    /**
     * JBoss version.
     */
    private String version;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(LocalConfiguration)
     */
    public AbstractJBossInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStart(JvmLauncher java) throws Exception
    {
        java.setSystemProperty("java.endorsed.dirs",
            new File(getHome(), "/lib/endorsed").getAbsolutePath());
        java.setSystemProperty("jboss.home.dir", getHome());
        java.setSystemProperty("jboss.server.home.dir", getConfiguration().getHome());
        java.setSystemProperty("jboss.server.home.url",
            new File(getConfiguration().getHome()).toURI().toURL().toString());
        java.setSystemProperty("jboss.server.name",
            getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION));
        java.setSystemProperty("jboss.server.lib.url",
                new File(getConfiguration().getHome(), "lib").toURI().toURL().toString());
        java.setSystemProperty("jboss.server.log.threshold",
            getJBossLogLevel(getConfiguration().getPropertyValue(GeneralPropertySet.LOGGING)));
        java.addAppArguments("--configuration="
            + getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION));

        // CARGO-758: To allow JBoss to be accessed from remote machines, it must be started with
        // the arguments <code>-b 0.0.0.0<code> or <code>--host 0.0.0.0<code>.
        final String runtimeArguments =
            getConfiguration().getPropertyValue(GeneralPropertySet.RUNTIME_ARGS);
        if (runtimeArguments == null || !runtimeArguments.contains("--host ")
            && !runtimeArguments.contains("-b "))
        {
            String hostname = getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME);
            if ("localhost".equals(hostname))
            {
                hostname = "0.0.0.0";
            }

            java.addAppArguments("--host=" + hostname);
        }

        java.addClasspathEntries(new File(getHome(), "bin/run.jar"));
        addToolsJarToClasspath(java);

        java.setMainClass("org.jboss.Main");

        java.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        java.addClasspathEntries(new File(getHome(), "bin/shutdown.jar"));
        java.setMainClass("org.jboss.Shutdown");

        java.addAppArguments("--server="
            + getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
            + getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));

        String username = getConfiguration().getPropertyValue(RemotePropertySet.USERNAME);
        String password = getConfiguration().getPropertyValue(RemotePropertySet.PASSWORD);
        if (username != null)
        {
            java.addAppArguments("--user=" + username);
            if (password != null)
            {
                java.addAppArguments("--password=" + password);
            }
        }

        java.execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void verify()
    {
        super.verify();

        verifyJBossHome();
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
                JarFile jarFile = new JarFile(new File(getHome(), "bin/run.jar"));
                ZipEntry entry = jarFile.getEntry("org/jboss/version.properties");
                if (entry != null)
                {
                    Properties properties = new Properties();
                    properties.load(jarFile.getInputStream(entry));
                    StringBuilder sb = new StringBuilder();
                    sb.append(properties.getProperty("version.major"));
                    sb.append(".");
                    sb.append(properties.getProperty("version.minor"));
                    sb.append(".");
                    sb.append(properties.getProperty("version.revision"));
                    version = sb.toString();

                    getLogger().info("Parsed JBoss version = [" + version + "]",
                        this.getClass().getName());
                }
                else
                {
                    getLogger().debug("Couldn't find version.properties in " + jarFile.getName(),
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
     * {@inheritDoc}
     */
    @Override
    public String getConfDir(String configurationName)
    {
        return getSpecificConfigurationDir("conf", configurationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLibDir(String configurationName)
    {
        return getSpecificConfigurationDir("lib", configurationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDeployDir(String configurationName)
    {
        String clustered = getConfiguration().getPropertyValue(JBossPropertySet.CLUSTERED);
        if (Boolean.parseBoolean(clustered))
        {
            return getSpecificConfigurationDir("farm", configurationName);
        }
        else
        {
            return getSpecificConfigurationDir("deploy", configurationName);
        }
    }

    /**
     * @param location the name of the directory to return inside the server configuration
     * @param configurationName the server configuration name to use. A server configuration is
     * located in the <code>server/</code> directory inside the JBoss installation dir.
     * @return the location of the passed directory name inside the server configuration, as a File
     */
    protected String getSpecificConfigurationDir(String location, String configurationName)
    {
        return getFileHandler().append(getHome(), "server/" + configurationName + "/" + location);
    }

    /**
     * Verify that the JBoss directory structure is valid and throw a ContainerException if not.
     */
    protected void verifyJBossHome()
    {
        List<String> requiredDirs = new ArrayList<String>();
        requiredDirs.add(getFileHandler().append(getHome(), "bin"));
        requiredDirs.add(getFileHandler().append(getHome(), "client"));
        requiredDirs.add(getFileHandler().append(getHome(), "lib"));
        requiredDirs.add(getFileHandler().append(getHome(), "lib/endorsed"));
        requiredDirs.add(getFileHandler().append(getHome(), "server"));

        String errorPrefix = "Invalid JBoss installation. ";
        String errorSuffix = "Make sure the JBoss container home directory you have specified "
            + "points to the right location (It's currently pointing to [" + getHome() + "])";

        for (String dir : requiredDirs)
        {
            if (!getFileHandler().exists(dir))
            {
                throw new ContainerException(errorPrefix + "The [" + dir
                    + "] directory doesn't exist. " + errorSuffix);
            }
            if (!getFileHandler().isDirectory(dir))
            {
                throw new ContainerException(errorPrefix + "The [" + dir
                    + "] path should be a directory. " + errorSuffix);
            }
            if (getFileHandler().isDirectoryEmpty(dir))
            {
                throw new ContainerException(errorPrefix + "The [" + dir
                    + "] directory is empty and it shouldn't be. " + errorSuffix);
            }
        }

        // Verify minimal jars for doStart() and doStop()
        String[] requiredJars = new String[] {"bin/run.jar", "bin/shutdown.jar"};
        for (String requiredJar : requiredJars)
        {
            String jarFile = getFileHandler().append(getHome(), requiredJar);
            if (!getFileHandler().exists(jarFile))
            {
                throw new ContainerException(errorPrefix + "The [" + jarFile
                    + "] JAR doesn't exist. " + errorSuffix);
            }
        }
    }

    /**
     * Translate Cargo logging levels into JBoss logging levels. The default implementation is for
     * log4j, but can be overridden by a sub-class should JBoss change logging framework.
     * 
     * @param cargoLogLevel Cargo logging level
     * @return the corresponding JBoss (log4j) logging level
     */
    protected String getJBossLogLevel(String cargoLogLevel)
    {
        String returnVal = "INFO";

        if (LoggingLevel.LOW.equalsLevel(cargoLogLevel))
        {
            returnVal = "WARN";
        }
        else if (LoggingLevel.HIGH.equalsLevel(cargoLogLevel))
        {
            returnVal = "DEBUG";
        }
        else
        {
            // accept default of medium/Info
        }

        return returnVal;
    }

}
