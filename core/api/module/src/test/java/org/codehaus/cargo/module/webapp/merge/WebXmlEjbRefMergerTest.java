/*
 * ========================================================================
 *
 * Copyright 2003 The Apache Software Foundation. Code from this file
 * was originally imported from the Jakarta Cactus project.
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jdom2.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlType;

/**
 * Unit tests for {@link WebXmlMerger}.
 */
public final class WebXmlEjbRefMergerTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests whether a single EJB reference is correctly inserted into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public void testMergeOneEjbRefIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml = WebXmlIo.parseWebXml(
            new ByteArrayInputStream(srcXml.getBytes(StandardCharsets.UTF_8)), null);
        String mergeXml = "<web-app>"
            + "  <ejb-ref>"
            + "    <ejb-ref-name>ejbref1</ejb-ref-name>"
            + "    <ejb-ref-type>ejbref1.type</ejb-ref-type>"
            + "    <home>ejbref1.homeInterface</home>"
            + "    <remote>ejbref1.remoteInterface</remote>"
            + "  </ejb-ref>"
            + "</web-app>";
        WebXml mergeWebXml = WebXmlIo.parseWebXml(
            new ByteArrayInputStream(mergeXml.getBytes(StandardCharsets.UTF_8)), null);

        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        List<Element> ejbRefs = srcWebXml.getElements(WebXmlType.EJB_REF);
        Assertions.assertEquals(1, ejbRefs.size());
    }

}
