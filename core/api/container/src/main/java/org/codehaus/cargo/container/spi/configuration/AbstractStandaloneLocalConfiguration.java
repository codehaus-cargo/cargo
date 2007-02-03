/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.container.spi.configuration;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

import java.io.IOException;

/**
 * Base implementation for a standalone local configuration.
 *
 * @version $Id$
 */
public abstract class AbstractStandaloneLocalConfiguration extends AbstractLocalConfiguration
    implements StandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @see AbstractLocalConfiguration#AbstractLocalConfiguration(String)
     */
    public AbstractStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        // Add all required properties that are common to all standalone configurations
        setProperty(GeneralPropertySet.LOGGING, "medium");
    }

    /**
     * Set up the configuration directory (create it and clean it). We clean it because we want
     * to be sure the container starts with the same set up every time and there's no side effects
     * introduced by a previous run or someone modifying some files in there.
     *
     * <p>Note: We only clean the configuration directory if it's empty or if there is a Cargo
     * timestamp file. This is to prevent deleting not empty directories if the user has mistakenly
     * pointed the configuration dir to an existing location.</p>
     * 
     * @throws IOException if the directory cannot be created
     */
    protected void setupConfigurationDir() throws IOException
    {
        String timestampFile = getFileHandler().append(getHome(), ".cargo");

        // Start by cleaning the configuration directory. Do it only if there's already a Cargo
        // timestamp or if the configuration directory exists but is empty or if the configuration
        // directory doesn't exist.
        if (getFileHandler().exists(timestampFile)
            || (getFileHandler().exists(getHome())
                && getFileHandler().isDirectoryEmpty(getHome()))
            || !getFileHandler().exists(getHome()))
        {
            getFileHandler().delete(getHome());

            getFileHandler().mkdirs(getHome());

            // Create Cargo timestamp file
            getFileHandler().createFile(timestampFile);
        }
        else
        {
            throw new ContainerException("Invalid configuration dir [" + getHome() + "]. "
                + "When using standalone configurations, the configuration dir must point to an "
                + "empty directory. Note that everything in that dir will get deleted by Cargo.");
        }
    }

    /**
     * Creates the default filter chain that should be applied while copying
     * container configuration files to the working directory from which 
     * the container is started.
     * 
     * @return The default filter chain
     */
    protected final FilterChain createFilterChain()
    {
        FilterChain filterChain = new FilterChain();

        getAntUtils().addTokenToFilterChain(filterChain,
            GeneralPropertySet.PROTOCOL, getPropertyValue(GeneralPropertySet.PROTOCOL));
        
        getAntUtils().addTokenToFilterChain(filterChain, 
            ServletPropertySet.PORT, getPropertyValue(ServletPropertySet.PORT));

        getAntUtils().addTokenToFilterChain(filterChain,
            GeneralPropertySet.HOSTNAME, getPropertyValue(GeneralPropertySet.HOSTNAME));
        
        return filterChain;
    }
    
    /**
     * {@inheritDoc}
     * @see ContainerConfiguration#verify()
     */
    public void verify()
    {
        super.verify();
        
        // Verify that the logging level is a valid level
        verifyLogging();
    }

    /**
     * Verify that the logging level specified is a valid level. 
     */
    private void verifyLogging()
    {
        String level = getPropertyValue(GeneralPropertySet.LOGGING);
        if (!level.equalsIgnoreCase("low")
            && !level.equalsIgnoreCase("medium")
            && !level.equalsIgnoreCase("high"))
        {
            throw new ContainerException("Invalid logging level [" + level 
                + "]. Valid levels are {\"low\", \"medium\", " + "\"high\"}");
        }
    }
   
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getType()
     */
    public ConfigurationType getType()
    {
        return ConfigurationType.STANDALONE;
    }
}
