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

import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import org.codehaus.cargo.container.property.DatasourcePropertySet;

/**
 * Unit tests for {@link PropertyUtils}.
 */
public class PropertyUtilsTest
{
    /**
     * Name-value delimiter.
     */
    private static final String NAME_VALUE_DELIM = "=";

    /**
     * Pipe delimiter.
     */
    private static final String PIPE_DELIM = "|";

    /**
     * Hello-goodbye (semicolon-separated).
     */
    private static final String ONE_SEMI_TWO = "inner-1=hello;inner-2=goodbye";

    /**
     * Goodbye-hello (semicolon-separated).
     */
    private static final String TWO_SEMI_ONE = "inner-2=goodbye;inner-1=hello";

    /**
     * Hello-goodbye (pipe-separated).
     */
    private static final String ONE_PIPE_TWO = "inner-1=hello|inner-2=goodbye";

    /**
     * Goodbye-hello (pipe-separated).
     */
    private static final String TWO_PIPE_ONE = "inner-2=goodbye|inner-1=hello";

    /**
     * Embedded properties.
     */
    private static final String ONE_THEN_TWO_OUTER_A =
        "outer-a" + NAME_VALUE_DELIM + "outer-1" + PIPE_DELIM + "outer-b" + NAME_VALUE_DELIM
            + ONE_SEMI_TWO;

    /**
     * Embedded properties.
     */
    private static final String TWO_THEN_ONE_OUTER_A =
        "outer-a" + NAME_VALUE_DELIM + "outer-1" + PIPE_DELIM + "outer-b" + NAME_VALUE_DELIM
            + TWO_SEMI_ONE;

    /**
     * Embedded properties.
     */
    private static final String ONE_THEN_TWO_OUTER_B =
        "outer-b" + NAME_VALUE_DELIM + ONE_SEMI_TWO + PIPE_DELIM + "outer-a" + NAME_VALUE_DELIM
            + "outer-1";

    /**
     * Embedded properties.
     */
    private static final String TWO_THEN_ONE_OUTER_B =
        "outer-b" + NAME_VALUE_DELIM + TWO_SEMI_ONE + PIPE_DELIM + "outer-a" + NAME_VALUE_DELIM
            + "outer-1";

    /**
     * Embedded properties.
     */
    private static final String ONE_DOT_TWO = "inner-1=hello.inner-2=goodbye";;

    /**
     * Embedded properties.
     */
    private static final String TWO_DOT_ONE = "inner-2=goodbye.inner-1=hello";

    /**
     * Test a single property.
     */
    @Test
    public void testSingleProperty()
    {
        testSingleProperty(DatasourcePropertySet.CONNECTION_TYPE, "javax.sql.DataSource");
        testSingleProperty(DatasourcePropertySet.DRIVER_CLASS, "org.hsqldb.jdbcDriver");
    }

    /**
     * Test double property.
     */
    @Test
    public void testDoubleProperty()
    {
        testDoubleProperty(DatasourcePropertySet.CONNECTION_TYPE, "javax.sql.DataSource",
            DatasourcePropertySet.DRIVER_CLASS, "org.hsqldb.jdbcDriver");
    }

    /**
     * Test double property.
     * @param name First property name.
     * @param value First property value.
     * @param name2 Second property name.
     * @param value2 Second property value.
     */
    private void testDoubleProperty(String name, String value, String name2, String value2)
    {
        String property =
            name + NAME_VALUE_DELIM + value + PIPE_DELIM + name2 + NAME_VALUE_DELIM + value2;
        final Properties map = PropertyUtils.splitPropertiesOnPipe(property);
        Assertions.assertEquals(2, map.size());
        Assertions.assertEquals(value, map.get(name));
        Assertions.assertEquals(value2, map.get(name2));
    }

    /**
     * Test single property.
     * @param name Property name.
     * @param value Property value.
     */
    private void testSingleProperty(String name, String value)
    {
        String property = name + NAME_VALUE_DELIM + value;
        final Properties map = PropertyUtils.splitPropertiesOnPipe(property);
        Assertions.assertEquals(1, map.size());
        Assertions.assertEquals(value, map.get(name));
    }

    /**
     * Test split and join of properties (using semicolon).
     */
    @Test
    public void testSplitAndJoinMultiplePropertiesOnSemicolon()
    {
        Properties inner = PropertyUtils.splitPropertiesOnDelimiter(ONE_SEMI_TWO, ';');
        Assertions.assertEquals("hello", inner.getProperty("inner-1"));
        Assertions.assertEquals("goodbye", inner.getProperty("inner-2"));
        Assertions.assertEquals(2, inner.size());

        try
        {
            Assertions.assertEquals(
                ONE_SEMI_TWO, PropertyUtils.joinOnSemicolon(PropertyUtils.toMap(inner)));
        }
        catch (AssertionFailedError e)
        {
            Assertions.assertEquals(
                TWO_SEMI_ONE, PropertyUtils.joinOnSemicolon(PropertyUtils.toMap(inner)));
        }
    }

    /**
     * Test split and join of properties (using dot).
     */
    @Test
    public void testSplitAndJoinMultiplePropertiesOnDot()
    {
        Properties inner = PropertyUtils.splitPropertiesOnDelimiter(ONE_DOT_TWO, '.');
        Assertions.assertEquals("hello", inner.getProperty("inner-1"));
        Assertions.assertEquals("goodbye", inner.getProperty("inner-2"));
        Assertions.assertEquals(2, inner.size());

        try
        {
            Assertions.assertEquals(
                ONE_DOT_TWO, PropertyUtils.joinOnDelimiter(PropertyUtils.toMap(inner), '.'));
        }
        catch (AssertionFailedError e)
        {
            Assertions.assertEquals(
                TWO_DOT_ONE, PropertyUtils.joinOnDelimiter(PropertyUtils.toMap(inner), '.'));
        }
    }

    /**
     * Test split and join of properties (using pipe).
     */
    @Test
    public void testSplitAndJoinMultiplePropertiesOnPipe()
    {
        Properties inner = PropertyUtils.splitPropertiesOnPipe(ONE_PIPE_TWO);
        Assertions.assertEquals("hello", inner.getProperty("inner-1"));
        Assertions.assertEquals("goodbye", inner.getProperty("inner-2"));
        Assertions.assertEquals(2, inner.size());

        try
        {
            Assertions.assertEquals(
                ONE_PIPE_TWO, PropertyUtils.joinOnPipe(PropertyUtils.toMap(inner)));
        }
        catch (AssertionFailedError e)
        {
            Assertions.assertEquals(
                TWO_PIPE_ONE, PropertyUtils.joinOnPipe(PropertyUtils.toMap(inner)));
        }
    }

    /**
     * Test split and join of nested properties (using pipe).
     */
    @Test
    public void testSplitAndJoinSemicolonNestedInPipeProperties()
    {
        Properties outer = PropertyUtils.splitPropertiesOnPipe(ONE_THEN_TWO_OUTER_A);
        Assertions.assertEquals(ONE_SEMI_TWO, outer.getProperty("outer-b"));
        Assertions.assertEquals(2, outer.size());

        // We have 4 possibilities for the merging of properties, and the order depends on the JVM.
        // Try them all.

        try
        {
            Assertions.assertEquals(ONE_THEN_TWO_OUTER_A, PropertyUtils.joinOnPipe(
                PropertyUtils.toMap(outer)));
            return;
        }
        catch (AssertionFailedError deviation1)
        {
            // that's ok, we'll try another order
        }

        try
        {
            Assertions.assertEquals(TWO_THEN_ONE_OUTER_A, PropertyUtils.joinOnPipe(
                PropertyUtils.toMap(outer)));
            return;
        }
        catch (AssertionFailedError deviation2)
        {
            // that's ok, we'll try another order
        }

        try
        {
            Assertions.assertEquals(ONE_THEN_TWO_OUTER_B, PropertyUtils.joinOnPipe(
                PropertyUtils.toMap(outer)));
            return;
        }
        catch (AssertionFailedError deviation3)
        {
            // that's ok, we'll try another order
        }

        try
        {
            Assertions.assertEquals(TWO_THEN_ONE_OUTER_B, PropertyUtils
                .joinOnPipe(PropertyUtils.toMap(outer)));
            return;
        }
        catch (AssertionFailedError deviation4)
        {
            // that's ok, we'll try another order
        }

        // this is the last combination
        Assertions.assertEquals(
            TWO_THEN_ONE_OUTER_A, PropertyUtils.joinOnPipe(PropertyUtils.toMap(outer)));
    }

    /**
     * Test whether Windows backslash escaping works properly.
     */
    @Test
    public void testCanEscapeWindowsSlashes()
    {
        Assertions.assertEquals("c:\\\\test", PropertyUtils.escapeBackSlashesIfNotNull("c:\\test"));
    }

    /**
     * Test split of escaped semicolons.
     */
    @Test
    public void testSplitEscapedSemicolons()
    {
        Properties inner = PropertyUtils.splitPropertiesOnSemicolon(
            PropertyUtils.escapeBackSlashesIfNotNull(
                "foo=bar;baz=blorple\\;zot;windows=c:\\test;glorg=gluux"));

        Assertions.assertEquals(4, inner.size());
        Assertions.assertEquals("bar", inner.getProperty("foo"));
        Assertions.assertEquals("blorple;zot", inner.getProperty("baz"));
        Assertions.assertEquals("gluux", inner.getProperty("glorg"));
        Assertions.assertEquals("c:\\test", inner.getProperty("windows"));
    }

    /**
     * Test join of escaped semicolons.
     */
    @Test
    public void testJoinEscapesSemicolons()
    {
        Properties inner = new Properties();
        inner.setProperty("baz", "blorple;zot");

        Assertions.assertEquals("baz=blorple\\;zot", PropertyUtils.joinOnSemicolon(
                PropertyUtils.toMap(inner)));
    }
}
