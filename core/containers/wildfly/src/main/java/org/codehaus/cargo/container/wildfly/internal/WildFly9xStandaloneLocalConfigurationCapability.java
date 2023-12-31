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
package org.codehaus.cargo.container.wildfly.internal;

import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.wildfly.WildFlyPropertySet;

/**
 * Capabilities of the WildFly's
 * {@link org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration} configuration.
 */
public class WildFly9xStandaloneLocalConfigurationCapability
    extends WildFly8xStandaloneLocalConfigurationCapability
{
    /**
     * Initialize WildFly-specific configuration Map.
     */
    public WildFly9xStandaloneLocalConfigurationCapability()
    {
        super();

        this.propertySupportMap.put(DatasourcePropertySet.CONNECTION_TYPE, Boolean.TRUE);
        this.propertySupportMap.put(ResourcePropertySet.RESOURCE, Boolean.TRUE);
        this.propertySupportMap.put(WildFlyPropertySet.CLI_OFFLINE_SCRIPT, Boolean.TRUE);
        this.propertySupportMap.put(WildFlyPropertySet.CLI_EMBEDDED_SCRIPT, Boolean.TRUE);
    }
}
