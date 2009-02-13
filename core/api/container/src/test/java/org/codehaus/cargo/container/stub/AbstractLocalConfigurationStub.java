/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.util.FileHandler;

import java.util.List;
import java.util.ArrayList;

/**
 * Code common to all local configuration stubs.
 *
 * @version $Id$
 */
public abstract class AbstractLocalConfigurationStub
    extends AbstractConfigurationStub implements LocalConfiguration
{
    private String home;

    private List deployables = new ArrayList();

    private List resources = new ArrayList();

    private List dataSources = new ArrayList();

    public AbstractLocalConfigurationStub()
    {
        // Allow creating a container with no configuration for test that do not require a 
        // configuration
    }

    public AbstractLocalConfigurationStub(String home)
    {
        setHome(home);
    }

    public String getHome()
    {
        return this.home;
    }

    public void setHome(String home)
    {
        this.home = home;
    }

    public void addDeployable(Deployable deployable)
    {
        this.deployables.add(deployable);
    }

    public List getDeployables()
    {
        return this.deployables;
    }

    public void addResource(Resource Resource)
    {
        this.resources.add(Resource);
    }

    public List getResources()
    {
        return this.resources;
    }
    
    public void addDataSource(DataSource DataSource)
    {
        this.dataSources.add(DataSource);
    }

    public List getDataSources()
    {
        return this.dataSources;
    }
    
    public void configure(LocalContainer container)
    {
        // Voluntarily do nothing for testing
    }
    
    public void setFileHandler(FileHandler handler)
    {
        throw new RuntimeException("Not implemented");        
    }

    public FileHandler getFileHandler()
    {
        throw new RuntimeException("Not implemented");
    }
}
