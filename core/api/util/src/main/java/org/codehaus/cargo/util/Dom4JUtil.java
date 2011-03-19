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

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

/**
 * This class offers utility methods not exposed in the current dom4j api.
 * 
 * @version $Id$
 */
public class Dom4JUtil
{
    /**
     * namespace prefixes used for selecting nodes in config.xml.
     */
    private Map<String, String> namespaces;

    /**
     * File utility class.
     */
    private FileHandler fileHandler;

    /**
     * default constructor will assign no namespaces and use a default file handler.
     */
    public Dom4JUtil()
    {
        this(new DefaultFileHandler());
    }

    /**
     * constructor will assign no namespaces.
     * 
     * @param fileHandler used to read and write xml files.
     */
    public Dom4JUtil(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
        this.setNamespaces(new HashMap<String, String>());
    }

    /**
     * The following will search the given element for the specified xpath and return a list of
     * nodes that match.
     * 
     * @param xpath - selection criteria
     * @param toSearch - element to start the search at
     * @return List of matching elements
     */
    public List<Element> selectElementsMatchingXPath(String xpath, Element toSearch)
    {
        XPath xpathSelector = DocumentHelper.createXPath(xpath);
        xpathSelector.setNamespaceURIs(getNamespaces());
        List<Element> results = xpathSelector.selectNodes(toSearch);
        return results;
    }

    /**
     * The following will search the given element for the specified xpath and return any node that
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
     * write the xml document to disk, closing the destination on completion.
     * 
     * @param document document to write to disk
     * @param destination where to write the document
     * @throws IOException when the document cannot be written to the destination
     */
    private void writeXmlToOutputStream(Document document, OutputStream destination)
        throws IOException
    {
        try
        {
            OutputFormat outformat = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(destination, outformat);
            writer.write(document);
            writer.flush();
            writer.close();
        }
        finally
        {
            destination.close();
        }
    }

    /**
     * read the specified file into a Document.
     * 
     * @param sourceFile file to read
     * @return Document corresponding with sourceFile
     */
    public Document loadXmlFromFile(String sourceFile)
    {
        Document xml;
        try
        {
            SAXReader reader = new SAXReader(false);
            setDontAccessExternalResources(reader);
            xml = reader.read(getFileHandler().getInputStream(sourceFile));
        }
        catch (DocumentException e)
        {
            throw new CargoException("Error parsing " + sourceFile, e);
        }
        return xml;
    }

    /**
     * Turn off anything that would make this class touch the outside world.
     * 
     * @param reader what to disable external fetches from.
     */
    private void setDontAccessExternalResources(SAXReader reader)
    {
        try
        {
            reader.setFeature("http://xml.org/sax/features/external-general-entities", false);
            reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        }
        catch (SAXException e)
        {
            throw new CargoException("Error disabling external xml resources", e);
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
        OutputStream destination = null;
        try
        {
            if (!getFileHandler().exists(filename))
            {
                getFileHandler().createFile(filename);
            }
            destination = getFileHandler().getOutputStream(filename);
            writeXmlToOutputStream(document, destination);
        }
        catch (IOException e)
        {
            throw new CargoException("Error writing " + filename, e);
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
        this.namespaces = namespaces;
    }

    /**
     * @return the namespaces
     */
    public Map<String, String> getNamespaces()
    {
        return namespaces;
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
            Document parsed = DocumentHelper.parseText(elementToParse);
            return parsed.getRootElement();
        }
        catch (DocumentException e)
        {
            throw new CargoException("Could not parse element: " + elementToParse);
        }
    }

}
