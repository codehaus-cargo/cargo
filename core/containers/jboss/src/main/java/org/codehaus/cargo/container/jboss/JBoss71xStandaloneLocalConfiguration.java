/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss71xStandaloneLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;

/**
 * JBoss 7.1.x standalone local configuration.
 * 
 * @version $Id$
 */
public class JBoss71xStandaloneLocalConfiguration extends JBoss7xStandaloneLocalConfiguration
{

    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss71xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss7xStandaloneLocalConfiguration#JBoss7xStandaloneLocalConfiguration(String)
     */
    public JBoss71xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        removeXmlReplacement(
            "configuration/standalone.xml",
            "//server/management/management-interfaces/native-interface[@interface='management']",
            "port");
        addXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='management-native']",
            "port", JBossPropertySet.JBOSS_MANAGEMENT_PORT);

        setProperty(JBossPropertySet.JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT, "4712");
        addXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='txn-recovery-environment']",
            "port", JBossPropertySet.JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT);

        setProperty(JBossPropertySet.JBOSS_TRANSACTION_STATUS_MANAGER_PORT, "4713");
        addXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='txn-status-manager']",
            "port", JBossPropertySet.JBOSS_TRANSACTION_STATUS_MANAGER_PORT);

        getProperties().remove(GeneralPropertySet.RMI_PORT);
        removeXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='jndi']",
            "port");

        getProperties().remove(JBossPropertySet.JBOSS_JRMP_PORT);
        removeXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='jmx-connector-registry']",
            "port");

        getProperties().remove(JBossPropertySet.JBOSS_JMX_PORT);
        removeXmlReplacement(
            "configuration/standalone.xml",
            "//server/socket-binding-group/socket-binding[@name='jmx-connector-server']",
            "port");

        removeXmlReplacement(
            "configuration/standalone.xml",
            "//server/profile/subsystem/periodic-rotating-file-handler/level",
            "name");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

}
