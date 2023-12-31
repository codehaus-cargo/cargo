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
package org.codehaus.cargo.container.websphere.internal.configuration.commands.resource;

import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;

/**
 * Implementation of datasource connection property configuration script command.
 */
public class DataSourceConnectionPropertyScriptCommand extends AbstractResourceScriptCommand
{

    /**
     * DataSource.
     */
    private DataSource ds;

    /**
     * Driver property.
     */
    private Entry<Object, Object> driverProperty;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param dataSource DataSource.
     * @param driverProperty DataSource driver property.
     */
    public DataSourceConnectionPropertyScriptCommand(Configuration configuration,
            String resourcePath, DataSource dataSource, Entry<Object, Object> driverProperty)
    {
        super(configuration, resourcePath);
        this.ds = dataSource;
        this.driverProperty = driverProperty;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "resource/datasource-connection-property.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.datasource.id", ds.getId());
        propertiesMap.put("cargo.datasource.properties.name", driverProperty.getKey().toString());
        propertiesMap.put("cargo.datasource.properties.value", driverProperty.getValue().
                toString());
    }
}
