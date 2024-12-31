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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;

/**
 * Configuration for existing local Jetty 12.x
 */
public class Jetty12xExistingLocalConfiguration extends Jetty11xExistingLocalConfiguration
{

    /**
     * {@inheritDoc}
     * @see Jetty11xExistingLocalConfiguration#Jetty11xExistingLocalConfiguration(String)
     */
    public Jetty12xExistingLocalConfiguration(String dir)
    {
        super(dir);
        setProperty(JettyPropertySet.MODULES, Jetty12xInstalledLocalContainer.DEFAULT_MODULES);
        setProperty(JettyPropertySet.DEPLOYER_EE_VERSION,
            Jetty12xInstalledLocalContainer.DEFAULT_DEPLOYER_EE_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractInstalledLocalDeployer createDeployer(LocalContainer container)
    {
        Jetty12xInstalledLocalDeployer deployer =
            new Jetty12xInstalledLocalDeployer((InstalledLocalContainer) container);
        return deployer;
    }

}
