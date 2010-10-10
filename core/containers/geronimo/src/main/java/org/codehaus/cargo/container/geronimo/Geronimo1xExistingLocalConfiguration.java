/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
import org.codehaus.cargo.container.geronimo.internal.GeronimoExistingLocalConfigurationCapability;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

/**
 * Geronimo 1.x series existing {@link org.codehaus.cargo.container.configuration.Configuration}
 * implementation.
 *
 * @version $Id$
 */
public class Geronimo1xExistingLocalConfiguration extends AbstractExistingLocalConfiguration
{
    /**
     * Geronimo configuration capability.
     */
    private static ConfigurationCapability capability =
        new GeronimoExistingLocalConfigurationCapability();

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public Geronimo1xExistingLocalConfiguration(String dir)
    {
        super(dir);

        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(RemotePropertySet.USERNAME, "system");
        setProperty(RemotePropertySet.PASSWORD, "manager");
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "Geronimo Existing Configuration";
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration#configure(org.codehaus.cargo.container.LocalContainer)
     */
    @Override
    protected void doConfigure(LocalContainer container) throws Exception
    {
        String deployDir = getFileHandler().createDirectory(getHome(), "deploy");

        if (!getFileHandler().exists(deployDir))
        {
            getFileHandler().mkdirs(deployDir);
        }
    }
}
