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
import org.codehaus.cargo.container.jboss.internal.JBoss6xExistingLocalConfigurationCapability;

/**
 * JBoss existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 */
public class JBoss6xExistingLocalConfiguration extends JBoss5xExistingLocalConfiguration
{
    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss6xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss5xExistingLocalConfiguration#JBoss5xExistingLocalConfiguration(String)
     */
    public JBoss6xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JBossPropertySet.JBOSS_AJP_PORT, "8009");
        setProperty(JBossPropertySet.JBOSS_JMX_PORT, "1091");
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
        return "JBoss 6.x Existing Configuration";
    }
}
