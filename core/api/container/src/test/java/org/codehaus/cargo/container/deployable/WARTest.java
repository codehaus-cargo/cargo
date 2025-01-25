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
package org.codehaus.cargo.container.deployable;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link WAR}.
 */
public class WARTest
{
    /**
     * Test context when WAR has an extension.
     */
    @Test
    public void testGetContextWhenWarHasExtension()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        Assertions.assertEquals("test", war.getContext());
    }

    /**
     * Test context when WAR has no extension.
     */
    @Test
    public void testGetContextWhenWarHasNoExtension()
    {
        WAR war = new WAR("/some/path/to/war/test");
        Assertions.assertEquals("test", war.getContext());
    }

    /**
     * Test overriden WAR context.
     */
    @Test
    public void testGetContextWhenOverride()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        war.setContext("context");
        Assertions.assertEquals("context", war.getContext());
    }

    /**
     * Test WAR context overriden with a leading slash.
     */
    @Test
    public void testGetContextWhenOverrideAndLeadingSlash()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        war.setContext("/");
        Assertions.assertEquals("", war.getContext());
    }

    /**
     * Test WAR context overriden with an ending slash.
     */
    @Test
    public void testGetContextWhenOverrideAndEndingSlash()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        war.setContext("/a/");
        Assertions.assertEquals("a", war.getContext());
    }

    /**
     * Test WAR context overriden with a slash in the middle.
     */
    @Test
    public void testGetContextWhenOverrideAndMiddleSlash()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        war.setContext("/a/b");
        Assertions.assertEquals("a/b", war.getContext());
    }

    /**
     * Test logger when getting WAR context.
     */
    @Test
    public void testLoggerWhenCallingGetContext()
    {
        MockLogger logger = new MockLogger();
        WAR war = new WAR("c:/test.war");
        war.setLogger(logger);

        // Calling getContext just to trigger the log
        war.getContext();

        Assertions.assertEquals(1, logger.severities.size());
        Assertions.assertEquals("debug", logger.severities.get(0));
        Assertions.assertEquals("Parsed web context = [test]", logger.messages.get(0));
        Assertions.assertEquals(
            "org.codehaus.cargo.container.deployable.WAR", logger.categories.get(0));
    }

    /**
     * Test name when WAR has an extension.
     */
    @Test
    public void testGetNameWhenWarHasExtension()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        Assertions.assertEquals("test", war.getName());
    }

    /**
     * Test name when WAR has no extension.
     */
    @Test
    public void testGetNameWhenWarHasNoExtension()
    {
        WAR war = new WAR("/some/path/to/war/test");
        Assertions.assertEquals("test", war.getName());
    }

    /**
     * Test name when WAR context is overriden.
     */
    @Test
    public void testGetNameWhenOverride()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        war.setContext("context");
        Assertions.assertEquals("context", war.getName());
    }

    /**
     * Test file name when WAR context is overriden.
     */
    @Test
    public void testGetFilenameWhenOverride()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        war.setContext("// this/../../is/../a//test/");
        Assertions.assertEquals("this/is/a/test.war", war.getFilename());
    }
}
