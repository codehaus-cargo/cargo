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
package org.codehaus.cargo.container.internal.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.util.StringUtils;

/**
 * A class to convert properties to strings and back.
 *
 * @version $Id$
 */
public final class PropertyUtils
{
    /**
     * The default delimiter that separates the properties in a string.
     */
    private static final char PIPE = '|';

    /**
     * The default delimiter that separates the properties in a string.
     */
    private static final char SEMICOLON = ';';
    
    /**
     *  Private constructor to prevent getting an instance.
     */
    private PropertyUtils()
    {
    }

    /**
     * Construct a Properties object from a single string, converting '|' symbols to end of line
     * characters for parsing.
     * <p>
     * Example: "abc=def|car=bmw" gets converted to "abc" -> "def", and "car" -> "bmw"
     * </p>
     *
     * @param toSplit The string value to convert to properties, pipe separated
     * @return the list of properties
     * @see getPropertiesFromDelimitedString
     */
    public static Properties splitPropertiesOnPipe(String toSplit)
    {
        return splitPropertiesOnDelimiter(toSplit, PIPE);
    }
    
    /**
     * Construct a Properties object from a single string, converting ';' symbols to end of line
     * characters for parsing.
     * <p>
     * Example: "abc=def;car=bmw" gets converted to "abc" -> "def", and "car" -> "bmw"
     * </p>
     *
     * @param toSplit The string value to convert to properties, semicolon separated
     * @return the list of properties
     * @see getPropertiesFromDelimitedString
     */
    public static Properties splitPropertiesOnSemicolon(String toSplit)
    {
        return splitPropertiesOnDelimiter(toSplit, SEMICOLON);
    }
    
    /**
     * Construct a Properties object from a single string, by splitting it on a specified delimiter.
     * <p>
     * Example: "abc=def;car=bmw" gets converted to "abc" -> "def", and "car" -> "bmw"
     * where: delimiter = ;
     * </p>
     * 
     * @param toSplit The string value to convert to properties
     * @param delimiter The delimiter of the string
     * @return the list of properties
     */
    public static Properties splitPropertiesOnDelimiter(String toSplit, char delimiter)
    {
        Properties properties = new Properties();

        String newLineSeparated = toSplit.replace(delimiter, '\n');

        try
        {
            properties.load(new ByteArrayInputStream(newLineSeparated.getBytes()));
            return properties;
        }
        catch (IOException e)
        {
            // this should never happen!
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Convert properties to a string representation.
     *
     * @param toJoin A list of properties to convert
     * @return the properties as a string, pipe delimited
     */
    public static String joinOnPipe(Map toJoin)
    {
        return joinOnDelimiter(toJoin, PIPE);
    }

    /**
     * Convert properties to a string representation.
     *
     * @param toJoin A list of properties to convert
     * @return the properties as a string, pipe delimited
     */
    public static String joinOnSemicolon(Map toJoin)
    {
        return joinOnDelimiter(toJoin, SEMICOLON);
    }
    
    /**
     * Convert properties to a string representation, based on the specified delimiter.
     *
     * @param toJoin object to serialize as a string
     * @param delimiter how to separate entries from each other
     * @return the properties as a string, delimited by the above
     */
    public static String joinOnDelimiter(Map toJoin, char delimiter)
    {
        StringBuilder buf = new StringBuilder();

        for (Iterator it = toJoin.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry e = (Map.Entry) it.next();
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            buf.append(key);
            buf.append("=");
            buf.append(value);
            if (it.hasNext())
            {
                buf.append(delimiter);
            }
        }

        return buf.toString();
    }
    
    /**
     * Sets a property value if the property is not null.
     *
     * @param properties the properties object to store the property into
     * @param property the property to set
     * @param value the value to set
     */
    public static void setPropertyIfNotNull(Properties properties, String property, Object value)
    {
        if (value != null)
        {
            properties.setProperty(property, value.toString());
        }
    }
    
    /**
     * Escapes backslashes so that they can parse properly.
     * 
     * @param in - string with backslashes
     * @return string with backslashes escaped, or null, if passed null
     */
    public static String escapeBackSlashesIfNotNull(String in)
    {
        if (in != null)
        {
            return StringUtils.replace(in, "\\", "\\\\");
        }
        else
        {
            return null;
        }
    }
}
