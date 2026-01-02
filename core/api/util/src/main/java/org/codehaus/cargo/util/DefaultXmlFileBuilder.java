/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@inheritDoc} This implementation uses the {@link XmlUtils} class to manipulate XML files.
 */
public class DefaultXmlFileBuilder implements XmlFileBuilder
{

    /**
     * used to access more sophisticated XML utility functions.
     */
    private XmlUtils xmlUtil;

    /**
     * the name of the file we are to load or save.
     */
    private String path;

    /**
     * representation of the document in progress.
     */
    private Document document;

    /**
     * creates the instance, which will use the specified @{link FileHandler fileHandler} to read or
     * write the xml file.
     * 
     * @param fileHandler used for file i/o.
     */
    public DefaultXmlFileBuilder(FileHandler fileHandler)
    {
        xmlUtil = new XmlUtils(fileHandler);
    }

    /**
     * creates the instance, which will use the specified @{link FileHandler fileHandler} to read or
     * write the xml file.
     * 
     * @param fileHandler used for file i/o.
     * @param namespaceAware true if builder should be namespace aware.
     */
    public DefaultXmlFileBuilder(FileHandler fileHandler, boolean namespaceAware)
    {
        xmlUtil = new XmlUtils(fileHandler, namespaceAware);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFile(String path)
    {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertElementsUnderXPath(String elementsToParse, String xpath)
    {
        Element parent = xmlUtil.selectElementMatchingXPath(xpath, document.getDocumentElement());

        StringBuilder nested = new StringBuilder();
        nested.append("<parent>");
        nested.append(elementsToParse);
        nested.append("</parent>");
        Element nestedElements = xmlUtil.parseIntoElement(nested.toString());
        NodeList children = nestedElements.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            Node clone = parent.getOwnerDocument().importNode(child, true);
            parent.appendChild(clone);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertElementUnderXPath(Element elementToInsert, String xpath)
    {
        Element parent = xmlUtil.selectElementMatchingXPath(xpath, document.getDocumentElement());

        Node clone = parent.getOwnerDocument().importNode(elementToInsert, true);
        parent.appendChild(clone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document loadFile()
    {
        this.document = xmlUtil.loadXmlFromFile(path);
        return this.document;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeFile()
    {
        xmlUtil.saveXml(document, path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNamespaces(Map<String, String> namespaces)
    {
        xmlUtil.setNamespaces(namespaces);
    }

}
