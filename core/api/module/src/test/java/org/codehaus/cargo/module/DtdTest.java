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
package org.codehaus.cargo.module;

import java.util.List;

import junit.framework.TestCase;

/**
 * Unit tests for {@link Dtd}.
 * 
 * @version $Id$
 */
public class DtdTest extends TestCase
{
    /**
     * Test that we can handle "zero or more" type.
     * @throws Exception If anything goes wrong.
     */
    public void testCanHandleZeroOrMore() throws Exception
    {
        Dtd dtd = new Dtd("http://java.sun.com/dtd/web-app_2_3.dtd");
        List<DescriptorTag> elementOrder = dtd.getElementOrder("web-resource-collection");
        assertEquals(4, elementOrder.size());
        DescriptorTag tag = elementOrder.get(0);
        assertEquals("web-resource-name", tag.getTagName());
        assertFalse(tag.isMultipleAllowed());
        tag = elementOrder.get(1);
        assertEquals("description", tag.getTagName());
        assertFalse(tag.isMultipleAllowed());
        tag = elementOrder.get(2);
        assertEquals("url-pattern", tag.getTagName());
        assertTrue(tag.isMultipleAllowed());
        tag = elementOrder.get(3);
        assertEquals("http-method", tag.getTagName());
        assertTrue(tag.isMultipleAllowed());
    }

    /**
     * Test that we can handle "one or more" type.
     * @throws Exception If anything goes wrong.
     */
    public void testCanHandleOneOrMore() throws Exception
    {
        Dtd dtd = new Dtd("http://java.sun.com/dtd/web-app_2_3.dtd");
        List<DescriptorTag> elementOrder = dtd.getElementOrder("security-constraint");
        assertEquals(4, elementOrder.size());
        DescriptorTag tag = elementOrder.get(1);
        assertEquals("web-resource-collection", tag.getTagName());
        assertTrue(tag.isMultipleAllowed());
    }

    /**
     * Test that we can handle "or" type.
     * @throws Exception If anything goes wrong.
     */
    public void testCanHandleOr() throws Exception
    {
        Dtd dtd = new Dtd("http://java.sun.com/dtd/web-app_2_3.dtd");
        List<DescriptorTag> elementOrder = dtd.getElementOrder("error-page");
        assertEquals(3, elementOrder.size());
        DescriptorTag tag = elementOrder.get(0);
        assertEquals("error-code", tag.getTagName());
        tag = elementOrder.get(1);
        assertEquals("exception-type", tag.getTagName());
        tag = elementOrder.get(2);
        assertEquals("location", tag.getTagName());
    }
}
