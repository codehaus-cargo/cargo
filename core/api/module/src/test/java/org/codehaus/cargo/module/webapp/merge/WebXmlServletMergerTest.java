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
import org.jdom.Element;

/**
 * Unit tests for {@link WebXmlMerger}.
 * 
 */
public final class WebXmlServletMergerTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests whether a single servlet is correctly merged into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasServlet(srcWebXml, "s1"));
    }

    /**
     * Tests whether a single servlet is correctly merged into a descriptor that already contains
     * the definition of an other servlet.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoDocumentWithAnotherServlet() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s2</servlet-name>"
            + "    <servlet-class>sclass2</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasServlet(srcWebXml, "s1"));
        assertTrue(WebXmlUtils.hasServlet(srcWebXml, "s2"));
    }

    /**
     * Tests whether a single servlet is correctly merged into a descriptor that already contains
     * the definition of a servlet with the same name.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoDocumentWithSameServlet() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasServlet(srcWebXml, "s1"));
    }

    /**
     * Tets whether a servlet with an initialization parameter is correctly merged into a descriptor
     * that contains the definition of a servlet with the same name.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoDocumentWithSameServletAndParam() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
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
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasServlet(srcWebXml, "s1"));
        List<String> initParams = WebXmlUtils.getServletInitParamNames(srcWebXml, "s1");
        assertEquals(1, initParams.size());
        assertEquals("s1param1", initParams.get(0));
        assertEquals("s1param1value",
            WebXmlUtils.getServletInitParam(srcWebXml, "s1", "s1param1"));
    }

    /**
     * Tests whether a single servlet is correctly merged into a descriptor with multiple servlets.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletIntoDocumentWithMultipleServlets() throws Exception
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
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s4</servlet-name>"
            + "    <servlet-class>sclass4</servlet-class>"
            + "  </servlet>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        List<String> servletNames = WebXmlUtils.getServletNames(srcWebXml);
        assertEquals(4, servletNames.size());
        assertEquals("s1", servletNames.get(0));
        assertEquals("s2", servletNames.get(1));
        assertEquals("s3", servletNames.get(2));
        assertEquals("s4", servletNames.get(3));
    }

    /**
     * Verifies that servlet init parameters are added after the load-on-startup element of an
     * already existing servlet definition.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergingServletWithInitParamsThatIsAlreadyDefined() throws Exception
    {
        String srcXml = "<web-app>".trim()
            + "  <servlet>".trim()
            + "    <servlet-name>s1</servlet-name>".trim()
            + "    <servlet-class>sclass1</servlet-class>".trim()
            + "    <load-on-startup>1</load-on-startup>".trim()
            + "  </servlet>".trim()
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
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
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        Element servletElement = WebXmlUtils.getServlet(srcWebXml, "s1");
        assertEquals("load-on-startup",
            ((Element) servletElement.getChildren().get(servletElement.getChildren().size() - 1))
                .getName());
    }

    /**
     * Tests whether multiple servlet in the merge file are correctly inserted into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeMultipleServletsIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
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
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        List<String> servletNames = WebXmlUtils.getServletNames(srcWebXml);
        assertEquals(3, servletNames.size());
        assertEquals("s1", servletNames.get(0));
        assertEquals("s2", servletNames.get(1));
        assertEquals("s3", servletNames.get(2));
    }

    /**
     * Tests whether a single servlet with one mapping is correctly inserted into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletWithOneMappingIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
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
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasServlet(srcWebXml, "s1"));
        List<String> servletMappings = WebXmlUtils.getServletMappings(srcWebXml, "s1");
        assertEquals(1, servletMappings.size());
        assertEquals("/s1mapping1", servletMappings.get(0));
    }

    /**
     * Tests whether a single servlet with multiple mappings is correctly inserted into an empty
     * descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneServletWithMultipleMappingsIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
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
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasServlet(srcWebXml, "s1"));
        List<String> servletMappings = WebXmlUtils.getServletMappings(srcWebXml, "s1");
        assertEquals(3, servletMappings.size());
        assertEquals("/s1mapping1", servletMappings.get(0));
        assertEquals("/s1mapping2", servletMappings.get(1));
        assertEquals("/s1mapping3", servletMappings.get(2));
    }

    /**
     * Tests the merging of multiple servlet mappings.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeMultipleServletMappings() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1</url-pattern>"
            + "  </servlet-mapping>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <servlet>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <servlet-class>sclass1</servlet-class>"
            + "  </servlet>"
            + "  <servlet-mapping>"
            + "    <servlet-name>s1</servlet-name>"
            + "    <url-pattern>/s1</url-pattern>"
            + "  </servlet-mapping>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasServlet(srcWebXml, "s1"));
        List<String> servletMappings = WebXmlUtils.getServletMappings(srcWebXml, "s1");
        assertEquals(1, servletMappings.size());
        assertEquals("/s1", servletMappings.get(0));
    }

}
