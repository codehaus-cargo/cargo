/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.maven2.configuration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.maven2.util.CargoProject;

/**
 * Holds configuration data for the <code>&lt;container&gt;</code> tag used to configure
 * the plugin in the <code>pom.xml</code> file.
 *
 * @version $Id$
 */
public class Container
{
    private String containerId;

    private String implementation;

    private Dependency[] dependencies;

    private String home;

    private String output;

    private ZipUrlInstaller zipUrlInstaller;

    private boolean append;

    private File log;

    private LogLevel logLevel;

    private String type = ContainerType.INSTALLED.getType();

    private Long timeout;

    private Map systemProperties;

    public Map getSystemProperties()
    {
        return this.systemProperties;
    }

    public void setSystemProperties(Map systemProperties)
    {
        this.systemProperties = systemProperties;
    }

    public Long getTimeout()
    {
        return this.timeout;
    }

    public void setTimeout(Long timeout)
    {
        this.timeout = timeout;
    }

    public Dependency[] getDependencies()
    {
        return this.dependencies;
    }

    public void setDependencies(Dependency[] dependencies)
    {
        this.dependencies = dependencies;
    }

    public ContainerType getType()
    {
        return ContainerType.toType(this.type);
    }

    public void setType(ContainerType type)
    {
        this.type = type.getType();
    }

    public String getContainerId()
    {
        return this.containerId;
    }

    public void setContainerId(String containerId)
    {
        this.containerId = containerId;
    }

    public String getHome()
    {
        return this.home;
    }

    public void setHome(String home)
    {
        this.home = home;
    }

    public String getOutput()
    {
        return this.output;
    }

    public void setOutput(String output)
    {
        this.output = output;
    }

    public ZipUrlInstaller getZipUrlInstaller()
    {
        return this.zipUrlInstaller;
    }

    public void setZipUrlInstaller(ZipUrlInstaller zipUrlInstaller)
    {
        this.zipUrlInstaller = zipUrlInstaller;
    }

    public boolean shouldAppend()
    {
        return this.append;
    }

    public void setAppend(boolean append)
    {
        this.append = append;
    }

    public void setLog(File log)
    {
        this.log = log;
    }

    public File getLog()
    {
        return this.log;
    }

    public void setLogLevel(String levelAsString)
    {
        this.logLevel = LogLevel.toLevel(levelAsString);
    }

    public LogLevel getLogLevel()
    {
        return this.logLevel;
    }

    public String getImplementation()
    {
        return this.implementation;
    }

    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    public org.codehaus.cargo.container.Container createContainer(
        Configuration configuration, Logger logger, CargoProject project)
        throws MojoExecutionException
    {
        ContainerFactory factory = new DefaultContainerFactory();

        // If the user has registered a custom container class, register it against the
        // default container factory.
        if (getImplementation() != null)
        {
            try
            {
                Class containerClass = Class.forName(getImplementation(), true,
                    this.getClass().getClassLoader());
                factory.registerContainer(getContainerId(), getType(), containerClass);
            }
            catch (ClassNotFoundException cnfe)
            {
               throw new MojoExecutionException("Custom container implementation ["
                   + getImplementation() + "] cannot be loaded", cnfe);
            }
        }

        org.codehaus.cargo.container.Container container = factory.createContainer(
            getContainerId(), getType(), configuration);

        if (container.getType().isLocal())
        {
            setupTimeout((LocalContainer) container, project);

            // Set the classloader for embedded containers
            if (container.getType() == ContainerType.EMBEDDED)
            {
                // Extra classpath and system properties are currently not set in Cargo core.
                // TODO: Move this code logic in Cargo core
                setupEmbeddedExtraClasspath((EmbeddedLocalContainer) container, project);
                setupEmbeddedSystemProperties((EmbeddedLocalContainer) container);
                ((EmbeddedLocalContainer) container).setClassLoader(project
                        .getEmbeddedClassLoader());

                // Embedded containers (at least Jetty) doesn't seem to use the classloader set
                // for them, but they do excute out of our thread, so the following works fine.
                Thread.currentThread().setContextClassLoader(project.getEmbeddedClassLoader());
            }
            else if (container.getType() == ContainerType.INSTALLED)
            {
                setupHome((InstalledLocalContainer) container);
                setupOutput((InstalledLocalContainer) container, project);
                setupExtraClasspath((InstalledLocalContainer) container, project);
                setupSystemProperties((InstalledLocalContainer) container);
            }
        }
        setupLogger(container, logger);

        return container;
    }

    private void setupEmbeddedExtraClasspath(EmbeddedLocalContainer container, CargoProject project)
        throws MojoExecutionException
    {
        if (getDependencies() != null)
        {
            URL[] dependencyURLs = new URL[getDependencies().length];
            for (int i = 0; i < getDependencies().length; i++)
            {
                File pathFile = new File(getDependencies()[i].getDependencyPath(project));

                try
                {
                    dependencyURLs[i] = pathFile.toURL();
                }
                catch (MalformedURLException e)
                {
                    throw new MojoExecutionException("Invalid classpath location ["
                        + pathFile.getPath() + "]");
                }
            }

            // Create a new classloader that adds the dependencies to the classpath and has the old
            // classloader as its parent classloader.
            URLClassLoader urlClassloader =
                new URLClassLoader(dependencyURLs, project.getEmbeddedClassLoader());

            // Set the Cargo project classloader to the newly constructed classloader.
            project.setEmbeddedClassLoader(urlClassloader);
        }
    }

    private void setupEmbeddedSystemProperties(EmbeddedLocalContainer container)
    {
        if (getSystemProperties() != null)
        {
            for (Iterator iter = getSystemProperties().entrySet().iterator(); iter.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iter.next();
                System.setProperty((String)entry.getKey(), (String)entry.getValue());
            }
        }
    }

    private void setupOutput(InstalledLocalContainer container, CargoProject project)
    {
        if (getOutput() != null)
        {
            container.setOutput(getOutput());
        }
        else
        {
            project.getLog().debug("No container log will be generated. Configure the plugin "
                + "using the <output> element under <container> to generate container logs");
        }
    }

    private void setupTimeout(org.codehaus.cargo.container.LocalContainer container,
        CargoProject project)
    {
        if (getTimeout() != null)
        {
            project.getLog().debug("Setting container timeout to [" + getTimeout() + "]");
            container.setTimeout(getTimeout().longValue());
        }
    }

    private void setupExtraClasspath(InstalledLocalContainer container, CargoProject project)
        throws MojoExecutionException
    {
        if (getDependencies() != null)
        {
            String[] classpaths = new String[getDependencies().length];
            for (int i = 0; i < getDependencies().length; i++)
            {
                classpaths[i] =  getDependencies()[i].getDependencyPath(project);
            }
            container.setExtraClasspath(classpaths);
        }
    }

    private void setupSystemProperties(InstalledLocalContainer container)
        throws MojoExecutionException
    {
        if (getSystemProperties() != null)
        {
            container.setSystemProperties(getSystemProperties());
        }
    }

    /**
     * Set up a home dir of container (possibly including installing the container, by a
     * ZipURLInstaller).
     */
    private void setupHome(InstalledLocalContainer container)
    {
        String tmpHome = null;

        // if a ZipUrlInstaller is specified, use it to install
        if (getZipUrlInstaller() != null)
        {
            ZipURLInstaller installer = getZipUrlInstaller().createInstaller();
            if (getLog() != null)
            {
                installer.setLogger(container.getLogger());
            }
            installer.install();
            tmpHome = installer.getHome();
        }
        if (getHome() != null)
        {
            // this will override a home provided by Installer
            tmpHome = getHome();
        }

        if (tmpHome != null)
        {
            container.setHome(tmpHome);
        }
    }

    /**
     * Set up a logger for the container.
     */
    private void setupLogger(org.codehaus.cargo.container.Container container, Logger logger)
    {
        container.setLogger(logger);

        // TODO: Split this task into 2 (one for local containers and one for remote containers
        // so that there's no need to check the container type).
        if (container instanceof LocalContainer)
        {
            ((LocalContainer) container).getConfiguration().setLogger(logger);
        }
        else
        {
            ((RemoteContainer) container).getConfiguration().setLogger(logger);
        }
    }
}
