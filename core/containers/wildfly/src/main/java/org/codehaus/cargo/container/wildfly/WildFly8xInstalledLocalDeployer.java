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
package org.codehaus.cargo.container.wildfly;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.jboss.JBoss7xInstalledLocalDeployer;

/**
 * Static deployer that deploys WARs and EARs to the WildFly <code>deployments</code> directory.
 * 
 */
public class WildFly8xInstalledLocalDeployer extends JBoss7xInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public WildFly8xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }
}
