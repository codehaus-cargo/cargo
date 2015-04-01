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
package org.codehaus.cargo.container.stub;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.util.FileHandler;

/**
 * Code common to all local configuration stubs.
 * 
 */
public abstract class AbstractLocalConfigurationStub extends AbstractConfigurationStub
    implements LocalConfiguration
{
    /**
     * Container home.
     */
    private String home;

    /**
     * Deployables to deploy.
     */
    private List<Deployable> deployables = new ArrayList<Deployable>();

    /**
     * Resources to deploy.
     */
    private List<Resource> resources = new ArrayList<Resource>();

    /**
     * Datasources to deploy.
     */
    private List<DataSource> dataSources = new ArrayList<DataSource>();

    /**
     * Empty constructor to allow creating a container with no configuration for test that do not
     * require a configuration.
     */
    public AbstractLocalConfigurationStub()
    {
        // Nothing
    }

    /**
     * Constructor that saves the <code>home</code>.
     * @param home Configuration home.
     */
    public AbstractLocalConfigurationStub(String home)
    {
        setHome(home);
    }

    /**
     * {@inheritDoc}
     * @return Configuration home.
     */
    public String getHome()
    {
        return this.home;
    }

    /**
     * {@inheritDoc}
     * @param home Configuration home to set.
     */
    public void setHome(String home)
    {
        this.home = home;
    }

    /**
     * {@inheritDoc}
     * @param deployable Deployable to add.
     */
    public void addDeployable(Deployable deployable)
    {
        this.deployables.add(deployable);
    }

    /**
     * {@inheritDoc}
     * @return List of deployables.
     */
    public List<Deployable> getDeployables()
    {
        return this.deployables;
    }

    /**
     * {@inheritDoc}
     * @param resource Resource to add.
     */
    public void addResource(Resource resource)
    {
        this.resources.add(resource);
    }

    /**
     * {@inheritDoc}
     * @return List of resources.
     */
    public List<Resource> getResources()
    {
        return this.resources;
    }

    /**
     * {@inheritDoc}
     * @param dataSource Datasource to add.
     */
    public void addDataSource(DataSource dataSource)
    {
        this.dataSources.add(dataSource);
    }

    /**
     * {@inheritDoc}
     * @return List of datasources.
     */
    public List<DataSource> getDataSources()
    {
        return this.dataSources;
    }

    /**
     * {@inheritDoc}
     * @return <code>null</code>
     */
    public List<FileConfig> getFileProperties()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     * @param fileConfig Ignored.
     */
    public void setConfigFileProperty(FileConfig fileConfig)
    {
        // TODO Auto-generated method stub
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     * @param fileConfig Ignored.
     */
    public void setFileProperty(FileConfig fileConfig)
    {
        // TODO Auto-generate method stub
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @return Nothing.
     */
    public FileHandler getFileHandler()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Throws a {@link RuntimeException}. {@inheritDoc}
     * @param handler Ignored.
     */
    public void setFileHandler(FileHandler handler)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     */
    public void applyPortOffset() 
    {
        // TODO Auto-generated method stub
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     */
    public void revertPortOffset() 
    {
        // TODO Auto-generated method stub
    }

    /**
     * Voluntarily do nothing for testing. {@inheritDoc}
     * @param container Ignored.
     */
    public void configure(LocalContainer container)
    {
        // Nothing
    }
}
