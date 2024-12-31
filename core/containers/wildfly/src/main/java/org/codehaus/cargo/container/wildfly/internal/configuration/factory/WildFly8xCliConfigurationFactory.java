/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.wildfly.internal.configuration.factory;

import java.util.List;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly8.server.AddModuleScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly8.server.ConnectToServerScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly8.server.ShutdownServerScriptCommand;

/**
 * WildFly8x CLI configuration factory returning specific configuration scripts.
 */
public class WildFly8xCliConfigurationFactory implements WildFlyCliConfigurationFactory
{
    /**
     * Path to configuration script resources.
     */
    private static final String RESOURCE_PATH =
            "org/codehaus/cargo/container/internal/resources/wildfly-8/cli/";

    /**
     * Container configuration.
     */
    protected Configuration configuration;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     */
    public WildFly8xCliConfigurationFactory(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /* Server configuration*/

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptCommand shutdownServerScript()
    {
        return new ShutdownServerScriptCommand(configuration, RESOURCE_PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptCommand connectToServerScript()
    {
        return new ConnectToServerScriptCommand(configuration, RESOURCE_PATH);
    }

    /* Domain configuration*/

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptCommand addModuleScript(String moduleName, List<String> jarFilePaths,
        List<String> moduleDependencies)
    {
        return new AddModuleScriptCommand(configuration, RESOURCE_PATH, moduleName, jarFilePaths,
            moduleDependencies);
    }
}
