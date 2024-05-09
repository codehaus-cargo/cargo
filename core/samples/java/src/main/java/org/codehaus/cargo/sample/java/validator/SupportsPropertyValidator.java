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
package org.codehaus.cargo.sample.java.validator;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;

/**
 * Validator, that check if the container supports a given property.
 */
public class SupportsPropertyValidator extends AbstractConfigurationCapabilityValidator
{
    /**
     * Configuration type to check.
     */
    private ConfigurationType type;

    /**
     * Property support to check.
     */
    private String supportsProperty;

    /**
     * Saves the attributes.
     * @param type Configuration type to check.
     * @param supportsProperty Property support to check.
     */
    public SupportsPropertyValidator(ConfigurationType type, String supportsProperty)
    {
        this.type = type;
        this.supportsProperty = supportsProperty;
    }

    /**
     * Checks if the container supports the given property with the given type. {@inheritDoc}
     */
    @Override
    public boolean validate(String containerId, ContainerType containerType)
    {
        return factory.createConfigurationCapability(containerId, containerType, this.type)
            .supportsProperty(this.supportsProperty);
    }
}
