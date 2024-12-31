/*
 * ========================================================================
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
package org.codehaus.cargo.sample.maven3.uberwar_test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.jdom2.Element;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlType;

/**
 * Test generation of Uberwars (merged WARs).
 */
public class UberwarTest extends AbstractDocumentBuilderTest
{

    /**
     * Test generation of Uberwars (merged WARs).
     * @throws Exception If anything fails.
     */
    public void testUberwar() throws Exception
    {
        File target = new File(System.getProperty("target"));

        String projectVersion = System.getProperty("project.version");
        assertNotNull("System property project.version not set", projectVersion);

        File uberwarExpandedDirectory = new File(target,
            "test-classes/target/cargo-sample-maven3-uberwar-test-artifact-" + projectVersion);
        assertNotNull("Not a directory: " + uberwarExpandedDirectory,
            uberwarExpandedDirectory.isDirectory());

        File webXmlFile = new File(uberwarExpandedDirectory, "WEB-INF/web.xml");
        assertNotNull("Not a file: " + webXmlFile, webXmlFile.isFile());

        try (FileInputStream webXmlStream = new FileInputStream(webXmlFile))
        {
            WebXml webXml = WebXmlIo.parseWebXml(webXmlStream, getEntityResolver());

            // Check that the security constraints are in the uberwar
            List<Element> securityConstraints = webXml.getTags(WebXmlType.SECURITY_CONSTRAINT);
            assertEquals(1, securityConstraints.size());
            Element securityConstraint = securityConstraints.get(0);
            List<Element> authConstraints = securityConstraint.getChildren(
                WebXmlType.AUTH_CONSTRAINT, securityConstraint.getNamespace());
            assertEquals(1, authConstraints.size());
            Element authConstraint = authConstraints.get(0);
            List<Element> roleNames = authConstraint.getChildren(
                WebXmlType.ROLE_NAME, authConstraint.getNamespace());
            assertEquals(1, roleNames.size());
            assertEquals("cargo", roleNames.get(0).getText());

            // Check that the datasource definitions are in the uberwar
            List<Element> resourceRefs = webXml.getTags(WebXmlType.RESOURCE_REF);
            assertEquals(1, resourceRefs.size());
            Element resourceRef = resourceRefs.get(0);
            List<Element> resRefNames = resourceRef.getChildren(
                "res-ref-name", resourceRef.getNamespace());
            assertEquals(1, resRefNames.size());
            assertEquals("jdbc/CargoDS", resRefNames.get(0).getText());
        }
    }

}
