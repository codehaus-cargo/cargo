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
package org.codehaus.cargo.container.jonas.internal;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.jonas.JonasPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractExistingLocalConfiguration;

/**
 * JOnAS existing {@link org.codehaus.cargo.container.configuration.Configuration} implementation.
 * 
 * @version $Id$
 */
public abstract class AbstractJonasExistingLocalConfiguration extends
    AbstractExistingLocalConfiguration
{
    /**
     * Capability of the JOnAS existing configuration.
     */
    private static ConfigurationCapability capability =
        new JonasExistingLocalConfigurationCapability();

    /**
     * Version information.
     */
    private String toString;

    /**
     * Creates the configuration.
     * 
     * @param dir JONAS_ROOT directory.
     * @param version JOnAS container version.
     * 
     * @see AbstractExistingLocalConfiguration#AbstractExistingLocalConfiguration(String)
     */
    public AbstractJonasExistingLocalConfiguration(String dir, String version)
    {
        super(dir);
        setProperty(GeneralPropertySet.PROTOCOL, "http");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(ServletPropertySet.PORT, "9000");
        setProperty(JonasPropertySet.JONAS_SERVER_NAME, "jonas");
        setProperty(JonasPropertySet.JONAS_DOMAIN_NAME, "jonas");
        this.toString = "JOnAS " + version + " Existing Local Configuration";
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
     * Check if the directory exists.
     * 
     * @param dir the directory name
     */
    protected void checkDirExists(String dir)
    {
        String path = getFileHandler().append(getHome(), dir);
        boolean exists = getFileHandler().exists(path);

        if (!exists)
        {
            throw new ContainerException("Invalid existing configuration: directory [" + path
                + "] does not exist in JONAS_BASE");
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see Object#toString()
     */
    @Override
    public String toString()
    {
        return toString;
    }
}
