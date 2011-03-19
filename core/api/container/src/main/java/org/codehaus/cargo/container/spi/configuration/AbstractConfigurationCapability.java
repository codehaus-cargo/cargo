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
package org.codehaus.cargo.container.spi.configuration;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;

/**
 * Base implementation of {@link org.codehaus.cargo.container.configuration.ConfigurationCapability}
 * that needs to be extended by the different configuration implementations.
 * 
 * @version $Id$
 */
public abstract class AbstractConfigurationCapability implements ConfigurationCapability
{
    /**
     * Default support Map.
     */
    protected Map<String, Boolean> defaultSupportsMap;

    /**
     * Initialize the default supports Map. This is so that extending classes will have less work to
     * do and they can simply specify what's different from the default.
     */
    public AbstractConfigurationCapability()
    {
        this.defaultSupportsMap = new HashMap<String, Boolean>();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.ConfigurationCapability#supportsProperty(String)
     */
    public boolean supportsProperty(String propertyName)
    {
        boolean supports = false;

        // Merge default support map with configuration specific one
        Map<String, Boolean> propertySupportMap =
            new HashMap<String, Boolean>(this.defaultSupportsMap);
        propertySupportMap.putAll(getPropertySupportMap());

        if (propertySupportMap.containsKey(propertyName))
        {
            supports = propertySupportMap.get(propertyName);
        }

        return supports;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.ConfigurationCapability#getProperties()
     */
    public Map<String, Boolean> getProperties()
    {
        // Merge default support map with configuration specific one
        Map<String, Boolean> propertySupportMap =
            new HashMap<String, Boolean>(this.defaultSupportsMap);
        propertySupportMap.putAll(getPropertySupportMap());
        return propertySupportMap;
    }

    /**
     * @return a map indexed on the configuration property and having Boolean values expressing
     * whether the configuration supports the said property or not
     */
    protected abstract Map<String, Boolean> getPropertySupportMap();
}
