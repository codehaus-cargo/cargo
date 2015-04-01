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
import org.codehaus.cargo.container.jboss.internal.JBoss71xExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;

/**
 * JBoss 7.1.x existing local configuration.
 * 
 */
public class JBoss71xExistingLocalConfiguration extends JBoss7xExistingLocalConfiguration
{

    /**
     * JBoss container capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new JBoss71xExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see JBoss7xExistingLocalConfiguration#JBoss7xExistingLocalConfiguration(String)
     */
    public JBoss71xExistingLocalConfiguration(String dir)
    {
        super(dir);

        getProperties().remove(GeneralPropertySet.RMI_PORT);
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

}
