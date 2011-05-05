/*
 * ========================================================================
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
package org.codehaus.cargo.maven2.configuration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.EmbeddedLocalContainer;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.maven2.util.CargoProject;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;

/**
 * Holds configuration data for the <code>&lt;container&gt;</code> tag used to configure the plugin
 * in the <code>pom.xml</code> file.
 * 
 * @version $Id$
 */
public class Container
{
    /**
     * Container id.
     */
    private String containerId;

    /**
     * Container implementation.
     */
    private String implementation;

    /**
     * List of dependencies.
     */
    private Dependency[] dependencies;

    /**
     * Container home.
     */
    private String home;

    /**
     * Output file.
     */
    private String output;

    /**
     * {@link ZipUrlInstaller} for the container.
     */
    private ZipUrlInstaller zipUrlInstaller;

    /**
     * {@link ArtifactInstaller} for the container.
     */
    private ArtifactInstaller artifactInstaller;

    /**
     * Whether to append logs.
     */
    private boolean append;

    /**
     * Log file.
     */
    private File log;

    /**
     * Log level.
     */
    private LogLevel logLevel;

    /**
     * Container type.
     */
    private String type = ContainerType.INSTALLED.getType();

    /**
     * Timeout (in milliseconds).
     */
    private Long timeout;

    /**
     * System properties.
     */
    private Map<String, String> systemProperties;

    /**
     * @return System properties.
     */
    public Map<String, String> getSystemProperties()
    {
        return this.systemProperties;
    }

    /**
     * @param systemProperties System properties.
     */
    public void setSystemProperties(Map<String, String> systemProperties)
    {
        this.systemProperties = systemProperties;
    }

    /**
     * @return Timeout (in milliseconds).
     */
    public Long getTimeout()
    {
        return this.timeout;
    }

    /**
     * @param timeout Timeout (in milliseconds).
     */
    public void setTimeout(Long timeout)
    {
        this.timeout = timeout;
    }

    /**
     * @return List of dependencies.
     */
    public Dependency[] getDependencies()
    {
        return this.dependencies;
    }

    /**
     * @param dependencies List of dependencies.
     */
    public void setDependencies(Dependency[] dependencies)
    {
        this.dependencies = dependencies;
    }

    /**
     * @return Container type.
     */
    public ContainerType getType()
    {
        return ContainerType.toType(this.type);
    }

    /**
     * @param type Container type.
     */
    public void setType(ContainerType type)
    {
        this.type = type.getType();
    }

    /**
     * @return Container id.
     */
    public String getContainerId()
    {
        return this.containerId;
    }

    /**
     * @param containerId Container id.
     */
    public void setContainerId(String containerId)
    {
        this.containerId = containerId;
    }

    /**
     * @return Container home.
     */
    public String getHome()
    {
        return this.home;
    }

    /**
     * @param home Container home.
     */
    public void setHome(String home)
    {
        this.home = home;
    }

    /**
     * @return Output file.
     */
    public String getOutput()
    {
        return this.output;
    }

    /**
     * @param output Output file.
     */
    public void setOutput(String output)
    {
        this.output = output;
    }

    /**
     * @return {@link ZipUrlInstaller} for the container.
     */
    public ZipUrlInstaller getZipUrlInstaller()
    {
        return this.zipUrlInstaller;
    }

    /**
     * @param zipUrlInstaller {@link ZipUrlInstaller} for the container.
     */
    public void setZipUrlInstaller(ZipUrlInstaller zipUrlInstaller)
    {
        this.zipUrlInstaller = zipUrlInstaller;
    }

    /**
     * @return {@link ArtifactInstaller} for the container.
     */
    public ArtifactInstaller getArtifactInstaller()
    {
        return this.artifactInstaller;
    }

    /**
     * @param artifactInstaller {@link ArtifactInstaller} for the container.
     */
    public void setArtifactInstaller(ArtifactInstaller artifactInstaller)
    {
        this.artifactInstaller = artifactInstaller;
    }

    /**
     * @return Whether to append logs.
     */
    public boolean shouldAppend()
    {
        return this.append;
    }

    /**
     * @param append Whether to append logs.
     */
    public void setAppend(boolean append)
    {
        this.append = append;
    }

    /**
     * @param log Log file.
     */
    public void setLog(File log)
    {
        this.log = log;
    }

    /**
     * @return Log file.
     */
    public File getLog()
    {
        return this.log;
    }

    /**
     * @param levelAsString Log level.
     */
    public void setLogLevel(String levelAsString)
    {
        this.logLevel = LogLevel.toLevel(levelAsString);
    }

    /**
     * @return Log level.
     */
    public LogLevel getLogLevel()
    {
        return this.logLevel;
    }

    /**
     * @return Container implementation.
     */
    public String getImplementation()
    {
        return this.implementation;
    }

    /**
     * @param implementation Container implementation.
     */
    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    /**
     * Creates the container based on its configuration and attaches the logger.
     * @param configuration Container configuration.
     * @param logger Logger.
     * @param project Cargo project.
     * @return Container configuration.
     * @throws MojoExecutionException If container creation fails.
     */
    public org.codehaus.cargo.container.Container createContainer(
        Configuration configuration, Logger logger, CargoProject project)
        throws MojoExecutionException
    {
        return createContainer(configuration, logger, project, null, null, null, null, null);
    }

    /**
     * Creates the container based on its configuration and attaches the logger.
     * @param configuration Container configuration.
     * @param logger Logger.
     * @param project Cargo project.
     * @param artifactFactory The artifact factory is used to create valid Maven
     * {@link org.apache.maven.artifact.Artifact} objects.
     * @param artifactResolver The artifact resolver is used to dynamically resolve
     * {@link org.apache.maven.artifact.Artifact} objects. It will automatically download whatever
     * needed.
     * @param localRepository The local Maven repository. This is used by the artifact resolver to
     * download resolved artifacts and put them in the local repository so that they won't have to
     * be fetched again next time the plugin is executed.
     * @param repositories The remote Maven repositories used by the artifact resolver to look for
     * artifacts.
     * @param settings Maven2 settings.
     * @return Container configuration.
     * @throws MojoExecutionException If container creation fails.
     */
    public org.codehaus.cargo.container.Container createContainer(
        Configuration configuration, Logger logger, CargoProject project,
        ArtifactFactory artifactFactory, ArtifactResolver artifactResolver,
        ArtifactRepository localRepository, List<ArtifactRepository> repositories,
        Settings settings) throws MojoExecutionException
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
                Proxy proxy = null;
                if (settings != null)
                {
                    proxy = settings.getActiveProxy();
                }
                setupHome((InstalledLocalContainer) container, project, artifactFactory,
                    artifactResolver, localRepository, repositories, proxy);
                setupOutput((InstalledLocalContainer) container, project);
                setupExtraClasspath((InstalledLocalContainer) container, project);
                setupSystemProperties((InstalledLocalContainer) container);
                setupSharedClasspath((InstalledLocalContainer) container, project);
            }
        }
        setupLogger(container, logger);

        return container;
    }

    /**
     * Setup the embedded container's extra classpath.
     * @param container Container.
     * @param project Cargo project.
     * @throws MojoExecutionException If dependency extraction fails.
     */
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

    /**
     * Setup the embedded container's system properties.
     * @param container Container.
     */
    private void setupEmbeddedSystemProperties(EmbeddedLocalContainer container)
    {
        if (getSystemProperties() != null)
        {
            for (Map.Entry<String, String> systemProperty : getSystemProperties().entrySet())
            {
                if (systemProperty.getValue() != null)
                {
                    System.setProperty(systemProperty.getKey(), systemProperty.getValue());
                }
            }
        }
    }

    /**
     * Setup the output file.
     * @param container Container.
     * @param project Cargo project.
     */
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

    /**
     * Setup timeout.
     * @param container Container.
     * @param project Cargo project.
     */
    private void setupTimeout(org.codehaus.cargo.container.LocalContainer container,
        CargoProject project)
    {
        if (getTimeout() != null)
        {
            project.getLog().debug("Setting container timeout to [" + getTimeout() + "]");
            container.setTimeout(getTimeout().longValue());
        }
    }

    /**
     * Setup extra classpath.
     * @param container Container.
     * @param project Cargo project.
     * @throws MojoExecutionException If dependency extraction fails.
     */
    private void setupExtraClasspath(InstalledLocalContainer container, CargoProject project)
        throws MojoExecutionException
    {
        if (getDependencies() != null)
        {
            HashSet<String> classpaths = new HashSet<String>();
            for (Dependency dependency : getDependencies())
            {
                if (dependency.isOnClasspath(Dependency.EXTRA_CLASSPATH))
                {
                    classpaths.add(dependency.getDependencyPath(project));
                }
            }
            container.setExtraClasspath(classpaths.toArray(new String[classpaths.size()]));
        }
    }

    /**
     * Setup shared classpath.
     * @param container Container.
     * @param project Cargo project.
     * @throws MojoExecutionException If dependency extraction fails.
     */
    private void setupSharedClasspath(InstalledLocalContainer container, CargoProject project)
        throws MojoExecutionException
    {
        if (getDependencies() != null)
        {
            HashSet<String> classpaths = new HashSet<String>();
            for (Dependency dependency : getDependencies())
            {
                if (dependency.isOnClasspath(Dependency.SHARED_CLASSPATH))
                {
                    classpaths.add(dependency.getDependencyPath(project));
                }
            }
            container.setSharedClasspath(classpaths.toArray(new String[classpaths.size()]));
        }
    }
    
    /**
     * Setup system properties.
     * @param container Container.
     * @throws MojoExecutionException If dependency extraction fails.
     */
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
     * {@link ZipUrlInstaller} or {@link ArtifactInstaller}).
     * @param container Container.
     * @param project Cargo project.
     * @param artifactFactory The artifact factory is used to create valid Maven
     * {@link org.apache.maven.artifact.Artifact} objects.
     * @param artifactResolver The artifact resolver is used to dynamically resolve
     * {@link org.apache.maven.artifact.Artifact} objects. It will automatically download whatever
     * needed.
     * @param localRepository The local Maven repository. This is used by the artifact resolver to
     * download resolved artifacts and put them in the local repository so that they won't have to
     * be fetched again next time the plugin is executed.
     * @param repositories The remote Maven repositories used by the artifact resolver to look for
     * artifacts.
     * @param proxy If exists, Maven2 proxy.
     * @throws MojoExecutionException If anything goes wrong.
     */
    private void setupHome(InstalledLocalContainer container, CargoProject project,
        ArtifactFactory artifactFactory, ArtifactResolver artifactResolver,
        ArtifactRepository localRepository, List<ArtifactRepository> repositories, Proxy proxy)
        throws MojoExecutionException
    {
        String tmpHome = null;

        if (getZipUrlInstaller() != null && getArtifactInstaller() != null)
        {
            throw new MojoExecutionException(
                "You can use either ZipUrlInstaller or ArtifactInstaller; not both!");
        }

        // if a ArtifactInstaller is specified, use it to install
        if (getArtifactInstaller() != null)
        {
            File home;
            URL homeURL;
            try
            {
                home = getArtifactInstaller().resolve(artifactFactory, artifactResolver,
                    localRepository, repositories);
                homeURL = home.toURI().toURL();
            }
            catch (Exception e)
            {
                throw new MojoExecutionException("Failed resolving artifact", e);
            }
            File installDirectory = new File(project.getBuildDirectory(),
                org.codehaus.cargo.maven2.configuration.ZipUrlInstaller.EXTRACT_SUBDIRECTORY);
            ZipURLInstaller installer = new ZipURLInstaller(homeURL, home.getParent(),
                installDirectory.getPath());
            if (getLog() != null)
            {
                installer.setLogger(container.getLogger());
            }
            installer.install();
            tmpHome = installer.getHome();
        }
        // if a ZipUrlInstaller is specified, use it to install
        if (getZipUrlInstaller() != null)
        {
            // if a Maven2 proxy is specified, use it
            if (proxy != null && getZipUrlInstaller().getProxy() == null)
            {
                org.codehaus.cargo.container.installer.Proxy zipUrlInstallerProxy =
                    getZipUrlInstaller().createProxy();

                zipUrlInstallerProxy.setExcludeHosts(proxy.getNonProxyHosts());
                zipUrlInstallerProxy.setHost(proxy.getHost());
                zipUrlInstallerProxy.setPassword(proxy.getPassword());
                zipUrlInstallerProxy.setPort(proxy.getPort());
                zipUrlInstallerProxy.setUser(proxy.getUsername());
            }

            ZipURLInstaller installer = getZipUrlInstaller().createInstaller(
                project.getBuildDirectory());
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
     * @param container Container.
     * @param logger Logger.
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
