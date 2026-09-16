/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.wildfly.internal.WildFly41xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFly41xCliConfigurationFactory;
import org.codehaus.cargo.container.wildfly.internal.configuration.factory.WildFlyCliConfigurationFactory;

/**
 * WildFly 41.x standalone local configuration.
 */
public class WildFly41xStandaloneLocalConfiguration extends WildFly40xStandaloneLocalConfiguration
{

    /**
     * WildFly container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WildFly41xStandaloneLocalConfigurationCapability();

    /**
     * CLI configuration factory.
     */
    private WildFly41xCliConfigurationFactory factory;

    /**
     * {@inheritDoc}
     * @see WildFly40xStandaloneLocalConfiguration#WildFly40xStandaloneLocalConfiguration(String)
     */
    public WildFly41xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JBossPropertySet.JBOSS_AJP_PORT, null);
        this.factory = new WildFly41xCliConfigurationFactory(this);
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
    public WildFlyCliConfigurationFactory getConfigurationFactory()
    {
        return factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WildFly 41.x Standalone Configuration";
    }
}
