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
package org.codehaus.cargo.container.websphere;

import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.script.ScriptCommand;

/**
 * Unit tests for the {@link WebSphere85xInstalledLocalDeployer} class.
 */
public class WebSphere85xInstalledLocalContainerWithNoWsAdminExecution
    extends WebSphere85xInstalledLocalContainer
{
    /**
     * Commands sent to the deployer.
     */
    private String commands = "";

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.AbstractInstalledLocalContainer#AbstractInstalledLocalContainer(org.codehaus.cargo.container.configuration.LocalConfiguration)
     */
    public WebSphere85xInstalledLocalContainerWithNoWsAdminExecution(
            LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeScript(List<ScriptCommand> configurationScript)
    {
        StringBuilder commandsBuilder = new StringBuilder();
        if (configurationScript != null)
        {
            for (ScriptCommand scriptCommand : configurationScript)
            {
                commandsBuilder.append(scriptCommand.readScript());
                commandsBuilder.append("\n");
            }
        }
        this.commands = commandsBuilder.toString();
    }

    /**
     * @return Commands that would be sent to WsAdmin.
     */
    public String getCommands()
    {
        return commands;
    }
}
