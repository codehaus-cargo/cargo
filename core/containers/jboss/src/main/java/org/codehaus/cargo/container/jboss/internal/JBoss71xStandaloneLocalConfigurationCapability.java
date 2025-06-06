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
package org.codehaus.cargo.container.jboss.internal;

import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;

/**
 * Capabilities of the JBoss's
 * {@link org.codehaus.cargo.container.jboss.JBoss71xStandaloneLocalConfiguration} configuration.
 */
public class JBoss71xStandaloneLocalConfigurationCapability
    extends JBoss7xStandaloneLocalConfigurationCapability
{
    /**
     * Initialize JBoss-specific configuration Map.
     */
    public JBoss71xStandaloneLocalConfigurationCapability()
    {
        this.propertySupportMap.put(ServletPropertySet.USERS, Boolean.TRUE);

        this.propertySupportMap.put(JBossPropertySet.JBOSS_AJP_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_MANAGEMENT_HTTPS_PORT, Boolean.TRUE);

        this.propertySupportMap.remove(GeneralPropertySet.RMI_PORT);
        this.propertySupportMap.remove(JBossPropertySet.JBOSS_JRMP_PORT);
        this.propertySupportMap.remove(JBossPropertySet.JBOSS_JMX_PORT);

        // JBoss 7.x has issues with port offset, this was fixed with JBoss 7.1.x
        this.propertySupportMap.put(GeneralPropertySet.PORT_OFFSET, Boolean.TRUE);
    }
}
