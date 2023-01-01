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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.LocalContainer;

/**
 * A Tomcat manager-based deployer to perform deployment to a local Tomcat 7.x to 10.x containers.
 */
public class TomcatManager7x8x9x10xInstalledLocalDeployer extends
    TomcatManager4x5x6xInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see TomcatManager4x5x6xInstalledLocalDeployer#TomcatManager4x5x6xInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public TomcatManager7x8x9x10xInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
        this.managerContext += "/text";
    }
}
