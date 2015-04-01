/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jonas.internal.Jonas4xRuntimeConfigurationCapability;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractRuntimeConfiguration;

/**
 * Configuration to use when using a JOnAS remote container.
 * 
 */
public class Jonas4xRuntimeConfiguration extends AbstractRuntimeConfiguration
{
    /**
     * Capability of the JOnAS runtime configuration.
     */
    private static ConfigurationCapability capability = new Jonas4xRuntimeConfigurationCapability();

    /**
     * Creates the configuration and saves the default values of options.
     */
    public Jonas4xRuntimeConfiguration()
    {
        setProperty(GeneralPropertySet.RMI_PORT, "1099");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(JonasPropertySet.JONAS_SERVER_NAME, "jonas");
        setProperty(JonasPropertySet.JONAS_DOMAIN_NAME, "jonas");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.Configuration#getCapability()
     */
    public ConfigurationCapability getCapability()
    {
        return capability;
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return "JOnAS Runtime Configuration";
    }
}
