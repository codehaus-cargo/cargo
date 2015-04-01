/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import java.util.Map;

import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * GlassFish 2.x standalone local configuration capability.
 * 
 */
public class GlassFish2xStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{

    /**
     * Initialize the configuration-specific supports Map.
     */
    public GlassFish2xStandaloneLocalConfigurationCapability()
    {
        super();

        // unsupported
        this.defaultSupportsMap.put(GeneralPropertySet.LOGGING, Boolean.FALSE);
        this.defaultSupportsMap.put(ServletPropertySet.USERS, Boolean.FALSE);

        // recognize those
        this.defaultSupportsMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.defaultSupportsMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);
        this.defaultSupportsMap.put(GeneralPropertySet.HOSTNAME, Boolean.TRUE);
        this.defaultSupportsMap.put(GeneralPropertySet.JVMARGS, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.REMOVE_DEFAULT_DATASOURCE, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.ADMIN_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.JMS_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.IIOP_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.HTTPS_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.IIOPS_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.IIOP_MUTUAL_AUTH_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.JMX_ADMIN_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.DOMAIN_NAME, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.DEBUG_MODE, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.PORT_BASE, Boolean.TRUE);

        // this.defaultSupportsMap.put(ServletPropertySet.PORT, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    @Override
    protected Map<String, Boolean> getPropertySupportMap()
    {
        return this.defaultSupportsMap;
    }

}
