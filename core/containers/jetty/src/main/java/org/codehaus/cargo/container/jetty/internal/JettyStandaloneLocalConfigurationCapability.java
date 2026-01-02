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
package org.codehaus.cargo.container.jetty.internal;

import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * Capabilities of the Jetty's
 * {@link org.codehaus.cargo.container.jetty.internal.AbstractJettyStandaloneLocalConfiguration}
 * configuration for installed containers.
 */
public class JettyStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public JettyStandaloneLocalConfigurationCapability()
    {
        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);
        this.propertySupportMap.put(GeneralPropertySet.HOSTNAME, Boolean.FALSE);

        this.propertySupportMap.put(ServletPropertySet.USERS, Boolean.FALSE);

        this.propertySupportMap.put(JettyPropertySet.SESSION_PATH, Boolean.TRUE);
        this.propertySupportMap.put(JettyPropertySet.USE_FILE_MAPPED_BUFFER, Boolean.TRUE);
        this.propertySupportMap.put(JettyPropertySet.DEPLOYER_CREATE_CONTEXT_XML, Boolean.TRUE);
        this.propertySupportMap.put(JettyPropertySet.REALM_NAME, Boolean.TRUE);
    }
}
