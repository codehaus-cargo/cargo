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
public final class WebXmlSecurityRoleMergerTest extends AbstractDocumentBuilderTest
{
    /**
     * Tests whether a single security role is correctly inserted into an empty descriptor.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeSecurityRoleIntoEmptyDocument() throws Exception
    {
        String srcXml = "<web-app></web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <security-role>"
            + "    <role-name>role1</role-name>"
            + "  </security-role>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeSecurityRoles(mergeWebXml);
        List<String> securityRoleNames = WebXmlUtils.getSecurityRoleNames(srcWebXml);
        assertEquals(1, securityRoleNames.size());
        assertEquals("role1", securityRoleNames.get(0));
    }

    /**
     * Tests whether a single security role is ignored when the source descriptor already contains a
     * role with the same name.
     * 
     * @throws Exception If an unexpected error occurs
     */
    public void testMergeSecurityRoleIntoDocumentWithSameRole() throws Exception
    {
        String srcXml = "<web-app>"
            + "  <security-role>"
            + "    <description>A role</description>"
            + "    <role-name>role1</role-name>"
            + "  </security-role>"
            + "</web-app>";
        WebXml srcWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(srcXml.getBytes("UTF-8")), null);
        String mergeXml = "<web-app>"
            + "  <security-role>"
            + "    <role-name>role1</role-name>"
            + "  </security-role>"
            + "</web-app>";
        WebXml mergeWebXml =
            WebXmlIo.parseWebXml(new ByteArrayInputStream(mergeXml.getBytes("UTF-8")), null);
        WebXmlMerger merger = new WebXmlMerger(srcWebXml);
        merger.mergeSecurityRoles(mergeWebXml);
        List<String> securityRoleNames = WebXmlUtils.getSecurityRoleNames(srcWebXml);
        assertEquals(1, securityRoleNames.size());
        assertEquals("role1", securityRoleNames.get(0));
    }

}
