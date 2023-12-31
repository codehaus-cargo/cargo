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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.jetty.internal.AbstractJettyExistingLocalConfiguration;
import org.codehaus.cargo.container.spi.deployer.AbstractInstalledLocalDeployer;

/**
 * Configuration for existing local Jetty 7.x
 */
public class Jetty7xExistingLocalConfiguration extends AbstractJettyExistingLocalConfiguration
{

    /**
     * {@inheritDoc}
     * @see AbstractJettyExistingLocalConfiguration#AbstractJettyExistingLocalConfiguration(String)
     */
    public Jetty7xExistingLocalConfiguration(String dir)
    {
        super(dir);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractInstalledLocalDeployer createDeployer(LocalContainer container)
    {
        Jetty7x8xInstalledLocalDeployer deployer =
            new Jetty7x8xInstalledLocalDeployer((InstalledLocalContainer) container);
        return deployer;
    }

}
