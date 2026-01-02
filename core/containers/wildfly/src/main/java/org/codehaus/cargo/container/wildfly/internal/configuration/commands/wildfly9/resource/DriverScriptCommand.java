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

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.AbstractWildFlyScriptCommand;

/**
 * Implementation of DataSource driver configuration script command.
 */
public class DriverScriptCommand extends AbstractWildFlyScriptCommand
{

    /**
     * DataSource.
     */
    private DataSource ds;

    /**
     * Driver module name.
     */
    private String driverModule;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param dataSource DataSource.
     * @param driverModule Module containing DataSource driver.
     */
    public DriverScriptCommand(Configuration configuration, String resourcePath,
            DataSource dataSource, String driverModule)
    {
        super(configuration, resourcePath);
        this.ds = dataSource;
        this.driverModule = driverModule;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "resource/driver.cli";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.datasource.driver.module", driverModule);
        propertiesMap.put("cargo.datasource.driver.class", ds.getDriverClass());
    }
}
