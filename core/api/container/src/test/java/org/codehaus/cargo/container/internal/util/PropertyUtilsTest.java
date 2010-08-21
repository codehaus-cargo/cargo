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

import java.util.Map;
import java.util.Properties;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.codehaus.cargo.container.property.DatasourcePropertySet;

/**
 * Unit tests for {@link PropertyUtils}.
 * 
 * @version $Id$
 */
public class PropertyUtilsTest extends TestCase
{
    private static final String NAME_VALUE_DELIM = "=";

    private static final String PIPE_DELIM = "|";

    private static final String ONE_SEMI_TWO = "inner-1=hello;inner-2=goodbye";

    private static final String TWO_SEMI_ONE = "inner-2=goodbye;inner-1=hello";

    private static final String ONE_PIPE_TWO = "inner-1=hello|inner-2=goodbye";

    private static final String TWO_PIPE_ONE = "inner-2=goodbye|inner-1=hello";

    private static final String ONE_THEN_TWO_OUTER_A =
        "outer-a" + NAME_VALUE_DELIM + "outer-1" + PIPE_DELIM + "outer-b" + NAME_VALUE_DELIM
            + ONE_SEMI_TWO;

    private static final String TWO_THEN_ONE_OUTER_A =
        "outer-a" + NAME_VALUE_DELIM + "outer-1" + PIPE_DELIM + "outer-b" + NAME_VALUE_DELIM
            + TWO_SEMI_ONE;

    private static final String ONE_THEN_TWO_OUTER_B =
        "outer-b" + NAME_VALUE_DELIM + ONE_SEMI_TWO + PIPE_DELIM + "outer-a" + NAME_VALUE_DELIM
            + "outer-1";

    private static final String TWO_THEN_ONE_OUTER_B =
        "outer-b" + NAME_VALUE_DELIM + TWO_SEMI_ONE + PIPE_DELIM + "outer-a" + NAME_VALUE_DELIM
            + "outer-1";

    private static final String ONE_DOT_TWO = "inner-1=hello.inner-2=goodbye";;

    private static final String TWO_DOT_ONE = "inner-2=goodbye.inner-1=hello";

    public void testSingleProperty()
    {
        _testSingleProperty(DatasourcePropertySet.CONNECTION_TYPE, "javax.sql.DataSource");
        _testSingleProperty(DatasourcePropertySet.DRIVER_CLASS, "org.hsqldb.jdbcDriver");
    }

    public void testDoubleProperty()
    {
        _testDoubleProperty(DatasourcePropertySet.CONNECTION_TYPE, "javax.sql.DataSource",
            DatasourcePropertySet.DRIVER_CLASS, "org.hsqldb.jdbcDriver");
    }

    public void _testDoubleProperty(String name, String value, String name2, String value2)
    {
        String property =
            name + NAME_VALUE_DELIM + value + PIPE_DELIM + name2 + NAME_VALUE_DELIM + value2;
        final Map map = PropertyUtils.splitPropertiesOnPipe(property);
        assertEquals(2, map.size());
        assertEquals(value, map.get(name));
        assertEquals(value2, map.get(name2));
    }

    private void _testSingleProperty(String name, String value)
    {
        String property = name + NAME_VALUE_DELIM + value;
        final Map map = PropertyUtils.splitPropertiesOnPipe(property);
        assertEquals(1, map.size());
        assertEquals(value, map.get(name));
    }

    public void testSplitAndJoinMultiplePropertiesOnSemicolon()
    {
        Properties inner = PropertyUtils.splitPropertiesOnDelimiter(ONE_SEMI_TWO, ';');
        assertEquals("hello", inner.getProperty("inner-1"));
        assertEquals("goodbye", inner.getProperty("inner-2"));
        assertEquals(2, inner.size());

        try
        {
            assertEquals(ONE_SEMI_TWO, PropertyUtils.joinOnSemicolon(inner));
        }
        catch (ComparisonFailure e)
        {
            assertEquals(TWO_SEMI_ONE, PropertyUtils.joinOnSemicolon(inner));
        }
    }

    public void testSplitAndJoinMultiplePropertiesOnDot()
    {
        Properties inner = PropertyUtils.splitPropertiesOnDelimiter(ONE_DOT_TWO, '.');
        assertEquals("hello", inner.getProperty("inner-1"));
        assertEquals("goodbye", inner.getProperty("inner-2"));
        assertEquals(2, inner.size());

        try
        {
            assertEquals(ONE_DOT_TWO, PropertyUtils.joinOnDelimiter(inner, '.'));
        }
        catch (ComparisonFailure e)
        {
            assertEquals(TWO_DOT_ONE, PropertyUtils.joinOnDelimiter(inner, '.'));
        }
    }

    public void testSplitAndJoinMultiplePropertiesOnPipe()
    {
        Properties inner = PropertyUtils.splitPropertiesOnPipe(ONE_PIPE_TWO);
        assertEquals("hello", inner.getProperty("inner-1"));
        assertEquals("goodbye", inner.getProperty("inner-2"));
        assertEquals(2, inner.size());

        try
        {
            assertEquals(ONE_PIPE_TWO, PropertyUtils.joinOnPipe(inner));
        }
        catch (ComparisonFailure e)
        {
            assertEquals(TWO_PIPE_ONE, PropertyUtils.joinOnPipe(inner));
        }
    }

    public void testSplitAndJoinSemicolonNestedInPipeProperties()
    {
        Properties outer = PropertyUtils.splitPropertiesOnPipe(ONE_THEN_TWO_OUTER_A);
        assertEquals(ONE_SEMI_TWO, outer.getProperty("outer-b"));
        assertEquals(2, outer.size());

        try
        {
            assertEquals(ONE_THEN_TWO_OUTER_A, PropertyUtils.joinOnPipe(outer));
        }
        catch (ComparisonFailure deviation1)
        {
            try
            {
                assertEquals(TWO_THEN_ONE_OUTER_A, PropertyUtils.joinOnPipe(outer));
            }
            catch (ComparisonFailure deviation2)
            {
                try
                {
                    assertEquals(ONE_THEN_TWO_OUTER_B, PropertyUtils.joinOnPipe(outer));
                }
                catch (ComparisonFailure deviation3)
                {
                    try
                    {
                        assertEquals(TWO_THEN_ONE_OUTER_B, PropertyUtils
                            .joinOnPipe(outer));
                    }
                    catch (ComparisonFailure deviation4)
                    {
                        assertEquals(TWO_THEN_ONE_OUTER_A, PropertyUtils
                            .joinOnPipe(outer));
                    }
                }
            }
        }
    }
    
    public void testCanEscapeWindowsSlashes()
    {
        assertEquals("c:\\\\test", PropertyUtils.escapeBackSlashesIfNotNull("c:\\test"));
    }

    public void testSplitEscapedSemicolons()
    {
        Properties inner = PropertyUtils.splitPropertiesOnSemicolon(
            PropertyUtils.escapeBackSlashesIfNotNull(
                "foo=bar;baz=blorple\\;zot;windows=c:\\test;glorg=gluux"));

        assertEquals(4, inner.size());
        assertEquals("bar", inner.getProperty("foo"));
        assertEquals("blorple;zot", inner.getProperty("baz"));
        assertEquals("gluux", inner.getProperty("glorg"));
        assertEquals("c:\\test", inner.getProperty("windows"));
    }
}
