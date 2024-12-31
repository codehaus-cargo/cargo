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
package org.codehaus.cargo.container.payara;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.glassfish.GlassFish5xInstalledLocalDeployer;

/**
 * Payara installed local deployer, which uses the GlassFish asadmin to deploy and undeploy
 * applications.
 */
public class PayaraInstalledLocalDeployer extends GlassFish5xInstalledLocalDeployer
{
    /**
     * Calls parent constructor, which saves the container.
     * 
     * @param localContainer Container.
     */
    public PayaraInstalledLocalDeployer(InstalledLocalContainer localContainer)
    {
        super(localContainer);
    }

    /**
     * <a href="https://codehaus-cargo.atlassian.net/browse/CARGO-1541">CARGO-1541</a>: GlassFish
     * 6.x as well as Payara 6.x onwards use Jakarta EE.
     * @return <code>true</code> if the target Payara container is version 6.x onwards.
     */
    @Override
    protected boolean isJakartaEe()
    {
        PayaraInstalledLocalContainer container =
            (PayaraInstalledLocalContainer) getLocalContainer();
        if (container.getVersion() >= 6)
        {
            return true;
        }
        else
        {
            return super.isJakartaEe();
        }
    }
}
