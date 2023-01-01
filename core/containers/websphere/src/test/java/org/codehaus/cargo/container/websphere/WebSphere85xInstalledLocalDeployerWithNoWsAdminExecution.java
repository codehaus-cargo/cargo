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
package org.codehaus.cargo.container.websphere;

/**
 * Unit tests for the {@link WebSphere85xInstalledLocalDeployer} class.
 */
public class WebSphere85xInstalledLocalDeployerWithNoWsAdminExecution
    extends WebSphere85xInstalledLocalDeployer
{
    /**
     * Empty constructor.
     */
    public WebSphere85xInstalledLocalDeployerWithNoWsAdminExecution()
    {
        super(new WebSphere85xInstalledLocalContainerWithNoWsAdminExecution(
            new WebSphere85xExistingLocalConfiguration("target")));
    }

    /**
     * @return Commands that would be sent to WsAdmin.
     */
    public String getCommands()
    {
        return ((WebSphere85xInstalledLocalContainerWithNoWsAdminExecution) getContainer())
                .getCommands();
    }
}
