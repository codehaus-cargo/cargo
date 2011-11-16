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
package org.codehaus.cargo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;
import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.util.FileHandler.XmlReplacement;

/**
 * Unit tests for {@link DefaultFileHandler}.
 * 
 * @version $Id$
 */
public class DefaultFileHandlerTest extends TestCase
{

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the file handler. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        this.fileHandler = new DefaultFileHandler();
    }

    /**
     * Test relative to absolute path.
     */
    public void testGetAbsolutePathFromRelative()
    {
        String path = this.fileHandler.getAbsolutePath("path");
        assertEquals(path, System.getProperty("user.dir") + System.getProperty("file.separator")
            + "path");
    }

    /**
     * Test explicit to absolute path.
     */
    public void testGetAbsolutePathFromExplicit()
    {
        String path = this.fileHandler.getAbsolutePath(System.getProperty("user.home"));
        assertEquals(path, System.getProperty("user.home"));
    }

    /**
     * Test file copy to a non-existing path.<br />
     * This has been raised by https://jira.codehaus.org/browse/CARGO-1004
     */
    public void testCopyToNonExistingPath()
    {
        String random = UUID.randomUUID().toString();
        assertFalse("Subdirectory " + random + " already exists",
            this.fileHandler.isDirectory("target/" + random));
        this.fileHandler.createFile("target/random.txt");
        this.fileHandler.copyFile("target/random.txt", "target/" + random + "/random.txt",
            new FilterChain(), "UTF-8");
        assertTrue("Subdirectory " + random + " does not exist after copy",
            this.fileHandler.isDirectory("target/" + random));
        assertTrue("File in subdirectory " + random + " missing after copy",
            this.fileHandler.exists("target/" + random + "/random.txt"));
    }

    /**
     * Test valid XML replacement
     */
    public void testValidXmlReplacement()
    {
        final String file = "target/jboss-standalone-valid.xml";
        final String old = "<socket-binding name=\"http\" port=\"@cargo.servlet.port@\"/>";
        final String new1 = "<socket-binding name=\"http\" port=\"test1\"/>";
        final String new2 = "<socket-binding name=\"http\" port=\"test1\">test2</socket-binding>";

        this.fileHandler.copyFile("src/test/resources/jboss-standalone.xml", file, true);

        String read = this.fileHandler.readTextFile(file, "UTF-8");
        assertTrue("File " + file + " does not contain: " + old, read.contains(old));

        Map<XmlReplacement, String> replacements = new HashMap<XmlReplacement, String>();
        replacements.put(new XmlReplacement(
            "//server/socket-binding-group/socket-binding[@name='http']", "port"), "test1");
        this.fileHandler.replaceInXmlFile(file, replacements);
        read = this.fileHandler.readTextFile(file, "UTF-8");
        assertFalse("File " + file + " still contains: " + old, read.contains(old));
        assertTrue("File " + file + " does not contain: " + new1, read.contains(new1));

        replacements.clear();
        replacements.put(new XmlReplacement(
            "//server/socket-binding-group/socket-binding[@name='http']", null), "test2");
        this.fileHandler.replaceInXmlFile(file, replacements);
        read = this.fileHandler.readTextFile(file, "UTF-8");
        assertFalse("File " + file + " still contains: " + old, read.contains(old));
        assertFalse("File " + file + " still contains: " + new1, read.contains(new1));
        assertTrue("File " + file + " does not contain: " + new2, read.contains(new2));
    }

    /**
     * Test invalid XML XPath replacement
     */
    public void testInvalidXmlXpathReplacement()
    {
        final String file = "target/jboss-standalone-invalid-xpath.xml";
        final String nonExistingXpath =
            "//server/socket-binding-group/socket-binding[@name='nonexisting']";

        this.fileHandler.copyFile("src/test/resources/jboss-standalone.xml", file, true);

        Map<XmlReplacement, String> replacements = new HashMap<XmlReplacement, String>();
        replacements.put(new XmlReplacement(nonExistingXpath, null), "test");

        try
        {
            this.fileHandler.replaceInXmlFile(file, replacements);
            fail();
        }
        catch (CargoException e)
        {
            assertNotNull(e.getCause());
            assertNotNull(e.getCause().getMessage());
            assertTrue(e.getCause().getMessage().contains(
                "Node " + nonExistingXpath + " not found"));
        }
    }

    /**
     * Test invalid XML attribute replacement
     */
    public void testInvalidXmlAttributeReplacement()
    {
        final String file = "target/jboss-standalone-invalid-xml-attribute.xml";
        final String nonExistingAttribute = "nonexisting";

        this.fileHandler.copyFile("src/test/resources/jboss-standalone.xml", file, true);

        Map<XmlReplacement, String> replacements = new HashMap<XmlReplacement, String>();
        replacements.put(new XmlReplacement(
            "//server/socket-binding-group/socket-binding[@name='http']", nonExistingAttribute),
            "test");

        try
        {
            this.fileHandler.replaceInXmlFile(file, replacements);
            fail();
        }
        catch (CargoException e)
        {
            assertNotNull(e.getCause());
            assertNotNull(e.getCause().getMessage());
            assertTrue(e.getCause().getMessage().contains(
                "Attribute " + nonExistingAttribute + " not found"));
        }
    }

}
