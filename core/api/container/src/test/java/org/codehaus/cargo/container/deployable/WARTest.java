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
package org.codehaus.cargo.container.deployable;

import junit.framework.TestCase;

/**
 * Unit tests for {@link WAR}.
 * 
 * @version $Id$
 */
public class WARTest extends TestCase
{
    public void testGetContextWhenWarHasExtension()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        assertEquals("test", war.getContext());
    }

    public void testGetContextWhenWarHasNoExtension()
    {
        WAR war = new WAR("/some/path/to/war/test");
        assertEquals("test", war.getContext());
    }

    public void testGetContextWhenOverride()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        war.setContext("context");
        assertEquals("context", war.getContext());
    }

    public void testGetContextWhenOverrideAndLeadingSlash()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        war.setContext("/");
        assertEquals("", war.getContext());
    }

    public void testGetContextWhenOverrideAndMiddleSlash()
    {
        WAR war = new WAR("c:/some/path/to/war/test.war");
        war.setContext("/a/b");
        assertEquals("a/b", war.getContext());
    }

    public void testLoggerWhenCallingGetContext()
    {
        MockLogger logger = new MockLogger();
        WAR war = new WAR("c:/test.war");
        war.setLogger(logger);

        // Calling getContext just to trigger the log
        war.getContext();

        assertEquals(1, logger.severities.size());
        assertEquals("debug", logger.severities.get(0));
        assertEquals("Parsed web context = [test]", logger.messages.get(0));
        assertEquals("org.codehaus.cargo.container.deployable.WAR", logger.categories.get(0));
    }
}
