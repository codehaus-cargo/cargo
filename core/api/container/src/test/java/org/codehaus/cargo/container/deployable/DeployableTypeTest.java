/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
 * Unit tests for {@link DeployableType}.
 */
public class DeployableTypeTest extends TestCase
{
    /**
     * Test equality.
     */
    public void testEquality()
    {
        DeployableType type = DeployableType.toType("war");
        assertEquals(DeployableType.WAR, type);
        assertSame(DeployableType.WAR, type);
    }

    /**
     * Test difference.
     */
    public void testDifference()
    {
        DeployableType type = DeployableType.toType("war");
        assertTrue(type != DeployableType.EAR);
        assertNotSame(DeployableType.EAR, type);
    }

    /**
     * Test equality between WAR deployables.
     */
    public void testWARTypeEquality()
    {
        WAR war1 = new WAR("/some/path/to/file.war");
        WAR war2 = new WAR("/otherfile.war");
        assertEquals(war1.getType(), war2.getType());
    }

    /**
     * Test equality between EAR deployables.
     */
    public void testEARTypeEquality()
    {
        EAR ear1 = new EAR("/some/path/to/file.ear");
        EAR ear2 = new EAR("/otherfile.ear");
        assertEquals(ear1.getType(), ear2.getType());
    }

    /**
     * Test difference between WAR and EAR deployables.
     */
    public void testWARAndEARDifference()
    {
        WAR war = new WAR("/some/path/to/file.war");
        EAR ear = new EAR("/file.ear");
        assertNotSame(war.getType(), ear.getType());
        assertTrue(war.getType() != ear.getType());
    }
}
