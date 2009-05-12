/* 
 * ========================================================================
 * 
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.maven2;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.maven2.configuration.Configuration;
import org.codehaus.cargo.maven2.configuration.Container;
import org.codehaus.cargo.maven2.configuration.Deployable;
import org.codehaus.cargo.maven2.configuration.Deployer;
import org.codehaus.cargo.maven2.jetty.JettyArtifactResolver;
import org.codehaus.cargo.maven2.log.MavenLogger;
import org.codehaus.cargo.maven2.util.CargoProject;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.FileLogger;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Common code used by Cargo MOJOs requiring <code>&lt;container&gt;</code> and
 * <code>&lt;configuration&gt;</code> elements and supporting the notion of Auto-deployable.
 *
 * @version $Id$
 */
public abstract class AbstractCargoMojo extends AbstractCommonMojo
{
    /**
     * The key under which the container instance is stored in the plugin context. We store it so
     * that it's possible to get back the same container instance even if this mojo is called in
     * a different Maven execution context. This is required for stopping embedded containers for
     * example as we need to use the same instance that was started in order to stop them.
     */
    public static final String CONTEXT_KEY_CONTAINER =
        AbstractCargoMojo.class.getName() + "-Container";

    /**
     * File utility class.
     */
    private FileHandler fileHandler = new DefaultFileHandler();
    
    /**
     * @parameter
     * @see #getConfigurationElement()
     */
    private Configuration configuration;

    /**
     * Configures a Cargo {@link org.codehaus.cargo.container.Container}.
     * See the <a href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2 plugin
     * reference guide</a> for more details.
     *
     * @parameter
     */
    private Container container;

    /**
     * @parameter
     * @see #getDeployerElement()
     */
    private Deployer deployer;

    /**
     * The artifact resolver is used to dynamically resolve JARs that have to be in the embedded
     * container's classpaths. Another solution would have been to statitically define them a
     * dependencies in the plugin's POM. Resolving them in a dynamic manner is much better as only
     * the required JARs for the defined embedded container are downloaded.
     *
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * The local Maven repository. This is used by the artifact resolver to download resolved
     * JARs and put them in the local repository so that they won't have to be fetched again next
     * time the plugin is executed.
     *
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * The remote Maven repositories used by the artifact resolver to look for JARs.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    private List repositories;

    /**
     * The artifact factory is used to create valid Maven
     * {@link org.apache.maven.artifact.Artifact} objects. This is used to pass Maven artifacts to
     * the artifact resolver so that it can download the required JARs to put in the embedded
     * container's classpaths.
     *
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @see org.codehaus.cargo.maven2.util.CargoProject
     */
    private CargoProject cargoProject;

    /**
     * Maven settings, injected automatically.
     * 
     * @parameter expression="${settings}"
     * @readonly
     */
    private Settings settings;

    /**
     * @return the Cargo file utility class
     */
    protected FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the Cargo file utility class to use. This method is useful for unit
     *        testing with Mock objects as it can be passed a test file handler that doesn't perform
     *        any real file action.
     */
    protected void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }
    
    /**
     * @return the user configuration of a Cargo
     *         {@link org.codehaus.cargo.container.deployer.Deployer}. See the
     *         <a href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2
     *         plugin reference guide</a> and
     *         {@link org.codehaus.cargo.maven2.configuration.Deployer} for more details.
     */
    protected Deployer getDeployerElement()
    {
        return this.deployer;
    }

    /**
     * @param deployerElement the {@link org.codehaus.cargo.container.deployer.Deployer}
     *         configuration defined by the user
     * @see #getDeployerElement() 
     */
    protected void setDeployerElement(Deployer deployerElement)
    {
        this.deployer = deployerElement;
    }

    /**
     * @return the user configuration of a Cargo
     *         {@link org.codehaus.cargo.container.configuration.Configuration}. See the
     *         <a href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2
     *         plugin reference guide</a> and
     *         {@link org.codehaus.cargo.maven2.configuration.Configuration} for more details.
     */
    protected Configuration getConfigurationElement()
    {
        return this.configuration;
    }

    /**
     * @param configurationElement the
              {@link org.codehaus.cargo.container.configuration.Configuration} configuration
              defined by the user
     * @see #getConfigurationElement()
     */
    protected void setConfigurationElement(Configuration configurationElement)
    {
        this.configuration = configurationElement;
    }

    /**
     * @return the user configuration of a Cargo
     *         {@link org.codehaus.cargo.container.Container}. See the
     *         <a href="http://cargo.codehaus.org/Maven2+Plugin+Reference+Guide">Cargo Maven2
     *         plugin reference guide</a> and
     *         {@link org.codehaus.cargo.maven2.configuration.Container} for more details.
     */
    protected Container getContainerElement()
    {
        return this.container;
    }

    /**
     * @param containerElement the {@link org.codehaus.cargo.container.Container} configuration
     *        defined by the user
     * @see #getContainerElement() 
     */
    protected void setContainerElement(Container containerElement)
    {
        this.container = containerElement;
    }

    /**
     * @see org.codehaus.cargo.maven2.util.CargoProject
     */
    protected void setCargoProject(CargoProject cargoProject)
    {
        this.cargoProject = cargoProject;
    }

    /**
     * @see org.codehaus.cargo.maven2.util.CargoProject
     */
    protected CargoProject getCargoProject()
    {
        return this.cargoProject;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Note: This method is final so that extending classes cannot extend it. Instead they should
     * implement the {@link #doExecute()} method.</p>
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public final void execute() throws MojoExecutionException
    {
        if (this.cargoProject == null)
        {
            this.cargoProject = new CargoProject(getProject(), getLog());
        }
        doExecute();
    }

    /**
     * Executes the plugin.
     *
     * <p>This method must be implemented by all Mojos extending this class. The
     * reason for this pattern is because we want the {@link #execute()} method to always be called
     * so that necessary plugin initialization can be performed. Without this pattern Mojos
     * extending this class could "forget" to call <code>super.execute()</code> thus leading to
     * unpredictible results.</p>
     *
     * @throws MojoExecutionException in case of error
     */
    protected abstract void doExecute() throws MojoExecutionException;

    /**
     * Creates a {@link org.codehaus.cargo.container.configuration.Configuration} instance. If the
     * user has not specified a configuration element in the POM file then automatically create
     * a standalone configuration if the container's type is local or otherwise create a runtime
     * configuration.
     * 
     * @return a valid {@link org.codehaus.cargo.container.configuration.Configuration} instance
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
                configurationElement.setType(ConfigurationType.STANDALONE);
                configurationElement.setHome(new File(getCargoProject().getBuildDirectory(),
                    getContainerElement().getContainerId()).getPath());
            }
            else
            {
                configurationElement.setType(ConfigurationType.RUNTIME);
            }

            setConfigurationElement(configurationElement);
        }

        configuration = getConfigurationElement().createConfiguration(
            getContainerElement().getContainerId(), getContainerElement().getType(),
            getCargoProject());

        // Find the cargo.server.settings for the current configuration. When found, iterate in
        // the list of servers in Maven's settings.xml file in order to find out which server id
        // corresponds to that identifier, and copy all non-set settings (cargo.remote.uri, ...).
        //
        // This feature helps people out in centralising their configurations.
        Map properties = configuration.getProperties();
        Iterator propertiesIterator = properties.entrySet().iterator();
        while (propertiesIterator.hasNext())
        {
            Map.Entry property = (Map.Entry) propertiesIterator.next();
            String propertyKey = (String) property.getKey();
            if ("cargo.server.settings".equals(propertyKey))
            {
                String serverId = (String) property.getValue();
                getLog().debug(
                    "Found cargo.server.settings: key is " + propertyKey + ", value is " + serverId);
                Iterator servers = settings.getServers().iterator();
                while (servers.hasNext())
                {
                    Server server = (Server) servers.next();
                    if (serverId.equals(server.getId()))
                    {
                        getLog().debug(
                            "The Maven settings.xml file contains a reference for the "
                                + "server with cargo.server.settings " + serverId
                                + ", starting property injection");

                        Xpp3Dom[] globalConfigurationOptions = ((Xpp3Dom) server.getConfiguration())
                            .getChildren();
                        for (int i = 0; i < globalConfigurationOptions.length; i++)
                        {
                            Xpp3Dom option = globalConfigurationOptions[i];
                            if (properties.get(option.getName()) == null)
                            {
                                properties.put(option.getName(), option.getValue());
                                getLog().debug(
                                    "\tInjected property: " + option.getName() + '='
                                        + option.getValue());
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }

        return configuration;
    }

    /**
     * @return a {@link org.codehaus.cargo.container.Container} instance if no container object
     *         was stored in the Maven Plugin Context or returns the saved instance otherwise. If
     *         a new container instance is created it's also saved in the Maven Plugin Context for
     *         later retrieval.
     * @throws MojoExecutionException in case of error
     */
    protected org.codehaus.cargo.container.Container createContainer()
        throws MojoExecutionException
    {
        org.codehaus.cargo.container.Container container = null;

        // Try to find the container in the Maven Plugin Context first.
        Map context = getPluginContext();

        String containerKey;
        // Containers don't have a unique ID which makes it hard to start  multiple containers 
        // on the same system. If the configuration has a home, then we can use that as an ID.
        // some containers (like embedded containers) do not have a home element specified, in
        // that case we will have to use the container type as the id (note: this means we can't 
        // start multiple embedded servers of the same type).
        // TODO: come up with a proper unique ID for each container.
        if (getConfigurationElement() == null)
        {
            // since we don't have a home element, just use the container 
            containerKey = CONTEXT_KEY_CONTAINER;
        }
        else
        {
            // use both the container and the container home
            containerKey = CONTEXT_KEY_CONTAINER + "." + getConfigurationElement().getHome();
        }
        
        if (context != null)
        {
            container = (org.codehaus.cargo.container.Container) context.get(containerKey);
        }

        if (container == null)
        {
            container = createNewContainer();
        }

        if (context != null)
        {
            context.put(containerKey, container);
        }

        return container;
    }

    /**
     * Creates a brand new {@link org.codehaus.cargo.container.Container} instance. If the user
     * has not specified a container element in the POM file or if the user has not specified the
     * container id then automatically create a default container (as defined in
     * {@link #computeContainerId}) if the project calling this plugin has a WAR packaging. If the
     * packaging is different then an exception is raised.
     *
     * @return a valid {@link org.codehaus.cargo.container.Container} instance
     * @throws MojoExecutionException in case of error or if a default container could not be
     *         created
     */
    protected org.codehaus.cargo.container.Container createNewContainer()
        throws MojoExecutionException
    {
        org.codehaus.cargo.container.Container container;

        if (getContainerElement() == null)
        {
            // Only accept default configuration if the packaging is not of type EAR as Cargo
            // currently doesn't have an embedded container that supports EAR (we need to add
            // openEJB support!).
            if ((getCargoProject().getPackaging() != null)
                && !getCargoProject().getPackaging().equalsIgnoreCase("war"))
            {
                throw new MojoExecutionException("For all packaging other than war you need to "
                    + "configure the container you wishes to use.");
            }

            Container containerElement = new Container();
            computeContainerId(containerElement);

            getLog().info("No container defined, using a default ["
                + containerElement.getContainerId() + ", " + containerElement.getType().getType()
                + "] container");

            setContainerElement(containerElement);
        }

        // If no container id is specified, default to Jetty
        if (getContainerElement().getContainerId() == null)
        {
            computeContainerId(getContainerElement());
        }

        if (getContainerElement().getType() == ContainerType.EMBEDDED)
        {
            loadEmbeddedContainerDependencies();
        }

        container = getContainerElement().createContainer(createConfiguration(),
            createLogger(), getCargoProject());

        return container;
    }

    /**
     * Sets the default container (Jetty 6.x) in the passed container element.
     *
     * @param containerElement the configuration specified by the user in the
     *        <code>&lt;container&gt;</code> element. Note that this parameter's is modified when
     *        the method returns. 
     */
    private void computeContainerId(Container containerElement)
    {
        // If no container has been specified then default to Jetty 6.x
        if (containerElement.getContainerId() == null)
        {
            containerElement.setContainerId("jetty6x");
            containerElement.setType(ContainerType.EMBEDDED);
        }
    }
    
    protected void loadEmbeddedContainerDependencies() throws MojoExecutionException
    {
        if (getContainerElement().getContainerId().startsWith("jetty"))
        {
            JettyArtifactResolver resolver = new JettyArtifactResolver(this.artifactResolver,
                this.localRepository, this.repositories, this.artifactFactory);
            ClassLoader classLoader = resolver.resolveDependencies(
                getContainerElement().getContainerId(), getCargoProject().getEmbeddedClassLoader());
            getCargoProject().setEmbeddedClassLoader(classLoader);
        }
    }

    protected org.codehaus.cargo.container.deployable.Deployable createAutoDeployDeployable(
        org.codehaus.cargo.container.Container container) throws MojoExecutionException
    {
        Deployable deployableElement = new Deployable();
        return deployableElement.createDeployable(container.getId(), getCargoProject());
    }

    protected boolean containsAutoDeployable(Deployable[] deployableElements)
    {
        boolean found = false;

        for (int i = 0; i < deployableElements.length; i++)
        {
            Deployable deployableElement = deployableElements[i];
            if (deployableElement.getGroupId().equals(getCargoProject().getGroupId())
                && deployableElement.getArtifactId().equals(getCargoProject().getArtifactId()))
            {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Create a logger. If a <code>&lt;log&gt;</code> configuration element has been specified
     * by the user then use it. If none is specified then log to the Maven 2 logging subsystem.
     *
     * @return the logger to use for logging this plugin's activity
     */
    protected Logger createLogger()
    {
        Logger logger;
        if ((getContainerElement() != null) && (getContainerElement().getLog() != null))
        {
            // Ensure that the directories where the log will go are created
            getContainerElement().getLog().getParentFile().mkdirs();

            logger = new FileLogger(getContainerElement().getLog(), true);
        }
        else
        {
            logger = new MavenLogger(getLog());
        }

        if ((getContainerElement() != null) && (getContainerElement().getLogLevel() != null))
        {
            logger.setLevel(getContainerElement().getLogLevel());
        }

        return logger;
    }
}
