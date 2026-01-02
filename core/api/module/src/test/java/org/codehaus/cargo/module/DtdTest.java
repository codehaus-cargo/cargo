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
package org.codehaus.cargo.module;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Dtd}.
 */
public class DtdTest
{
    /**
     * Test that we can handle "zero or more" type.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testCanHandleZeroOrMore() throws Exception
    {
        Dtd dtd = new Dtd("http://java.sun.com/dtd/web-app_2_3.dtd");
        List<DescriptorTag> elementOrder = dtd.getElementOrder("web-resource-collection");
        Assertions.assertEquals(4, elementOrder.size());
        DescriptorTag tag = elementOrder.get(0);
        Assertions.assertEquals("web-resource-name", tag.getTagName());
        Assertions.assertFalse(tag.isMultipleAllowed());
        tag = elementOrder.get(1);
        Assertions.assertEquals("description", tag.getTagName());
        Assertions.assertFalse(tag.isMultipleAllowed());
        tag = elementOrder.get(2);
        Assertions.assertEquals("url-pattern", tag.getTagName());
        Assertions.assertTrue(tag.isMultipleAllowed());
        tag = elementOrder.get(3);
        Assertions.assertEquals("http-method", tag.getTagName());
        Assertions.assertTrue(tag.isMultipleAllowed());
    }

    /**
     * Test that we can handle "one or more" type.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testCanHandleOneOrMore() throws Exception
    {
        Dtd dtd = new Dtd("http://java.sun.com/dtd/web-app_2_3.dtd");
        List<DescriptorTag> elementOrder = dtd.getElementOrder("security-constraint");
        Assertions.assertEquals(4, elementOrder.size());
        DescriptorTag tag = elementOrder.get(1);
        Assertions.assertEquals("web-resource-collection", tag.getTagName());
        Assertions.assertTrue(tag.isMultipleAllowed());
    }

    /**
     * Test that we can handle "or" type.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testCanHandleOr() throws Exception
    {
        Dtd dtd = new Dtd("http://java.sun.com/dtd/web-app_2_3.dtd");
        List<DescriptorTag> elementOrder = dtd.getElementOrder("error-page");
        Assertions.assertEquals(3, elementOrder.size());
        DescriptorTag tag = elementOrder.get(0);
        Assertions.assertEquals("error-code", tag.getTagName());
        tag = elementOrder.get(1);
        Assertions.assertEquals("exception-type", tag.getTagName());
        tag = elementOrder.get(2);
        Assertions.assertEquals("location", tag.getTagName());
    }
}
