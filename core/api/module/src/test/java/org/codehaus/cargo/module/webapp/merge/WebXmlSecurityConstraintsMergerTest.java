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
 * @version $Id$
 */
public class WebXmlSecurityConstraintsMergerTest extends AbstractDocumentBuilderTest
{

    /**
     * Tests whether a single security constraint is correctly merged into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeOneSecurityConstraintIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>resource1</web-resource-name>"
            + "      <url-pattern>/s1/*</url-pattern>"
            + "    </web-resource-collection>"
            + "    <auth-constraint>"
            + "      <role-name>role1</role-name>"
            + "    </auth-constraint>"
            + "  </security-constraint>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertNotNull(WebXmlUtils.getSecurityConstraint(srcWebXml, "/s1/*"));
        assertNull(WebXmlUtils.getSecurityConstraint(srcWebXml, "/s2/*"));
    }

    /**
     * Tests the merging of multiple security constraints of different patterns.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeMultipleSecurityConstraintsForDifferentPatterns() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>resource1</web-resource-name>"
            + "      <url-pattern>/s1/*</url-pattern>"
            + "    </web-resource-collection>"
            + "    <auth-constraint>"
            + "      <role-name>role1</role-name>"
            + "    </auth-constraint>"
            + "  </security-constraint>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>resource2</web-resource-name>"
            + "      <url-pattern>/s2/*</url-pattern>"
            + "    </web-resource-collection>"
            + "    <auth-constraint>"
            + "      <role-name>role2</role-name>"
            + "    </auth-constraint>"
            + "  </security-constraint>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertNotNull(WebXmlUtils.getSecurityConstraint(srcWebXml, "/s1/*"));
        assertNotNull(WebXmlUtils.getSecurityConstraint(srcWebXml, "/s2/*"));
        assertNull(WebXmlUtils.getSecurityConstraint(srcWebXml, "/s3/*"));
    }

    /**
     * Tests the merging of multiple security constraints with the same pattern.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeMultipleSecurityConstraintsOfSamePattern() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>resource1</web-resource-name>"
            + "      <url-pattern>/s1/*</url-pattern>"
            + "    </web-resource-collection>"
            + "    <auth-constraint>"
            + "      <role-name>role1</role-name>"
            + "    </auth-constraint>"
            + "  </security-constraint>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <security-constraint>"
            + "    <web-resource-collection>"
            + "      <web-resource-name>resource1</web-resource-name>"
            + "      <url-pattern>/s1/*</url-pattern>"
            + "    </web-resource-collection>"
            + "    <auth-constraint>"
            + "      <role-name>role1</role-name>"
            + "    </auth-constraint>"
            + "  </security-constraint>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.merge(mergeWebXml);
        assertNotNull(WebXmlUtils.getSecurityConstraint(srcWebXml, "/s1/*"));
        assertNull(WebXmlUtils.getSecurityConstraint(srcWebXml, "/s2/*"));
    }   
}
