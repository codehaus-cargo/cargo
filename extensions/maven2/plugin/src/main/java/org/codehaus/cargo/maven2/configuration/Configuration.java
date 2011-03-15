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
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.maven2.util.CargoProject;

/**
 * Holds configuration data for the <code>&lt;configuration&gt;</code> tag used to configure the
 * plugin in the <code>pom.xml</code> file.
 * 
 * @version $Id$
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
     * Deployables.
     */
    private Deployable[] deployables;

    /**
     * Extra files.
     */
    private FileConfig[] fileConfigs;

    /**
     * Configuration files.
     */
    private FileConfig[] configfiles;

    /**
     * Extra resources.
     */
    private Resource[] resources;

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
     * @return Deployables.
     */
    public Deployable[] getDeployables()
    {
        return this.deployables;
    }

    /**
     * @param deployables Deployables.
     */
    public void setDeployables(Deployable[] deployables)
    {
        this.deployables = deployables;
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
     * Creates a configuration.
     * @param containerId Container id.
     * @param containerType Container type.
     * @param project Cargo project.
     * @return Configuration.
     * @throws MojoExecutionException If configuration creation fails.
     */
    public org.codehaus.cargo.container.configuration.Configuration createConfiguration(
        String containerId, ContainerType containerType, CargoProject project)
        throws MojoExecutionException
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
            if (ConfigurationType.RUNTIME.equals(getType()))
            {
                configuration = factory.createConfiguration(containerId, containerType, getType(),
                    null);
            }
            else
            {
                File home = new File(project.getBuildDirectory(), "cargo/configurations/"
                    + containerId);
                configuration = factory.createConfiguration(containerId, containerType, getType(),
                    home.getAbsolutePath());
            }
        }
        else
        {
            configuration = factory.createConfiguration(containerId, containerType, getType(),
                getHome());
        }

        // Set all container properties (if any)
        if (getProperties() != null)
        {
            for (Map.Entry<String, String> property : getProperties().entrySet())
            {
                // Maven2 doesn't like empty elements and will set them to Null. Thus we
                // need to modify that behavior and change them to an empty string. For example
                // this allows users to pass an empty password for the cargo.remote.password
                // configuration property.
                String propertyValue = property.getValue();
                if (propertyValue == null)
                {
                    propertyValue = "";
                }

                configuration.setProperty(property.getKey(), propertyValue);
            }
        }

        // Add static deployables for local configurations
        if (configuration instanceof LocalConfiguration)
        {
            if (getDeployables() != null)
            {
                addStaticDeployables(containerId, (LocalConfiguration) configuration, project);
            }

            if (getResources() != null)
            {
                addResources(containerId, (LocalConfiguration) configuration, project);
            }
        }

        // Add configfiles for standalone local configurations
        if (configuration instanceof StandaloneLocalConfiguration)
        {
            if (getConfigfiles() != null)
            {
                for (int i = 0; i < getConfigfiles().length; i++)
                {
                    FileConfig fileConfig = getConfigfiles()[i];
                    ((StandaloneLocalConfiguration) configuration)
                        .setConfigFileProperty(fileConfig);
                }
            }
            if (getFiles() != null)
            {
                for (int i = 0; i < getFiles().length; i++)
                {
                    FileConfig fileConfig = getFiles()[i];
                    ((StandaloneLocalConfiguration) configuration).setFileProperty(fileConfig);
                }
            }
        }

        return configuration;
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
        for (int i = 0; i < getResources().length; i++)
        {
            configuration.addResource(getResources()[i].createResource(containerId, project));
        }
    }

    /**
     * Add static deployables to the configuration.
     * 
     * @param containerId the container id to which to deploy to.
     * @param configuration the local configuration to which to add Deployables to.
     * @param project Cargo project.
     * @throws MojoExecutionException If resource creation fails.
     */
    private void addStaticDeployables(String containerId, LocalConfiguration configuration,
        CargoProject project) throws MojoExecutionException
    {
        for (int i = 0; i < getDeployables().length; i++)
        {
            project.getLog().debug("Scheduling deployable for deployment: [groupId ["
                + getDeployables()[i].getGroupId() + "], artifactId ["
                + getDeployables()[i].getArtifactId() + "], type ["
                + getDeployables()[i].getType() + "], location ["
                + getDeployables()[i].getLocation() + "], pingURL ["
                + getDeployables()[i].getPingURL() + "]]");

            configuration.addDeployable(
                getDeployables()[i].createDeployable(containerId, project));
        }
    }
}
