/* 
 * ========================================================================
 * 
 * Copyright 2003 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004 Vincent Massol.
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
package org.codehaus.cargo.module.webapp;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

/**
 * Unit tests for {@link WebXmlMerger}.
 * 
 * TODO: we need more tests for the security sections and the various references
 * 
 * @version $Id$
 */
public final class WebXmlMergerTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests whether a single filter is correctly merged into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
    }

    /**
     * Tests whether a single context param is correctly merged into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneContextParamIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param</param-name>"
            + "    <param-value>value</param-value>"
            + "  </context-param>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeContextParams(mergeWebXml);
        assertTrue(srcWebXml.hasContextParam("param"));
    }
    
    /**
     * Tests whether a single filter is correctly merged into a descriptor that
     * already contains another filter.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoDocumentWithAnotherFilter()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f2</filter-name>"
            + "    <filter-class>fclass2</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
        assertTrue(srcWebXml.hasFilter("f2"));
    }

    /**
     * Tests whether a single context param is correctly merged into a 
     * descriptor that already contains another context param.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneContextParamIntoDocumentWithAnotherContextParam()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param1</param-name>"
            + "    <param-value>value1</param-value>"
            + "  </context-param>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param2</param-name>"
            + "    <param-value>value2</param-value>"
            + "  </context-param>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeContextParams(mergeWebXml);
        assertTrue(srcWebXml.hasContextParam("param1"));
        assertTrue(srcWebXml.hasContextParam("param2"));
    }

    /**
     * Tests whether a single filter in the merge descriptor is ignored because
     * a filter with the same name already exists in the source descriptor. 
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoDocumentWithSameFilter()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
    }

    /**
     * Tests whether a single context param in the merge descriptor is ignored 
     * because a context param with the same name already exists in the source 
     * descriptor. 
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneContextParamIntoDocumentWithSameContextParam()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param</param-name>"
            + "    <param-value>value</param-value>"
            + "  </context-param>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeContextParams(mergeWebXml);
        assertTrue(srcWebXml.hasContextParam("param"));
    }
    
    /**
     * Tests whether a filter initialization parameter is merged into the
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoDocumentWithSameFilterAndParam()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f1</filter-name>"
            + "    <filter-class>fclass1</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
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
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
        Iterator initParams = srcWebXml.getFilterInitParamNames("f1");
        assertEquals("f1param1", initParams.next());
        assertTrue(!initParams.hasNext());
    }

    /**
     * Tests whether a single filter is correctly merged into a descriptor that
     * already contains multiple other filter definitions.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterIntoDocumentWithMultipleFilters()
        throws Exception
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
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <filter>"
            + "    <filter-name>f4</filter-name>"
            + "    <filter-class>fclass4</filter-class>"
            + "  </filter>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        Iterator filterNames = srcWebXml.getFilterNames();
        assertEquals("f1", filterNames.next());
        assertEquals("f2", filterNames.next());
        assertEquals("f3", filterNames.next());
        assertEquals("f4", filterNames.next());
        assertTrue(!filterNames.hasNext());
    }

    /**
     * Tests whether multiple filters are correctly merged into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeMultipleFiltersIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
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
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        Iterator filterNames = srcWebXml.getFilterNames();
        assertEquals("f1", filterNames.next());
        assertEquals("f2", filterNames.next());
        assertEquals("f3", filterNames.next());
        assertTrue(!filterNames.hasNext());
    }

    /**
     * Tests whether a filter with one mapping is correctly merged into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterWithOneMappingIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
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
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
        Iterator filterMappings = srcWebXml.getFilterMappings("f1");
        assertEquals("/f1mapping1", filterMappings.next());
        assertTrue(!filterMappings.hasNext());
    }

    /**
     * Tests wether a single filter with multiple mappings is correctly merged
     * into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneFilterWithMultipleMappingsIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
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
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeFilters(mergeWebXml);
        assertTrue(srcWebXml.hasFilter("f1"));
        Iterator filterMappings = srcWebXml.getFilterMappings("f1");
        assertEquals("/f1mapping1", filterMappings.next());
        assertEquals("/f1mapping2", filterMappings.next());
        assertEquals("/f1mapping3", filterMappings.next());
        assertTrue(!filterMappings.hasNext());
    }

    /**
     * Tests whether a single servlet is correctly merged into an empty 
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
    }

    /**
     * Tests whether a single servlet is correctly merged into a descriptor that
     * already contains the definition of an other servlet.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoDocumentWithAnotherServlet()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s2</servlet-name>"
            + "    <servlet-class>sclass2</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
        assertTrue(srcWebXml.hasServlet("s2"));
    }

    /**
     * Tests whether a single servlet is correctly merged into a descriptor that
     * already contains the definition of a servlet with the same name.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoDocumentWithSameServlet()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
    }

    /**
     * Tets whether a servlet with an initialization parameter is correctly
     * merged into a descriptor that contains the definition of a servlet with
     * the same name.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoDocumentWithSameServletAndParam()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "    <init-param>"
            + "      <param-name>s1param1</param-name>"
            + "      <param-value>s1param1value</param-value>"
            + "    </init-param>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
        Iterator initParams = srcWebXml.getServletInitParamNames("s1");
        assertEquals("s1param1", initParams.next());
        assertTrue(!initParams.hasNext());
        assertEquals("s1param1value",
            srcWebXml.getServletInitParam("s1", "s1param1"));
    }

    /**
     * Tests whether a single servlet is correctly merged into a descriptor with
     * multiple servlets.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoDocumentWithMultipleServlets()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s2</servlet-name>"
            + "    <servlet-class>sclass2</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s3</servlet-name>"
            + "    <servlet-class>sclass3</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s4</servlet-name>"
            + "    <servlet-class>sclass4</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        Iterator servletNames = srcWebXml.getServletNames();
        assertEquals("s1", servletNames.next());
        assertEquals("s2", servletNames.next());
        assertEquals("s3", servletNames.next());
        assertEquals("s4", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }

    /**
     * Verifies that servlet init parameters are added after the load-on-startup
     * element of an already existing servlet definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergingServletWithInitParamsThatIsAlreadyDefined()
        throws Exception
    {
        String srcXml = "<web-app>".trim()
            + "  <servlet>".trim()
            + "    <servlet-name>s1</servlet-name>".trim()
            + "    <servlet-class>sclass1</servlet-class>".trim()
            + "    <load-on-startup>1</load-on-startup>".trim()
            + "  </servlet>".trim()
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>".trim()
            + "    <servlet-name>s1</servlet-name>".trim()
            + "    <servlet-class>sclass1</servlet-class>".trim()
            + "    <init-param>".trim()
            + "      <param-name>s1param1</param-name>".trim()
            + "      <param-value>s1param1value</param-value>".trim()
            + "    </init-param>".trim()
            + "  </servlet>".trim()
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        Element servletElement = srcWebXml.getServlet("s1");
        assertEquals("load-on-startup",
            ((Element) servletElement.getLastChild()).getTagName());
    }

    /**
     * Tests whether multiple servlet in the merge file are correctly inserted
     * into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeMultipleServletsIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s2</servlet-name>"
            + "    <servlet-class>sclass2</servlet-class>"
            + "  </servlet>"
            + "  <servlet>"
            + "    <servlet-name>s3</servlet-name>"
            + "    <servlet-class>sclass3</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        Iterator servletNames = srcWebXml.getServletNames();
        assertEquals("s1", servletNames.next());
        assertEquals("s2", servletNames.next());
        assertEquals("s3", servletNames.next());
        assertTrue(!servletNames.hasNext());
    }

    /**
     * Tests whether a single servlet with one mapping is correctly inserted
     * into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletWithOneMappingIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping1</url-pattern>"
            + "  </servlet-mapping>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
        Iterator servletMappings = srcWebXml.getServletMappings("s1");
        assertEquals("/s1mapping1", servletMappings.next());
        assertTrue(!servletMappings.hasNext());
    }

    /**
     * Tests whether a single servlet with multiple mappings is correctly 
     * inserted into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletWithMultipleMappingsIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping1</url-pattern>"
            + "  </servlet-mapping>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping2</url-pattern>"
            + "  </servlet-mapping>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1mapping3</url-pattern>"
            + "  </servlet-mapping>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeServlets(mergeWebXml);
        assertTrue(srcWebXml.hasServlet("s1"));
        Iterator servletMappings = srcWebXml.getServletMappings("s1");
        assertEquals("/s1mapping1", servletMappings.next());
        assertEquals("/s1mapping2", servletMappings.next());
        assertEquals("/s1mapping3", servletMappings.next());
        assertTrue(!servletMappings.hasNext());
    }

    /**
     * Tests whether a single security role is correctly inserted into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeSecurityRoleIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <security-role>"
            + "    <role-name>role1</role-name>"
            + "  </security-role>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeSecurityRoles(mergeWebXml);
        Iterator securityRoleNames = srcWebXml.getSecurityRoleNames();
        assertTrue(securityRoleNames.hasNext());
        assertEquals("role1", securityRoleNames.next());
        assertTrue(!securityRoleNames.hasNext());
    }

    /**
     * Tests whether a single security role is ignored when the source
     * descriptor already contains a role with the same name.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeSecurityRoleIntoDocumentWithSameRole()
        throws Exception
    {
        String srcXml = "<web-app>"
            + "  <security-role>"
            + "    <description>A role</description>"
            + "    <role-name>role1</role-name>"
            + "  </security-role>"
            + "</web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <security-role>"
            + "    <role-name>role1</role-name>"
            + "  </security-role>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeSecurityRoles(mergeWebXml);
        Iterator securityRoleNames = srcWebXml.getSecurityRoleNames();
        assertTrue(securityRoleNames.hasNext());
        assertEquals("role1", securityRoleNames.next());
        assertTrue(!securityRoleNames.hasNext());
    }

    /**
     * Tests whether a single EJB reference is correctly inserted into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneEjbRefIntoEmptyDocument()
        throws Exception
    {
        String srcXml = "<web-app></web-app>";
        Document srcDoc =
            builder.parse(new ByteArrayInputStream(srcXml.getBytes()));
        WebXml srcWebXml = new WebXml(srcDoc);
        String mergeXml = "<web-app>"
            + "  <ejb-ref>"
            + "    <ejb-ref-name>ejbref1</ejb-ref-name>"
            + "    <ejb-ref-type>ejbref1.type</ejb-ref-type>"
            + "    <home>ejbref1.homeInterface</home>"
            + "    <remote>ejbref1.remoteInterface</remote>"
            + "  </ejb-ref>"
            + "</web-app>";
        Document mergeDoc =
            builder.parse(new ByteArrayInputStream(mergeXml.getBytes()));
        WebXml mergeWebXml = new WebXml(mergeDoc);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeEjbRefs(mergeWebXml);
        Iterator ejbRefs = srcWebXml.getElements(WebXmlTag.EJB_REF); 
        assertTrue(ejbRefs.hasNext());
    }

}
