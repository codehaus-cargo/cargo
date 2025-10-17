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
package org.codehaus.cargo.container.spi.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.cargo.container.configuration.ConfigurationCapability;

/**
 * Base implementation of {@link org.codehaus.cargo.container.configuration.ConfigurationCapability}
 * that needs to be extended by the different configuration implementations.
 */
public abstract class AbstractConfigurationCapability implements ConfigurationCapability
{
    /**
     * Property support Map.
     */
    protected Map<String, Boolean> propertySupportMap;

    /**
     * Initialize the property supports Map. This is so that extending classes will have less work
     * to do and they can simply specify what's different from the default.
     */
    public AbstractConfigurationCapability()
    {
        this.propertySupportMap = new HashMap<String, Boolean>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsProperty(String propertyName)
    {
        boolean supports = false;

        if (this.propertySupportMap.containsKey(propertyName))
        {
            supports = this.propertySupportMap.get(propertyName);
        }

        return supports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getProperties()
    {
        return Collections.unmodifiableSortedSet(
            new TreeSet<String>(this.propertySupportMap.keySet()));
    }
}
