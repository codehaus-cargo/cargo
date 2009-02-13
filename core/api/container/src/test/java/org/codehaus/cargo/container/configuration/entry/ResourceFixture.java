/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
package org.codehaus.cargo.container.configuration.entry;

import java.util.Properties;

import org.codehaus.cargo.container.internal.util.PropertyUtils;
import org.codehaus.cargo.container.property.ResourceConverter;
import org.codehaus.cargo.container.property.ResourcePropertySet;

/**
 * Fixture used to provide inputs for Resource testing.
 * 
 * @version $Id: $
 */
public class ResourceFixture
{
    public String name;

    public String type;

    public String className;

    public String parameters;

    public ResourceFixture(String name, String type, String className, String parameters)
    {
        super();
        this.name = name;
        this.type = type;
        this.className = className;
        this.parameters = parameters;
    }

    public Properties buildResourceProperties()
    {
        Properties properties = new Properties();
        PropertyUtils.setPropertyIfNotNull(properties, ResourcePropertySet.RESOURCE_TYPE, type);
        PropertyUtils.setPropertyIfNotNull(properties, ResourcePropertySet.RESOURCE_CLASS,
            className);
        PropertyUtils.setPropertyIfNotNull(properties, ResourcePropertySet.RESOURCE_NAME, name);
        PropertyUtils
            .setPropertyIfNotNull(properties, ResourcePropertySet.PARAMETERS, parameters);
        return properties;
    }

    public Resource buildResource()
    {
        return new ResourceConverter().fromProperties(buildResourceProperties());
    }

    public String buildResourcePropertyString()
    {
        ResourceConverter converter = new ResourceConverter();
        return converter.toPropertyString(buildResource());
    }

}
