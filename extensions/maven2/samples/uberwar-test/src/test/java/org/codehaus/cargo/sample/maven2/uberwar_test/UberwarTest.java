/*
 * ========================================================================
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
package org.codehaus.cargo.sample.maven2.uberwar_test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.jdom.Element;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlType;

public class UberwarTest extends AbstractDocumentBuilderTest
{

    private static final String UBERWAR_EXPANDED_LOCATION_PROPERTY = "uberwar.expanded.location";

    public void testUberwar() throws Exception
    {
        String uberwarExpandedLocation = System.getProperty(UBERWAR_EXPANDED_LOCATION_PROPERTY);
        assertNotNull("System property " + UBERWAR_EXPANDED_LOCATION_PROPERTY + " not set",
            uberwarExpandedLocation);

        File uberwarExpandedDirectory = new File(uberwarExpandedLocation);
        assertNotNull("Not a directory: " + uberwarExpandedDirectory,
            uberwarExpandedDirectory.isDirectory());

        File webXmlFile = new File(uberwarExpandedDirectory, "WEB-INF/web.xml");
        assertNotNull("Not a file: " + webXmlFile, webXmlFile.isFile());

        FileInputStream webXmlStream = new FileInputStream(webXmlFile);
        try
        {
            WebXml webXml = WebXmlIo.parseWebXml(webXmlStream, getEntityResolver());

            // Check that the security constraints are in the uberwar
            List<Element> securityConstraints = webXml.getTags(WebXmlType.SECURITY_CONSTRAINT);
            assertEquals(1, securityConstraints.size());
            List<Element> authConstraints =
                securityConstraints.get(0).getChildren(WebXmlType.AUTH_CONSTRAINT);
            assertEquals(1, authConstraints.size());
            List<Element> roleNames = authConstraints.get(0).getChildren(WebXmlType.ROLE_NAME);
            assertEquals(1, roleNames.size());
            assertEquals("cargo", roleNames.get(0).getText());

            // Check that the datasource definitions are in the uberwar
            List<Element> resourceRefs = webXml.getTags(WebXmlType.RESOURCE_REF);
            assertEquals(1, resourceRefs.size());
            List<Element> resRefNames = resourceRefs.get(0).getChildren("res-ref-name");
            assertEquals(1, resRefNames.size());
            assertEquals("jdbc/CargoDS", resRefNames.get(0).getText());
        }
        finally
        {
            webXmlStream.close();
        }
    }

}
