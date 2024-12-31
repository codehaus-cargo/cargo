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
package org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly9.custom;

import java.nio.charset.StandardCharsets;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.script.AbstractScriptCommand;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Implementation of custom configuration script command.
 */
public class CustomWildFlyScriptCommand extends AbstractScriptCommand
{

    /**
     * Path to configuration script resource.
     */
    private String resourcePath;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resource.
     */
    public CustomWildFlyScriptCommand(Configuration configuration, String resourcePath)
    {
        super(configuration);
        this.resourcePath = resourcePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String readScript()
    {
        FileHandler fileHandler = new DefaultFileHandler();
        fileHandler.setLogger(this.getConfiguration().getLogger());
        String customScript = fileHandler.readTextFile(resourcePath, StandardCharsets.UTF_8);
        return customScript;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable()
    {
        return true;
    }
}
