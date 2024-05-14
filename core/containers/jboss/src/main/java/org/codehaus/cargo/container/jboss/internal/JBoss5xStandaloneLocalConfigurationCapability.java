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
package org.codehaus.cargo.container.jboss.internal;

import org.codehaus.cargo.container.jboss.JBossPropertySet;

/**
 * Capabilities of the JBoss's
 * {@link org.codehaus.cargo.container.jboss.JBoss3x4xStandaloneLocalConfiguration} configuration.
 */
public class JBoss5xStandaloneLocalConfigurationCapability extends
    JBoss3x4xStandaloneLocalConfigurationCapability
{
    /**
     * Initialize JBoss-specific configuration Map.
     */
    public JBoss5xStandaloneLocalConfigurationCapability()
    {
        this.propertySupportMap.put(JBossPropertySet.JBOSS_EJB3_REMOTING_PORT, Boolean.TRUE);
        this.propertySupportMap.put(
            JBossPropertySet.JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT, Boolean.TRUE);
        this.propertySupportMap.put(
            JBossPropertySet.JBOSS_TRANSACTION_STATUS_MANAGER_PORT, Boolean.TRUE);
    }
}
