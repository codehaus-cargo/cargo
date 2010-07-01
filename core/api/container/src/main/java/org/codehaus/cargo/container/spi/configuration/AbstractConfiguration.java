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
package org.codehaus.cargo.container.spi.configuration;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.util.log.LoggedObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation of 
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} that can be 
 * specialized for any type of configuration.
 *
 * @version $Id$
 */
public abstract class AbstractConfiguration extends LoggedObject
    implements ContainerConfiguration, Configuration
{
    /**
     * List of all configuration properties (port, logs, etc).
     */
    private Map properties;

    /**
     * Default setup.
     */
    public AbstractConfiguration()
    {
        this.properties = new HashMap();

        // Add all required properties that are common to all configurations
        setProperty(GeneralPropertySet.PROTOCOL, "http");
        setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        setProperty(ServletPropertySet.PORT, "8080");
    }
    
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#setProperty(String, String)
     */
    public void setProperty(String name, String value)
    {
        getLogger().debug("Setting property [" + name + "] = [" + value + "]",
            this.getClass().getName());
        this.properties.put(name, value);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getProperties()
     */
    public Map getProperties()
    {
        return this.properties;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.Configuration#getPropertyValue(String)
     */
    public String getPropertyValue(String name)
    {
        return (String) this.properties.get(name);
    }

    /**
     * {@inheritDoc}
     * @see ContainerConfiguration#verify()
     */
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
                + "]. The port value must be an integer", e);
        }
    }
}
