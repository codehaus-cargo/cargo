/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.websphere.internal.configuration.commands.resource;

import java.util.Collection;
import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;

/**
 * Implementation of datasource configuration script command.
 */
public class DataSourceScriptCommand extends AbstractResourceScriptCommand
{

    /**
     * DataSource.
     */
    private DataSource ds;

    /**
     * Shared libraries containing database drivers.
     */
    private Collection<String> sharedLibraries;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param dataSource DataSource.
     * @param sharedLibraries Shared libraries containing database drivers.
     */
    public DataSourceScriptCommand(Configuration configuration, String resourcePath,
            DataSource dataSource, Collection<String> sharedLibraries)
    {
        super(configuration, resourcePath);
        this.ds = dataSource;
        this.sharedLibraries = sharedLibraries;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "resource/datasource.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.datasource.id", ds.getId());
        propertiesMap.put("cargo.datasource.driver", ds.getDriverClass());
        propertiesMap.put("cargo.datasource.url", ds.getUrl());
        propertiesMap.put("cargo.datasource.username", ds.getUsername());
        propertiesMap.put("cargo.datasource.password", ds.getPassword());
        propertiesMap.put("cargo.datasource.jndi", ds.getJndiLocation());
        propertiesMap.put("cargo.datasource.driver.classpath", getDataSourceClasspath());
    }

    /**
     * @return Classpath of DataSource drivers.
     */
    private String getDataSourceClasspath()
    {
        StringBuilder classpath = new StringBuilder();

        for (String sharedLibrary : sharedLibraries)
        {
            if (classpath.length() == 0)
            {
                classpath.append(sharedLibrary);
            }
            else
            {
                classpath.append(";");
                classpath.append(sharedLibrary);
            }
        }

        return classpath.toString();
    }
}
