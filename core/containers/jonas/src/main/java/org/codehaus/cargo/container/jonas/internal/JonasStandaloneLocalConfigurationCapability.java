/*
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * ========================================================================
 */
package org.codehaus.cargo.container.jonas.internal;

import java.util.Map;

import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * Capabilities of the JOnAS 4.x
 * {@link org.codehaus.cargo.container.jonas.Jonas4xStandaloneLocalConfiguration} configuration.
 * 
 * @version $Id$
 */
public class JonasStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{

    /**
     * Initialize Jonas-specific configuration Map.
     */
    public JonasStandaloneLocalConfigurationCapability()
    {
        super();

        this.defaultSupportsMap.put(ServletPropertySet.USERS, Boolean.FALSE);

        this.defaultSupportsMap.put(GeneralPropertySet.RMI_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(JonasPropertySet.JONAS_SERVER_NAME, Boolean.TRUE);
        this.defaultSupportsMap.put(JonasPropertySet.JONAS_DOMAIN_NAME, Boolean.TRUE);
        this.defaultSupportsMap.put(JonasPropertySet.JONAS_SERVICES_LIST, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    protected Map getPropertySupportMap()
    {
        return this.defaultSupportsMap;
    }
}
