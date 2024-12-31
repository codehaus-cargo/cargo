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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss4xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractRuntimeConfiguration;

/**
 * Configuration to use when using a JBoss remote container.
 */
public class JBoss4xRuntimeConfiguration extends AbstractRuntimeConfiguration
{
    /**
     * Capability of the JBoss runtime configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss4xRuntimeConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractRuntimeConfiguration#AbstractRuntimeConfiguration()
     */
    public JBoss4xRuntimeConfiguration()
    {
        this.setProperty(RemotePropertySet.TIMEOUT, "120000");
        this.setProperty(JBossPropertySet.DEPLOYER_KEEP_ORIGINAL_WAR_FILENAME, "false");
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
        return "JBoss Runtime Configuration";
    }
}
