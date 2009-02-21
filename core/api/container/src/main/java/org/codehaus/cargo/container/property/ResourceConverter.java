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
package org.codehaus.cargo.container.property;

import java.util.Properties;

import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.internal.util.PropertyUtils;

/**
 * A Resource is a representation of an object bound to JNDI. This converter will take a property
 * and convert it to a Resource and visa versa.
 * 
 * @version $Id: $
 */
public class ResourceConverter
{

    /**
     * Construct a Resource from a single String. Note that parameters can be nested as long as they
     * are semicolon delimited Example: <code>CreateDatabase=create;DatabaseName=TEST</code>.
     * 
     * @param resourceInformation A string, really a list of properties, representing a Resource
     * @return Resource representing the string.
     * @see org.codehaus.cargo.container.internal.util.PropertyUtils#splitPropertiesOnPipe(String)
     */
    public Resource fromPropertyString(String resourceInformation)
    {
        return fromProperties(PropertyUtils.splitPropertiesOnPipe(PropertyUtils
            .escapeBackSlashesIfNotNull(resourceInformation)));
    }

    /**
     * Construct a Resource from a list of properties.
     * 
     * @param properties A list of properties representing this Resource
     * @return Resource representing the properties.
     * @see PropertyUtils#splitPropertiesOnPipe(String)
     */
    public Resource fromProperties(Properties properties)
    {
        String name = properties.getProperty(ResourcePropertySet.RESOURCE_NAME);
        String type = properties.getProperty(ResourcePropertySet.RESOURCE_TYPE);
        Resource data = new Resource(name, type);

        if (properties.containsKey(ResourcePropertySet.RESOURCE_CLASS))
        {
            String className = properties.getProperty(ResourcePropertySet.RESOURCE_CLASS);
            data.setClassName(className);
        }

        String parametersAsASemicolonDelimitedString =
            properties.getProperty(ResourcePropertySet.PARAMETERS);
        data.setParameters(getParametersFromString(parametersAsASemicolonDelimitedString));
        return data;
    }

    /**
     * tests to see if the value is null before attempting to extract the parameters from it.
     * 
     * @param property to parse, semicolon delimited
     * @return parsed or empty properties.
     */
    private Properties getParametersFromString(String property)
    {
        if (property != null && !property.trim().equals(""))
        {
            return PropertyUtils.splitPropertiesOnSemicolon(property);
        }
        else
        {
            return new Properties();
        }
    }

    /**
     * Get a string representation of this Resource.
     * 
     * @param data the Resource we are serializing
     * @return a string representation
     * @see PropertyUtils#joinPropertiesOnPipe(java.util.Properties)
     */
    public String toPropertyString(Resource data)
    {
        Properties properties = toProperties(data);
        return PropertyUtils.joinOnPipe(properties);
    }

    /**
     * Get a properties object containing all of the members of this Resource object. Note that
     * driver properties will be nested and delimited by a semicolon.
     * 
     * @param data the Resource we are serializing
     * @return a properties object corresponding to this Resource
     */
    public Properties toProperties(Resource data)
    {
        Properties properties = new Properties();
        PropertyUtils.setPropertyIfNotNull(properties, ResourcePropertySet.RESOURCE_NAME, data
            .getName());
        PropertyUtils.setPropertyIfNotNull(properties, ResourcePropertySet.RESOURCE_TYPE, data
            .getType());

        PropertyUtils.setPropertyIfNotNull(properties, ResourcePropertySet.RESOURCE_CLASS, data
            .getClassName());
        PropertyUtils.setPropertyIfNotNull(properties, ResourcePropertySet.PARAMETERS,
            getParametersAsASemicolonDelimitedString(data));
        return properties;
    }

    /**
     * tests to see if the value is null before attempting to join the database properties on a
     * semicolon.
     * 
     * @param data the Resource we are serializing
     * @return property string delimited by semicolon, or null, if they cannot be parsed because the
     *         input properties weren't set or empty
     */
    public String getParametersAsASemicolonDelimitedString(Resource data)
    {
        if (data.getParameterNames().size() != 0)
        {
            return PropertyUtils.joinOnSemicolon(data.getParameters());
        }
        else
        {
            return null;
        }
    }

}
