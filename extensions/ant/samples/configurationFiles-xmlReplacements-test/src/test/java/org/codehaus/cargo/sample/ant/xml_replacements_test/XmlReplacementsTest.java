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
package org.codehaus.cargo.sample.ant.xml_replacements_test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;

public class XmlReplacementsTest extends TestCase
{

    public void testXmlReplacements() throws Exception
    {
        String lookFor = "test-xmlreplacement-attribute=\"test-xmlreplacement-value\"";
        File serverXml = new File("target/catalina-base/conf/server.xml");
        assertTrue(serverXml + " is not a file", serverXml.isFile());

        BufferedReader serverXmlReader = new BufferedReader(new FileReader(serverXml));
        for (String read = ""; read != null; read = serverXmlReader.readLine())
        {
            if (read.contains(lookFor))
            {
                return;
            }
        }

        fail("File " + serverXml + " does not contain: " + lookFor);
    }

}
