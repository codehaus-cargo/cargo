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
package org.codehaus.cargo.maven2.configuration;

import java.util.Iterator;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.maven2.util.CargoProject;

/**
 * Holds configuration data for the <code>&lt;configuration&gt;</code> tag used to configure
 * the plugin in the <code>pom.xml</code> file.
 *  
 * @version $Id$
 */
public class Configuration
{
    private String type = ConfigurationType.STANDALONE.getType();
   
    private Class implementation;

    private String home;

    private Map properties;

    private Deployable[] deployables;
    
    public ConfigurationType getType()
    {
        return ConfigurationType.toType(this.type);
    }

    public void setType(ConfigurationType type)
    {
        this.type = type.getType();
    }

    public String getHome()
    {
        return this.home;
    }

    public void setHome(String home)
    {
        this.home = home;
    }

    public Map getProperties()
    {
        return this.properties;
    }

    public void setProperties(Map properties)
    {
        this.properties = properties;
    }

    public Deployable[] getDeployables()
    {
        return this.deployables;
    }
    
    public void setDeployables(Deployable[] deployables)
    {
        this.deployables = deployables;
    }
    
    public Class getImplementation()
    {
        return this.implementation;
    }

    public void setImplementation(Class implementation)
    {
        this.implementation = implementation;
    }

    public org.codehaus.cargo.container.configuration.Configuration createConfiguration(
        String containerId, ContainerType containerType, CargoProject project)
        throws MojoExecutionException
    {
        ConfigurationFactory factory = new DefaultConfigurationFactory();

        if (getImplementation() != null)
        {
            factory.registerConfiguration(containerId, containerType, getType(),
                getImplementation());
        }

        // Only use the dir if specified
        org.codehaus.cargo.container.configuration.Configuration configuration;
        if (getHome() == null)
        {
            configuration = factory.createConfiguration(containerId, containerType, getType());
        }
        else
        {
            configuration = factory.createConfiguration(containerId, containerType, getType(),
                getHome());
        }

        // Set all container properties (if any)
        if (getProperties() != null)
        {
            Iterator itProperties = getProperties().keySet().iterator();
            while (itProperties.hasNext())
            {
                String propertyName = (String) itProperties.next();

                // Maven2 doesn't like empty elements and will set them to Null. Thus we
                // need to modify that behavior and change them to an empty string. For example
                // this allows users to pass an empty password for the cargo.remote.password
                // configuration property.
                String propertyValue = (String) getProperties().get(propertyName);
                if (propertyValue == null)
                {
                    propertyValue = "";
                }

                configuration.setProperty(propertyName, propertyValue);
            }
        }

        // Add static deployables for local configurations
        if (configuration instanceof LocalConfiguration)
        {
            if (getDeployables() != null)
            {
                addStaticDeployables(containerId, (LocalConfiguration) configuration, project);
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
