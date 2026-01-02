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
package org.codehaus.cargo.container.wildfly.internal.configuration.commands.wildfly8.server;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.internal.util.ComplexPropertyUtils;
import org.codehaus.cargo.container.wildfly.internal.configuration.commands.AbstractWildFlyScriptCommand;

/**
 * Implementation of adding of modules configuration script command.
 */
public class AddModuleScriptCommand extends AbstractWildFlyScriptCommand
{

    /**
     * Module name.
     */
    private String moduleName;

    /**
     * Paths to module's jar files.
     */
    private List<String> jarFilePaths;

    /**
     * List of module names the module being added depends on.
     */
    private List<String> moduleDependencies;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     * @param resourcePath Path to configuration script resources.
     * @param moduleName Module name.
     * @param moduleDependencies List of module names the module being added depends on.
     * @param jarFilePaths Paths to module's jar files.
     */
    public AddModuleScriptCommand(Configuration configuration, String resourcePath,
            String moduleName, List<String> jarFilePaths, List<String> moduleDependencies)
    {
        super(configuration, resourcePath);
        this.moduleName = moduleName;
        this.jarFilePaths = jarFilePaths;
        this.moduleDependencies = moduleDependencies;
    }

    @Override
    protected String getScriptRelativePath()
    {
        return "server/add-module.cli";
    }

    @Override
    protected void addConfigurationScriptProperties(Map<String, String> propertiesMap)
    {
        propertiesMap.put("cargo.wildfly.module.name", moduleName);

        // Create jar file path string as list of filesystem paths separated by path separator
        char delimiter = File.pathSeparatorChar;
        String resources = ComplexPropertyUtils.joinOnDelimiter(jarFilePaths, delimiter);
        propertiesMap.put("cargo.wildfly.module.resources", resources);

        delimiter = ',';
        String depProp = ComplexPropertyUtils.joinOnDelimiter(moduleDependencies, delimiter);
        propertiesMap.put("cargo.wildfly.module.dependencies", depProp);
    }
}
