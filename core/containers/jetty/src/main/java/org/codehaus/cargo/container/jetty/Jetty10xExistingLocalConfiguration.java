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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jetty.internal.Jetty10xExistingLocalConfigurationCapability;

/**
 * Configuration for existing local Jetty 10.x
 */
public class Jetty10xExistingLocalConfiguration extends Jetty9xExistingLocalConfiguration
{
    /**
     * Capability of the Jetty Existing local configuration.
     */
    private static final ConfigurationCapability CAPABILITY =
        new Jetty10xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see Jetty9xExistingLocalConfiguration#Jetty9xExistingLocalConfiguration(String)
     */
    public Jetty10xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JettyPropertySet.MODULES, Jetty10xInstalledLocalContainer.DEFAULT_MODULES);
        setProperty(JettyPropertySet.CONNECTOR_HTTPS_PORT, "9443");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

}
