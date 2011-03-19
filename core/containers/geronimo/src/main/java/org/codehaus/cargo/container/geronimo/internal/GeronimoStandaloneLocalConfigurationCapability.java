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
package org.codehaus.cargo.container.geronimo.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.geronimo.GeronimoPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * Capabilities of the Geronimo's standalone local configuration.
 * 
 * @version $Id$
 */
public class GeronimoStandaloneLocalConfigurationCapability
    extends AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Geronimo-specific configuration Map.
     */
    private Map<String, Boolean> propertySupportMap;

    /**
     * Initialize Geronimo-specific configuration Map.
     */
    public GeronimoStandaloneLocalConfigurationCapability()
    {
        super();

        this.propertySupportMap = new HashMap<String, Boolean>();

        this.propertySupportMap.put(GeneralPropertySet.LOGGING, Boolean.FALSE);
        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);
        this.propertySupportMap.put(ServletPropertySet.USERS, Boolean.FALSE);

        this.propertySupportMap.put(GeneralPropertySet.RMI_PORT, Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);
        this.propertySupportMap.put(GeronimoPropertySet.GERONIMO_CONSOLE_LOGLEVEL, Boolean.TRUE);
        this.propertySupportMap.put(GeronimoPropertySet.GERONIMO_FILE_LOGLEVEL, Boolean.TRUE);
        this.propertySupportMap.put(GeronimoPropertySet.GERONIMO_SERVLET_CONTAINER_ID,
            Boolean.TRUE);
        this.propertySupportMap.put(GeronimoPropertySet.GERONIMO_USERS, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    @Override
    protected Map<String, Boolean> getPropertySupportMap()
    {
        return this.propertySupportMap;
    }
}
