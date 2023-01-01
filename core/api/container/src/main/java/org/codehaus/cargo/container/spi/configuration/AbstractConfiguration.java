/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.spi.configuration;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Base implementation of
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} that can be
 * specialized for any type of configuration.
 */
public abstract class AbstractConfiguration extends LoggedObject
    implements ContainerConfiguration, Configuration
{
    /**
     * List of all configuration properties (port, logs, etc).
     */
    private Map<String, String> properties;

    /**
     * Default setup.
     */
    public AbstractConfiguration()
    {
        this.properties = new HashMap<String, String>();

        // Add all required properties that are common to all configurations
        setProperty(GeneralPropertySet.PROTOCOL, "http");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(ServletPropertySet.PORT, "8080");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String name, String value)
    {
        if (value != null)
        {
            getLogger().debug("Setting property [" + name + "] = [" + value + "]",
                this.getClass().getName());
            this.properties.put(name, value);
        }
        else
        {
            getLogger().debug("Removing property [" + name + "]", this.getClass().getName());
            this.properties.remove(name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPropertyValue(String name)
    {
        String systemProperty = System.getProperties().getProperty(name);
        if (systemProperty != null)
        {
            return systemProperty;
        }
        return this.properties.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify()
    {
        // Verify that the port is a valid number.
        verifyServletPortProperty();
    }

    /**
     * Verify that the Servlet port specified is a valid integer.
     */
    private void verifyServletPortProperty()
    {
        try
        {
            Integer.parseInt(getPropertyValue(ServletPropertySet.PORT));
        }
        catch (NumberFormatException e)
        {
            throw new ContainerException("Invalid port number ["
                + getPropertyValue(ServletPropertySet.PORT)
                    + "] for property " + ServletPropertySet.PORT
                        + ". The port value must be an integer", e);
        }
    }
}
