/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.codehaus.cargo.container.Container;

/**
 * Unit tests for {@link Jetty8xInstalledLocalContainer}.
 */
public class Jetty8xInstalledLocalContainerTest
{
    /**
     * Test the <code>getName</code> method.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testGetName() throws Exception
    {
        Container c = new Jetty8xInstalledLocalContainer(null);
        String name = c.getName();
        Assertions.assertEquals("Jetty 8.x", name);
    }
}
