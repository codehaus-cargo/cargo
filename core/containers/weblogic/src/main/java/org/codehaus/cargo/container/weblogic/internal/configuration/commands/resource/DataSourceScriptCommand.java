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
package org.codehaus.cargo.container.weblogic.internal.configuration.commands.resource;

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
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param dataSource DataSource.
     */
    public DataSourceScriptCommand(Configuration configuration, String resourcePath,
            DataSource dataSource)
    {
        super(configuration, resourcePath);
        this.ds = dataSource;
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
        propertiesMap.put("cargo.datasource.type", ds.getConnectionType());
        propertiesMap.put("cargo.datasource.transactionsupport", ds.getTransactionSupport().
                toString());
    }
}
