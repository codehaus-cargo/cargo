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
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfigurationCapability;

/**
 * Capabilities of the JBoss's
 * {@link org.codehaus.cargo.container.jboss.JBossStandaloneLocalConfiguration} configuration.
 */
public class JBoss7xStandaloneLocalConfigurationCapability extends
    AbstractStandaloneLocalConfigurationCapability
{
    /**
     * Initialize JBoss-specific configuration Map.
     */
    public JBoss7xStandaloneLocalConfigurationCapability()
    {
        this.propertySupportMap.put(GeneralPropertySet.PROTOCOL, Boolean.FALSE);
        this.propertySupportMap.put(ServletPropertySet.USERS, Boolean.FALSE);
        this.propertySupportMap.put(GeneralPropertySet.RMI_PORT, Boolean.TRUE);
        this.propertySupportMap.put(DatasourcePropertySet.DATASOURCE, Boolean.TRUE);
        this.propertySupportMap.put(DatasourcePropertySet.TRANSACTION_SUPPORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.CONFIGURATION, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_HTTPS_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_JRMP_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_JMX_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_MANAGEMENT_HTTPS_PORT, Boolean.FALSE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_OSGI_HTTP_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.JBOSS_REMOTING_TRANSPORT_PORT, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.ALTERNATIVE_DEPLOYMENT_DIR, Boolean.TRUE);
        this.propertySupportMap.put(
            JBossPropertySet.DEPLOYER_KEEP_ORIGINAL_WAR_FILENAME, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.ALTERNATIVE_MODULES_DIR, Boolean.TRUE);
        this.propertySupportMap.put(JBossPropertySet.CLI_ONLINE_SCRIPT, Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.USERNAME, Boolean.TRUE);
        this.propertySupportMap.put(RemotePropertySet.PASSWORD, Boolean.TRUE);

        // JBoss 7.x has issues with port offset, this was fixed with JBoss 7.1.x
        this.propertySupportMap.put(GeneralPropertySet.PORT_OFFSET, Boolean.FALSE);
    }
}
