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

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss72xStandaloneLocalConfigurationCapability;

/**
 * JBoss 7.2.x standalone local configuration.
 * 
 * @version $Id$
 */
public class JBoss72xStandaloneLocalConfiguration extends JBoss71xStandaloneLocalConfiguration
{

    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss72xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss71xStandaloneLocalConfiguration#JBoss71xStandaloneLocalConfiguration(String)
     */
    public JBoss72xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        getProperties().remove(JBossPropertySet.JBOSS_OSGI_HTTP_PORT);
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
     * @see JBoss7xStandaloneLocalConfiguration#doConfigure(LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer c) throws Exception
    {
        super.doConfigure(c);

        String configurationXmlFile = "configuration/"
            + getPropertyValue(JBossPropertySet.CONFIGURATION) + ".xml";

        removeXmlReplacement(
            configurationXmlFile,
            "//server/socket-binding-group/socket-binding[@name='osgi-http']",
            "port");
    }

}
