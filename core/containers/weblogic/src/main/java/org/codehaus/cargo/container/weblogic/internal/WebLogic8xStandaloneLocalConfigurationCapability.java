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
package org.codehaus.cargo.container.weblogic.internal;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;

/**
 * Capabilities of the WebLogic's
 * {@link org.codehaus.cargo.container.weblogic.WebLogicConfiguration} configuration.
 * 
 */
public class WebLogic8xStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Configuration-specific supports Map.
     */
    protected Map<String, Boolean> supportsMap;

    /**
     * Initialize the configuration-specific supports Map.
     */
    public WebLogic8xStandaloneLocalConfigurationCapability()
    {
        super();

        this.supportsMap = new HashMap<String, Boolean>();

        this.supportsMap.put(GeneralPropertySet.HOSTNAME, Boolean.TRUE);

        // changing this would require generating hashes for the default
        // authenticator ldift files
        this.supportsMap.put(WebLogicPropertySet.ADMIN_USER, Boolean.FALSE);
        this.supportsMap.put(WebLogicPropertySet.ADMIN_PWD, Boolean.FALSE);

        this.supportsMap.put(WebLogicPropertySet.SERVER, Boolean.TRUE);
        this.supportsMap.put(WebLogicPropertySet.BEA_HOME, Boolean.TRUE);

        // server log thresholds are not supported in WebLogic 8x
        this.supportsMap.put(GeneralPropertySet.LOGGING, Boolean.FALSE);
        supportDataSources();
    }

    /**
     * Add capability for all DataSource implementations
     */
    private void supportDataSources()
    {
        this.supportsMap.put(DatasourcePropertySet.DATASOURCE, Boolean.TRUE);
        this.supportsMap.put(DatasourcePropertySet.CONNECTION_TYPE, Boolean.TRUE);
        this.supportsMap.put(DatasourcePropertySet.TRANSACTION_SUPPORT, Boolean.TRUE);
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
