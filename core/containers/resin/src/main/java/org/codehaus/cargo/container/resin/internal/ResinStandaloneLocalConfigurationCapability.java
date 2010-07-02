/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.container.resin.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * Capabilities of the Resin's 
 * {@link org.codehaus.cargo.container.resin.internal.AbstractResinStandaloneLocalConfiguration}
 * configuration.
 *  
 * @version $Id$
 */
public class ResinStandaloneLocalConfigurationCapability 
    extends AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Configuration-specific supports Map.
     */
    private Map supportsMap;
    
    /**
     * Initialize the configuration-specific supports Map.
     */
    public ResinStandaloneLocalConfigurationCapability()
    {
        super();
        
        this.supportsMap = new HashMap();
        
        this.supportsMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);
        this.supportsMap.put(GeneralPropertySet.HOSTNAME, Boolean.FALSE);
        this.supportsMap.put(DatasourcePropertySet.DATASOURCE, Boolean.TRUE);
        this.supportsMap.put(ResourcePropertySet.RESOURCE, Boolean.TRUE);
    }

    /**
     * {@inheritDoc}
     * @see AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    @Override
    protected Map getPropertySupportMap()
    {
        return this.supportsMap;
    }
}
