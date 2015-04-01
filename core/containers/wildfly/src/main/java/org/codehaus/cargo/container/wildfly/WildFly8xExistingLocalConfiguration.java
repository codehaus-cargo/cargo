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
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.JBoss71xExistingLocalConfiguration;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractDeployer;
import org.codehaus.cargo.container.wildfly.internal.WildFly8xExistingLocalConfigurationCapability;

/**
 * WildFly existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 * 
 */
public class WildFly8xExistingLocalConfiguration extends JBoss71xExistingLocalConfiguration
{
    /**
     * WildFly container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WildFly8xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss71xExistingLocalConfiguration#JBoss71xExistingLocalConfiguration(String)
     */
    public WildFly8xExistingLocalConfiguration(String dir)
    {
        super(dir);

        getProperties().remove(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT);
        getProperties().put(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT, "9990");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.jboss.JBoss71xExistingLocalConfiguration#createDeployer(org.codehaus.cargo.container.InstalledLocalContainer)
     */
    @Override
    protected AbstractDeployer createDeployer(InstalledLocalContainer jbossContainer)
    {
        return new WildFly8xInstalledLocalDeployer(jbossContainer);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "WildFly Existing Configuration";
    }
}
