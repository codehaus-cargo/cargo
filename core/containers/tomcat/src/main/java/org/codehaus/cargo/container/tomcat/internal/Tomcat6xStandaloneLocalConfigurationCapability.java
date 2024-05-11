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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;

/**
 * Capabilities of Tomcat's {@link AbstractCatalinaStandaloneLocalConfiguration} configuration.
 */
public class Tomcat6xStandaloneLocalConfigurationCapability extends
    Tomcat5xStandaloneLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public Tomcat6xStandaloneLocalConfigurationCapability()
    {
        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.TRUE);
        this.propertySupportMap.put(TomcatPropertySet.CONNECTOR_KEY_STORE_FILE, Boolean.TRUE);
        this.propertySupportMap.put(TomcatPropertySet.CONNECTOR_KEY_STORE_PASSWORD, Boolean.TRUE);
        this.propertySupportMap.put(TomcatPropertySet.CONNECTOR_PROTOCOL_CLASS, Boolean.TRUE);
    }

}
