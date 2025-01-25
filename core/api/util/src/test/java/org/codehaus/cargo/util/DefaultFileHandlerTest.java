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
package org.codehaus.cargo.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DefaultFileHandler}.
 */
public class DefaultFileHandlerTest
{

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the file handler.
     */
    @BeforeEach
    protected void setUp()
    {
        this.fileHandler = new DefaultFileHandler();
    }

    /**
     * Test relative to absolute path.
     */
    @Test
    public void testGetAbsolutePathFromRelative()
    {
        String path = this.fileHandler.getAbsolutePath("path");
        Assertions.assertEquals(
            path, System.getProperty("user.dir") + System.getProperty("file.separator") + "path");
    }

    /**
     * Test explicit to absolute path.
     */
    @Test
    public void testGetAbsolutePathFromExplicit()
    {
        String path = this.fileHandler.getAbsolutePath(System.getProperty("user.home"));
        Assertions.assertEquals(path, System.getProperty("user.home"));
    }

    /**
     * Test file copy to a non-existing path.<br>
     * This has been raised by https://codehaus-cargo.atlassian.net/browse/CARGO-1004
     */
    @Test
    public void testCopyToNonExistingPath()
    {
        String random = UUID.randomUUID().toString();
        Assertions.assertFalse(
            this.fileHandler.isDirectory("target/" + random),
                "Subdirectory " + random + " already exists");
        this.fileHandler.createFile("target/random.txt");
        this.fileHandler.copyFile(
            "target/random.txt", "target/" + random + "/random.txt",
                null, StandardCharsets.UTF_8);
        Assertions.assertTrue(
            this.fileHandler.isDirectory("target/" + random),
                "Subdirectory " + random + " does not exist after copy");
        Assertions.assertTrue(
            this.fileHandler.exists("target/" + random + "/random.txt"),
                "File in subdirectory " + random + " missing after copy");
    }

    /**
     * Test valid XML replacement
     */
    @Test
    public void testValidXmlReplacement()
    {
        final String file = "target/jboss-standalone-valid.xml";
        final String old = "<socket-binding name=\"http\" port=\"@cargo.servlet.port@\"/>";
        final String new1 = "<socket-binding name=\"http\" port=\"test1\"/>";
        final String new2 = "<socket-binding name=\"http\" port=\"test1\">test2</socket-binding>";

        this.fileHandler.copyFile("src/test/resources/jboss-standalone.xml", file, true);

        String read = this.fileHandler.readTextFile(file, StandardCharsets.UTF_8);
        Assertions.assertTrue(read.contains(old), "File " + file + " does not contain: " + old);

        XmlReplacement xmlReplacement = new XmlReplacement(file,
            "//server/socket-binding-group/socket-binding[@name='http']", "port",
                XmlReplacement.ReplacementBehavior.THROW_EXCEPTION, "test1");
        this.fileHandler.replaceInXmlFile(xmlReplacement);
        read = this.fileHandler.readTextFile(file, StandardCharsets.UTF_8);
        Assertions.assertFalse(read.contains(old), "File " + file + " still contains: " + old);
        Assertions.assertTrue(read.contains(new1), "File " + file + " does not contain: " + new1);

        xmlReplacement.setAttributeName(null);
        xmlReplacement.setValue("test2");
        this.fileHandler.replaceInXmlFile(xmlReplacement);
        read = this.fileHandler.readTextFile(file, StandardCharsets.UTF_8);
        Assertions.assertFalse(read.contains(old), "File " + file + " still contains: " + old);
        Assertions.assertFalse(read.contains(new1), "File " + file + " still contains: " + new1);
        Assertions.assertTrue(read.contains(new2), "File " + file + " does not contain: " + new2);
    }

    /**
     * Test valid XML replacement for replacing a neighbor
     */
    @Test
    public void testXmlNeighborReplacement()
    {
        final String file = "target/bindings-jboss-beans.xml";
        final String old = "<property name=\"port\">@cargo.rmi.port@</property>";
        final String permanent = "<property name=\"port\">@cargo.jboss.naming.port@</property>";
        final String new1 = "<property name=\"port\">test1</property>";

        this.fileHandler.copyFile("src/test/resources/bindings-jboss-beans.xml", file, true);

        String read = this.fileHandler.readTextFile(file, StandardCharsets.UTF_8);
        Assertions.assertTrue(read.contains(old), "File " + file + " does not contain: " + old);
        Assertions.assertTrue(
            read.contains(permanent), "File " + file + " does not contain: " + permanent);

        XmlReplacement xmlReplacement = new XmlReplacement(file,
            "//deployment/bean[@name='StandardBindings']/constructor/parameter/set/bean"
                + "/property[@name='serviceName' and text()='jboss:service=Naming']/parent::bean"
                + "/property[@name='bindingName' and text()='Port']/parent::bean"
                + "/property[@name='port']", null,
                XmlReplacement.ReplacementBehavior.THROW_EXCEPTION, "test1");
        this.fileHandler.replaceInXmlFile(xmlReplacement);
        read = this.fileHandler.readTextFile(file, StandardCharsets.UTF_8);
        Assertions.assertFalse(read.contains(old), "File " + file + " still contains: " + old);
        Assertions.assertTrue(read.contains(new1), "File " + file + " does not contain: " + new1);
        Assertions.assertTrue(
            read.contains(permanent), "File " + file + " does not contain anymore: " + permanent);
    }

    /**
     * Test invalid XML XPath replacement
     */
    @Test
    public void testInvalidXmlXpathReplacement()
    {
        final String file = "target/jboss-standalone-invalid-xpath.xml";
        final String nonExistingXpath =
            "//server/socket-binding-group/socket-binding[@name='nonexisting']";

        this.fileHandler.copyFile("src/test/resources/jboss-standalone.xml", file, true);

        XmlReplacement xmlReplacement =
            new XmlReplacement(file, nonExistingXpath, null,
                    XmlReplacement.ReplacementBehavior.THROW_EXCEPTION, "test");
        try
        {
            this.fileHandler.replaceInXmlFile(xmlReplacement);
            Assertions.fail();
        }
        catch (CargoException e)
        {
            Assertions.assertNotNull(e.getCause());
            Assertions.assertNotNull(e.getCause().getMessage());
            Assertions.assertTrue(e.getCause().getMessage().contains(
                "Node " + nonExistingXpath + " not found"));
        }
    }

    /**
     * Test non-existing XML attribute replacement
     */
    @Test
    public void testNonExistingXmlAttributeReplacement()
    {
        final String file = "target/jboss-standalone-nonexisting-xml-attribute.xml";
        final String nonExistingAttribute = "nonexisting";
        final String test = "nonexisting=\"test\"";

        this.fileHandler.copyFile("src/test/resources/jboss-standalone.xml", file, true);

        XmlReplacement xmlReplacement = new XmlReplacement(file,
            "//server/socket-binding-group/socket-binding[@name='http']", nonExistingAttribute,
                XmlReplacement.ReplacementBehavior.THROW_EXCEPTION, "test");

        this.fileHandler.replaceInXmlFile(xmlReplacement);
        String read = this.fileHandler.readTextFile(file, StandardCharsets.UTF_8);
        Assertions.assertTrue(read.contains(test), "File " + file + " does not contain: " + test);
    }

}
