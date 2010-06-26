/* 
 * ========================================================================
 * 
 * Copyright 2005 Vincent Massol.
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

import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.VisitorSupport;

/**
 * {@inheritDoc} This implementation uses @{link Dom4JUtil Dom4JUtil} to manipulate xml files.
 * 
 * @version $Id$
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
        Element parent = xmlUtil.selectElementMatchingXPath(xpath, document.getRootElement());

        StringBuffer nested = new StringBuffer();
        nested.append("<parent>");
        nested.append(elementsToParse);
        nested.append("</parent>");
        Element nestedElements = xmlUtil.parseIntoElement(nested.toString());
        Iterator elements = nestedElements.elements().iterator();

        while (elements.hasNext())
        {
            Element element = (Element) elements.next();
            setNamespaceOfElementToTheSameAsParent(element, parent);
            nestedElements.remove(element);
            parent.add(element);
        }
    }

    /**
     * @param element to traverse and change namespace of
     * @param parent - who to match namespaces with.
     */
    private void setNamespaceOfElementToTheSameAsParent(Element element, Element parent)
    {
        final Namespace namespaceOfParent = parent.getNamespace();
        element.accept(new VisitorSupport()
        {
            @Override
            public void visit(Element node)
            {
                QName nameOfElementWithCorrectNamespace =
                    new QName(node.getName(), namespaceOfParent);
                node.setQName(nameOfElementWithCorrectNamespace);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void loadFile()
    {
        this.document = xmlUtil.loadXmlFromFile(path);
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
    public void setNamespaces(Map namespaces)
    {
        xmlUtil.setNamespaces(namespaces);
    }

}
