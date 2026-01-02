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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss5xExistingLocalConfigurationCapability;

/**
 * JBoss existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 */
public class JBoss5xExistingLocalConfiguration extends JBoss42xExistingLocalConfiguration
{
    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss5xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss42xExistingLocalConfiguration#JBoss42xExistingLocalConfiguration(String)
     */
    public JBoss5xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JBossPropertySet.JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT, "4712");
        setProperty(JBossPropertySet.JBOSS_TRANSACTION_STATUS_MANAGER_PORT, "4713");
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
        return "JBoss 5.x Existing Configuration";
    }
}
