/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
import org.codehaus.cargo.container.jboss.internal.JBoss7xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractRuntimeConfiguration;

/**
 * Configuration to use when using a JBoss remote container.
 */
public class JBoss7xRuntimeConfiguration extends AbstractRuntimeConfiguration
{
    /**
     * Capability of the JBoss runtime configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss7xRuntimeConfigurationCapability();

    /**
     * Set the default values for various port numbers.
     */
    public JBoss7xRuntimeConfiguration()
    {
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT, "9999");

        getProperties().remove(GeneralPropertySet.RMI_PORT);
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
