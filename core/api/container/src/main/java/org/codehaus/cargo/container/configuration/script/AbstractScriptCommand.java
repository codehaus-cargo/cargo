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
package org.codehaus.cargo.container.configuration.script;

import org.codehaus.cargo.container.configuration.Configuration;

/**
 * Implementation of general functionality for configuration script commands.
 */
public abstract class AbstractScriptCommand implements ScriptCommand
{

    /**
     * Container configuration.
     */
    private Configuration configuration;

    /**
     * Sets configuration containing all needed information for building configuration scripts.
     * 
     * @param configuration Container configuration.
     */
    public AbstractScriptCommand(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isApplicable()
    {
        return true;
    }

    /**
     * @return Container configuration.
     */
    protected Configuration getConfiguration()
    {
        return configuration;
    }

    /**
     * Removes all special characters from a deployable name, to make a "safe" identifier.
     * 
     * @param name Deployable name.
     * @return "Safe" version of the deployable name, where:
     * <ul>
     * <li>Empty or <code>null</code> names are rewritten as <code>ROOT</code></li>
     * <li>Else, from yhe name, everything outside the base alphabet, numbers, <code>-</code> and
     * <code>_</code> are changed into <code>_</code>.</li>
     * </ul>
     */
    protected String escapeDeployableName(String name)
    {
        if (name == null)
        {
            return "ROOT";
        }
        else
        {
            String escapedName = name.replace('\\', '/');
            while (escapedName.startsWith("/") || escapedName.startsWith(" "))
            {
                escapedName = escapedName.substring(1);
            }
            while (escapedName.endsWith("/") || escapedName.endsWith(" "))
            {
                escapedName = escapedName.substring(0, escapedName.length() - 1);
            }
            if (escapedName.length() < 1)
            {
                return "ROOT";
            }
            else
            {
                escapedName = escapedName.replaceAll("[^a-zA-Z0-9-_]", "_");
                return escapedName;
            }
        }
    }
}
