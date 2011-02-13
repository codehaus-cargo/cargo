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
package org.codehaus.cargo.container.jrun.internal;

import org.codehaus.cargo.container.jrun.JRun4xPropertySet;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

import java.util.HashMap;
import java.util.Map;

/**
 * Capabilities of the JRun's {@link JRunExistingLocalConfigurationCapability} configuration.
 *
 * @version $Id$
 */
public class JRun4xStandaloneLocalConfigurationCapability
    extends AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Configuration-specific supports Map.
     */
    private Map<String, Boolean> supportsMap;

    /**
     * Initialize the configuration-specific supports Map.
     */
    public JRun4xStandaloneLocalConfigurationCapability()
    {
        super();
        this.supportsMap = new HashMap<String, Boolean>();
        
        // container properties.
        this.supportsMap.put(JRun4xPropertySet.JRUN_HOME, Boolean.TRUE);
        this.supportsMap.put(JRun4xPropertySet.SERVER_NAME, Boolean.TRUE);        

        // general property support.
        this.supportsMap.put(GeneralPropertySet.HOSTNAME, Boolean.FALSE);
        this.supportsMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);
        this.supportsMap.put(GeneralPropertySet.RMI_PORT, Boolean.TRUE);

        // resource support.
        this.supportsMap.put(ResourcePropertySet.RESOURCE, Boolean.FALSE);

        // datasource support.
        this.supportsMap.put(DatasourcePropertySet.DATASOURCE, Boolean.TRUE);
        this.supportsMap.put(DatasourcePropertySet.CONNECTION_TYPE, Boolean.FALSE);
        this.supportsMap.put(DatasourcePropertySet.TRANSACTION_SUPPORT, Boolean.FALSE);
    }
    
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability#getPropertySupportMap()
     */
    @Override
    protected Map<String, Boolean> getPropertySupportMap()
    {
        return this.supportsMap;
    }
}
