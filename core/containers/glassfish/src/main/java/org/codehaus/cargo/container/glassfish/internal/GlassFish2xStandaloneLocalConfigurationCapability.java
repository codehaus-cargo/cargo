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
package org.codehaus.cargo.container.glassfish.internal;

import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * GlassFish 2.x standalone local configuration capability.
 */
public class GlassFish2xStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public GlassFish2xStandaloneLocalConfigurationCapability()
    {
        // unsupported
        this.propertySupportMap.put(GeneralPropertySet.LOGGING, Boolean.FALSE);
        this.propertySupportMap.put(ServletPropertySet.USERS, Boolean.FALSE);

        // recognize those
        this.propertySupportMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);
        this.propertySupportMap.put(GeneralPropertySet.HOSTNAME, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.REMOVE_DEFAULT_DATASOURCE, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.ADMIN_PORT, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.JMS_PORT, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.IIOP_PORT, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.HTTPS_PORT, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.IIOPS_PORT, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.IIOP_MUTUAL_AUTH_PORT, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.JMX_ADMIN_PORT, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.DOMAIN_NAME, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.DEBUG_MODE, Boolean.TRUE);
        this.propertySupportMap.put(GlassFishPropertySet.PORT_BASE, Boolean.TRUE);
    }
}
