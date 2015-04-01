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
package org.codehaus.cargo.container.wildfly;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.JBoss73xStandaloneLocalConfiguration;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.wildfly.internal.WildFly8xStandaloneLocalConfigurationCapability;

/**
 * WildFly 8.x standalone local configuration.
 * 
 */
public class WildFly8xStandaloneLocalConfiguration extends JBoss73xStandaloneLocalConfiguration
{

    /**
     * WildFly container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new WildFly8xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss73xStandaloneLocalConfiguration#JBoss73xStandaloneLocalConfiguration(String)
     */
    public WildFly8xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        getProperties().remove(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT);
        getProperties().remove(JBossPropertySet.JBOSS_REMOTING_TRANSPORT_PORT);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     * @see JBoss73xStandaloneLocalConfiguration#doConfigure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer c) throws Exception
    {
        super.doConfigure(c);

        String configurationXmlFile = "configuration/"
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml";

        removeXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='remoting']",
            "port");

        removeXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='management-native']",
            "port");
    }

    /**
     * {@inheritDoc}. This is not supported anymore in WildFly 8.0.0 Alpha1.
     */
    @Override
    protected void disableWelcomeRoot()
    {
        // Not supported anymore in WildFly 8.0.0 Alpha1.
    }

}
