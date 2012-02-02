/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
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
package org.codehaus.cargo.module.webapp.merge;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlUtils;

/**
 * Unit tests for {@link WebXmlMerger}.
 * 
 * @version $Id$
 */
public final class WebXmlFilterMergerTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests whether a single filter is correctly merged into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";

        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);

        // WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(WebXmlUtils.hasFilter(srcWebXml, "f1"));
    }

    /**
     * Tests whether a single filter is correctly merged into a descriptor that already contains
     * another filter.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoDocumentWithAnotherFilter() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f2</filter-name>"
            + "    <filter-class>fclass2</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(WebXmlUtils.hasFilter(srcWebXml, "f1"));
        assertTrue(WebXmlUtils.hasFilter(srcWebXml, "f2"));
    }

    /**
     * Tests whether a single filter in the merge descriptor is ignored because a filter with the
     * same name already exists in the source descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoDocumentWithSameFilter() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        WebXml mergeWebXml = WebXmlIo
            .parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(WebXmlUtils.hasFilter(srcWebXml, "f1"));
    }

    /**
     * Tests whether a single filter is correctly merged into a descriptor that already contains
     * multiple other filter definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoDocumentWithMultipleFilters() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f2</filter-name>"
            + "    <filter-class>fclass2</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f3</filter-name>"
            + "    <filter-class>fclass3</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f4</filter-name>"
            + "    <filter-class>fclass4</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        List<String> filterNames = WebXmlUtils.getFilterNames(srcWebXml);
        assertEquals(4, filterNames.size());
        assertEquals("f1", filterNames.get(0));
        assertEquals("f2", filterNames.get(1));
        assertEquals("f3", filterNames.get(2));
        assertEquals("f4", filterNames.get(3));
    }

    /**
     * Tests whether multiple filters are correctly merged into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeMultipleFiltersIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f2</filter-name>"
            + "    <filter-class>fclass2</filter-class>"
            + "  </filter>"
            + "  <filter>"
            + "    <filter-name>f3</filter-name>"
            + "    <filter-class>fclass3</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        List<String> filterNames = WebXmlUtils.getFilterNames(srcWebXml);
        assertEquals(3, filterNames.size());
        assertEquals("f1", filterNames.get(0));
        assertEquals("f2", filterNames.get(1));
        assertEquals("f3", filterNames.get(2));
    }

    /**
     * Tests whether a filter with one mapping is correctly merged into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterWithOneMappingIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping1</url-pattern>"
            + "  </filter-mapping>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(WebXmlUtils.hasFilter(srcWebXml, "f1"));
        List<String> filterMappings = WebXmlUtils.getFilterMappings(srcWebXml, "f1");
        assertEquals(1, filterMappings.size());
        assertEquals("/f1mapping1", filterMappings.get(0));
    }

    /**
     * Tests whether a single filter with multiple mappings is correctly merged into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterWithMultipleMappingsIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping1</url-pattern>"
            + "  </filter-mapping>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping2</url-pattern>"
            + "  </filter-mapping>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping3</url-pattern>"
            + "  </filter-mapping>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(WebXmlUtils.hasFilter(srcWebXml, "f1"));
        List<String> filterMappings = WebXmlUtils.getFilterMappings(srcWebXml, "f1");
        assertEquals(3, filterMappings.size());
        assertEquals("/f1mapping1", filterMappings.get(0));
        assertEquals("/f1mapping2", filterMappings.get(1));
        assertEquals("/f1mapping3", filterMappings.get(2));
    }

    /**
     * Tests whether the same filter in two different files is mapped correctly (i.e., once).
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeSameFilterInTwoDocuments() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping1</url-pattern>"
            + "  </filter-mapping>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "  <filter-mapping>"
            + "    <filter-name>f1</filter-name>"
            + "    <url-pattern>/f1mapping1</url-pattern>"
            + "  </filter-mapping>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(WebXmlUtils.hasFilter(srcWebXml, "f1"));
        List<String> filterMappings = WebXmlUtils.getFilterMappings(srcWebXml, "f1");
        assertEquals(1, filterMappings.size());
        assertEquals("/f1mapping1", filterMappings.get(0));
    }

    /**
     * Tests whether a filter initialization parameter is merged into the descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoDocumentWithSameFilterAndParam() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "    <init-param>"
            + "      <param-name>f1param1</param-name>"
            + "      <param-value>f1param1value</param-value>"
            + "    </init-param>"
            + "  </filter>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(WebXmlUtils.hasFilter(srcWebXml, "f1"));
        List<String> initParams = WebXmlUtils.getFilterInitParamNames(srcWebXml, "f1");
        assertEquals(1, initParams.size());
        assertEquals("f1param1", initParams.get(0));
    }

}
