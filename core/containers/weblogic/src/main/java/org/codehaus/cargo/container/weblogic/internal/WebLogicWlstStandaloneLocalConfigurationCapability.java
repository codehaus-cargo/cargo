/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2011-2015 Ali Tokmen.
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

import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;

/**
 * Capabilities of WebLogic configured via WSLT.
 */
public class WebLogicWlstStandaloneLocalConfigurationCapability extends
    WebLogic9x10x103x12xStandaloneLocalConfigurationCapability
{
    /**
     * WLST implementation allows easy support of resources.
     */
    public WebLogicWlstStandaloneLocalConfigurationCapability()
    {
        // support resources
        this.propertySupportMap.put(ResourcePropertySet.RESOURCE, Boolean.TRUE);

        // support setting of admin user and password as they are set by WLST when creating domain
        this.propertySupportMap.put(WebLogicPropertySet.ADMIN_USER, Boolean.TRUE);
        this.propertySupportMap.put(WebLogicPropertySet.ADMIN_PWD, Boolean.TRUE);

        // support external scripts
        this.propertySupportMap.put(WebLogicPropertySet.JYTHON_SCRIPT_OFFLINE, Boolean.TRUE);
        this.propertySupportMap.put(WebLogicPropertySet.JYTHON_SCRIPT_ONLINE, Boolean.TRUE);

        // Logging configuration
        this.propertySupportMap.put(WebLogicPropertySet.LOG_ROTATION_TYPE, Boolean.TRUE);

        // SSL
        this.propertySupportMap.put(WebLogicPropertySet.SSL_HOSTNAME_VERIFICATION_IGNORED,
                Boolean.TRUE);
        this.propertySupportMap.put(WebLogicPropertySet.SSL_HOSTNAME_VERIFIER_CLASS, Boolean.TRUE);

        // WLST-based configuration doesn't need these anymore
        this.propertySupportMap.remove(WebLogicPropertySet.CONFIGURATION_VERSION);
        this.propertySupportMap.remove(WebLogicPropertySet.DOMAIN_VERSION);
    }
}
