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
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for {@link XmlFileBuilder}.
 */
public class DefaultXmlFileBuilderTest
{
    /**
     * Test file name.
     */
    private static final String TEST_FILE = "ram:/path/to/file.xml";

    /**
     * XML file builder.
     */
    private XmlFileBuilder manager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * File system manager.
     */
    private FileSystemManager fsManager;

    /**
     * Dom4j utilities.
     */
    private XmlUtils util;

    /**
     * XML document builder.
     */
    private DocumentBuilder builder;

    /**
     * XML namespaces map.
     */
    private Map<String, String> namespaces;

    /**
     * Creates the various XML test elements.
     * @throws Exception If anything goes wrong.
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.fsManager = new StandardFileSystemManager();
        ((StandardFileSystemManager) this.fsManager).init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        util = new XmlUtils(fileHandler);
        manager = new DefaultXmlFileBuilder(fileHandler);
        namespaces = new HashMap<String, String>();
        namespaces.put("weblogic", "http://www.bea.com/ns/weblogic/920/domain");
        NamespaceContext ctx = new SimpleNamespaceContext(namespaces);
        XMLUnit.setXpathNamespaceContext(ctx);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        builder = domFactory.newDocumentBuilder();
    }

    /**
     * Closes the file system manager created for tests.
     */
    @AfterEach
    protected void tearDown()
    {
        if (fsManager != null)
        {
            ((StandardFileSystemManager) fsManager).close();
        }
    }

    /**
     * Test that the file system manager can insert an XML element into a file.
     * @throws Exception If anything goes wrong
     */
    @Test
    public void testManagerCanInsertAnElementIntoFile() throws Exception
    {
        Document document = builder.newDocument();
        Element application = document.createElement("Application");
        document.appendChild(application);
        util.saveXml(document, TEST_FILE);
        fileHandler.createFile(TEST_FILE);
        manager.setFile(TEST_FILE);
        manager.loadFile();
        manager.insertElementsUnderXPath("<subnode property='hello' />", "//Application");
        manager.writeFile();

        String xml = fileHandler.readTextFile(TEST_FILE, StandardCharsets.UTF_8);

        XMLAssert.assertXpathEvaluatesTo("hello", "//Application/subnode/@property", xml);
    }

    /**
     * Test that the file system manager can insert an XML element deep in the XML tree into a file.
     * This also involves a lookup.
     * @throws Exception If anything goes wrong
     */
    @Test
    public void testManagerCanInsertAnElementIntoFileThreeLevelsDeep() throws Exception
    {
        Document document = builder.newDocument();
        Element application = document.createElement("Application");
        document.appendChild(application);
        Element foo = document.createElement("foo");
        application.appendChild(foo);
        Element bar = document.createElement("bar");
        foo.appendChild(bar);
        util.saveXml(document, TEST_FILE);
        fileHandler.createFile(TEST_FILE);
        manager.setFile(TEST_FILE);
        manager.loadFile();
        manager.insertElementsUnderXPath("<subnode property='hello' />", "//Application/foo/bar");
        manager.writeFile();

        String xml = fileHandler.readTextFile(TEST_FILE, StandardCharsets.UTF_8);

        XMLAssert.assertXpathEvaluatesTo("hello", "//Application/foo/bar/subnode/@property", xml);
    }

    /**
     * Test that the file system manager can insert an XML element with a namespace.
     * @throws Exception If anything goes wrong
     */
    @Test
    public void testManagerCanInsertAnElementIntoFileWithNamespace() throws Exception
    {
        Document document = builder.newDocument();
        Element domain = document.createElement("domain");
        domain.setAttribute("xmlns", "http://www.bea.com/ns/weblogic/920/domain");
        domain.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        domain.setAttribute("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/920/domain "
            + "http://www.bea.com/ns/weblogic/920/domain.xsd");
        document.appendChild(domain);
        util.saveXml(document, TEST_FILE);
        fileHandler.createFile(TEST_FILE);
        manager.setNamespaces(namespaces);
        manager.setFile(TEST_FILE);
        manager.loadFile();
        manager.insertElementsUnderXPath("<subnode property='hello' />", "//weblogic:domain");
        manager.writeFile();

        String xml = fileHandler.readTextFile(TEST_FILE, StandardCharsets.UTF_8);

        XMLAssert.assertXpathEvaluatesTo("hello", "//weblogic:domain/weblogic:subnode/@property",
            xml);
    }

}
