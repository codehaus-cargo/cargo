/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2016 Ali Tokmen.
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
 * {@inheritDoc} This implementation uses @{link Dom4JUtil Dom4JUtil} to manipulate xml files.
 * 
 */
public class Dom4JXmlFileBuilder implements XmlFileBuilder
{

    /**
     * used to access more sophisticated @{link org.dom4j dom4j} functions.
     */
    private Dom4JUtil xmlUtil;

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
    public Dom4JXmlFileBuilder(FileHandler fileHandler)
    {
        xmlUtil = new Dom4JUtil(fileHandler);
    }

    /**
     * {@inheritDoc}
     */
    public void setFile(String path)
    {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
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
    public void insertElementUnderXPath(Element elementToInsert, String xpath)
    {
        Element parent = xmlUtil.selectElementMatchingXPath(xpath, document.getDocumentElement());

        Node clone = parent.getOwnerDocument().importNode(elementToInsert, true);
        parent.appendChild(clone);
    }

    /**
     * {@inheritDoc}
     */
    public Document loadFile()
    {
        this.document = xmlUtil.loadXmlFromFile(path);
        return this.document;
    }

    /**
     * {@inheritDoc}
     */
    public void writeFile()
    {
        xmlUtil.saveXml(document, path);
    }

    /**
     * {@inheritDoc}
     */
    public void setNamespaces(Map<String, String> namespaces)
    {
        xmlUtil.setNamespaces(namespaces);
    }

}
