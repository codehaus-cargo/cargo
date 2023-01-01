/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
import org.codehaus.cargo.container.configuration.script.AbstractResourceScriptCommand;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Implementation of deploying shared library configuration script command.
 */
public class DeploySharedLibraryScriptCommand extends AbstractResourceScriptCommand
{

    /**
     * Shared library path.
     */
    private String sharedLibraryPath;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param sharedLibraryPath Shared library path.
     */
    public DeploySharedLibraryScriptCommand(Configuration configuration, String resourcePath,
            String sharedLibraryPath)
    {
        super(configuration, resourcePath);
        this.sharedLibraryPath = sharedLibraryPath;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "deployment/deploy-shared-library.py";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        FileHandler fileHandler = new DefaultFileHandler();
        fileHandler.setLogger(this.getConfiguration().getLogger());
        propertiesMap.put("cargo.library.shared.path.absolute", sharedLibraryPath);
        propertiesMap.put("cargo.library.shared.id", fileHandler.getName(sharedLibraryPath));
    }
}
