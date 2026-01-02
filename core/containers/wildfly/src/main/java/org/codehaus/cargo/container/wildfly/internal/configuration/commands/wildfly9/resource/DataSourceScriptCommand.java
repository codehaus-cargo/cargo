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
package org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.resource;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.AbstractWildFlyScriptCommand;

/**
 * Implementation of DataSource configuration script command.
 */
public class DataSourceScriptCommand extends AbstractWildFlyScriptCommand
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
        return "resource/datasource.cli";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        Map<String, String> datasourceProperties = new HashMap<String, String>();

        datasourceProperties.put("name", ds.getId());
        datasourceProperties.put("jndi-name", getDataSourceJndi(ds));
        datasourceProperties.put("driver-name", ds.getDriverClass());
        datasourceProperties.put("connection-url", ds.getUrl());
        datasourceProperties.put("user-name", ds.getUsername());
        datasourceProperties.put("password", ds.getPassword());

        if (TransactionSupport.NO_TRANSACTION.equals(ds.getTransactionSupport()))
        {
            datasourceProperties.put("jta", "false");
        }
        else
        {
            datasourceProperties.put("jta", "true");
        }

        String dsProps = mapResourceProperties(datasourceProperties);
        propertiesMap.put("cargo.datasource.properties", dsProps);
    }
}
