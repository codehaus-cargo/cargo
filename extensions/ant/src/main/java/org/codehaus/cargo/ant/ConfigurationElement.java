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
package org.codehaus.cargo.ant;

import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Nested Ant element to wrap the
 * {@link org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory} class.
 *
 * @version $Id$
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
    private List properties = new ArrayList();

    /**
     * Custom configuration class to associate to the containing container.
     */
    private Class configurationClass;

    /**
     * List of deployables to deploy.
     */
    private List deployables = new ArrayList();

    /**
     *List of configuration files
     */
    private List fileConfigs = new ArrayList();
    
    /**
     * List of files
     */
    private List files = new ArrayList();

    /**
     * @param configurationClass the configuration class to associate to the containing container
     */
    public final void setClass(Class configurationClass)
    {
        this.configurationClass = configurationClass;
    }

    /**
     * @return the configuration class associated with the containing container
     */
    protected final Class getConfigurationClass()
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
    protected final List getDeployables()
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
     * @param fileConfigElement the nested file element to deploy
     */
    public void addConfiguredFile(FileConfig fileConfigElement)
    {
        this.files.add(fileConfigElement);
    }
    
    /**
     * Get the list of configFiles
     * @return the configFiles
     */
    protected final List getFileConfigs()
    {
        return this.fileConfigs;
    }
    
    /**
     * Get the list of files
     * @return the files
     */
    protected final List getFiles()
    {
        return this.files;
    }

    /**
     * @return the list of container properties
     */
    protected final List getProperties()
    {
        return this.properties;
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
     * Defaults to the standalone configuration if type is not specified by the user.
     *
     * @param typeAsString the configuration type as a string
     * @see org.codehaus.cargo.generic.configuration.ConfigurationFactory
     */
    public final void setType(String typeAsString)
    {
        this.type = ConfigurationType.toType(typeAsString);
    }

    /**
     * @param home the home directory to set
     */
    public final void setHome(String home)
    {
        this.home = home;
    }

    /**
     * @return the configuration type
     */
    public final ConfigurationType getType()
    {
        return this.type;
    }
    
    /**
     * @return the home directory
     */
    public final String getHome()
    {
        return this.home;
    }

    /**
     * @param containerId the container id associated with this configuration
     * @param containerType the container type associated with this configuration
     * @return a configuration instance matching this container and the defined type
     */
    public Configuration createConfiguration(String containerId, ContainerType containerType)
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

        // Set all container properties
        Iterator itProperties = getProperties().iterator();
        while (itProperties.hasNext())
        {
            Property property = (Property) itProperties.next();
            configuration.setProperty(property.getName(), property.getValue());
        }

        // Add static deployables for local configurations
        if (configuration instanceof LocalConfiguration)
        {
            addStaticDeployables(containerId, (LocalConfiguration) configuration);
        }

        if (configuration instanceof StandaloneLocalConfiguration)
        {
            if (getFileConfigs() != null)
            {
                for (int i = 0; i < getFileConfigs().size(); i++)
                {
                    FileConfig configfile = (FileConfig) getFileConfigs().get(i);
                    ((StandaloneLocalConfiguration) configuration)
                            .setConfigFileProperty(configfile);
                }
            }
            if (getFiles() != null)
            {
                for (int i = 0; i < getFiles().size(); i++)
                {
                    FileConfig configfile = (FileConfig) getFiles().get(i);
                    ((StandaloneLocalConfiguration) configuration)
                            .setFileProperty(configfile);
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
        Iterator deps = getDeployables().iterator();
        while (deps.hasNext())
        {
            DeployableElement deployableElement = (DeployableElement) deps.next();
            configuration.addDeployable(deployableElement.createDeployable(containerId));
        }
    }
}
