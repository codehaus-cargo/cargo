/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.util.Arrays;
import java.util.Properties;

import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.internal.util.PropertyUtils;

/**
 * A Resource is a representation of an object bound to JNDI. This converter will take a property
 * and convert it to a Resource and visa versa.
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
        Resource resource = new Resource(name, type);

        if (properties.containsKey(ResourcePropertySet.RESOURCE_CLASS))
        {
            String className = properties.getProperty(ResourcePropertySet.RESOURCE_CLASS);
            resource.setClassName(className);
        }

        if (properties.containsKey(ResourcePropertySet.RESOURCE_ID))
        {
            String id = properties.getProperty(ResourcePropertySet.RESOURCE_ID);
            resource.setId(id);
        }
        else
        {
            resource.setId(createIdFromJndiLocationIfNotNull(name));
        }

        String parametersAsASemicolonDelimitedString =
            properties.getProperty(ResourcePropertySet.PARAMETERS);
        resource.setParameters(PropertyUtils.toMap(getParametersFromString(
            PropertyUtils.escapeBackSlashesIfNotNull(parametersAsASemicolonDelimitedString))));
        return resource;
    }

    /**
     * tests to see if the value is null before attempting to extract the parameters from it.
     * 
     * @param property to parse, semicolon delimited
     * @return parsed or empty properties.
     */
    private Properties getParametersFromString(String property)
    {
        if (property != null && !property.trim().isEmpty())
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
     */
    public String toPropertyString(Resource data)
    {
        Properties properties = toProperties(data);
        return PropertyUtils.joinOnPipe(PropertyUtils.toMap(properties));
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
        PropertyUtils.setPropertyIfNotNull(properties, ResourcePropertySet.RESOURCE_ID, data
            .getId());
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
     * input properties weren't set or empty
     */
    public String getParametersAsASemicolonDelimitedString(Resource data)
    {
        if (!data.getParameterNames().isEmpty())
        {
            return PropertyUtils.joinOnSemicolon(data.getParameters());
        }
        else
        {
            return null;
        }
    }

    /**
     * return a string that can be used to name this configuration or null, if jndiLocation was not
     * specified.
     * 
     * @param jndiLocation used to construct the id
     * @return a string that can be used to name this configuration or null, if jndiLocation was not
     * specified.
     * @see org.codehaus.cargo.container.configuration.entry.Resource#createIdFromJndiLocation(String)
     */
    private static String createIdFromJndiLocationIfNotNull(String jndiLocation)
    {
        String id = null;
        if (jndiLocation != null)
        {
            id = createIdFromJndiLocation(jndiLocation);
        }
        return id;
    }

    /**
     * Get a string name for the configuration of this resource. This should be XML and filesystem
     * friendly. For example, the String returned will have no slashes or punctuation, and be as
     * short as possible.
     * 
     * @param jndiLocation used to construct the id
     * @return a string that can be used to name this configuration
     */
    protected static String createIdFromJndiLocation(String jndiLocation)
    {
        // using indexOf to avoid introducing a regex package dependency. when we move
        // to jdk 5+, this can be more easily performed with regex.

        int[] delimeters =
            new int[] {
                // jndi locations are organized by dots or slashes. In JBoss, it could have a colon
                jndiLocation.lastIndexOf('/'), jndiLocation.lastIndexOf('.'),
                jndiLocation.lastIndexOf(':')};
        Arrays.sort(delimeters);

        int highestIndex = delimeters[2];

        // highestIndex could be -1, or a location of a character we don't want. In either case, we
        // want to increase it by one
        return jndiLocation.substring(highestIndex + 1);
    }
}
