/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
package org.codehaus.cargo.container.jboss.internal;

import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;

/**
 * Capabilities of JBoss's runtime configuration.
 */
public class JBoss7xRuntimeConfigurationCapability extends
    AbstractJBossRuntimeConfigurationCapability
{
    /**
     * Initialize the configuration-specific supports Map.
     */
    public JBoss7xRuntimeConfigurationCapability()
    {
        this.propertySupportMap.put(JBossPropertySet.CONFIGURATION, Boolean.FALSE);
        this.propertySupportMap.put(JBossPropertySet.CLUSTERED, Boolean.FALSE);
        this.propertySupportMap.put(JBossPropertySet.PROFILE, Boolean.FALSE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT, Boolean.FALSE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_MANAGEMENT_HTTPS_PORT, Boolean.FALSE);

        this.propertySupportMap.remove(GeneralPropertySet.RMI_PORT);
    }
}
