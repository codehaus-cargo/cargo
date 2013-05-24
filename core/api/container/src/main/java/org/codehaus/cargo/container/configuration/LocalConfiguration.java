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
package org.codehaus.cargo.container.configuration;

import java.util.List;

import org.apache.tools.ant.types.FilterChain;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.util.FileHandler;

/**
 * A local configuration represents a container configuration located somewhere on the local file
 * system. A local configuration is activated before the container is started. In addition, a local
 * configuration allows you to deploy {@link Deployable}s before the container is started.
 * 
 * @version $Id$
 */
public interface LocalConfiguration extends Configuration
{
    /**
     * @return the configuration home directory. Note that we're returning a String instead of a
     * File because we want to leave the possibility of using URIs for specifying the home location.
     */
    String getHome();

    /**
     * @param handler means by which we affect local files.
     */
    void setFileHandler(FileHandler handler);

    /**
     * @return the means by which we affect local files.
     */
    FileHandler getFileHandler();

    /**
     * set the config file property for a configuration.
     * 
     * @param fileConfig The FileConfig to use
     */
    void setConfigFileProperty(FileConfig fileConfig);

    /**
     * set the file property for a configuration.
     * @param fileConfig The FileConfig to use
     */
    void setFileProperty(FileConfig fileConfig);

    /**
     * Returns the file configurations.
     * 
     * @return The configuration file properies
     */
    List<FileConfig> getFileProperties();

    /**
     * Returns the filterchain for this configuration.
     * 
     * @return The filterchain
     */
    FilterChain getFilterChain();

    /**
     * Deploy a {@link Deployable} in the container. It installs the {@link Deployable} in the
     * container's configuration directory.
     * 
     * @param deployable the {@link org.codehaus.cargo.container.deployable.Deployable} to deploy
     */
    void addDeployable(Deployable deployable);

    /**
     * @return the list of {@link Deployable}s that are going to be deployed in the container when
     * it is started
     */
    List<Deployable> getDeployables();

    /**
     * Add resources the container can take advantage of. I.e. datasources.
     * 
     * @param resource the {@link Resource} to add.
     */
    void addResource(Resource resource);

    /**
     * @return the list of {@link Resource}s that are going to be added to the container when it is
     * started.
     */
    List<Resource> getResources();

    /**
     * Add data source the container can take advantage of.
     * 
     * @param dataSource the {@link DataSource} to add.
     */
    void addDataSource(DataSource dataSource);

    /**
     * @return the configured DataSources for this container.
     */
    List<DataSource> getDataSources();

    /**
     * Apply the port offset to the configuration port properties.
     */
    void applyPortOffset();

    /**
     * Revert the port offset on the configuration port properties.
     */
    void revertPortOffset();

    /**
     * Setup the container which means setting up a valid directory structure, setting up
     * configuration files and deploying static deployables.
     * 
     * @param container the container to configure
     */
    void configure(LocalContainer container);
}
