/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.maven3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;
import org.apache.maven.shared.transfer.artifact.DefaultArtifactCoordinate;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.dependencies.DefaultDependableCoordinate;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolver;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.installer.Proxy;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.maven3.configuration.ArtifactInstaller;
import org.codehaus.cargo.maven3.configuration.Configuration;
import org.codehaus.cargo.maven3.configuration.Container;
import org.codehaus.cargo.maven3.configuration.Daemon;
import org.codehaus.cargo.maven3.configuration.Deployable;
import org.codehaus.cargo.maven3.configuration.Deployer;
import org.codehaus.cargo.maven3.configuration.ZipUrlInstaller;
import org.codehaus.cargo.maven3.deployer.DefaultDeployableMonitorFactory;
import org.codehaus.cargo.maven3.deployer.DeployableMonitorFactory;
import org.codehaus.cargo.maven3.log.MavenLogger;
import org.codehaus.cargo.maven3.util.CargoProject;
import org.codehaus.cargo.maven3.util.EmbeddedContainerArtifactResolver;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.Logger;

/**
 * Common code used by Cargo MOJOs requiring <code>&lt;container&gt;</code> and
 * <code>&lt;configuration&gt;</code> elements and supporting the notion of
 * Auto-deployable.
 */
public abstract class AbstractCargoMojo extends AbstractCommonMojo
{
    /**
     * The key under which the container instance is stored in the plugin context.
     * We store it so that it's possible to get back the same container instance
     * even if this mojo is called in a different Maven execution context. This is
     * required for stopping embedded containers for example as we need to use the
     * same instance that was started in order to stop them.
     */
    public static final String CONTEXT_KEY_CONTAINER =
        AbstractCargoMojo.class.getName() + "-Container";

    /**
     * The key suffix under which the classloader of the container instance is
     * stored in the plugin context. We store it so that it's possible to get back
     * the same classloader even if this mojo is called in a different Maven
     * execution context. This is required for starting and stopping multiple
     * containers as each container initialization requires different classloader.
     */
    public static final String CONTEXT_KEY_CLASSLOADER = "-classloader";

    /**
     * URL prefix for the Maven repository.
     */
    private static final String MAVEN_REPOSITORY_URL = "https://repo.maven.apache.org/maven2/";

    /**
     * Configures a Cargo
     * {@link org.codehaus.cargo.container.configuration.Configuration}. See the
     * <a href=
     * "https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">Cargo
     * Maven 3 plugin reference guide</a> for more details.
     * 
     * @see #getConfigurationElement()
     */
    @Parameter
    private Configuration configuration;

    /**
     * Configures a Cargo {@link org.codehaus.cargo.container.Container}. See the
     * <a href=
     * "https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">Cargo
     * Maven 3 plugin reference guide</a> for more details.
     */
    @Parameter
    private Container container;

    /**
     * Daemon configuration.
     */
    @Parameter
    private Daemon daemon;

    /**
     * Configures a Cargo {@link org.codehaus.cargo.container.deployer.Deployer}.
     * See the <a href=
     * "https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">Cargo
     * Maven 3 plugin reference guide</a> for more details.
     * 
     * @see #getDeployerElement()
     */
    @Parameter
    private Deployer deployer;

    /**
     * List of {@link org.codehaus.cargo.maven3.configuration.Deployable}. See the
     * <a href=
     * "https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">Cargo
     * Maven 3 plugin reference guide</a> for more details.
     * 
     * @see #getDeployablesElement()
     */
    @Parameter
    private Deployable[] deployables;

    /**
     * Maven artifact resolver, used to dynamically resolve JARs for the containers
     * and also to resolve the JARs for the embedded container's classpaths.
     */
    @Component
    private ArtifactResolver artifactResolver;

    /**
     * Maven dependency resolver, used to dynamically resolve dependencies of
     * artifacts.
     */
    @Component
    private DependencyResolver dependencyResolver;

    /**
     * Maven project building request.
     */
    @Parameter(
        defaultValue = "${session.projectBuildingRequest}", readonly = true, required = true)
    private ProjectBuildingRequest projectBuildingRequest;

    /**
     * Set this to <code>true</code> to bypass cargo execution.
     */
    @Parameter(property = "cargo.maven.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Set this to <code>true</code> to show the [category] log prefix. This was the default
     * behaviour until the Codehaus Cargo Maven 3 plugin's version 1.10.11.
     */
    @Parameter(property = "cargo.maven.useLogCategoryPrefix", defaultValue = "false")
    private boolean useLogCategoryPrefix;

    /**
     * @see org.codehaus.cargo.maven3.util.CargoProject
     */
    private CargoProject cargoProject;

    /**
     * Maven settings, injected automatically.
     */
    @Parameter(property = "settings", readonly = true, required = true)
    private Settings settings;

    /**
     * Cargo plugin version.
     */
    @Parameter(property = "plugin.version", readonly = true, required = true)
    private String pluginVersion;

    /**
     * Should the mojo ignore failures if something fails
     */
    @Parameter(property = "cargo.ignore.failures", defaultValue = "false")
    private boolean ignoreFailures = false;

    /**
     * Calculates the container artifact ID for a given container ID. Note that all
     * containers identifier are in the form
     * <code>containerArtifactId + the version number + x</code>; for example
     * <code>jboss42x</code> is from container artifact ID
     * <code>cargo-core-container-jboss</code>.
     * 
     * @param containerId Container ID, for example <code>jboss42x</code>.
     * @return Container artifact ID, for example
     *         <code>cargo-core-container-jboss</code>.
     */
    public static String calculateContainerArtifactId(String containerId)
    {
        return "cargo-core-container-" + containerId.replaceAll("\\d+x", "");
    }

    /**
     * Calculate the Maven artifact for a URL.
     * @param url Maven URL.
     * @return Maven artifact installer for given URL.
     */
    public static ArtifactInstaller calculateArtifact(String url)
    {
        if (!url.startsWith(AbstractCargoMojo.MAVEN_REPOSITORY_URL))
        {
            throw new IllegalArgumentException("URL [" + url + "] does not start with ["
                + AbstractCargoMojo.MAVEN_REPOSITORY_URL + "]");
        }

        ArtifactInstaller installer = new ArtifactInstaller();
        String[] urlParts =
            url.substring(AbstractCargoMojo.MAVEN_REPOSITORY_URL.length()).split("/+");

        StringBuilder groupId = new StringBuilder();
        for (int i = 0; i < urlParts.length - 3; i++)
        {
            String urlPart = urlParts[i];
            if (groupId.length() > 0)
            {
                groupId.append(".");
            }
            groupId.append(urlPart);
        }
        installer.setGroupId(groupId.toString());
        installer.setArtifactId(urlParts[urlParts.length - 3]);
        installer.setVersion(urlParts[urlParts.length - 2]);

        String filePrefix = installer.getArtifactId() + "-" + installer.getVersion();
        String file = urlParts[urlParts.length - 1];
        installer.setType(file.substring(file.indexOf('.', filePrefix.length()) + 1));
        if (!file.equals(filePrefix + "." + installer.getType()))
        {
            String classifier = file.substring(filePrefix.length() + 1);
            installer.setClassifier(classifier.substring(0, classifier.indexOf('.')));
        }

        return installer;
    }

    /**
     * See the <a href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">
     * Cargo Maven 3 plugin reference guide</a> for more details.
     * 
     * @return the user configuration of a Cargo
     *         {@link org.codehaus.cargo.container.deployer.Deployer}.
     * @see #setDeployerElement(Deployer)
     */
    protected Deployer getDeployerElement()
    {
        return this.deployer;
    }

    /**
     * See the <a href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">
     * Cargo Maven 3 plugin reference guide</a> for more details.
     * 
     * @param deployerElement the
     *                        {@link org.codehaus.cargo.container.deployer.Deployer}
     *                        configuration defined by the user
     * @see #getDeployerElement()
     */
    protected void setDeployerElement(Deployer deployerElement)
    {
        this.deployer = deployerElement;
    }

    /**
     * @return The daemon configuration
     */
    protected Daemon getDaemon()
    {
        if (this.daemon == null)
        {
            this.daemon = new Daemon();
        }

        return this.daemon;
    }

    /**
     * See the <a href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">
     * Cargo Maven 3 plugin reference guide</a> for more details.
     * 
     * @return the user configuration of the list of
     *         {@link org.codehaus.cargo.maven3.configuration.Deployable}.
     * @see #setDeployablesElement(Deployable[])
     */
    protected Deployable[] getDeployablesElement()
    {
        return this.deployables;
    }

    /**
     * See the <a href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">
     * Cargo Maven 3 plugin reference guide</a> for more details.
     * 
     * @param deployablesElement the list of
     *                           {@link org.codehaus.cargo.maven3.configuration.Deployable}.
     * @see #getDeployablesElement()
     */
    protected void setDeployablesElement(Deployable[] deployablesElement)
    {
        this.deployables = deployablesElement;
    }

    /**
     * See the <a href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">
     * Cargo Maven 3 plugin reference guide</a> for more details.
     * 
     * @return the user configuration of a Cargo
     *         {@link org.codehaus.cargo.container.configuration.Configuration}.
     * @see #setConfigurationElement(Configuration)
     */
    protected Configuration getConfigurationElement()
    {
        return this.configuration;
    }

    /**
     * See the <a href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">
     * Cargo Maven 3 plugin reference guide</a> for more details.
     * 
     * @param configurationElement the
     *                             {@link org.codehaus.cargo.container.configuration.Configuration}
     *                             configuration defined by the user
     * @see #getConfigurationElement()
     */
    protected void setConfigurationElement(Configuration configurationElement)
    {
        this.configuration = configurationElement;
    }

    /**
     * See the <a href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">
     * Cargo Maven 3 plugin reference guide</a> for more details.
     * 
     * @return the user configuration of a Cargo
     *         {@link org.codehaus.cargo.container.Container}.
     * @see #setConfigurationElement(Configuration)
     */
    protected Container getContainerElement()
    {
        return this.container;
    }

    /**
     * See the <a href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">
     * Cargo Maven 3 plugin reference guide</a> for more details.
     * 
     * @param containerElement the {@link org.codehaus.cargo.container.Container}
     *                         configuration defined by the user
     * @see #getContainerElement()
     */
    protected void setContainerElement(Container containerElement)
    {
        this.container = containerElement;
    }

    /**
     * @param cargoProject Cargo project
     */
    protected void setCargoProject(CargoProject cargoProject)
    {
        this.cargoProject = cargoProject;
    }

    /**
     * @return Cargo project
     */
    protected CargoProject getCargoProject()
    {
        return this.cargoProject;
    }

    /**
     * @return the ignoreFailures
     */
    public boolean isIgnoreFailures()
    {
        return ignoreFailures;
    }

    /**
     * @param ignoreFailures the ignoreFailures to set
     */
    public void setIgnoreFailures(boolean ignoreFailures)
    {
        this.ignoreFailures = ignoreFailures;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Note: This method is final so that extending classes cannot extend it.
     * Instead they should implement the {@link #doExecute()} method.
     * </p>
     */
    @Override
    public final void execute() throws MojoExecutionException
    {
        if (this.skip)
        {
            getLog().info("Skipping cargo execution");
            return;
        }
        if (this.cargoProject == null)
        {
            this.cargoProject = new CargoProject(getProject(), getLog());
        }

        Proxy proxy = null;
        if (settings != null)
        {
            org.apache.maven.settings.Proxy activeProxy = settings.getActiveProxy();
            if (activeProxy != null)
            {
                // CARGO-1042, CARGO-1119, CARGO-1121 and CARGO-1533:
                // Use proxy definitions from the Maven settings - Even if the JVM is executing
                // with http.proxyHost / https.proxyHost properties
                getLog().debug("Using proxy definitions from the Maven settings");
                proxy = new Proxy();
                proxy.setHost(activeProxy.getHost());
                proxy.setPort(activeProxy.getPort());
                proxy.setExcludeHosts(activeProxy.getNonProxyHosts());
                proxy.setUser(activeProxy.getUsername());
                proxy.setPassword(activeProxy.getPassword());
            }
        }

        Map<String, String> previousProperties = null;
        try
        {
            if (proxy != null)
            {
                previousProperties = proxy.configure();
            }
            doExecute();
        }
        catch (MojoExecutionException e)
        {
            if (ignoreFailures)
            {
                getLog().error("Ignoring failures during execution", e);
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            if (proxy != null)
            {
                proxy.clear(previousProperties);
            }
        }
    }

    /**
     * Executes the plugin.
     * 
     * <p>
     * This method must be implemented by all Mojos extending this class. The reason
     * for this pattern is because we want the {@link #execute()} method to always
     * be called so that necessary plugin initialization can be performed. Without
     * this pattern Mojos extending this class could "forget" to call
     * <code>super.execute()</code> thus leading to unpredictible results.
     * </p>
     * 
     * @throws MojoExecutionException in case of error
     */
    protected abstract void doExecute() throws MojoExecutionException;

    /**
     * Creates a {@link org.codehaus.cargo.container.configuration.Configuration}
     * instance. If the user has not specified a configuration element in the POM
     * file then automatically create a standalone configuration if the container's
     * type is local or otherwise create a runtime configuration.
     * 
     * @return a valid
     *         {@link org.codehaus.cargo.container.configuration.Configuration}
     *         instance
     * @throws MojoExecutionException in case of error
     */
    protected org.codehaus.cargo.container.configuration.Configuration createConfiguration()
        throws MojoExecutionException
    {
        org.codehaus.cargo.container.configuration.Configuration configuration;

        // If no configuration element has been specified create one with default values.
        if (getConfigurationElement() == null)
        {
            Configuration configurationElement = new Configuration();

            if (getContainerElement().getType().isLocal())
            {
                File home = new File(getCargoProject().getBuildDirectory(),
                    "cargo/configurations/" + getContainerElement().getContainerId());

                configurationElement.setType(ConfigurationType.STANDALONE);
                configurationElement.setHome(home.getAbsolutePath());
            }
            else
            {
                configurationElement.setType(ConfigurationType.RUNTIME);
            }

            setConfigurationElement(configurationElement);
        }
        else
        {
            if (getConfigurationElement().getHome() != null && !getCargoProject().isDaemonRun())
            {
                getConfigurationElement().setHome(calculateAbsoluteDirectory(
                    "configuration home", getConfigurationElement().getHome()));
            }
        }

        String contextKey = null;
        if (getContainerElement() != null && getContainerElement().getContextKey() != null
            && !getContainerElement().getContextKey().trim().isEmpty())
        {
            contextKey = getContainerElement().getContextKey().trim();
        }
        configuration = getConfigurationElement().createConfiguration(
            getContainerElement().getContainerId(), getContainerElement().getType(),
                getDeployablesElement(), getCargoProject(), getProject(), contextKey, settings,
                    getLog());

        return configuration;
    }

    /**
     * @return a {@link org.codehaus.cargo.container.Container} instance if no
     *         container object was stored in the Maven Plugin Context or returns
     *         the saved instance otherwise. If a new container instance is created
     *         it's also saved in the Maven Plugin Context for later retrieval.
     * @throws MojoExecutionException    in case of error
     */
    protected org.codehaus.cargo.container.Container createContainer()
            throws MojoExecutionException
    {
        org.codehaus.cargo.container.Container container = null;

        // Try to find the container in the Maven Plugin Context first.
        Map<Object, Object> context = getPluginContext();

        String containerKey = CONTEXT_KEY_CONTAINER;
        if (getContainerElement() != null)
        {
            if (getContainerElement().getHome() != null && !getCargoProject().isDaemonRun())
            {
                getContainerElement().setHome(calculateAbsoluteDirectory(
                    "container home", getContainerElement().getHome()));
            }
            if (getContainerElement().getArtifactInstaller() != null
                && getContainerElement().getArtifactInstaller().getExtractDir() != null)
            {
                getContainerElement().getArtifactInstaller().setExtractDir(
                    calculateAbsoluteDirectory("artifact installer extract",
                        getContainerElement().getArtifactInstaller().getExtractDir()));
            }
            if (getContainerElement().getZipUrlInstaller() != null
                && getContainerElement().getZipUrlInstaller().getDownloadDir() != null)
            {
                getContainerElement().getZipUrlInstaller().setDownloadDir(
                    calculateAbsoluteDirectory("zip URL installer download",
                        getContainerElement().getZipUrlInstaller().getDownloadDir()));
            }
            if (getContainerElement().getZipUrlInstaller() != null
                && getContainerElement().getZipUrlInstaller().getExtractDir() != null)
            {
                getContainerElement().getZipUrlInstaller().setExtractDir(
                    calculateAbsoluteDirectory("zip URL installer extract",
                        getContainerElement().getZipUrlInstaller().getExtractDir()));
            }

            if (getContainerElement().getContextKey() != null
                && !getContainerElement().getContextKey().isEmpty())
            {
                containerKey += "." + getContainerElement().getContextKey();
            }
            else
            {
                containerKey += "." + getContainerElement().getType()
                    + "." + getContainerElement().getHome();
            }
        }
        if (getConfigurationElement() != null && !getCargoProject().isDaemonRun())
        {
            if (getConfigurationElement().getHome() != null)
            {
                getConfigurationElement().setHome(calculateAbsoluteDirectory(
                    "configuration home", getConfigurationElement().getHome()));
            }

            if (getContainerElement() == null || getContainerElement().getContextKey() == null
                || getContainerElement().getContextKey().isEmpty())
            {
                containerKey += "." + getConfigurationElement().getHome();
            }
        }

        if (context != null)
        {
            container = (org.codehaus.cargo.container.Container) context.get(containerKey);
            String classloaderKey = containerKey + CONTEXT_KEY_CLASSLOADER;
            if (context.containsKey(classloaderKey))
            {
                ResourceUtils.setResourceLoader((ClassLoader) context.get(classloaderKey));
            }
        }

        if (container == null)
        {
            container = createNewContainer();
        }
        else if (getConfigurationElement() != null)
        {
            createDefaultContainerElementIfNecessary();
            org.codehaus.cargo.container.configuration.Configuration configuration =
                createConfiguration();
            configuration.setLogger(container.getLogger());

            // CARGO-1053: Update the container's configuration, since different executions might
            // have defined different configurations but the "put the container in the Maven 3
            // context" (for handling multiple containers and also for handling embedded
            // containers) mechanism will reuse existing container along with its configuration.
            if (container instanceof RemoteContainer)
            {
                if (!(configuration instanceof RuntimeConfiguration))
                {
                    throw new MojoExecutionException("Expected a "
                        + RuntimeConfiguration.class.getName() + " but got a "
                            + configuration.getClass().getName());
                }

                ((RemoteContainer) container).setConfiguration(
                    (RuntimeConfiguration) configuration);
            }
            else if (container instanceof LocalContainer)
            {
                if (!(configuration instanceof LocalConfiguration))
                {
                    throw new MojoExecutionException("Expected a "
                        + LocalConfiguration.class.getName() + " but got a "
                            + configuration.getClass().getName());
                }

                ((LocalContainer) container).setConfiguration((LocalConfiguration) configuration);
            }
            else
            {
                throw new MojoExecutionException(
                    "Unknown container type " + container.getClass().getName());
            }
        }

        if (context != null)
        {
            context.put(containerKey, container);
            if (ResourceUtils.getResourceLoader() != null)
            {
                context.put(
                    containerKey + CONTEXT_KEY_CLASSLOADER, ResourceUtils.getResourceLoader());
            }
        }

        return container;
    }

    /**
     * Creates a brand new {@link org.codehaus.cargo.container.Container} instance.
     * If the user has not specified a container element in the POM file or if the
     * user has not specified the container id then automatically create a default
     * container (as defined in {@link #createDefaultContainerElementIfNecessary})
     * if the project calling this plugin has a WAR packaging. If the packaging is
     * different then an exception is raised.
     * 
     * @return a valid {@link org.codehaus.cargo.container.Container} instance
     * @throws MojoExecutionException    in case of error or if a default container
     *                                   could not be created
     */
    protected org.codehaus.cargo.container.Container createNewContainer()
        throws MojoExecutionException
    {
        org.codehaus.cargo.container.Container container;

        createDefaultContainerElementIfNecessary();
        try
        {
            createDefaultInstallerElementIfNecessary();
        }
        catch (IOException e)
        {
            getLog().warn("Cannot load the URL to put as the optional default installer element "
                    + "for the container, skipping as this won't kill anyway...", e);
        }

        Logger logger = createLogger();
        org.codehaus.cargo.container.configuration.Configuration configuration =
            createConfiguration();
        configuration.setLogger(logger);
        if (getContainerElement().getType() == ContainerType.EMBEDDED)
        {
            EmbeddedContainerArtifactResolver resolver = new EmbeddedContainerArtifactResolver(
                this.artifactResolver, projectBuildingRequest);
            ClassLoader classLoader = resolver.resolveDependencies(
                getContainerElement().getContainerId(), configuration,
                    getCargoProject().getEmbeddedClassLoader());
            getCargoProject().setEmbeddedClassLoader(classLoader);

            if ("tomcat8x".equals(getContainerElement().getContainerId())
                || "tomcat9x".equals(getContainerElement().getContainerId()))
            {
                // The reference javax.security.auth.message API, as opposed to the one provided by
                // Apache Tomcat (which, unfortunately, isn't part of Maven repositories) doesn't
                // have a default authentication context configuration factory, so set it manually.
                if (Security.getProperty("authconfigprovider.factory") == null)
                {
                    Security.setProperty("authconfigprovider.factory",
                        "org.apache.catalina.authenticator.jaspic.AuthConfigFactoryImpl");
                }
            }
        }
        container = getContainerElement().createContainer(configuration, logger, getCargoProject(),
            artifactResolver, projectBuildingRequest, settings);

        return container;
    }

    /**
     * Creates a container element if required.
     * 
     * @throws MojoExecutionException    in case of error or if a default container
     *                                   could not be created
     */
    protected void createDefaultContainerElementIfNecessary() throws MojoExecutionException
    {
        if (getContainerElement() == null)
        {
            // Only accept default configuration if the packaging is not of type EAR as Cargo
            // currently doesn't have an embedded container that supports EAR (we need to add
            // openEJB support!).
            if (getCargoProject().getPackaging() != null
                && !getCargoProject().getPackaging().equalsIgnoreCase("war"))
            {
                throw new MojoExecutionException("For all packaging other than war you need to "
                    + "configure the container you wish to use.");
            }

            Container containerElement = new Container();
            setContainerElement(containerElement);
        }

        // If no container id is specified, default to Jetty
        if (getContainerElement().getContainerId() == null)
        {
            if (getContainerElement().getArtifactInstaller() != null)
            {
                throw new MojoExecutionException("You have specified no containerId but have "
                    + "specified an artifactInstaller. Please check the plugin configuration.");
            }

            if (getContainerElement().getZipUrlInstaller() != null)
            {
                throw new MojoExecutionException("You have specified no containerId but have "
                    + "specified a zipUrlInstaller. Please check the plugin configuration.");
            }

            getContainerElement().setContainerId("jetty9x");
            getContainerElement().setType(ContainerType.INSTALLED);

            ArtifactInstaller artifactInstaller = new ArtifactInstaller();
            artifactInstaller.setGroupId("org.eclipse.jetty");
            artifactInstaller.setArtifactId("jetty-distribution");
            artifactInstaller.setVersion("9.4.58.v20250814");
            getContainerElement().setArtifactInstaller(artifactInstaller);

            getLog().info("No container defined, using a default ["
                + getContainerElement().getContainerId() + ", "
                    + getContainerElement().getType().getType() + "] container");
        }

        String containerId = getContainerElement().getContainerId();
        if (containerId != null && artifactResolver != null && dependencyResolver != null)
        {
            String containerArtifactId =
                AbstractCargoMojo.calculateContainerArtifactId(containerId);

            DefaultArtifactCoordinate containerArtifactCoordinate =
                new DefaultArtifactCoordinate();
            containerArtifactCoordinate.setGroupId("org.codehaus.cargo");
            containerArtifactCoordinate.setArtifactId(containerArtifactId);
            containerArtifactCoordinate.setVersion(pluginVersion);
            containerArtifactCoordinate.setExtension("jar");

            try
            {
                // Here, we are to resolve the container's Maven artifact dynamically and, most
                // importantly, add it to the list of JARs the generic API searches into.
                Artifact containerArtifact = artifactResolver.resolveArtifact(
                    projectBuildingRequest, containerArtifactCoordinate).getArtifact();
                URL containerArtifactUrl = containerArtifact.getFile().toURI().toURL();
                List<URL> urlClassLoaderURLs = new ArrayList<URL>();

                // We use the Codehaus Cargo ResourceUtils' ClassLoader as that is one of the key
                // core classes part of all container dependencies.
                for (ClassLoader classLoader = ResourceUtils.getResourceLoader();
                    classLoader != null; classLoader = classLoader.getParent())
                {
                    if (classLoader instanceof URLClassLoader)
                    {
                        urlClassLoaderURLs.addAll(
                            Arrays.asList(((URLClassLoader) classLoader).getURLs()));
                    }
                }

                if (urlClassLoaderURLs.contains(containerArtifactUrl))
                {
                    createLogger().debug("Container artifact " + containerArtifact
                        + " for container " + containerId + " already in class loader",
                            this.getClass().getName());
                }
                else
                {
                    List<URL> containerArtifactURLs = new ArrayList<URL>();
                    containerArtifactURLs.add(containerArtifactUrl);

                    DefaultDependableCoordinate containerDependableCoordinate =
                        new DefaultDependableCoordinate();
                    containerDependableCoordinate.setGroupId("org.codehaus.cargo");
                    containerDependableCoordinate.setArtifactId(containerArtifactId);
                    containerDependableCoordinate.setVersion(pluginVersion);
                    containerDependableCoordinate.setType("jar");
                    for (ArtifactResult artifactResult : dependencyResolver.resolveDependencies(
                        projectBuildingRequest, containerDependableCoordinate,
                            ScopeFilter.including(Artifact.SCOPE_COMPILE)))
                    {
                        URL artifactURL = artifactResult.getArtifact().getFile().toURI().toURL();
                        if (!urlClassLoaderURLs.contains(artifactURL))
                        {
                            containerArtifactURLs.add(artifactURL);
                        }
                    }
                    URL[] containerArtifactArray = new URL[containerArtifactURLs.size()];
                    containerArtifactArray = containerArtifactURLs.toArray(containerArtifactArray);

                    URLClassLoader containerArtifactClassLoader =
                        new URLClassLoader(containerArtifactArray,
                            this.getClass().getClassLoader());
                    ResourceUtils.setResourceLoader(containerArtifactClassLoader);

                    createLogger().debug("Resolved artifact and dependencies: "
                        + containerArtifactURLs, this.getClass().getName());
                    createLogger().info("Resolved container artifact " + containerArtifact
                        + " for container " + containerId, this.getClass().getName());
                }
            }
            catch (Exception e)
            {
                createLogger().debug("Cannot resolve container artifact "
                    + containerArtifactCoordinate + " for container " + containerId + ": "
                        + e.toString(), this.getClass().getName());
            }
        }
    }

    /**
     * Creates a installer element if required.
     * 
     * @throws IOException If the properties file cannot be loaded.
     */
    protected void createDefaultInstallerElementIfNecessary() throws IOException
    {
        if (getContainerElement().getType() == ContainerType.INSTALLED
            && getContainerElement().getHome() == null
                && getContainerElement().getZipUrlInstaller() == null
                    && getContainerElement().getArtifactInstaller() == null)
        {
            InputStream containerUrls = this.getClass().getResourceAsStream(
                "/org/codehaus/cargo/documentation/container-urls.properties");
            if (containerUrls != null)
            {
                Properties containerUrlsProperties = new Properties();
                containerUrlsProperties.load(containerUrls);
                String url = containerUrlsProperties.getProperty("cargo."
                    + getContainerElement().getContainerId() + ".url");
                if (url != null)
                {
                    if (url.startsWith(AbstractCargoMojo.MAVEN_REPOSITORY_URL))
                    {
                        try
                        {
                            ArtifactInstaller installerElement =
                                AbstractCargoMojo.calculateArtifact(url);
                            getContainerElement().setArtifactInstaller(installerElement);
                            getLog().info("You did not specify a container home nor any installer. "
                                + "Codehaus Cargo will automatically download your container's "
                                    + "binaries as a Maven 3 artifact from [" + url + "].");
                            return;
                        }
                        catch (Exception e)
                        {
                            getLog().debug("Cannot parse URL [" + url + "].", e);
                        }
                    }

                    ZipUrlInstaller installerElement = new ZipUrlInstaller();
                    getContainerElement().setZipUrlInstaller(installerElement);
                    installerElement.setUrl(new URL(url));
                    getLog().info("You did not specify a container home nor any installer. "
                        + "Codehaus Cargo will automatically download your container's "
                            + "binaries from [" + url + "].");
                }
            }
        }
    }

    /**
     * Create the autodeploy deployable (if the current project is a Java EE deployable)
     * @param container Container.
     * @return The autodeploy deployable.
     * @throws MojoExecutionException If deployable creation fails.
     */
    protected org.codehaus.cargo.container.deployable.Deployable createAutoDeployDeployable(
        org.codehaus.cargo.container.Container container) throws MojoExecutionException
    {
        Deployable deployableElement = new Deployable();
        return deployableElement.createDeployable(container.getId(), getCargoProject());
    }

    /**
     * Create a logger. If a <code>&lt;log&gt;</code> configuration element has been specified by
     * the user then use it. If none is specified then log to the Maven 3 logging subsystem.
     * 
     * @return the logger to use for logging this plugin's activity
     */
    protected Logger createLogger()
    {
        Logger logger;
        if (getContainerElement() != null && getContainerElement().getLog() != null)
        {
            // Ensure that the directories where the log will go are created
            getContainerElement().getLog().getParentFile().mkdirs();

            logger = new FileLogger(getContainerElement().getLog(), true);
        }
        else
        {
            logger = new MavenLogger(getLog(), useLogCategoryPrefix);
        }

        if (getContainerElement() != null && getContainerElement().getLogLevel() != null)
        {
            logger.setLevel(getContainerElement().getLogLevel());
        }
        else
        {
            if (getLog().isDebugEnabled())
            {
                logger.setLevel(LogLevel.DEBUG);
            }
            else if (getLog().isInfoEnabled())
            {
                logger.setLevel(LogLevel.INFO);
            }
            else
            {
                logger.setLevel(LogLevel.WARN);
            }
        }

        return logger;
    }

    /**
     * Waits until all deployables with a deployable monitor are deployed / undeployed.
     * 
     * @param container Container where is deployable deployed.
     * @param starting <code>true</code> if container is starting (i.e., wait for deployment),
     * <code>false</code> otherwise.
     */
    protected void waitDeployableMonitor(org.codehaus.cargo.container.Container container,
        boolean starting)
    {
        if (getDeployablesElement() != null)
        {
            Logger watchdogLogger = createLogger();

            for (Deployable deployable : getDeployablesElement())
            {
                DeployableMonitorFactory monitorFactory = new DefaultDeployableMonitorFactory();
                DeployableMonitor monitor =
                    monitorFactory.createDeployableMonitor(container, deployable);

                if (monitor != null)
                {
                    DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
                    watchdog.setLogger(watchdogLogger);
                    monitor.setLogger(watchdogLogger);
                    watchdog.watch(starting);
                }
            }
        }
    }

    /**
     * Calculate the absolute directory for any given path. This method will also emit a warning if
     * the given path is not absolute.
     * @param type Directory type, for example <code>container home</code>.
     * @param directory Directory path.
     * @return Absolute directory path.
     */
    private String calculateAbsoluteDirectory(String type, String directory)
    {
        File directoryFile = new File(directory);
        if (!directoryFile.isAbsolute())
        {
            String absoluteDirectory = directoryFile.getAbsolutePath();
            getLog().warn("The provided " + type + " directory [" + directory
                + "] is not an absolute directory. Replacing it with its absolute directory "
                    + "counterpart, i.e. [" + absoluteDirectory + "]. To avoid this message in "
                        + "the future, you can also use the ${basedir} variable in your paths.");
            return absoluteDirectory;
        }
        else
        {
            return directory;
        }
    }
}
