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
 * Unit tests for {@link DeployableType}.
 */
public class DeployableTypeTest
{
    /**
     * Test equality.
     */
    @Test
    public void testEquality()
    {
        DeployableType type = DeployableType.toType("war");
        Assertions.assertEquals(DeployableType.WAR, type);
        Assertions.assertSame(DeployableType.WAR, type);
    }

    /**
     * Test difference.
     */
    @Test
    public void testDifference()
    {
        DeployableType type = DeployableType.toType("war");
        Assertions.assertTrue(type != DeployableType.EAR);
        Assertions.assertNotEquals(DeployableType.EAR, type);
    }

    /**
     * Test equality between WAR deployables.
     */
    @Test
    public void testWARTypeEquality()
    {
        WAR war1 = new WAR("/some/path/to/file.war");
        WAR war2 = new WAR("/otherfile.war");
        Assertions.assertEquals(war1.getType(), war2.getType());
    }

    /**
     * Test equality between EAR deployables.
     */
    @Test
    public void testEARTypeEquality()
    {
        EAR ear1 = new EAR("/some/path/to/file.ear");
        EAR ear2 = new EAR("/otherfile.ear");
        Assertions.assertEquals(ear1.getType(), ear2.getType());
    }

    /**
     * Test difference between WAR and EAR deployables.
     */
    @Test
    public void testWARAndEARDifference()
    {
        WAR war = new WAR("/some/path/to/file.war");
        EAR ear = new EAR("/file.ear");
        Assertions.assertNotEquals(war.getType(), ear.getType());
        Assertions.assertTrue(war.getType() != ear.getType());
    }
}
