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
package org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.domain;

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.internal.util.PropertyUtils;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.AbstractWildFlyScriptCommand;

/**
 * Implementation of system property configuration script command.
 */
public class SystemPropertyScriptCommand extends AbstractWildFlyScriptCommand
{

    /**
     * System property name.
     */
    private String name;

    /**
     * System property value.
     */
    private String value;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param name System property name.
     * @param value System property value.
     */
    public SystemPropertyScriptCommand(Configuration configuration, String resourcePath,
            String name, String value)
    {
        super(configuration, resourcePath);

        if (name == null || name.isEmpty())
        {
            throw new IllegalArgumentException("System property name is null or empty.");
        }
        else if (value == null)
        {
            throw new IllegalArgumentException("Value of system property " + name
                    + " is null.");
        }
        else
        {
            this.name = name;
            this.value = value;
        }
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "domain/system-property.cli";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.wildfly.property.name", name);

        // CARGO-1429 - escape backslashes in system properties
        propertiesMap.put("cargo.wildfly.property.value",
                PropertyUtils.escapeBackSlashesIfNotNull(value));
    }
}
