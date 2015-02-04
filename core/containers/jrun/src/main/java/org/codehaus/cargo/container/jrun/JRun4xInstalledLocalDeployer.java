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
package org.codehaus.cargo.container.jrun;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Static deployer that deploys WARs to the JRun <code>servers/server_name</code> directory.
 * 
 * @version $Id$
 */
public class JRun4xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public JRun4xInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}. For JRun this is the <code>servers/server_name</code> directory.
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        InstalledLocalContainer localContainer = (InstalledLocalContainer) this.getContainer();

        String serverName =
            localContainer.getConfiguration().getPropertyValue(JRun4xPropertySet.SERVER_NAME);

        return getFileHandler().append(getContainer().getConfiguration().getHome(),
            "servers/" + serverName);
    }
}
