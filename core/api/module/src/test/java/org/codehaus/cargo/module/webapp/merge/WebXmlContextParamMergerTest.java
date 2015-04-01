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
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlUtils;

/**
 * Unit tests for {@link WebXmlMerger}.
 * 
 */
public final class WebXmlContextParamMergerTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests whether a single context param is correctly merged into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneContextParamIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";

        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param</param-name>"
            + "    <param-value>value</param-value>"
            + "  </context-param>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
    }

    /**
     * Tests whether a single context param is correctly merged into a descriptor that already
     * contains another context param.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneContextParamIntoDocumentWithAnotherContextParam() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param1</param-name>"
            + "    <param-value>value1</param-value>"
            + "  </context-param>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param2</param-name>"
            + "    <param-value>value2</param-value>"
            + "  </context-param>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param1"));
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param2"));
    }

    /**
     * Tests whether a single context param in the merge descriptor is ignored because a context
     * param with the same name already exists in the source descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneContextParamIntoDocumentWithSameContextParam() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <context-param>"
            + "    <param-name>param</param-name>"
            + "    <param-value>value</param-value>"
            + "  </context-param>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertTrue(WebXmlUtils.hasContextParam(srcWebXml, "param"));
    }

}
