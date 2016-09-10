/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2016 Ali Tokmen.
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for {@link Dom4JUtil}.
 * 
 */
public class Dom4JUtilTest extends TestCase
{
    /**
     * Dom4j utilities.
     */
    private Dom4JUtil util;

    /**
     * XML test element.
     */
    private Element testElement;

    /**
     * Creates the various XML test elements. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        util = new Dom4JUtil();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document document = builder.newDocument();
        this.testElement = document.createElement("test");
        document.appendChild(this.testElement);
    }

    /**
     * Test that random content doesn't parse.
     */
    public void testUnparsableElementThrowsException()
    {
        String string = "asdasd";
        try
        {
            util.parseIntoElement(string);
            fail("should have thrown an exception");
        }
        catch (CargoException e)
        {
            assertEquals("Could not parse element: " + string, e.getMessage());
        }
    }

    /**
     * Test simple element parse.
     * @throws Exception If anything does wrong.
     */
    public void testParsableElement() throws Exception
    {
        String string = "<element>dog</element>";
        Element element = util.parseIntoElement(string);
        assertEquals("element", element.getNodeName());
        assertEquals("dog", element.getTextContent());
    }

    /**
     * Test that search for a non-existing element throws an exception.
     */
    public void testNoElementThrowsException()
    {
        try
        {
            util.selectElementMatchingXPath("weblogic:app-deployment", testElement);
            fail("should have thrown an exception");
        }
        catch (ElementNotFoundException e)
        {
            assertEquals(testElement, e.getSearched());
        }
    }

}
