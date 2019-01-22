/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2018 Ali Tokmen.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import java.util.Set;
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
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.installer.ZipURLInstaller;
import org.codehaus.cargo.generic.ContainerFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.maven2.util.CargoProject;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Loggable;
import org.codehaus.cargo.util.log.Logger;

/**
 * Holds configuration data for the <code>&lt;container&gt;</code> tag used to configure the plugin
 * in the <code>pom.xml</code> file.
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
     * Downloaded zip file.
     */
    private String installerZipFile;

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
     * System properties loaded from file.
     */
    private File systemPropertiesFile;

    /**
     * Container context key, which can be used to start, stop, configure or deploy to the same
     * Cargo container (together with its configuration) from different Maven artifacts.
     */
    private String contextKey;

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
     * @return System properties loaded from file.
     */
    public File getSystemPropertiesFile()
    {
        return systemPropertiesFile;
    }

    /**
     * @param systemPropertiesFile System properties loaded from file.
     */
    public void setSystemPropertiesFile(File systemPropertiesFile)
    {
        this.systemPropertiesFile = systemPropertiesFile;
    }

    /**
     * @return Timeout (in milliseconds).
     */
    public Long getTimeout()
    {
        return this.timeout;
    }

    /**
     * @return installed zip file (null if not downloaded).
     */
    public String getInstallerZipFile()
    {
        return this.installerZipFile;
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
     * @return Container context key, which can be used to start, stop, configure or deploy to the
     * same Cargo container (together with its configuration) from different Maven artifacts.
     */
    public String getContextKey()
    {
        return contextKey;
    }

    /**
     * @param contextKey Container context key, which can be used to start, stop, configure or
     * deploy to the same Cargo container (together with its configuration) from different Maven
     * artifacts.
     */
    public void setContextKey(String contextKey)
    {
        this.contextKey = contextKey;
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
        container.setLogger(logger);

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

        return container;
    }

   /**
    * Update container based on this configuration
    * @param container Container
    * @param cargoProject Cargo project.
    */
    public void updateContainer(org.codehaus.cargo.container.Container container,
          CargoProject cargoProject)
    {
        if (!container.getType().isLocal())
        {
            return;
        }

        setupTimeout((LocalContainer) container, cargoProject);
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
     * Merges static and system properties loaded from file together
     * @param container Container.
     * @return merged system properties
     * @throws MojoExecutionException in case something fails
     */
    private Map<String, String> mergeSystemProperties(Loggable container)
        throws MojoExecutionException
    {
        Map<String, String> systemProperties = null;
        if (getSystemPropertiesFile() != null)
        {
            Properties properties = new Properties();
            try
            {
                try (InputStream inputStream = new FileInputStream(getSystemPropertiesFile()))
                {
                    properties.load(new BufferedInputStream(inputStream));
                }
                systemProperties = new HashMap<String, String>(properties.size());
                for (Enumeration<?> propertyNames = properties.propertyNames();
                    propertyNames.hasMoreElements();)
                {
                    String propertyName = (String) propertyNames.nextElement();
                    String propertyValue = properties.getProperty(propertyName);
                    systemProperties.put(propertyName, propertyValue);
                }
            }
            catch (FileNotFoundException e)
            {
                container.getLogger().warn("System property file ["
                    + getSystemPropertiesFile() + "] cannot be read", getClass().getName());
            }
            catch (IOException ioe)
            {
                throw new MojoExecutionException("System property file ["
                    + getSystemPropertiesFile() + "] cannot be loaded", ioe);
            }
        }
        if (getSystemProperties() != null)
        {
            if (systemProperties != null)
            {
                systemProperties.putAll(getSystemProperties());
            }
            else
            {
                systemProperties = getSystemProperties();
            }
        }
        return systemProperties;
    }

    /**
     * Setup the embedded container's system properties.
     * @param container Container.
     * @throws MojoExecutionException throws an exception in case the system properties are not ok
     */
    private void setupEmbeddedSystemProperties(EmbeddedLocalContainer container)
        throws MojoExecutionException
    {
        Map<String, String> systemProperties = mergeSystemProperties(container);
        if (systemProperties != null)
        {
            for (Map.Entry<String, String> systemProperty : systemProperties.entrySet())
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
            container.setAppend(shouldAppend());
        }
        else if (project.getLog() != null)
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
            Set<String> classpaths = new LinkedHashSet<String>();
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
            Set<String> classpaths = new LinkedHashSet<String>();
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
        Map<String, String> systemProperties = mergeSystemProperties(container);
        if (systemProperties != null)
        {
            container.setSystemProperties(systemProperties);
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
            String extractDirectory = getArtifactInstaller().getExtractDir();
            if (extractDirectory == null)
            {
                File extractDirectoryFile = new File(project.getBuildDirectory(),
                    org.codehaus.cargo.maven2.configuration.ZipUrlInstaller.EXTRACT_SUBDIRECTORY);
                extractDirectory = extractDirectoryFile.getPath();
            }
            ZipURLInstaller installer = new ZipURLInstaller(homeURL, home.getParent(),
                extractDirectory);
            if (getLog() != null)
            {
                installer.setLogger(container.getLogger());
            }

            if (project.isDaemonRun())
            {
                // A daemon run needs to have the container zip file downloaded,
                // to send it to the daemon server
                if (!installer.isAlreadyDownloaded())
                {
                    installer.download();
                }
            }
            else
            {
                installer.install();
                tmpHome = installer.getHome();
            }

            this.installerZipFile = installer.getDownloadFile();
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

            if (project.isDaemonRun())
            {
                // A daemon run needs to have the container zip file downloaded,
                // to send it to the daemon server
                if (!installer.isAlreadyDownloaded())
                {
                    installer.download();
                }
            }
            else
            {
                installer.install();
                tmpHome = installer.getHome();
            }

            this.installerZipFile = installer.getDownloadFile();
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

}
