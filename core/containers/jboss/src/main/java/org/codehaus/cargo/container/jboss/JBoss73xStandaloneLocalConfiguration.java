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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.LocalContainer;

/**
 * JBoss 7.3.x (EAP 6.2.x) standalone local configuration.
 */
public class JBoss73xStandaloneLocalConfiguration extends JBoss72xStandaloneLocalConfiguration
{

    /**
     * {@inheritDoc}
     * @see JBoss72xStandaloneLocalConfiguration#JBoss72xStandaloneLocalConfiguration(String)
     */
    public JBoss73xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(JBossPropertySet.JBOSS_OSGI_HTTP_PORT, null);
    }

    /**
     * {@inheritDoc}
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
