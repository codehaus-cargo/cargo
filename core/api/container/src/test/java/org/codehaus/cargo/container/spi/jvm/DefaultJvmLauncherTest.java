/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import org.codehaus.cargo.util.CargoException;

import junit.framework.TestCase;

/**
 * Unit tests for {@link DefaultJvmLauncher}.
 */
public class DefaultJvmLauncherTest extends TestCase
{
    /**
     * Test {@link DefaultJvmLauncher#translateCommandline(String)}.
     * @throws Exception If anything goes wrong.
     */
    public void testTranslateCommandline() throws Exception
    {
        String[] s = DefaultJvmLauncher.translateCommandline("1 2 3");
        assertEquals("Simple case", 3, s.length);
        for (int i = 0; i < 3; i++)
        {
            assertEquals("" + (i + 1), s[i]);
        }

        s = DefaultJvmLauncher.translateCommandline("");
        assertEquals("empty string", 0, s.length);

        s = DefaultJvmLauncher.translateCommandline(null);
        assertEquals("null", 0, s.length);

        s = DefaultJvmLauncher.translateCommandline("1 '2' 3");
        assertEquals("Simple case with single quotes", 3, s.length);
        assertEquals("Single quotes have been stripped", "2", s[1]);

        s = DefaultJvmLauncher.translateCommandline("1 \"2\" 3");
        assertEquals("Simple case with double quotes", 3, s.length);
        assertEquals("Double quotes have been stripped", "2", s[1]);

        s = DefaultJvmLauncher.translateCommandline("1 \"2 3\" 4");
        assertEquals("Case with double quotes and whitespace", 3, s.length);
        assertEquals("Double quotes stripped, space included", "2 3", s[1]);

        s = DefaultJvmLauncher.translateCommandline("1 \"2'3\" 4");
        assertEquals("Case with double quotes around single quote", 3, s.length);
        assertEquals("Double quotes stripped, single quote included", "2'3", s[1]);

        s = DefaultJvmLauncher.translateCommandline("1 '2 3' 4");
        assertEquals("Case with single quotes and whitespace", 3, s.length);
        assertEquals("Single quotes stripped, space included", "2 3", s[1]);

        s = DefaultJvmLauncher.translateCommandline("1 '2\"3' 4");
        assertEquals("Case with single quotes around double quote", 3, s.length);
        assertEquals("Single quotes stripped, double quote included", "2\"3", s[1]);

        // \ doesn't have a special meaning anymore - this is different from
        // what the Unix sh does but causes a lot of problems on DOS
        // based platforms otherwise
        s = DefaultJvmLauncher.translateCommandline("1 2\\ 3 4");
        assertEquals("case with quoted whitespace", 4, s.length);
        assertEquals("backslash included", "2\\", s[1]);

        // "" should become a single empty argument, same for ''
        s = DefaultJvmLauncher.translateCommandline("\"\" a");
        assertEquals("Doublequoted null arg prepend", 2, s.length);
        assertEquals("Doublequoted null arg prepend", "", s[0]);
        assertEquals("Doublequoted null arg prepend", "a", s[1]);
        s = DefaultJvmLauncher.translateCommandline("a \"\"");
        assertEquals("Doublequoted null arg append", 2, s.length);
        assertEquals("Doublequoted null arg append", "a", s[0]);
        assertEquals("Doublequoted null arg append", "", s[1]);
        s = DefaultJvmLauncher.translateCommandline("\"\"");
        assertEquals("Doublequoted null arg", 1, s.length);
        assertEquals("Doublequoted null arg", "", s[0]);

        s = DefaultJvmLauncher.translateCommandline("'' a");
        assertEquals("Singlequoted null arg prepend", 2, s.length);
        assertEquals("Singlequoted null arg prepend", "", s[0]);
        assertEquals("Singlequoted null arg prepend", "a", s[1]);
        s = DefaultJvmLauncher.translateCommandline("a ''");
        assertEquals("Singlequoted null arg append", 2, s.length);
        assertEquals("Singlequoted null arg append", "a", s[0]);
        assertEquals("Singlequoted null arg append", "", s[1]);
        s = DefaultJvmLauncher.translateCommandline("''");
        assertEquals("Singlequoted null arg", 1, s.length);
        assertEquals("Singlequoted null arg", "", s[0]);

        try
        {
            DefaultJvmLauncher.translateCommandline("a 'b c");
            fail("No exception thrown for unbalanced single quotes");
        }
        catch (CargoException e)
        {
            assertEquals("Wrong exception detail", "unbalanced quotes in a 'b c", e.getMessage());
        }

        try
        {
            DefaultJvmLauncher.translateCommandline("a \"b c");
            fail("No exception thrown for unbalanced double quotes");
        }
        catch (CargoException e)
        {
            assertEquals("Wrong exception detail", "unbalanced quotes in a \"b c", e.getMessage());
        }
    }
}
