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
package org.codehaus.cargo.container.internal.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * A class to convert properties to strings and back.
 *
 * @version $Id$
 */
public final class PropertyUtils
{
    /**
     * The separator to use to separate the properties in a string.
     */
    private static final char SEPARATOR = '|';

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
     * @param datasource The string value to convert to properties, pipe separated
     * @return the list of properties
     */
    public static Properties getDataSourceProperties(String datasource)
    {
        Properties properties = new Properties();

        String datasourceAsProperties = datasource.replace(SEPARATOR, '\n');

        try
        {
            properties.load(new ByteArrayInputStream(datasourceAsProperties.getBytes()));
            return properties;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            // this should never happen!
            throw new IllegalArgumentException();
        }
    }

    /**
     * Convert properties to a string representation.
     *
     * @param properties A list of properties to convert
     * @return the properties as a string, pipe separated
     */
    public static String getDataSourceString(Properties properties)
    {
        StringBuffer buf = new StringBuffer();

        for (Iterator it = properties.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry e = (Map.Entry) it.next();
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            buf.append(key);
            buf.append("=");
            buf.append(value);
            if (it.hasNext())
            {
                buf.append(SEPARATOR);
            }
        }

        return buf.toString();
    }
}
