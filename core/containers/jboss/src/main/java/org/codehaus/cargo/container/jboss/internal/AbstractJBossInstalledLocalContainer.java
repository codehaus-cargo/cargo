/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file was
 * originally imported from the Jakarta Cactus project.
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
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;

/**
 * Abstract class for JBoss container family.
 * 
 * @version $Id$
 */
public abstract class AbstractJBossInstalledLocalContainer extends
    AbstractInstalledLocalContainer implements JBossInstalledLocalContainer
{
    /**
     * Capability of the JBoss Container.
     */
    private ContainerCapability capability = new JBossContainerCapability();

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
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#doStart(JvmLauncher)
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
        java.setSystemProperty(
                "jboss.server.lib.url",
                new File(getLibDir(getConfiguration().getPropertyValue(
                    JBossPropertySet.CONFIGURATION)))
                    .toURI().toURL().toString());
        java.setSystemProperty("jboss.server.log.threshold",
            getJBossLogLevel(getConfiguration().getPropertyValue(GeneralPropertySet.LOGGING)));

        // CARGO-758: To allow JBoss to be accessed from remote machines,
        // it must be started with the arguments -b 0.0.0.0 or --host 0.0.0.0
        //
        // As a result, allow the --host or -b to be different than GeneralPropertySet.HOSTNAME
        final String runtimeArguments = getConfiguration().getPropertyValue(
            GeneralPropertySet.RUNTIME_ARGS);
        if (runtimeArguments == null || !runtimeArguments.contains("--host 0.0.0.0")
            && !runtimeArguments.contains("-b 0.0.0.0"))
        {
            java.addAppArguments("--host="
                + getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME));
        }
        java.addAppArguments("--configuration="
            + getConfiguration().getPropertyValue(JBossPropertySet.CONFIGURATION));

        java.addClasspathEntries(new File(getHome(), "bin/run.jar"));
        addToolsJarToClasspath(java);

        java.setMainClass("org.jboss.Main");

        java.start();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#doStop(JvmLauncher)
     */
    @Override
    protected void doStop(JvmLauncher java) throws Exception
    {
        java.addClasspathEntries(new File(getHome(), "bin/shutdown.jar"));
        java.setMainClass("org.jboss.Shutdown");

        java.addAppArguments("--server="
            + getConfiguration().getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
            + getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));

        String jbossUser = getConfiguration().getPropertyValue(JBossPropertySet.JBOSS_USER);
        String jbossPassword = getConfiguration().getPropertyValue(JBossPropertySet.JBOSS_PASSWORD);
        if (jbossUser != null)
        {
            java.addAppArguments("--user=" + jbossUser);
            if (jbossPassword != null)
            {
                java.addAppArguments("--password=" + jbossPassword);
            }
        }

        java.execute();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.Container#getId()
     */
    @Override
    protected void waitForCompletion(boolean waitForStarting) throws InterruptedException
    {
        if (!waitForStarting)
        {
            LocalConfiguration config = getConfiguration();

            waitForPortShutdown(
                config.getPropertyValue(ServletPropertySet.PORT),
                config.getPropertyValue(GeneralPropertySet.RMI_PORT),
                config.getPropertyValue(JBossPropertySet.JBOSS_CLASSLOADING_WEBSERVICE_PORT),
                config.getPropertyValue(JBossPropertySet.JBOSS_EJB3_REMOTING_PORT),
                config.getPropertyValue(JBossPropertySet.JBOSS_INVOKER_POOL_PORT),
                config.getPropertyValue(JBossPropertySet.JBOSS_JRMP_INVOKER_PORT),
                config.getPropertyValue(JBossPropertySet.JBOSS_JRMP_PORT),
                config.getPropertyValue(JBossPropertySet.JBOSS_NAMING_PORT),
                config.getPropertyValue(JBossPropertySet.JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT),
                config.getPropertyValue(JBossPropertySet.JBOSS_TRANSACTION_STATUS_MANAGER_PORT));
            return;
        }

        super.waitForCompletion(waitForStarting);
    }

    /**
     * @see org.codehaus.cargo.container.spi.AbstractLocalContainer#verify()
     */
    @Override
    protected final void verify()
    {
        super.verify();

        verifyJBossHome();
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
     * Parse installed JBoss version.
     * 
     * @return the JBoss version, or <code>defaultVersion</code> if the version number could not be
     * determined
     * @param defaultVersion the default version used if the exact JBoss version can't be determined
     */
    protected final String getVersion(String defaultVersion)
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
                    StringBuilder buffer = new StringBuilder();
                    buffer.append(properties.getProperty("version.major"));
                    buffer.append(".");
                    buffer.append(properties.getProperty("version.minor"));
                    buffer.append(".");
                    buffer.append(properties.getProperty("version.revision"));
                    version = buffer.toString();
                }
                else
                {
                    version = defaultVersion;
                    getLogger().debug("Couldn't find version.properties in " + jarFile.getName(),
                        this.getClass().getName());
                }
            }
            catch (Exception e)
            {
                version = defaultVersion;
                getLogger().debug(
                    "Failed to find JBoss version, base error [" + e.getMessage() + "]",
                    this.getClass().getName());
            }

            getLogger().info("Parsed JBoss version = [" + version + "]",
                this.getClass().getName());

            this.version = version;
        }
        return version;
    }

    /**
     * {@inheritDoc}
     * @see JBossInstalledLocalContainer#getConfDir(String)
     */
    public String getConfDir(String configurationName)
    {
        return getSpecificConfigurationDir("conf", configurationName);
    }

    /**
     * {@inheritDoc}
     * @see JBossInstalledLocalContainer#getLibDir(String)
     */
    public String getLibDir(String configurationName)
    {
        return getSpecificConfigurationDir("lib", configurationName);
    }

    /**
     * {@inheritDoc}
     * @see JBossInstalledLocalContainer#getDeployDir(String)
     */
    public String getDeployDir(String configurationName)
    {
        String clustered = getConfiguration().getPropertyValue(JBossPropertySet.CLUSTERED);
        if (Boolean.valueOf(clustered).booleanValue())
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
     * located in the <code>server/</code> directory inside the JBoss installation ir.
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
