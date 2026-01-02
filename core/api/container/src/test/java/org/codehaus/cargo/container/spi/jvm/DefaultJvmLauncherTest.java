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
package org.codehaus.cargo.container.spi.jvm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.util.CargoException;

/**
 * Unit tests for {@link DefaultJvmLauncher}.
 */
public class DefaultJvmLauncherTest
{
    /**
     * Test {@link DefaultJvmLauncher#translateCommandline(String)}.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testTranslateCommandline() throws Exception
    {
        String[] s = DefaultJvmLauncher.translateCommandline("1 2 3");
        Assertions.assertEquals(3, s.length, "Simple case");
        for (int i = 0; i < 3; i++)
        {
            Assertions.assertEquals("" + (i + 1), s[i]);
        }

        s = DefaultJvmLauncher.translateCommandline("");
        Assertions.assertEquals(0, s.length, "empty string");

        s = DefaultJvmLauncher.translateCommandline(null);
        Assertions.assertEquals(0, s.length, "null");

        s = DefaultJvmLauncher.translateCommandline("1 '2' 3");
        Assertions.assertEquals(3, s.length, "Simple case with single quotes");
        Assertions.assertEquals("2", s[1], "Single quotes have been stripped");

        s = DefaultJvmLauncher.translateCommandline("1 \"2\" 3");
        Assertions.assertEquals(3, s.length, "Simple case with double quotes");
        Assertions.assertEquals("2", s[1], "Double quotes have been stripped");

        s = DefaultJvmLauncher.translateCommandline("1 \"2 3\" 4");
        Assertions.assertEquals(3, s.length, "Case with double quotes and whitespace");
        Assertions.assertEquals("2 3", s[1], "Double quotes stripped, space included");

        s = DefaultJvmLauncher.translateCommandline("1 \"2'3\" 4");
        Assertions.assertEquals(3, s.length, "Case with double quotes around single quote");
        Assertions.assertEquals("2'3", s[1], "Double quotes stripped, single quote included");

        s = DefaultJvmLauncher.translateCommandline("1 '2 3' 4");
        Assertions.assertEquals(3, s.length, "Case with single quotes and whitespace");
        Assertions.assertEquals("2 3", s[1], "Single quotes stripped, space included");

        s = DefaultJvmLauncher.translateCommandline("1 '2\"3' 4");
        Assertions.assertEquals(3, s.length, "Case with single quotes around double quote");
        Assertions.assertEquals("2\"3", s[1], "Single quotes stripped, double quote included");

        // \ doesn't have a special meaning anymore - this is different from
        // what the Unix sh does but causes a lot of problems on DOS
        // based platforms otherwise
        s = DefaultJvmLauncher.translateCommandline("1 2\\ 3 4");
        Assertions.assertEquals(4, s.length, "case with quoted whitespace");
        Assertions.assertEquals("2\\", s[1], "backslash included");

        // "" should become a single empty argument, same for ''
        s = DefaultJvmLauncher.translateCommandline("\"\" a");
        Assertions.assertEquals(2, s.length, "Doublequoted null arg prepend");
        Assertions.assertEquals("", s[0], "Doublequoted null arg prepend");
        Assertions.assertEquals("a", s[1], "Doublequoted null arg prepend");

        s = DefaultJvmLauncher.translateCommandline("a \"\"");
        Assertions.assertEquals(2, s.length, "Doublequoted null arg append");
        Assertions.assertEquals("a", s[0], "Doublequoted null arg append");
        Assertions.assertEquals("", s[1], "Doublequoted null arg append");

        s = DefaultJvmLauncher.translateCommandline("\"\"");
        Assertions.assertEquals(1, s.length, "Doublequoted null arg");
        Assertions.assertEquals("", s[0], "Doublequoted null arg");

        s = DefaultJvmLauncher.translateCommandline("'' a");
        Assertions.assertEquals(2, s.length, "Singlequoted null arg prepend");
        Assertions.assertEquals("", s[0], "Singlequoted null arg prepend");
        Assertions.assertEquals("a", s[1], "Singlequoted null arg prepend");

        s = DefaultJvmLauncher.translateCommandline("a ''");
        Assertions.assertEquals(2, s.length, "Singlequoted null arg append");
        Assertions.assertEquals("a", s[0], "Singlequoted null arg append");
        Assertions.assertEquals("", s[1], "Singlequoted null arg append");

        s = DefaultJvmLauncher.translateCommandline("''");
        Assertions.assertEquals(1, s.length, "Singlequoted null arg");
        Assertions.assertEquals("", s[0], "Singlequoted null arg");

        try
        {
            DefaultJvmLauncher.translateCommandline("a 'b c");
            Assertions.fail("No exception thrown for unbalanced single quotes");
        }
        catch (CargoException e)
        {
            Assertions.assertEquals(
                "unbalanced quotes in a 'b c", e.getMessage(), "Wrong exception detail");
        }

        try
        {
            DefaultJvmLauncher.translateCommandline("a \"b c");
            Assertions.fail("No exception thrown for unbalanced double quotes");
        }
        catch (CargoException e)
        {
            Assertions.assertEquals(
                "unbalanced quotes in a \"b c", e.getMessage(), "Wrong exception detail");
        }
    }
}
