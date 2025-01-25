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
 * Unit tests for {@link EAR}.
 */
public class EARTest
{
    /**
     * Test name when EAR has an extension.
     */
    @Test
    public void testGetNameWhenEarHasExtension()
    {
        EAR ear = new EAR("c:/some/path/to/ear/test.ear");
        Assertions.assertEquals("test", ear.getName());
    }

    /**
     * Test name when EAR has no extension.
     */
    @Test
    public void testGetNameWhenEarHasNoExtension()
    {
        EAR ear = new EAR("/some/path/to/ear/test");
        Assertions.assertEquals("test", ear.getName());
    }

    /**
     * Test name when EAR name is set.
     */
    @Test
    public void testGetNameWhenOverride()
    {
        EAR ear = new EAR("c:/some/path/to/ear/test.ear");
        ear.setName("name");
        Assertions.assertEquals("name", ear.getName());
    }
}
