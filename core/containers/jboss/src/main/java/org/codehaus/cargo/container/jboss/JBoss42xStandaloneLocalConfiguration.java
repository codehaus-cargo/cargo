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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jboss.internal.JBoss42xStandaloneLocalConfigurationCapability;

/**
 * JBoss 4.2.x standalone local configuration.
 */
public class JBoss42xStandaloneLocalConfiguration extends JBoss3x4xStandaloneLocalConfiguration
{

    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss42xStandaloneLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss3x4xStandaloneLocalConfiguration#JBoss3x4xStandaloneLocalConfiguration(String)
     */
    public JBoss42xStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        this.log4jFileName = "jboss-log4j.xml";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

}
