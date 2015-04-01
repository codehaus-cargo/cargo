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
package org.codehaus.cargo.container.jetty;

import junit.framework.TestCase;
import org.codehaus.cargo.container.Container;

/**
 * Unit tests for {@link Jetty9xInstalledLocalContainer}.
 * 
 */
public class Jetty9xInstalledLocalContainerTest extends TestCase
{
    /**
     * Test the <code>getName</code> method.
     * @throws Exception If anything goes wrong.
     */
    public void testGetName() throws Exception
    {
        Container c = new Jetty9xInstalledLocalContainer(null);
        String name = c.getName();
        assertEquals("Jetty 9.x", name);
    }
}
