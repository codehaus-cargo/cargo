/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2011-2015 Ali Tokmen.
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
package org.codehaus.cargo.generic.configuration.builder;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Allow instantiating a configuration builder by id and resource type.
 */
public interface ConfigurationBuilderFactory
{

    /**
     * Registers a configuration builder implementation.
     *
     * @param containerId the container id attached to this configuration class
     * @param containerType the container type attached to this configuration class
     * @param configurationEntryType the type to differentiate this configuration entry (Resource)
     *            from others for the specified container
     * @param configurationBuilderClass the configuration builder implementation class to register
     */
    void registerConfigurationBuilder(String containerId, ContainerType containerType,
        String configurationEntryType,
        Class< ? extends ConfigurationBuilder> configurationBuilderClass);

    /**
     * @param containerId the container id attached to this configuration class
     * @param containerType the container type attached to this configuration class
     * @param configurationEntryType the type to differentiate this configuration entry ((Resource))
     *            from others for the specified container
     * @return true if the specified configuration is already registered or false otherwise
     */
    boolean isConfigurationBuilderRegistered(String containerId, ContainerType containerType,
        String configurationEntryType);

    /**
     * Create a configuration instance matching the specified container and type.
     *
     * @param container container instance carrying all needed informations for configuration
     *            builder
     * @param resource resource which will be built by this configuration builder
     * @return the configuration builder instance
     */
    ConfigurationBuilder createConfigurationBuilder(LocalContainer container, Resource resource);
}
