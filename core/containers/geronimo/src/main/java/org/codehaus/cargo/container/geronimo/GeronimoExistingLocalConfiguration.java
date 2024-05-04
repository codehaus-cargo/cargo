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
package org.codehaus.cargo.container.geronimo;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.geronimo.internal.GeronimoExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

/**
 * Geronimo existing {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 */
public class GeronimoExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * Geronimo configuration capability.
     */
    private static final ConfigurationCapability CAPABILITY =
        new GeronimoExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public GeronimoExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(RemotePropertySet.USERNAME, "system");
        setProperty(RemotePropertySet.PASSWORD, "manager");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return CAPABILITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "Geronimo Existing Configuration";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        // Nothing to configure
    }
}
