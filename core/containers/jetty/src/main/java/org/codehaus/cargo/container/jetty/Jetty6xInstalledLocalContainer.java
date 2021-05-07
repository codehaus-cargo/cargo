/*
 * ========================================================================
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
package org.codehaus.cargo.container.jetty;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.codehaus.cargo.container.ContainerCapability;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.internal.ServletContainerCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer;
import org.codehaus.cargo.container.spi.jvm.JvmLauncher;
import org.codehaus.cargo.util.CargoException;

/**
 * Special container support for the Jetty 6.x servlet container.
 */
public class Jetty6xInstalledLocalContainer extends AbstractInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty6x";

    /**
     * Capability of the Jetty container.
     */
    private ContainerCapability capability = new ServletContainerCapability();

    /**
     * Parsed version of the container.
     */
    private String version;

    /**
     * Jetty6xInstalledLocalContainer Constructor.
     * @param configuration The configuration associated with the container
     */
    public Jetty6xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        if (getHome() != null
            && getFileHandler().exists(getFileHandler().append(getHome(), "start.jar")))
        {
            return "Jetty " + getVersion();
        }
        else
        {
            String className = this.getClass().getName();
            int jettyStart = className.lastIndexOf(".Jetty") + 6;
            String version = className.substring(jettyStart, className.indexOf('x', jettyStart));
            return "Jetty " + version + ".x";
        }
    }

    /**
     * Returns the version of the Jetty installation.
     * 
     * @return The Jetty version
     */
    protected synchronized String getVersion()
    {
        if (this.version == null)
        {
            try (JarFile startJar = new JarFile(new File(getHome(), "start.jar")))
            {
                ZipEntry manifestFile = startJar.getEntry("META-INF/MANIFEST.MF");
                Properties manifest = new Properties();
                manifest.load(startJar.getInputStream(manifestFile));
                this.version = manifest.getProperty("Implementation-Version");
                if (this.version == null)
                {
                    this.version = manifest.getProperty("implementation-version");
                }
            }
            catch (IOException e)
            {
                throw new CargoException("Cannot open the start.jar file", e);
            }
            if (this.version == null)
            {
                throw new CargoException(
                    "The MANIFEST file of start.jar doesn't contain any Implementation Version");
            }
        }

        return this.version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStart(JvmLauncher java) throws Exception
    {
        invoke(java, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStop(JvmLauncher java) throws Exception
    {
        invoke(java, false);
    }

    /**
     * @param java the predefined JVM launcher to use to start the container, passed by Cargo
     * @param isGettingStarted if true then start the container, stop it otherwise
     * @throws Exception in case of startup or shutdown error
     */
    protected void invoke(JvmLauncher java, boolean isGettingStarted) throws Exception
    {
        // CARGO-1093 allow container to start/stop with configuration only on the server
        //    It will skip the settings below if there are RUNTIME_ARGS with --ini=
        //         this will allow full usage of --ini=file.ini
        if (getConfiguration().getPropertyValue(GeneralPropertySet.RUNTIME_ARGS) == null
                || !getConfiguration().getPropertyValue(
                    GeneralPropertySet.RUNTIME_ARGS).contains("--ini="))
        {
            // If logging is set to "high" the turn it on by setting the DEBUG system property
            if (LoggingLevel.HIGH.equalsLevel(getConfiguration().getPropertyValue(
                GeneralPropertySet.LOGGING)))
            {
                java.setSystemProperty("DEBUG", "true");
            }

            // Set location where Jetty is installed
            java.setSystemProperty("jetty.home", getHome());

            // Add shutdown port
            java.setSystemProperty("STOP.PORT",
                getConfiguration().getPropertyValue(GeneralPropertySet.RMI_PORT));
            // Add shutdown key
            java.setSystemProperty("STOP.KEY", "secret");

            // Add listening port
            java.setSystemProperty(getJettyPortPropertyName(),
                getConfiguration().getPropertyValue(ServletPropertySet.PORT));

            // Define the location of the configuration directory as a System property so that it
            // can be referenced from within the jetty.xml file.
            java.setSystemProperty("config.home", getConfiguration().getHome());

            // Location where logs will be generated
            java.setSystemProperty("jetty.logs",
                getFileHandler().append(getConfiguration().getHome(), "logs"));
        }

        java.setJarFile(new File(getHome(), "start.jar"));

        if (isGettingStarted)
        {
            // if RUNTIME_ARGS specified, use'm, otherwise use Jetty default OPTIONS
            if (getConfiguration().getPropertyValue(GeneralPropertySet.RUNTIME_ARGS) == null)
            {
                java.addAppArguments(getStartArguments(java.getClasspath()));
            }

            // For Jetty to pick up on the extra classpath it needs to export
            // the classpath as an environment variable 'CLASSPATH'
            java.setSystemProperty("CLASSPATH", java.getClasspath());
        }
        else
        {
            java.addAppArguments("--stop");

            java.addAppArguments(getStopArguments());
        }

        // integration tests need to let us verify how we're running
        addToolsJarToClasspath(java);
        this.getLogger().debug("Running Jetty As: " + java.getCommandLine(),
                this.getClass().getName());

        if (isGettingStarted)
        {
            java.start();
        }
        else
        {
            int exitCode = java.execute();

            if (exitCode != 0 && exitCode != 252)
            {
                throw new CargoException("Jetty command failed: exit code was " + exitCode);
            }
        }
    }

    /**
     * Returns the arguments to pass to the Jetty <code>start</code> command.
     * @param classpath Jetty classpath (exludes <code>tools.jar</code>).
     * @return Arguments to add to the Jetty <code>start.jar</code> command.
     */
    protected String[] getStartArguments(String classpath)
    {
        return new String[]
        {
            getFileHandler().append(getConfiguration().getHome(), "etc/jetty-logging.xml"),
            getFileHandler().append(getConfiguration().getHome(), "etc/jetty.xml")
        };
    }

    /**
     * @return Arguments to add to the Jetty <code>start.jar</code> command.
     */
    protected String[] getStopArguments()
    {
        return new String[0];
    }

    /**
     * @return The Jetty property name for HTTP port.
     */
    protected String getJettyPortPropertyName()
    {
        return "jetty.port";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerCapability getCapability()
    {
        return this.capability;
    }
}
