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
package org.codehaus.cargo.container.stub;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.spi.configuration.NullConfigurationCapability;
import org.codehaus.cargo.util.log.Logger;
import org.codehaus.cargo.util.log.NullLogger;

/**
 * Code common to all configuration stubs.
 */
public abstract class AbstractConfigurationStub implements Configuration
{
    /**
     * Null capability.
     */
    private static final ConfigurationCapability CAPABILITY = new NullConfigurationCapability();

    /**
     * Null logger.
     */
    private static final Logger LOGGER = new NullLogger();

    /**
     * Properties.
     */
    private Map<String, String> properties = new HashMap<String, String>();

    /**
     * Doesn't do anything. {@inheritDoc}
     * @param logger Ignored.
     */
    @Override
    public void setLogger(Logger logger)
    {
        // Voluntarily not doing anything for testing
    }

    /**
     * {@inheritDoc}
     * @return {@link NullConfigurationCapability}
     */
    @Override
    public ConfigurationCapability getCapability()
    {
        return AbstractConfigurationStub.CAPABILITY;
    }

    /**
     * {@inheritDoc}
     * @return {@link NullLogger}
     */
    @Override
    public Logger getLogger()
    {
        return AbstractConfigurationStub.LOGGER;
    }

    /**
     * Saves the property. {@inheritDoc}
     * @param name Property name.
     * @param value Property value.
     */
    @Override
    public void setProperty(String name, String value)
    {
        this.properties.put(name, value);
    }

    /**
     * {@inheritDoc}
     * @return All properties.
     */
    @Override
    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    /**
     * {@inheritDoc}
     * @param name Name of the property to get.
     * @return The value for <code>name</code>, <code>null</code> if not set.
     */
    @Override
    public String getPropertyValue(String name)
    {
        return this.properties.get(name);
    }
}
