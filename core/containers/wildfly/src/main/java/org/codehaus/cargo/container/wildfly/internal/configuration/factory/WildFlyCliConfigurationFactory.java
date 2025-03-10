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

import org.codehaus.cargo.container.configuration.script.ScriptCommand;

/**
 * CLI configuration factory returning base shared configuration scripts.
 */
public interface WildFlyCliConfigurationFactory
{
    /* Server configuration*/

    /**
     * @return Shutdown server CLI script.
     */
    ScriptCommand shutdownServerScript();

    /**
     * @return Connect to running server CLI script.
     */
    ScriptCommand connectToServerScript();

    /**
     * @param moduleName Module name.
     * @param jarFilePaths Paths to module's jar files.
     * @param moduleDependencies List of module names the module being added depends on.
     * @return Add module CLI script.
     */
    ScriptCommand addModuleScript(String moduleName, List<String> jarFilePaths,
        List<String> moduleDependencies);
}
