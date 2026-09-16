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
package org.codehaus.cargo.container.wildfly.internal.configuration.factory;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.domain.ConfigurePortsScriptCommand;

/**
 * WildFly 41.x CLI configuration factory returning specific configuration scripts.
 */
public class WildFly41xCliConfigurationFactory extends WildFly9xCliConfigurationFactory
{
    /**
     * Path to configuration script resources.
     */
    private static final String RESOURCE_PATH =
            "org/codehaus/cargo/container/internal/resources/wildfly-41/cli/";

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     */
    public WildFly41xCliConfigurationFactory(Configuration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScriptCommand configurePortsScript()
    {
        return new ConfigurePortsScriptCommand(configuration, RESOURCE_PATH);
    }
}
