package org.codehaus.cargo.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.SAXException;

public class Dom4JXmlFileBuilderTest extends TestCase
{
    private static final String TEST_FILE = "ram:/path/to/file.xml";

    private XmlFileBuilder manager;

    private DefaultFileHandler fileHandler;

    private StandardFileSystemManager fsManager;

    private Dom4JUtil util;

    private Map namespaces;

    @Override
    public void setUp() throws Exception
    {
        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        util = new Dom4JUtil(fileHandler);
        manager = new Dom4JXmlFileBuilder(fileHandler);
        namespaces = new HashMap();
        namespaces.put("weblogic", "http://www.bea.com/ns/weblogic/920/domain");
        NamespaceContext ctx = new SimpleNamespaceContext(namespaces);
        XMLUnit.setXpathNamespaceContext(ctx);
    }

    /**
     * {@inheritDoc} This method closes the VFS File System used in the test cases.
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (fsManager != null)
            fsManager.close();
        super.tearDown();
    }

    public void testManagerCanInsertAnElementIntoFile() throws XpathException, SAXException,
        IOException
    {
        Document document = DocumentHelper.createDocument();
        document.addElement("Application");
        util.saveXml(document, TEST_FILE);
        fileHandler.createFile(TEST_FILE);
        manager.setFile(TEST_FILE);
        manager.loadFile();
        manager.insertElementsUnderXPath("<subnode property='hello' />", "//Application");
        manager.writeFile();

        String xml = fileHandler.readTextFile(TEST_FILE);

        XMLAssert.assertXpathEvaluatesTo("hello", "//Application/subnode/@property", xml);
    }

    public void testManagerCanInsertAnElementIntoFileThreeLevelsDeep() throws XpathException,
        SAXException, IOException
    {
        Document document = DocumentHelper.createDocument();
        document.addElement("Application").addElement("foo").addElement("bar");
        util.saveXml(document, TEST_FILE);
        fileHandler.createFile(TEST_FILE);
        manager.setFile(TEST_FILE);
        manager.loadFile();
        manager.insertElementsUnderXPath("<subnode property='hello' />", "//Application/foo/bar");
        manager.writeFile();

        String xml = fileHandler.readTextFile(TEST_FILE);

        XMLAssert.assertXpathEvaluatesTo("hello", "//Application/foo/bar/subnode/@property", xml);
    }

    public void testManagerCanInsertAnElementIntoFileWithNamespace() throws XpathException,
        SAXException, IOException
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

        String xml = fileHandler.readTextFile(TEST_FILE);

        XMLAssert.assertXpathEvaluatesTo("hello", "//weblogic:domain/weblogic:subnode/@property",
            xml);
    }

}
