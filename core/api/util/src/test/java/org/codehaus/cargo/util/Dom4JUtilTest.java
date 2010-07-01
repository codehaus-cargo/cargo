/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
package org.codehaus.cargo.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import junit.framework.TestCase;

public class Dom4JUtilTest extends TestCase
{
    private Dom4JUtil util;

    private Element testElement;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        util = new Dom4JUtil();
        Document document = DocumentHelper.createDocument();
        this.testElement = document.addElement("test");
        document.setRootElement(testElement);
    }

    public void testUnparsableElementThrowsException()
    {
        String string = "asdasd";
        try
        {
            util.parseIntoElement(string);
        }
        catch (CargoException e)
        {
            assertEquals("Could not parse element: " + string, e.getMessage());
        }
    }

    public void testParsableElement() throws DocumentException
    {
        String string = "<element>dog</element>";
        assertEquals(string, util.parseIntoElement(string).asXML());
    }

    public void testNoElementThrowsException()
    {
        try
        {
            util.selectElementMatchingXPath("weblogic:app-deployment", testElement);

            fail("should have returned an exception");
        }
        catch (ElementNotFoundException e)
        {
            // this exception is good!
            assertEquals(testElement, e.getSearched());
            return;
        }
    }

}
