/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Parser used for processing complexly defined properties
 * like WebSpherePropertySet.EJB_TO_RES_REF_BINDING.
 */
public final class ComplexPropertyUtils
{

    /**
     * Ensures that this utility class cannot be instantiated.
     */
    private ComplexPropertyUtils()
    {
        // Utility classes have no public constructors
    }

    /**
     * Parse provided property using property delimiter | and property value delimiter : .
     * @param property Property to be parsed.
     * @return List of properties containing list of property values.
     */
    public static List<List<String>> parseProperty(String property)
    {
        return parseProperty(property, "|", ":");
    }

    /**
     * Parse provided property using defined property delimiter and property value delimiter.
     * @param property Property to be parsed.
     * @param propertyDelimiter Property delimiter.
     * @param propertyElementDelimiter Property value delimiter.
     * @return List of properties containing list of property values.
     */
    public static List<List<String>> parseProperty(String property, String propertyDelimiter,
            String propertyElementDelimiter)
    {
        List<List<String>> parsedProperty = new ArrayList<List<String>>();

        if (property != null && !property.isEmpty())
        {
            StringTokenizer propertyEntries = new StringTokenizer(property, propertyDelimiter);
            while (propertyEntries.hasMoreTokens())
            {
                String propertyEntry = propertyEntries.nextToken().trim();
                if (propertyEntry != null && !propertyEntry.isEmpty())
                {
                    List<String> propertyValueList = new ArrayList<String>();
                    StringTokenizer propertyValues = new StringTokenizer(propertyEntry,
                            propertyElementDelimiter);

                    while (propertyValues.hasMoreTokens())
                    {
                        propertyValueList.add(propertyValues.nextToken().trim());
                    }
                    parsedProperty.add(propertyValueList);
                }
            }
        }

        return parsedProperty;
    }

    /**
     * Parse provided property using defined property delimiter.
     * @param property Property to be parsed.
     * @param propertyDelimiter Property delimiter.
     * @return List of properties containing list of property values.
     */
    public static List<String> parseProperty(String property, String propertyDelimiter)
    {
        List<String> parsedProperty = new ArrayList<String>();

        if (property != null && !property.isEmpty())
        {
            StringTokenizer propertyEntries = new StringTokenizer(property, propertyDelimiter);
            while (propertyEntries.hasMoreTokens())
            {
                String propertyEntry = propertyEntries.nextToken().trim();
                if (propertyEntry != null && !propertyEntry.isEmpty())
                {
                    parsedProperty.add(propertyEntry);
                }
            }
        }

        return parsedProperty;
    }

    /**
     * Convert list of properties to a string representation, based on the specified delimiter.
     * 
     * @param toJoin object to serialize as a string
     * @param delimiter how to separate entries from each other
     * @return the properties as a string, delimited by the above
     */
    public static String joinOnDelimiter(List<String> toJoin, char delimiter)
    {
        StringBuilder buf = new StringBuilder();

        for (Iterator<String> it = toJoin.iterator(); it.hasNext();)
        {
            String value = it.next();
            buf.append(value);
            if (it.hasNext())
            {
                buf.append(delimiter);
            }
        }

        return buf.toString();
    }
}
