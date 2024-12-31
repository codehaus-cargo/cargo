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
package org.codehaus.cargo.container.weblogic.internal.configuration.commands.deployment;

import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployable.AbstractDeployable;
import org.codehaus.cargo.container.weblogic.WebLogicConfiguration;
import org.codehaus.cargo.util.FileHandler;

/**
 * Implementation of deploy deployable configuration script command.
 */
public class DeployDeployableScriptCommand extends AbstractResourceScriptCommand
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
     * @param deployable Deployable to be deployed.
     */
    public DeployDeployableScriptCommand(Configuration configuration, String resourcePath,
            Deployable deployable)
    {
        super(configuration, resourcePath);
        this.deployable = deployable;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "deployment/deploy-deployable.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.deployable.id", deployable.getName());

        FileHandler fileHandler = ((AbstractDeployable) deployable).getFileHandler();
        String absolutePath = fileHandler.getAbsolutePath(deployable.getFile());
        if (deployable.getType() == DeployableType.WAR && fileHandler.exists(absolutePath))
        {
            // CARGO-1402: Add support for context path configuration for WebLogic
            WAR war = (WAR) deployable;
            boolean needsCopy;
            if (deployable.isExpanded())
            {
                needsCopy = !fileHandler.getName(absolutePath).equals(war.getContext());
            }
            else
            {
                needsCopy = !fileHandler.getName(absolutePath).equals(war.getContext() + ".war");
            }
            if (needsCopy)
            {
                String cargodeploy = fileHandler.createDirectory(
                    ((WebLogicConfiguration) getConfiguration()).getDomainHome(), "cargodeploy");
                String targetDirectoryname = fileHandler.append(cargodeploy, war.getContext());
                fileHandler.delete(targetDirectoryname);
                String targetFilename = fileHandler.append(cargodeploy, war.getContext() + ".war");
                fileHandler.delete(targetFilename);
                if (deployable.isExpanded())
                {
                    fileHandler.copyDirectory(deployable.getFile(), targetDirectoryname);
                    absolutePath = targetDirectoryname;
                }
                else
                {
                    fileHandler.copyFile(deployable.getFile(), targetFilename, true);
                    absolutePath = targetFilename;
                }
            }
        }
        propertiesMap.put("cargo.deployable.path.absolute", absolutePath);
    }
}
