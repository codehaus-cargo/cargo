/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

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
     * Private constructor to prevent getting an instance.
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
     * Example: "abc=def;car=bmw" gets converted to "abc" -> "def", and "car" -> "bmw" where:
     * delimiter = ;
     * </p>
     * 
     * @param toSplit The string value to convert to properties
     * @param delimiter The delimiter of the string
     * @return the list of properties
     */
    public static Properties splitPropertiesOnDelimiter(String toSplit, char delimiter)
    {
        Properties properties = new Properties();

        // Be careful on double-escapes since escapeBackSlashesIfNotNull is always called before.
        String toSplitHalfEscaped = toSplit.replace("\\\\" + delimiter, "\\" + delimiter);

        // CARGO-829: Allow escaping of delimiter in property values using the \ character.
        String newLineSeparated = toSplitHalfEscaped.replaceAll("([^\\\\])"
            + Pattern.quote(String.valueOf(delimiter)), "$1\n");

        try
        {
            properties.load(new ByteArrayInputStream(newLineSeparated.getBytes("ISO-8859-1")));
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
    public static String joinOnPipe(Map<String, String> toJoin)
    {
        return joinOnDelimiter(toJoin, PIPE);
    }

    /**
     * Convert properties to a string representation.
     * 
     * @param toJoin A list of properties to convert
     * @return the properties as a string, pipe delimited
     */
    public static String joinOnSemicolon(Map<String, String> toJoin)
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
    public static String joinOnDelimiter(Map<String, String> toJoin, char delimiter)
    {
        StringBuilder buf = new StringBuilder();

        for (Iterator<Map.Entry<String, String>> it = toJoin.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry<String, String> e = it.next();
            String key = e.getKey();
            String value = e.getValue();
            if (value.indexOf(delimiter) != -1)
            {
                // CARGO-829: Delimiter in property values are escaped using the \ character.
                value = value.replace(String.valueOf(delimiter), "\\" + delimiter);
            }
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

    /**
     * Returns a <code>Map&lt;String, String&gt</code> out of a Java Properties object.
     * 
     * @param properties the properties object to convert
     * @return Java Map corresponding to the Java Properties object.
     */
    public static Map<String, String> toMap(Properties properties)
    {
        Map<String, String> result = new HashMap<String, String>(properties.size());
        for (Map.Entry<Object, Object> parameter : properties.entrySet())
        {
            String value = null;
            if (parameter.getValue() != null)
            {
                value = parameter.getValue().toString();
            }
            result.put(parameter.getKey().toString(), value);
        }
        return result;
    }
}
