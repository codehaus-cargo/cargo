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
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.JBoss71xExistingLocalConfiguration;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractDeployer;
import org.codehaus.cargo.container.wildfly.internal.WildFlyConfiguration;
import org.codehaus.cargo.container.wildfly.internal.WildFlyExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFly8xCliConfigurationFactory;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFlyCliConfigurationFactory;

/**
 * WildFly 8.x existing {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 */
public class WildFly8xExistingLocalConfiguration extends JBoss71xExistingLocalConfiguration
    implements WildFlyConfiguration
{
    /**
     * WildFly container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WildFlyExistingLocalConfigurationCapability();

    /**
     * CLI configuration factory.
     */
    private WildFly8xCliConfigurationFactory factory =
            new WildFly8xCliConfigurationFactory(this);

    /**
     * {@inheritDoc}
     * @see JBoss71xExistingLocalConfiguration#JBoss71xExistingLocalConfiguration(String)
     */
    public WildFly8xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT, null);
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT, "9990");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractDeployer createDeployer(InstalledLocalContainer jbossContainer)
    {
        return new WildFly8xInstalledLocalDeployer(jbossContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WildFly 8.x Existing Configuration";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WildFlyCliConfigurationFactory getConfigurationFactory()
    {
        return factory;
    }
}
