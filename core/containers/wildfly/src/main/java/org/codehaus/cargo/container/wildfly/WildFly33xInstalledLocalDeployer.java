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
package org.codehaus.cargo.container.wildfly;

import org.codehaus.cargo.container.InstalledLocalContainer;

/**
 * Static deployer that deploys WARs and EARs to the WildFly <code>deployments</code> directory.
 */
public class WildFly33xInstalledLocalDeployer extends WildFly32xInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see WildFly32xInstalledLocalDeployer#WildFly32xInstalledLocalDeployer(org.codehaus.cargo.container.InstalledLocalContainer)
     */
    public WildFly33xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }
}
