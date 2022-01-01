/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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

import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;

/**
 * Capabilities of the WebLogic's
 * {@link org.codehaus.cargo.container.weblogic.WebLogic9xStandaloneLocalConfiguration}
 * configuration.
 */
public class WebLogic9x10x103x12xStandaloneLocalConfigurationCapability extends
    WebLogic8xStandaloneLocalConfigurationCapability
{
    /**
     * WebLogic 9.x onwards supports additional features not available in 8.x.
     */
    public WebLogic9x10x103x12xStandaloneLocalConfigurationCapability()
    {
        super();

        // it is possible to set server logging thresholds in WLS 9+
        this.propertySupportMap.put(GeneralPropertySet.LOGGING, Boolean.TRUE);

        this.propertySupportMap.put(WebLogicPropertySet.CONFIGURATION_VERSION, Boolean.TRUE);
        this.propertySupportMap.put(WebLogicPropertySet.DOMAIN_VERSION, Boolean.TRUE);
    }
}
