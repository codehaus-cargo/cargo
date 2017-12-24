/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2017 Ali Tokmen.
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
package org.codehaus.cargo.container.websphere.internal.configuration.commands.deployment;

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.script.AbstractScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;

/**
 * Implementation of deployable starting configuration script command.
 */
public class StartDeployableScriptCommand extends AbstractScriptCommand
{

    /**
     * Deployable.
     */
    private Deployable deployable;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     *
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param deployable Deployable to be started.
     */
    public StartDeployableScriptCommand(Configuration configuration, String resourcePath,
            Deployable deployable)
    {
        super(configuration, resourcePath);
        this.deployable = deployable;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "deployment/start-deployable.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.deployable.id", deployable.getName());
    }
}
