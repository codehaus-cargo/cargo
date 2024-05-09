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
package org.codehaus.cargo.container.jetty.internal;

import org.codehaus.cargo.container.jetty.JettyPropertySet;

/**
 * Capabilities of the Jetty 10.x and onward's
 * {@link org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration}
 * configuration for installed containers.
 */
public class Jetty10xStandaloneLocalConfigurationCapability extends
    Jetty7x8x9xStandaloneLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public Jetty10xStandaloneLocalConfigurationCapability()
    {
        this.propertySupportMap.put(JettyPropertySet.MODULES, Boolean.TRUE);

        this.propertySupportMap.put(JettyPropertySet.CONNECTOR_HTTPS_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JettyPropertySet.CONNECTOR_KEY_STORE_FILE, Boolean.TRUE);
        this.propertySupportMap.put(JettyPropertySet.CONNECTOR_KEY_STORE_TYPE, Boolean.TRUE);
        this.propertySupportMap.put(JettyPropertySet.CONNECTOR_KEY_STORE_PASSWORD, Boolean.TRUE);
    }
}
