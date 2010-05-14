/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.jonas.internal;

import java.util.Map;

import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractRuntimeConfigurationCapability;

/**
 * Capabilities of JOnAS {@link org.codehaus.cargo.container.jonas.JonasRuntimeConfiguration}
 * configuration.
 * 
 * @version $Id$
 */
public class JonasRuntimeConfigurationCapability extends AbstractRuntimeConfigurationCapability
{

    /**
     * Initialize the configuration-specific supports Map.
     */
    public JonasRuntimeConfigurationCapability()
    {
        super();
        this.defaultSupportsMap.put(RemotePropertySet.URI, Boolean.TRUE);
        this.defaultSupportsMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.defaultSupportsMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);

        this.defaultSupportsMap.put(JonasPropertySet.JONAS_SERVER_NAME, Boolean.TRUE);
        this.defaultSupportsMap.put(JonasPropertySet.JONAS_DOMAIN_NAME, Boolean.TRUE);
        this.defaultSupportsMap.put(JonasPropertySet.JONAS_CLUSTER_NAME, Boolean.TRUE);

        this.defaultSupportsMap.put(JonasPropertySet.JONAS_DEPLOYABLE_IDENTIFIER, Boolean.TRUE);

        this.defaultSupportsMap.put(JonasPropertySet.JONAS_MEJB_JNDI_PATH, Boolean.TRUE);
        this.defaultSupportsMap.put(JonasPropertySet.JONAS_MEJB_JAAS_FILE, Boolean.TRUE);
        this.defaultSupportsMap.put(JonasPropertySet.JONAS_MEJB_JAAS_ENTRY, Boolean.TRUE);
        this.defaultSupportsMap.put(JonasPropertySet.JONAS_MEJB_JNDI_INIT_CTX_FACT, Boolean.TRUE);
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
