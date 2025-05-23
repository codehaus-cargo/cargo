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
package org.codehaus.cargo.container.websphere.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for JvmArguments class.
 */
public final class JvmArgumentsTest
{
    /**
     * Test parsing of java arguments.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testParseArguments() throws Exception
    {
        String toBeParsed = "-Djava.compiler=NONE -Xms150m -Xmx2g";

        JvmArguments parsedArguments = JvmArguments.parseArguments(toBeParsed);
        Assertions.assertEquals(150L, parsedArguments.getInitialHeap(ByteUnit.MEGABYTES));
        Assertions.assertEquals(2048L, parsedArguments.getMaxHeap(ByteUnit.MEGABYTES));
        Assertions.assertEquals("-Djava.compiler=NONE", parsedArguments.getGenericArgs());
    }
}
