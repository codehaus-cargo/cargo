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

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.JBoss7xRuntimeConfiguration;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.wildfly.internal.WildFlyRuntimeConfigurationCapability;

/**
 * Configuration to use when using a WildFly 8.x remote container.
 */
public class WildFly8xRuntimeConfiguration extends JBoss7xRuntimeConfiguration
{
    /**
     * Capability of the WildFly runtime configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WildFlyRuntimeConfigurationCapability();

    /**
     * Set the default values for various port numbers.
     */
    public WildFly8xRuntimeConfiguration()
    {
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT, null);
        setProperty(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT, "9990");
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
        return "WildFly 8.x Runtime Configuration";
    }
}
