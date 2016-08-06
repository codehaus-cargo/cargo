/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2016 Ali Tokmen.
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
import org.codehaus.cargo.container.jboss.JBoss8xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.wildfly.internal.WildFlyStandaloneLocalConfigurationCapability;

/**
 * WildFly 8.x standalone local configuration.
 */
public class WildFly8xStandaloneLocalConfiguration extends JBoss8xStandaloneLocalConfiguration
{

    /**
     * WildFly container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WildFlyStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss8xStandaloneLocalConfiguration#JBoss8xStandaloneLocalConfiguration(String)
     */
    public WildFly8xStandaloneLocalConfiguration(String dir)
    {
        super(dir);
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

}
