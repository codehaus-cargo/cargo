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

import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;

/**
 * GlassFish 3.x standalone local configuration capability.
 * 
 */
public class GlassFish3xStandaloneLocalConfigurationCapability extends
    GlassFish2xStandaloneLocalConfigurationCapability
{

    /**
     * Initialize the configuration-specific supports Map.
     */
    public GlassFish3xStandaloneLocalConfigurationCapability()
    {
        super();

        // recognize those as well
        this.defaultSupportsMap.put(DatasourcePropertySet.DATASOURCE, Boolean.TRUE);
        this.defaultSupportsMap.put(DatasourcePropertySet.TRANSACTION_SUPPORT, Boolean.TRUE);
        this.defaultSupportsMap.put(ResourcePropertySet.RESOURCE, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.DEBUGGER_PORT, Boolean.TRUE);
        this.defaultSupportsMap.put(GlassFishPropertySet.OSGI_SHELL_PORT, Boolean.TRUE);
    }

}
