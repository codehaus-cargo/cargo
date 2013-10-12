/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

/**
 * Unit tests for the {@link WebSphere85xInstalledLocalDeployer} class.
 * 
 * @version $Id$
 */
public class WebSphere85xInstalledLocalDeployerWithNoWsAdminExecution
    extends WebSphere85xInstalledLocalDeployer
{
    /**
     * Commands sent to the deployer.
     */
    private String commands = "";

    /**
     * Empty constructor.
     */
    public WebSphere85xInstalledLocalDeployerWithNoWsAdminExecution()
    {
        super(new WebSphere85xInstalledLocalContainer(
            new WebSphere85xExistingLocalConfiguration("target")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeWsAdmin(String... commands) throws Exception
    {
        StringBuilder commandsBuilder = new StringBuilder();
        if (commands != null)
        {
            for (String command : commands)
            {
                commandsBuilder.append(command);
                commandsBuilder.append("\n");
            }
        }
        this.commands = commandsBuilder.toString();
    }

    /**
     * @return Commands sent to the deployer.
     */
    public String getCommands()
    {
        return commands;
    }
}
