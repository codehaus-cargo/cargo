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

import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;

/**
 * Capabilities of WebLogic existing configuration based on WSLT files.
 */
public class WebLogicWlstExistingLocalConfigurationCapability
    extends WebLogicExistingLocalConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public WebLogicWlstExistingLocalConfigurationCapability()
    {
        this.propertySupportMap.put(WebLogicPropertySet.ONLINE_DEPLOYMENT, Boolean.TRUE);
        this.propertySupportMap.put(WebLogicPropertySet.JYTHON_SCRIPT_ONLINE, Boolean.TRUE);
        this.propertySupportMap.put(
            WebLogicPropertySet.JYTHON_SCRIPT_REPLACE_PROPERTIES, Boolean.TRUE);
    }
}