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
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.cli.MavenCli;

import org.jdom.Element;

import org.codehaus.cargo.module.AbstractDocumentBuilderTest;
import org.codehaus.cargo.module.webapp.WebXml;
import org.codehaus.cargo.module.webapp.WebXmlIo;
import org.codehaus.cargo.module.webapp.WebXmlType;

/**
 * Test generation of Uberwars (merged WARs).
 * 
 * @version $Id$
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
        final File projectDirectory = new File(target, "classes").getAbsoluteFile();

        final File output = new File(target, "output.log");
        final PrintStream outputStream = new PrintStream(output);

        final String[] options = new String[] {"-o", "-X", "clean", "verify"};

        new Thread(new Runnable()
        {
            public void run()
            {
                MavenCli maven2 = new MavenCli();
                maven2.doMain(options , projectDirectory.getPath(), outputStream, outputStream);
            }
        }).start();

        String outputString = null;
        long timeout = 90 * 1000 + System.currentTimeMillis();
        while (System.currentTimeMillis() < timeout)
        {
            try
            {
                outputString = FileUtils.readFileToString(output);
            }
            catch (FileNotFoundException e)
            {
                outputString = e.toString();
            }

            if (outputString.contains("BUILD SUCCESS"))
            {
                return;
            }
            else if (outputString.contains("BUILD FAILURE"))
            {
                fail("There has been a BUILD FAILURE. Please check file " + output);
                return;
            }

            Thread.sleep(1000);
            System.gc();
        }

        fail("The file " + output + " did not have the BUILD SUCCESS message after 60 seconds. "
            + "Current content: \n\n" + outputString);

        String projectVersion = System.getProperty("project.version");
        assertNotNull("System property project.version not set", projectVersion);

        File uberwarExpandedDirectory = new File(target,
            "classes/target/cargo-sample-maven2-uberwar-test-artifact-" + projectVersion);
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
