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

import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.DataSource;
import junit.framework.TestCase;

import java.util.Map;

/**
 * Unit tests for {@link PropertyUtil}.
 *
 * @version $Id$
 */
public class PropertyUtilsTest extends TestCase
{
    private static final String NAME_VALUE_DELIM = "=";
    private static final String PROPERTY_DELIM = "|";

    public void testSingleProperty()
    {
        _testSingleProperty(DatasourcePropertySet.DATASOURCE_TYPE, "javax.sql.DataSource");
        _testSingleProperty(DatasourcePropertySet.DRIVER_CLASS, "org.hsqldb.jdbcDriver");
    }

    public void testDoubleProperty()
    {
        _testDoubleProperty(DatasourcePropertySet.DATASOURCE_TYPE, "javax.sql.DataSource", 
            DatasourcePropertySet.DRIVER_CLASS, "org.hsqldb.jdbcDriver");
    }

    public void testSetDataSource()
    {
        new DataSource("jdbc/JiraDS","javax.sql.DataSource", "org.hsqldb.jdbcDriver", 
            "postresql:localhost:jirads", "sa", "");
    }

    public void _testDoubleProperty(String name, String value, String name2, String value2)
    {
        String property = name + NAME_VALUE_DELIM + value + PROPERTY_DELIM + name2 
            + NAME_VALUE_DELIM + value2;
        final Map map = getMap(property);
        assertEquals(2, map.size());
        assertEquals(value, map.get(name));
        assertEquals(value2, map.get(name2));
    }

    private void _testSingleProperty(String name, String value)
    {
        String property = name + NAME_VALUE_DELIM + value;
        final Map map = getMap(property);
        assertEquals(1, map.size());
        assertEquals(value, map.get(name));
    }

    private Map getMap(String property)
    {
        return PropertyUtils.getDataSourceProperties(property);
    }
}
