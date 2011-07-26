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

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.merge.DescriptorMergerByTag;
import org.codehaus.cargo.module.merge.tagstrategy.NodeMergeStrategy;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.codehaus.cargo.module.webapp.WebXmlUtils;
import org.jdom.Element;

/**
 * Unit tests for {@link WebXmlMerger} with merge strategies.
 * 
 * @version $Id: WebXmlMergerTest.java 2760 2011-03-19 17:33:47Z alitokmen $
 */
public final class WebXmlContextParamMergeStrategyMergerTest extends AbstractDocumentBuilderTest
{
    /**
     * Creates WebXml with given context parameter.
     * 
     * @param name parameter name
     * @param value parameter value
     * @return corresponding WebXml object
     * @throws Exception If an unexpected error occurs
     */
    private WebXml getWebXml(String name, String value) throws Exception
    {
        String xml = "<web-app>" 
            + "  <context-param>" 
            + "    <param-name>" + name + "</param-name>" 
            + "    <param-value>" + value + "</param-value>" 
            + "  </context-param>" 
            + "</web-app>";
        return WebXmlIo.parseWebXml(new ByteArrayInputStream(xml.getBytes("UTF-8")), null);
    }

    /**
     * Creates JDOM element with given context parameter.
     * 
     * @param name parameter name
     * @param value parameter value
     * @return JDOM object
     * @throws Exception If an unexpected error occurs
     */
    private Element getContextParamElement(String name, String value) throws Exception
    {
        String xml = "<context-param>" 
            + "  <param-name>" + name + "</param-name>"
            + "  <param-value>" + value + "</param-value>" 
            + "</context-param>";
        return WebXmlIo.parseWebXml(new ByteArrayInputStream(xml.getBytes("UTF-8")), null)
                .getRootElement();
    }

    /**
     * Retrieves parameter value from "context-param" element.
     * 
     * @param param element to inspect
     * @return parameter value, null for null input
     */
    private String getContextParamValue(Element param)
    {
        if (param == null)
        {
            return null;
        }
        return param.getChild(WebXmlType.PARAM_VALUE).getText();
    }

    /**
     * Tests {@link DescriptorMergerByTag#PRESERVE} merge strategy. This test
     * assumes that only initial descriptor have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInLeftWithPreserveStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("other", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, DescriptorMergerByTag.PRESERVE);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value1",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests {@link DescriptorMergerByTag#PRESERVE} merge strategy. This test
     * assumes that only overlay descriptor have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInRightWithPreserveStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("other", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, DescriptorMergerByTag.PRESERVE);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value2",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests {@link DescriptorMergerByTag#PRESERVE} merge strategy. This test
     * assumes that both descriptors have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInBothWithPreserveStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, DescriptorMergerByTag.PRESERVE);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value1",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests {@link DescriptorMergerByTag#OVERWRITE} merge strategy. This test
     * assumes that only initial descriptor have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInLeftWithOverwiteStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("other", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, DescriptorMergerByTag.OVERWRITE);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value1",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests {@link DescriptorMergerByTag#OVERWRITE} merge strategy. This test
     * assumes that only overlay descriptor have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInRightWithOverwiteStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("other", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, DescriptorMergerByTag.OVERWRITE);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value2",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests {@link DescriptorMergerByTag#OVERWRITE} merge strategy. This test
     * assumes that both descriptors have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInBothWithOverwiteStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, DescriptorMergerByTag.OVERWRITE);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value2",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests {@link DescriptorMergerByTag#IGNORE} merge strategy. This test
     * assumes that only initial descriptor have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInLeftWithIgnoreStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("other", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, DescriptorMergerByTag.IGNORE);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value1",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests {@link DescriptorMergerByTag#IGNORE} merge strategy. This test
     * assumes that only overlay descriptor have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInRightWithIgnoreStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("other", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, DescriptorMergerByTag.IGNORE);
        merger.merge(mergeWebXml);
        assertFalse(WebXmlUtils.hasContextParam(srcWebXml, "param"));
    }

    /**
     * Tests {@link DescriptorMergerByTag#IGNORE} merge strategy. This test
     * assumes that both descriptors have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInBothWithIgnoreStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, DescriptorMergerByTag.IGNORE);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value1",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests NodeMerge merge strategy. This test assumes that only initial
     * descriptor have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInLeftWithNodeMergeStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("other", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        Element format = getContextParamElement("$left:param-name",
                "$left:param-value $right:param-value");
        NodeMergeStrategy strategy = new NodeMergeStrategy(null, format);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, strategy);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value1",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests NodeMerge merge strategy. This test assumes that only overlay
     * descriptor have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInRightWithNodeMergeStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("other", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        Element format = getContextParamElement("$left:param-name",
                "$left:param-value $right:param-value");
        NodeMergeStrategy strategy = new NodeMergeStrategy(null, format);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, strategy);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value2",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests NodeMerge merge strategy. This test assumes that both descriptors
     * have the parameter under test.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInBothWithNodeMergeStrategy() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        Element format = getContextParamElement("$left:param-name",
                "$left:param-value $right:param-value");
        NodeMergeStrategy strategy = new NodeMergeStrategy(null, format);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, strategy);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value1 value2",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests NodeMerge merge strategy. In this test merge descriptor uses left
     * value only.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInBothWithNodeMergeStrategyWithLeftValueOnly() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        Element format = getContextParamElement("$left:param-name", "$left:param-value");
        NodeMergeStrategy strategy = new NodeMergeStrategy(null, format);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, strategy);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value1",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests NodeMerge merge strategy. In this test merge descriptor uses right
     * value only.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInBothWithNodeMergeStrategyWithRightValueOnly() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        Element format = getContextParamElement("$left:param-name", "$right:param-value");
        NodeMergeStrategy strategy = new NodeMergeStrategy(null, format);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, strategy);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value2",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests NodeMerge merge strategy. In this test merge descriptor contains
     * static text in addition to dynamic values.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInBothWithNodeMergeStrategyWithMixedContent() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("param", "value2");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        Element format = getContextParamElement("$left:param-name",
                "111 $left:param-value 222 $right:param-value 333");
        NodeMergeStrategy strategy = new NodeMergeStrategy(null, format);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, strategy);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("111 value1 222 value2 333",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

    /**
     * Tests NodeMerge merge strategy.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeInBothWithNodeMergeStrategyWithDollarSignInValue() throws Exception
    {
        WebXml srcWebXml = getWebXml("param", "value1");
        WebXml mergeWebXml = getWebXml("param", "${value2}");
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        Element format = getContextParamElement("$left:param-name",
                "$left:param-value $right:param-value");
        NodeMergeStrategy strategy = new NodeMergeStrategy(null, format);
        merger.setMergeStrategy(WebXmlType.CONTEXT_PARAM, strategy);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
        assertEquals("value1 ${value2}",
                getContextParamValue(WebXmlUtils.getContextParam(srcWebXml, "param")));
    }

}
