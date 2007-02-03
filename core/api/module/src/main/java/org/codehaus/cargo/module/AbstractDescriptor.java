/* 
 * ========================================================================
 * 
 * Copyright 2003-2004 The Apache Software Foundation. Code from this file 
 * was originally imported from the Jakarta Cactus project.
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.module;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Encapsulates the DOM representation of a deployment descriptor to provide convenience methods for
 * easy access and manipulation.
 *
 * @version $Id$
 */
public abstract class AbstractDescriptor implements Descriptor
{
    /**
     * The DOM representation of the deployment descriptor.
     */
    private Document document;

    /**
     * The root element of the descriptor.
     */
    private Element rootElement;

    /**
     * Grammar of the descriptor.
     */
    private Grammar grammar;

    /**
     * Constructor.
     *
     * @param document The DOM document representing the parsed deployment descriptor
     * @param grammar The DTD of the descriptor
     */
    public AbstractDescriptor(Document document, Grammar grammar)
    {
        this.document = document;
        this.rootElement = document.getDocumentElement();
        this.grammar = grammar;
    }

    /**
     * Returns the DOM document representing the deployment descriptor. The document will contain
     * any modifications made through this instance.
     *
     * @return The document representing the deploy descriptor
     */
    public Document getDocument()
    {
        return this.document;
    }

    /**
     * @return the DOM root element
     */
    public Element getRootElement()
    {
        return this.rootElement;
    }

    /**
     * Returns an iterator over the elements that match the specified tag.
     *
     * @param tag The descriptor tag of which the elements should be returned
     * @return An iterator over the elements matching the tag, in the order they occur in the
     *         descriptor
     */
    public Iterator getElements(DescriptorTag tag)
    {
        List elements = new ArrayList();
        NodeList nodeList = getRootElement().getElementsByTagName(tag.getTagName());
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            elements.add(nodeList.item(i));
        }
        return elements.iterator();
    }

    /**
     * Checks an element whether its name matches the specified name.
     *
     * @param element The element to check
     * @param expectedTag The expected tag name
     *
     * @throws IllegalArgumentException If the element name doesn't match
     */
    protected void checkElement(Element element, DescriptorTag expectedTag)
        throws IllegalArgumentException
    {
        if (!expectedTag.getTagName().equals(element.getNodeName()))
        {
            throw new IllegalArgumentException("Not a [" + expectedTag + "] element");
        }
    }

    /**
     * Returns an iterator over the child elements of the specified element that match the specified
     * tag.
     *
     * @param parent The element of which the nested elements should be retrieved
     * @param tag The descriptor tag of which the elements should be returned
     * @return An iterator over the elements matching the tag, in the order they occur in the
     *         descriptor
     */
    protected Iterator getNestedElements(Element parent, DescriptorTag tag)
    {
        List elements = new ArrayList();
        NodeList nodeList = parent.getElementsByTagName(tag.getTagName());
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            elements.add(nodeList.item(i));
        }
        return elements.iterator();
    }

    /**
     * Creates an element that contains nested text.
     *
     * @param tag The tag to create an instance of
     * @param text The text that should be nested in the element
     * @return The created DOM element
     */
    protected Element createNestedText(DescriptorTag tag, String text)
    {
        Element element = this.document.createElement(tag.getTagName());
        element.appendChild(this.document.createTextNode(text));
        return element;
    }

    /**
     * Returns the text nested inside a child element of the specified element.
     *
     * @param parent The element of which the nested text should be returned
     * @param tag The descriptor tag in which the text is nested
     * @return The text nested in the element
     */
    protected String getNestedText(Element parent, DescriptorTag tag)
    {
        String text = null;
        NodeList nestedElements = parent.getElementsByTagName(tag.getTagName());
        if (nestedElements.getLength() > 0)
        {
            text = getText((Element) nestedElements.item(0));
        }
        return text;
    }

    /**
     * Returns the text value of an element.
     *
     * @param element the element of wich the text value should be returned
     * @return the text value of an element
     */
    protected String getText(Element element)
    {
        String text = null;
        Node nestedText = element.getFirstChild();
        if (nestedText != null)
        {
            text = nestedText.getNodeValue();
        }
        return text;
    }

    /**
     * Gets a certain tag directly under the parent tag.
     *
     * @param parent the tag to get the cild from
     * @param tag name of the child tag
     * @return tag directly under the parent tag.
     */
    protected Element getImmediateChild(Element parent, DescriptorTag tag)
    {
        Element e = null;
        NodeList nl = parent.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++)
        {
            Node n = nl.item(i);
            if (n.getNodeName().equals(tag.getTagName())
                && n.getNodeType() == Node.ELEMENT_NODE)
            {
                e = (Element) n;
            }
        }

        return e;
    }

    /**
     * Returns the text value from a child directly under the parent tag.
     *
     * @param parent the parent tag to get the child text from
     * @param tag the name of the child tag
     * @return the text value of a child tag directly under the parent tag
     */
    protected String getChildText(Element parent, DescriptorTag tag)
    {
        String text = null;
        Element e = getImmediateChild(parent, tag);
        if (e != null)
        {
            text = getText(e);
        }
        return text;
    }

    /**
     * Adds an element of the specified tag to the descriptor.
     *
     * @param tag The descriptor tag
     * @param child The child element to add
     * @param parent The parent element to add the child to
     *
     * @return the inserted element
     */
    protected Element addElement(DescriptorTag tag, Element child, Element parent)
    {
        Node importedNode = getDocument().importNode(child, true);
        Node refNode = getInsertionPointFor(tag, parent.getNodeName());
        return (Element) parent.insertBefore(importedNode, refNode);
    }

    /**
     * Replaces all elements of the specified tag with the provided element.
     *
     * @param tag The descriptor tag
     * @param child The element to replace the current elements with
     * @param parent The parent element to add the child to
     *
     * @return the replaced element
     */
    public Element replaceElement(DescriptorTag tag, Element child, Element parent)
    {
        Iterator elements = getElements(tag);
        while (elements.hasNext())
        {
            Element e = (Element) elements.next();
            e.getParentNode().removeChild(e);
        }
        return addElement(tag, child, parent);
    }

    /**
     * Returns the node before which the specified tag should be inserted, or <code>null</code> if
     * the node should be inserted at the end of the descriptor.
     *
     * @param tag The tag that should be inserted
     * @param parent name of the parent tag
     *
     * @return The node before which the tag can be inserted
     */
    protected Node getInsertionPointFor(DescriptorTag tag, String parent)
    {
        List elementOrder = this.grammar.getElementOrder(parent);
        for (int i = 0; i < elementOrder.size(); i++)
        {
            DescriptorTag orderTag = (DescriptorTag) elementOrder.get(i);
            if (orderTag.equals(tag))
            {
                for (int j = i + 1; j < elementOrder.size(); j++)
                {
                    NodeList elements = getRootElement().getElementsByTagName(
                        ((DescriptorTag) elementOrder.get(j)).getTagName());
                    if (elements.getLength() > 0)
                    {
                        Node result = elements.item(0);
                        Node previous = result.getPreviousSibling();
                        while ((previous != null) && ((previous.getNodeType() == Node.COMMENT_NODE)
                            || (previous.getNodeType() == Node.TEXT_NODE)))
                        {
                            result = previous;
                            previous = result.getPreviousSibling();
                        }
                        return result;
                    }
                }
                break;
            }
        }
        return null;
    }
}
