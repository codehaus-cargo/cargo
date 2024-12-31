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
package org.codehaus.cargo.container.glassfish.internal;

import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfigurationCapability;

/**
 * GlassFish existing local configuration capability.
 */
public class GlassFishExistingLocalConfigurationCapability extends
    AbstractExistingLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public GlassFishExistingLocalConfigurationCapability()
    {
        this.propertySupportMap.remove(GeneralPropertySet.PROTOCOL);
        this.propertySupportMap.remove(GeneralPropertySet.JAVA_HOME);

        this.propertySupportMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.ADMIN_PORT, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.DOMAIN_NAME, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.DEBUG_MODE, Boolean.TRUE);
    }
}
