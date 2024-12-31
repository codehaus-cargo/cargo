/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import org.codehaus.cargo.container.tomcat.TomcatPropertySet;

/**
 * Capabilities of Tomcat's {@link AbstractCatalinaStandaloneLocalConfiguration} configuration.
 */
public class Tomcat7xStandaloneLocalConfigurationCapability extends
    Tomcat6xStandaloneLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public Tomcat7xStandaloneLocalConfigurationCapability()
    {
        this.propertySupportMap.put(TomcatPropertySet.CUSTOM_VALVE, Boolean.TRUE);
        this.propertySupportMap.put(TomcatPropertySet.CONTEXT_ALLOW_MULTIPART, Boolean.TRUE);
        this.propertySupportMap.put(TomcatPropertySet.HOST_START_STOP_THREADS, Boolean.TRUE);

        // CARGO-1271: Starting Tomcat 7 with Cargo logs warning on emptySessionPath
        this.propertySupportMap.remove(TomcatPropertySet.CONNECTOR_EMPTY_SESSION_PATH);
    }

}
