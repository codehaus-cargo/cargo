/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.generic.configuration;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationCapability;
import org.codehaus.cargo.container.configuration.ConfigurationType;

/**
 * Allow finding a Configuration's capability for a given container identified by its id and its
 * type. Indeed, configurations for a given container id may have different capabilities.
 */
public interface ConfigurationCapabilityFactory
{
    /**
     * @param containerId the id of the container to register against
     * @param containerType the type of the container to register against
     * @param configurationType the configuration type under which the capability should be
     * registered
     * @param configurationCapabilityClass the configuration capability implementation class to
     * register
     */
    void registerConfigurationCapability(String containerId, ContainerType containerType,
        ConfigurationType configurationType,
        Class<? extends ConfigurationCapability> configurationCapabilityClass);

    /**
     * Create a {@link org.codehaus.cargo.container.configuration.ConfigurationCapability} instance
     * for a given container.
     * 
     * @param containerId the container id associated with the configuration capability
     * @param containerType the container type associated with the configuration capability
     * @param configurationType the configuration type associated with the capability
     * @return the configuration capability instance matching the parameter passed
     */
    ConfigurationCapability createConfigurationCapability(String containerId,
        ContainerType containerType, ConfigurationType configurationType);
}
