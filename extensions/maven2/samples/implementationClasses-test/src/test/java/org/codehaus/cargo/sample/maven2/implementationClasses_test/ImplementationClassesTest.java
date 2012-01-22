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
package org.codehaus.cargo.sample.maven2.implementationClasses_test;

import java.io.File;

import junit.framework.TestCase;

public class ImplementationClassesTest extends TestCase
{

    /**
     * List of file or folder names to ignore.
     */
    public static String[] IGNORED_FILENAME_PREFIXES = new String[]
    {
        "apidocs", "checkstyle", "javadoc", "maven-archiver", "site", "surefire", "test-classes"
    };

    /**
     * The implementation classes we set during the test are supposed to create no configuration
     * directory; so tests verify that nothing has been created.
     */
    public void testImplementationClasses() throws Exception
    {
        String artifactId = System.getProperty("artifactId");
        boolean foundWebapp = false;

        File target = new File(System.getProperty("target"));
        for (File content : target.listFiles())
        {
            String name = content.getName();
            if (name.startsWith(artifactId))
            {
                foundWebapp = true;
                continue;
            }

            boolean toBeIgnored = false;
            for (String ignoredFilenamePrefix : IGNORED_FILENAME_PREFIXES)
            {
                if (name.startsWith(ignoredFilenamePrefix))
                {
                    toBeIgnored = true;
                    break;
                }
            }
            if (!toBeIgnored)
            {
                fail("Found unexpected file: " + content);
            }
        }
        assertTrue("Cannot find " + artifactId, foundWebapp);
    }

}
