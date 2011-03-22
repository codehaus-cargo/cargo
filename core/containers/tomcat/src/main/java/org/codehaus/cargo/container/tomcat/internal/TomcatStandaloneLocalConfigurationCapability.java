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
package org.codehaus.cargo.container.tomcat.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;

/**
 * Capabilities of Tomcat's {@link AbstractTomcatStandaloneLocalConfiguration} configuration.
 * 
 * @version $Id$
 */
public class TomcatStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Configuration-specific supports Map.
     */
    private Map<String, Boolean> supportsMap;

    /**
     * Initialize the configuration-specific supports Map.
     */
    public TomcatStandaloneLocalConfigurationCapability()
    {
        super();

        this.supportsMap = new HashMap<String, Boolean>();
        this.supportsMap.put(TomcatPropertySet.AJP_PORT, Boolean.TRUE);
        this.supportsMap.put(TomcatPropertySet.CONTEXT_RELOADABLE, Boolean.TRUE);
        this.supportsMap.put(DatasourcePropertySet.DATASOURCE, Boolean.TRUE);
        this.supportsMap.put(ResourcePropertySet.RESOURCE, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    @Override
    protected Map<String, Boolean> getPropertySupportMap()
    {
        return this.supportsMap;
    }

}
