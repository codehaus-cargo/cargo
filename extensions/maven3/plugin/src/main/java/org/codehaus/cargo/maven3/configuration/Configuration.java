/*
 * ========================================================================
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
package org.codehaus.cargo.maven3.configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.maven3.util.CargoProject;
import org.codehaus.cargo.util.XmlReplacement;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Holds configuration data for the <code>&lt;configuration&gt;</code> tag used to configure the
 * plugin in the <code>pom.xml</code> file.
 */
public class Configuration
{
    /**
     * Configuration type.
     */
    private String type = ConfigurationType.STANDALONE.getType();

    /**
     * Implementation name.
     */
    private String implementation;

    /**
     * Container home.
     */
    private String home;

    /**
     * Container properties.
     */
    private Map<String, String> properties;

    /**
     * Container properties loaded from file.
     */
    private File propertiesFile;

    /**
     * Extra files.
     */
    private FileConfig[] fileConfigs;

    /**
     * Configuration files.
     */
    private FileConfig[] configfiles;

    /**
     * XML replacements.
     */
    private XmlReplacement[] xmlReplacements;

    /**
     * Extra datasources.
     */
    private DataSource[] datasources;

    /**
     * Extra resources.
     */
    private Resource[] resources;

    /**
     * Users.
     */
    private User[] users;

    /**
     * @return Configuration type.
     */
    public ConfigurationType getType()
    {
        return ConfigurationType.toType(this.type);
    }

    /**
     * @param type Configuration type.
     */
    public void setType(ConfigurationType type)
    {
        this.type = type.getType();
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
     * @return Container properties.
     */
    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    /**
     * @param properties Container properties.
     */
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    /**
     * @return Container properties loaded from file.
     */
    public File getPropertiesFile()
    {
        return propertiesFile;
    }

    /**
     * @param propertiesFile Container properties loaded from file.
     */
    public void setPropertiesFile(File propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

    /**
     * @return Configuration files.
     */
    public FileConfig[] getConfigfiles()
    {
        return this.configfiles;
    }

    /**
     * @param configfiles Configuration files.
     */
    public void setConfigfiles(FileConfig[] configfiles)
    {
        this.configfiles = configfiles;
    }

    /**
     * @return Extra files.
     */
    public FileConfig[] getFiles()
    {
        return this.fileConfigs;
    }

    /**
     * @param fileConfigs Extra files.
     */
    public void setFiles(FileConfig[] fileConfigs)
    {
        this.fileConfigs = fileConfigs;
    }

    /**
     * @return XML replacements.
     */
    public XmlReplacement[] getXmlReplacements()
    {
        return this.xmlReplacements;
    }

    /**
     * @param xmlReplacements XML replacements.
     */
    public void setXmlReplacements(XmlReplacement[] xmlReplacements)
    {
        this.xmlReplacements = xmlReplacements;
    }

    /**
     * @return Implementation name.
     */
    public String getImplementation()
    {
        return this.implementation;
    }

    /**
     * @param implementation Implementation name.
     */
    public void setImplementation(String implementation)
    {
        this.implementation = implementation;
    }

    /**
     * @return Extra datasources.
     */
    public DataSource[] getDatasources()
    {
        return datasources;
    }

    /**
     * @param datasources Extra datasources.
     */
    public void setDatasources(DataSource[] datasources)
    {
        this.datasources = datasources;
    }

    /**
     * @return Extra resources.
     */
    public Resource[] getResources()
    {
        return this.resources;
    }

    /**
     * @param rlist Extra resources.
     */
    public void setResources(Resource[] rlist)
    {
        this.resources = rlist;
    }

    /**
     * @return Users.
     */
    public User[] getUsers()
    {
        return users;
    }

    /**
     * @param users Users.
     */
    public void setUsers(User[] users)
    {
        this.users = users;
    }

    /**
     * Creates a configuration.
     * @param containerId Container id.
     * @param containerType Container type.
     * @param deployables Deployables to deploy.
     * @param project Cargo project.
     * @param mavenProject Maven project.
     * @param contextKey Container context key, which can be used to start, stop, configure or
     * deploy to the same Cargo container (together with its configuration) from different Maven
     * artifacts.
     * @param mavenSettings Maven settings.
     * @param log Maven logger.
     * @return Configuration.
     * @throws MojoExecutionException If configuration creation fails.
     */
    public org.codehaus.cargo.container.configuration.Configuration createConfiguration(
        String containerId, ContainerType containerType, Deployable[] deployables,
        CargoProject project, MavenProject mavenProject, String contextKey, Settings mavenSettings,
        Log log) throws MojoExecutionException
    {
        ConfigurationFactory factory = new DefaultConfigurationFactory();

        if (getImplementation() != null)
        {
            try
            {
                Class configurationClass = Class.forName(getImplementation(), true,
                    this.getClass().getClassLoader());
                factory.registerConfiguration(containerId, containerType, getType(),
                    configurationClass);
            }
            catch (ClassNotFoundException cnfe)
            {
                throw new MojoExecutionException("Custom configuration implementation ["
                    + getImplementation() + "] cannot be loaded", cnfe);
            }
        }

        // Only use the dir if specified
        org.codehaus.cargo.container.configuration.Configuration configuration;
        if (getHome() == null)
        {
            if (project.isDaemonRun())
            {
                configuration =
                    factory.createConfiguration(containerId, containerType, getType(), "");
            }
            else if (ConfigurationType.RUNTIME.equals(getType()))
            {
                configuration =
                    factory.createConfiguration(containerId, containerType, getType(), null);
            }
            else
            {
                File home =
                    new File(project.getBuildDirectory(), "cargo/configurations/" + containerId);
                configuration = factory.createConfiguration(
                    containerId, containerType, getType(), home.getAbsolutePath());
            }
        }
        else
        {
            configuration =
                factory.createConfiguration(containerId, containerType, getType(), getHome());
        }

        // CARGO-596 / CARGO-1020: Find the container context key or cargo.server.settings for the
        // current configuration. When found, iterate in the list of servers in Maven's
        // settings.xml file in order to find out which server id corresponds to that identifier,
        // and copy all settings.
        //
        // This feature helps people out in centralising their configurations.
        if (mavenSettings != null)
        {
            String serverId = null;
            if (getProperties() != null)
            {
                serverId = getProperties().get("cargo.server.settings");
            }
            if (serverId != null && !serverId.isEmpty())
            {
                log.debug("Found cargo.server.settings: " + serverId);
            }
            else if (contextKey != null)
            {
                serverId = contextKey;
                log.debug("Found container context key: " + serverId);
            }
            if (serverId != null && !serverId.isEmpty())
            {
                for (Server server : mavenSettings.getServers())
                {
                    if (serverId.equals(server.getId()))
                    {
                        log.info(
                            "The Maven settings.xml file contains a reference for the server with "
                                + "identifier [" + serverId
                                    + "], injecting configuration properties");

                        Xpp3Dom[] globalConfigurationOptions =
                            ((Xpp3Dom) server.getConfiguration()).getChildren();
                        for (Xpp3Dom option : globalConfigurationOptions)
                        {
                            log.info(
                                "\tInjecting container configuration property [" + option.getName()
                                    + "] based on the Maven settings.xml reference");
                            configuration.setProperty(
                                option.getName(), transformMavenPropertyValue(option.getValue()));
                        }
                        break;
                    }
                }
            }
        }

        // CARGO-1579: Allow container configuration properties to be set using
        //             Maven project properties
        if (mavenProject != null && mavenProject.getProperties() != null)
        {
            for (Map.Entry<Object, Object> property : mavenProject.getProperties().entrySet())
            {
                if (property.getKey() != null && property.getKey() instanceof String
                    && property.getValue() instanceof String)
                {
                    String key = (String) property.getKey();
                    if (key.startsWith("cargo.")
                        && configuration.getCapability().supportsProperty(key))
                    {
                        log.info(
                            "Injecting container configuration property [" + key
                                + "] based on the associated Maven project property");
                        configuration.setProperty(
                            key, transformMavenPropertyValue((String) property.getValue()));
                    }
                }
            }
        }

        // Set container properties loaded from file (if any)
        if (getPropertiesFile() != null)
        {
            Properties properties = new Properties();
            try
            {
                try (InputStream inputStream = new FileInputStream(getPropertiesFile()))
                {
                    properties.load(new BufferedInputStream(inputStream));
                }
                for (Enumeration<?> propertyNames = properties.propertyNames();
                    propertyNames.hasMoreElements();)
                {
                    String propertyName = (String) propertyNames.nextElement();
                    String propertyValue = properties.getProperty(propertyName);
                    configuration.setProperty(propertyName, propertyValue);
                }
            }
            catch (FileNotFoundException e)
            {
                log.warn(
                    "Configuration property file [" + getPropertiesFile()
                        + "] does not exist, skipping");
            }
            catch (IOException ioe)
            {
                throw new MojoExecutionException("Configuration property file ["
                    + getPropertiesFile() + "] cannot be loaded", ioe);
            }
        }

        // Set static container properties (if any)
        if (getProperties() != null)
        {
            for (Map.Entry<String, String> property : getProperties().entrySet())
            {
                configuration.setProperty(
                    property.getKey(), transformMavenPropertyValue(property.getValue()));
            }
        }

        if (configuration instanceof StandaloneLocalConfiguration && getXmlReplacements() != null)
        {
            StandaloneLocalConfiguration standaloneLocalConfiguration =
                (StandaloneLocalConfiguration) configuration;

            // Set all XML replacements
            for (XmlReplacement xmlReplacement : getXmlReplacements())
            {
                standaloneLocalConfiguration.addXmlReplacement(xmlReplacement);
            }
        }

        // Add static deployables and config files for local configurations
        if (configuration instanceof LocalConfiguration)
        {
            LocalConfiguration localConfiguration = (LocalConfiguration) configuration;

            if (deployables != null)
            {
                addStaticDeployables(containerId, localConfiguration, deployables, project);
            }

            if (getDatasources() != null)
            {
                addDatasources(localConfiguration);
            }

            if (getResources() != null)
            {
                addResources(containerId, localConfiguration, project);
            }

            if (getUsers() != null)
            {
                addUsers(getUsers(), localConfiguration);
            }

            if (getConfigfiles() != null)
            {
                for (FileConfig fileConfig : getConfigfiles())
                {
                    localConfiguration.setConfigFileProperty(fileConfig);
                }
            }
            if (getFiles() != null)
            {
                for (FileConfig fileConfig : getFiles())
                {
                    localConfiguration.setFileProperty(fileConfig);
                }
            }
        }

        return configuration;
    }

    /**
     * Maven 3 doesn't like empty elements and will set them to <code>null</code>. Thus we we need
     * need to modify that behavior and change them to an empty string. For example this allows
     * users to pass an empty password for the <code>cargo.remote.password</code> configuration
     * property.
     * @param value Maven property value.
     * @return Transformed Maven property value, in particular <code>null</code> changed to an
     * empty string.
     */
    private String transformMavenPropertyValue(String value)
    {
        if (value == null)
        {
            return "";
        }
        else
        {
            return value;
        }
    }

    /**
     * Add datasources to the configuration.
     * @param configuration Container configuration.
     * @throws MojoExecutionException If datasource creation fails.
     */
    private void addDatasources(LocalConfiguration configuration)
        throws MojoExecutionException
    {
        for (DataSource dataSource : datasources)
        {
            configuration.addDataSource(dataSource.createDataSource());
        }
    }

    /**
     * Add resources to the configuration.
     * @param containerId Container id.
     * @param configuration Container configuration.
     * @param project Cargo project.
     * @throws MojoExecutionException If resource creation fails.
     */
    private void addResources(String containerId, LocalConfiguration configuration,
        CargoProject project) throws MojoExecutionException
    {
        for (Resource resource : getResources())
        {
            configuration.addResource(resource.createResource(containerId, project));
        }
    }

    /**
     * Add users to the configuration.
     * @param users Users to be added.
     * @param configuration Container configuration.
     */
    private void addUsers(User[] users, LocalConfiguration configuration)
    {
        for (User user : users)
        {
            configuration.addUser(user.createUser());
        }
    }

    /**
     * Add static deployables to the configuration.
     * 
     * @param containerId the container id to which to deploy to.
     * @param configuration the local configuration to which to add Deployables to.
     * @param deployables Deployables to deploy.
     * @param project Cargo project.
     * @throws MojoExecutionException If resource creation fails.
     */
    private void addStaticDeployables(String containerId, LocalConfiguration configuration,
        Deployable[] deployables, CargoProject project) throws MojoExecutionException
    {
        for (Deployable deployable : deployables)
        {
            project.getLog().debug("Scheduling deployable for deployment: [groupId ["
                + deployable.getGroupId() + "], artifactId ["
                + deployable.getArtifactId() + "], type ["
                + deployable.getType() + "], location ["
                + deployable.getLocation() + "], pingURL ["
                + deployable.getPingURL() + "]]");

            configuration.addDeployable(deployable.createDeployable(containerId, project));
        }
    }
}
