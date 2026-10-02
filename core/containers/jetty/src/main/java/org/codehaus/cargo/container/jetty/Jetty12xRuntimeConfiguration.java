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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty12xRuntimeConfigurationCapability;

/**
 * Configuration to use when using a {@link Jetty12xRemoteContainer}.
 */
public class Jetty12xRuntimeConfiguration extends JettyRuntimeConfiguration
{
    /**
     * Capability of the Jetty runtime configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Jetty12xRuntimeConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JettyRuntimeConfiguration#JettyRuntimeConfiguration()
     */
    public Jetty12xRuntimeConfiguration()
    {
        this.setProperty(JettyPropertySet.DEPLOYER_EE_VERSION,
            Jetty12xInstalledLocalContainer.DEFAULT_DEPLOYER_EE_VERSION);
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
        return "Jetty 12.x Runtime Configuration";
    }
}
