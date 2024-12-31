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
package org.codehaus.cargo.container.websphere.internal;

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.websphere.WebSpherePropertySet;

/**
 * Capabilities of WebSphere 8.5.x existing local configuration.
 */
public class WebSphere85xExistingLocalConfigurationCapability
    extends AbstractExistingLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public WebSphere85xExistingLocalConfigurationCapability()
    {
        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);

        this.propertySupportMap.put(WebSpherePropertySet.PROFILE, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.NODE, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.CELL, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.SERVER, Boolean.TRUE);

        this.propertySupportMap.put(WebSpherePropertySet.ADMIN_USERNAME, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.ADMIN_PASSWORD, Boolean.TRUE);

        this.propertySupportMap.put(WebSpherePropertySet.OVERWRITE_EXISTING_CONFIGURATION,
            Boolean.TRUE);

        this.propertySupportMap.put(WebSpherePropertySet.ADMINISTRATION_PORT, Boolean.TRUE);

        this.propertySupportMap.put(WebSpherePropertySet.ONLINE_DEPLOYMENT, Boolean.TRUE);
        this.propertySupportMap.put(WebSpherePropertySet.JYTHON_SCRIPT_ONLINE, Boolean.TRUE);
        this.propertySupportMap.put(
            WebSpherePropertySet.JYTHON_SCRIPT_REPLACE_PROPERTIES, Boolean.TRUE);
    }
}
