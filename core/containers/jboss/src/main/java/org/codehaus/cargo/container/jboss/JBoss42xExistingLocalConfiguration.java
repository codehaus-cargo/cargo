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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss42xExistingLocalConfigurationCapability;

/**
 * JBoss existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 */
public class JBoss42xExistingLocalConfiguration extends JBoss3x4xExistingLocalConfiguration
{
    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss42xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss3x4xExistingLocalConfiguration#JBossExistingLocalConfiguration(String)
     */
    public JBoss42xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JBossPropertySet.JBOSS_EJB3_REMOTING_PORT, "3873");
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
        return "JBoss 4.2.x Existing Configuration";
    }
}
