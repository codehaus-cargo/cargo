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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * This class offers utility methods for handling XML files.
 */
public class XmlUtils
{
    /**
     * File utility class.
     */
    private FileHandler fileHandler;

    /**
     * XML document builder.
     */
    private DocumentBuilder builder;

    /**
     * XPath.
     */
    private XPath xPath;

    /**
     * XML namespaces map.
     */
    private NamespaceContextImpl namespaceContext;

    /**
     * True if XmlUtils should be namespace aware.
     */
    private boolean namespaceAware;

    /**
     * default constructor will assign no namespaces and use a default file handler.
     */
    public XmlUtils()
    {
        this(new DefaultFileHandler());
    }

    /**
     * default constructor will assign no namespaces and use a default file handler.
     * 
     * @param namespaceAware true if XmlUtils should be namespace aware.
     */
    public XmlUtils(boolean namespaceAware)
    {
        this(new DefaultFileHandler(), namespaceAware);
    }

    /**
     * constructor will assign no namespaces.
     * 
     * @param fileHandler used to read and write xml files.
     */
    public XmlUtils(FileHandler fileHandler)
    {
        this(fileHandler, false);
    }

    /**
     * constructor will assign no namespaces.
     * 
     * @param fileHandler used to read and write xml files.
     * @param namespaceAware true if XmlUtils should be namespace aware.
     */
    public XmlUtils(FileHandler fileHandler, boolean namespaceAware)
    {
        this.fileHandler = fileHandler;
        this.namespaceContext = new NamespaceContextImpl();
        this.namespaceAware = namespaceAware;
        XPathFactory xPathFactory = XPathFactory.newInstance();
        this.xPath = xPathFactory.newXPath();
        this.xPath.setNamespaceContext(namespaceContext);
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(namespaceAware);
        // Do not load remote DTDS as remote servers sometimes become unreachable
        try
        {
            domFactory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            domFactory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        }
        catch (AbstractMethodError | ParserConfigurationException ignored)
        {
            // Ignored
        }
        try
        {
            this.builder = domFactory.newDocumentBuilder();
        }
        catch (Exception e)
        {
            throw new CargoException("Cannot create XML DOM builder", e);
        }
    }

    /**
     * The following will search the given element for the specified XPath and return a list of
     * nodes that match.
     * 
     * @param xpath - selection criteria
     * @param toSearch - element to start the search at
     * @return List of matching elements
     */
    public List<Element> selectElementsMatchingXPath(String xpath, Element toSearch)
    {
        NodeList nodelist;
        try
        {
            String xpathWithoutNamespace = xpath;
            Map<String, String> namespaces = namespaceContext.getNamespaces();
            if (namespaces != null && !namespaces.isEmpty() && !namespaceAware)
            {
                for (Map.Entry<String, String> namespace : namespaces.entrySet())
                {
                    String key = namespace.getKey() + ":";
                    while (xpathWithoutNamespace.contains(key))
                    {
                        xpathWithoutNamespace = xpathWithoutNamespace.replace(key, "");
                    }
                }
            }
            XPathExpression xPathExpr = xPath.compile(xpathWithoutNamespace);
            nodelist = (NodeList) xPathExpr.evaluate(toSearch, XPathConstants.NODESET);
        }
        catch (Exception e)
        {
            throw new CargoException("Cannot evaluate XPath: " + xpath, e);
        }
        List<Element> result = new ArrayList<Element>(nodelist.getLength());
        for (int i = 0; i < nodelist.getLength(); i++)
        {
            result.add((Element) nodelist.item(i));
        }
        return result;
    }

    /**
     * The following will search the given element for the specified XPath and return any node that
     * matches.
     * 
     * @param xpath - selection criteria
     * @param toSearch - element to start the search at
     * @return a matching element
     */
    public Element selectElementMatchingXPath(String xpath, Element toSearch)
    {
        List<Element> results = selectElementsMatchingXPath(xpath, toSearch);
        if (results.isEmpty())
        {
            throw new ElementNotFoundException(xpath, toSearch);
        }
        Element match = results.get(0);
        return match;
    }

    /**
     * read the specified file into a Document.
     * 
     * @param sourceFile file to read
     * @return Document corresponding with sourceFile
     */
    public Document loadXmlFromFile(String sourceFile)
    {
        if (!getFileHandler().exists(sourceFile))
        {
            throw new CargoException("Cannot find file: " + sourceFile);
        }
        if (getFileHandler().isDirectory(sourceFile))
        {
            throw new CargoException("The destination is a directory: " + sourceFile);
        }
        try (InputStream is = getFileHandler().getInputStream(sourceFile))
        {
            return this.builder.parse(is);
        }
        catch (Exception e)
        {
            throw new CargoException("Cannot parse XML file " + sourceFile, e);
        }
    }

    /**
     * write the xml document to disk, rethrowing checked exceptions as runtime.
     * 
     * @param document document to write to disk
     * @param filename where to write the document
     */
    public void saveXml(Document document, String filename)
    {
        try (OutputStream os = getFileHandler().getOutputStream(filename))
        {
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(os));
        }
        catch (Exception e)
        {
            throw new CargoException("Cannot modify XML file " + filename, e);
        }
    }

    /**
     * @return the Cargo file utility class
     */
    public FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the Cargo file utility class to use. This method is useful for unit
     * testing with Mock objects as it can be passed a test file handler that doesn't perform any
     * real file action.
     */
    public void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }

    /**
     * @param namespaces the namespaces to set
     */
    public void setNamespaces(Map<String, String> namespaces)
    {
        this.namespaceContext.setNamespaces(namespaces);
    }

    /**
     * @return the namespaces
     */
    public Map<String, String> getNamespaces()
    {
        return namespaceContext.getNamespaces();
    }

    /**
     * parse the passed string into an {@link Element Element} object.
     * 
     * @param elementToParse string to parse
     * @return result of parsing
     */
    public Element parseIntoElement(String elementToParse)
    {
        try
        {
            Document parsed = this.builder.parse(
                new ByteArrayInputStream(elementToParse.getBytes(StandardCharsets.UTF_8)));
            return parsed.getDocumentElement();
        }
        catch (Exception e)
        {
            throw new CargoException("Could not parse element: " + elementToParse);
        }
    }

    /**
     * Creates a new, blank XML document.
     * 
     * @return New, blank XML document.
     */
    public Document createDocument()
    {
        return builder.newDocument();
    }

    /**
     * Output an XML node as string, without the XML header.
     * 
     * @param node Node to output.
     * @return String representation of node.
     */
    public String toString(Element node)
    {
        DOMImplementationLS implementation =
            (DOMImplementationLS) node.getOwnerDocument().getImplementation();
        LSSerializer serializer = implementation.createLSSerializer();
        serializer.getDomConfig().setParameter("format-pretty-print", true);
        serializer.getDomConfig().setParameter("xml-declaration", false);
        return serializer.writeToString(node);
    }
}
