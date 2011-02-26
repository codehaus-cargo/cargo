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
package org.codehaus.cargo.sample.java.validator;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.generic.configuration.ConfigurationCapabilityFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationCapabilityFactory;

/**
 * Validate that a container supports DataSource configuration.
 * 
 * @version $Id$
 */
public class HasDataSourceSupportValidator implements Validator
{
    private ConfigurationCapabilityFactory factory = new DefaultConfigurationCapabilityFactory();

    private ConfigurationType type;

    public HasDataSourceSupportValidator(ConfigurationType type)
    {
        this.type = type;
    }

    public boolean validate(String containerId, ContainerType containerType)
    {
        return factory.createConfigurationCapability(containerId, containerType, this.type)
            .supportsProperty(DatasourcePropertySet.DATASOURCE);
    }
}
