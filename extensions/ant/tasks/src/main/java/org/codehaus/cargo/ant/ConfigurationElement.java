/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.ant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.util.XmlReplacement;
import org.codehaus.cargo.util.log.Logger;

/**
 * Nested Ant element to wrap the
 * {@link org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory} class.
 */
public class ConfigurationElement
{
    /**
     * The default configuration type is standalone.
     * @see #setType(String)
     */
    private ConfigurationType type = ConfigurationType.STANDALONE;

    /**
     * @see #setHome(String)
     */
    private String home;

    /**
     * Configuration properties.
     */
    private List<Property> properties = new ArrayList<Property>();

    /**
     * Container properties loaded from file.
     */
    private File propertiesFile;

    /**
     * Custom configuration class to associate to the containing container.
     */
    private Class configurationClass;

    /**
     * List of deployables to deploy.
     */
    private List<DeployableElement> deployables = new ArrayList<DeployableElement>();

    /**
     * List of configuration files
     */
    private List<FileConfig> fileConfigs = new ArrayList<FileConfig>();

    /**
     * List of files
     */
    private List<FileConfig> files = new ArrayList<FileConfig>();

    /**
     * List of XML replacements
     */
    private List<XmlReplacement> xmlReplacements = new ArrayList<XmlReplacement>();

    /**
     * List of datasources
     */
    private List<DataSourceElement> dataSources = new ArrayList<DataSourceElement>();

    /**
     * List of users
     */
    private List<UserElement> users = new ArrayList<UserElement>();

    /**
     * @param configurationClass the configuration class to associate to the containing container
     */
    public void setClass(Class configurationClass)
    {
        this.configurationClass = configurationClass;
    }

    /**
     * @return the configuration class associated with the containing container
     */
    protected Class getConfigurationClass()
    {
        return this.configurationClass;
    }

    /**
     * @param deployableElement the nested deployable element to deploy
     */
    public void addConfiguredDeployable(DeployableElement deployableElement)
    {
        this.deployables.add(deployableElement);
    }

    /**
     * @return the nested deployable elements to deploy
     */
    protected List<DeployableElement> getDeployables()
    {
        return this.deployables;
    }

    /**
     * @param configfileElement the nested config element to deploy
     */
    public void addConfiguredConfigfile(FileConfig configfileElement)
    {
        this.fileConfigs.add(configfileElement);
    }

    /**
     * Get the list of configFiles
     * @return the configFiles
     */
    protected List<FileConfig> getFileConfigs()
    {
        return this.fileConfigs;
    }

    /**
     * @param fileConfigElement the nested file element to deploy
     */
    public void addConfiguredFile(FileConfig fileConfigElement)
    {
        this.files.add(fileConfigElement);
    }

    /**
     * Get the list of files
     * @return the files
     */
    protected List<FileConfig> getFiles()
    {
        return this.files;
    }

    /**
     * Add a container property.
     * 
     * @param property the container property to add
     */
    public void addConfiguredProperty(Property property)
    {
        this.properties.add(property);
    }

    /**
     * @return the list of container properties
     */
    protected List<Property> getProperties()
    {
        return this.properties;
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
     * @param xmlReplacement the list of XML replacements
     */
    public void addConfiguredXmlreplacement(XmlReplacement xmlReplacement)
    {
        this.xmlReplacements.add(xmlReplacement);
    }

    /**
     * @return the list of XML replacements
     */
    protected List<XmlReplacement> getXmlReplacements()
    {
        return this.xmlReplacements;
    }

    /**
     * @param dataSourceElement the nested datasource element
     */
    public void addConfiguredDataSource(DataSourceElement dataSourceElement)
    {
        this.dataSources.add(dataSourceElement);
    }

    /**
     * @return the nested datasource elements
     */
    public List<DataSourceElement> getDataSources()
    {
        return dataSources;
    }

    /**
     * @param userElement the nested user element
     */
    public void addConfiguredUser(UserElement userElement)
    {
        this.users.add(userElement);
    }

    /**
     * @return the nested user elements
     */
    protected List<UserElement> getUsers()
    {
        return this.users;
    }

    /**
     * Defaults to the standalone configuration if type is not specified by the user.
     * 
     * @param typeAsString the configuration type as a string
     * @see org.codehaus.cargo.generic.configuration.ConfigurationFactory
     */
    public void setType(String typeAsString)
    {
        this.type = ConfigurationType.toType(typeAsString);
    }

    /**
     * @return the configuration type
     */
    public ConfigurationType getType()
    {
        return this.type;
    }

    /**
     * @param home the home directory to set
     */
    public void setHome(String home)
    {
        this.home = home;
    }

    /**
     * @return the home directory
     */
    public String getHome()
    {
        return this.home;
    }

    /**
     * @param containerId the container id associated with this configuration
     * @param containerType the container type associated with this configuration
     * @param antProject the Ant project
     * @param log the logger
     * @return a configuration instance matching this container and the defined type
     */
    public Configuration createConfiguration(String containerId, ContainerType containerType,
        Project antProject, Logger log)
    {
        ConfigurationFactory factory = new DefaultConfigurationFactory();

        if (getConfigurationClass() != null)
        {
            factory.registerConfiguration(containerId, containerType, getType(),
                getConfigurationClass());
        }

        // Only use the dir if specified
        Configuration configuration;
        if (getHome() == null)
        {
            configuration = factory.createConfiguration(containerId, containerType, getType());
        }
        else
        {
            configuration = factory.createConfiguration(containerId, containerType, getType(),
                getHome());
        }

        // CARGO-1578: Allow container configuration properties to be set using
        //             Ant build properties
        if (antProject != null && antProject.getProperties() != null)
        {
            for (Map.Entry<String, Object> property : antProject.getProperties().entrySet())
            {
                if (property.getKey() != null && property.getValue() != null
                    && property.getValue() instanceof String)
                {
                    if (property.getKey().startsWith("cargo.")
                        && configuration.getCapability().supportsProperty(property.getKey()))
                    {
                        log.debug(
                            "Injecting container configuration property [" + property.getKey()
                                + "] based on the Ant build property", this.getClass().getName());
                        configuration.setProperty(
                            property.getKey(), (String) property.getValue());
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
            catch (IOException e)
            {
                throw new BuildException("Configuration property file ["
                    + getPropertiesFile() + "] cannot be loaded", e);
            }
        }

        // Set all container properties
        for (Property property : getProperties())
        {
            configuration.setProperty(property.getName(), property.getValue());
        }

        if (configuration instanceof StandaloneLocalConfiguration)
        {
            StandaloneLocalConfiguration standaloneLocalConfiguration =
                (StandaloneLocalConfiguration) configuration;

            // Set all XML replacements
            for (XmlReplacement xmlReplacement : getXmlReplacements())
            {
                standaloneLocalConfiguration.addXmlReplacement(xmlReplacement);
            }
        }

        // Add static deployables and configuration files for local configurations
        if (configuration instanceof LocalConfiguration)
        {
            LocalConfiguration localConfiguration = (LocalConfiguration) configuration;

            addStaticDeployables(containerId, localConfiguration);

            if (getFileConfigs() != null)
            {
                for (FileConfig configfile : getFileConfigs())
                {
                    localConfiguration.setConfigFileProperty(configfile);
                }
            }

            if (getFiles() != null)
            {
                for (FileConfig file : getFiles())
                {
                    localConfiguration.setFileProperty(file);
                }
            }

            if (getDataSources() != null)
            {
                for (DataSourceElement dataSource : getDataSources())
                {
                    localConfiguration.addDataSource(dataSource.createDataSource());
                }
            }

            if (getUsers() != null)
            {
                for (UserElement user : getUsers())
                {
                    localConfiguration.addUser(user.createUser());
                }
            }
        }

        return configuration;
    }

    /**
     * Add static deployables to the configuration.
     * 
     * @param containerId the container id to which to deploy to
     * @param configuration the local configuration to which to add Deployables to
     */
    private void addStaticDeployables(String containerId, LocalConfiguration configuration)
    {
        for (DeployableElement deployableElement : getDeployables())
        {
            configuration.addDeployable(deployableElement.createDeployable(containerId));
        }
    }
}
