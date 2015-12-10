/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2011-2015 Ali Tokmen.
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
package org.codehaus.cargo.container.weblogic.internal.configuration.commands.deployment;

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.script.AbstractScriptCommand;

/**
 * Implementation of deploy deployable configuration script command.
 */
public class DeployDeployableScriptCommand extends AbstractScriptCommand
{

    /**
     * Deployable id.
     */
    private String deployableId;

    /**
     * Deployable path.
     */
    private String deployablePath;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     *
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param deployableId Id of deployable for deploy.
     * @param deployablePath Absolute path of deployable.
     */
    public DeployDeployableScriptCommand(Configuration configuration, String resourcePath,
            String deployableId, String deployablePath)
    {
        super(configuration, resourcePath);
        this.deployableId = deployableId;
        this.deployablePath = deployablePath;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "deployment/deploy-deployable.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.deployable.id", deployableId);
        propertiesMap.put("cargo.deployable.path.absolute", deployablePath);
    }
}
