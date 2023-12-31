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
package org.codehaus.cargo.container.websphere.internal.configuration.commands.domain;

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;

/**
 * Implementation of setting logging configuration script command.
 */
public class LoggingScriptCommand extends AbstractResourceScriptCommand
{
    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     */
    public LoggingScriptCommand(Configuration configuration, String resourcePath)
    {
        super(configuration, resourcePath);
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "domain/logging.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        String logLevel = getConfiguration().getPropertyValue(GeneralPropertySet.LOGGING);
        propertiesMap.put("cargo.websphere.logging", getWebSphereLogLevel(logLevel));
    }

    /**
     * Translate Cargo logging levels into WebSphere logging levels.
     * 
     * @param cargoLogLevel Cargo logging level
     * @return the corresponding WebLogic logging level
     */
    private String getWebSphereLogLevel(String cargoLogLevel)
    {
        String returnVal = "info";

        if (LoggingLevel.LOW.equalsLevel(cargoLogLevel))
        {
            returnVal = "warning";
        }
        else if (LoggingLevel.HIGH.equalsLevel(cargoLogLevel))
        {
            returnVal = "detail";
        }

        return returnVal;
    }
}
