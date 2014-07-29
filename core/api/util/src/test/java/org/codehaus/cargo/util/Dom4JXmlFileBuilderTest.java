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

import junit.framework.TestCase;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * Unit tests for {@link XmlFileBuilder}.
 * 
 * @version $Id$
 */
public class Dom4JXmlFileBuilderTest extends TestCase
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
    private Dom4JUtil util;

    /**
     * XML namespaces map.
     */
    private Map<String, String> namespaces;

    /**
     * Creates the various XML test elements. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void setUp() throws Exception
    {
        this.fsManager = new StandardFileSystemManager();
        ((StandardFileSystemManager) this.fsManager).init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        util = new Dom4JUtil(fileHandler);
        manager = new Dom4JXmlFileBuilder(fileHandler);
        namespaces = new HashMap<String, String>();
        namespaces.put("weblogic", "http://www.bea.com/ns/weblogic/920/domain");
        NamespaceContext ctx = new SimpleNamespaceContext(namespaces);
        XMLUnit.setXpathNamespaceContext(ctx);
    }

    /**
     * Closes the file system manager created for tests. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (fsManager != null)
        {
            ((StandardFileSystemManager) fsManager).close();
        }
        super.tearDown();
    }

    /**
     * Test that the file system manager can insert an XML element into a file.
     * @throws Exception If anything goes wrong
     */
    public void testManagerCanInsertAnElementIntoFile() throws Exception
    {
        Document document = DocumentHelper.createDocument();
        document.addElement("Application");
        util.saveXml(document, TEST_FILE);
        fileHandler.createFile(TEST_FILE);
        manager.setFile(TEST_FILE);
        manager.loadFile();
        manager.insertElementsUnderXPath("<subnode property='hello' />", "//Application");
        manager.writeFile();

        String xml = fileHandler.readTextFile(TEST_FILE, "UTF-8");

        XMLAssert.assertXpathEvaluatesTo("hello", "//Application/subnode/@property", xml);
    }

    /**
     * Test that the file system manager can insert an XML element deep in the XML tree into a file.
     * This also involves a lookup.
     * @throws Exception If anything goes wrong
     */
    public void testManagerCanInsertAnElementIntoFileThreeLevelsDeep() throws Exception
    {
        Document document = DocumentHelper.createDocument();
        document.addElement("Application").addElement("foo").addElement("bar");
        util.saveXml(document, TEST_FILE);
        fileHandler.createFile(TEST_FILE);
        manager.setFile(TEST_FILE);
        manager.loadFile();
        manager.insertElementsUnderXPath("<subnode property='hello' />", "//Application/foo/bar");
        manager.writeFile();

        String xml = fileHandler.readTextFile(TEST_FILE, "UTF-8");

        XMLAssert.assertXpathEvaluatesTo("hello", "//Application/foo/bar/subnode/@property", xml);
    }

    /**
     * Test that the file system manager can insert an XML element with a namespace.
     * @throws Exception If anything goes wrong
     */
    public void testManagerCanInsertAnElementIntoFileWithNamespace() throws Exception
    {
        Document document = DocumentHelper.createDocument();
        Element domain = document.addElement("domain");
        document.setRootElement(domain);
        domain.addNamespace("", "http://www.bea.com/ns/weblogic/920/domain");
        util.saveXml(document, TEST_FILE);
        fileHandler.createFile(TEST_FILE);
        manager.setNamespaces(namespaces);
        manager.setFile(TEST_FILE);
        manager.loadFile();
        manager.insertElementsUnderXPath("<subnode property='hello' />", "//weblogic:domain");
        manager.writeFile();

        String xml = fileHandler.readTextFile(TEST_FILE, "UTF-8");

        XMLAssert.assertXpathEvaluatesTo("hello", "//weblogic:domain/weblogic:subnode/@property",
            xml);
    }

}
