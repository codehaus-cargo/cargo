/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.module.internal.util.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * Simple wrapped abstract element so that subclasses can bind to particular attributes.
 *
 * @version $Id: $
 */
public abstract class AbstractElement extends AbstractNode implements Element
{
    /**
     * Constructor.
     *
     * @param element in element to wrap
     */
    protected AbstractElement(Element element)
    {
        super(element);
    }

    /**
     * @return the wrapped element
     */
    public Element getElement()
    {
        return (Element) getNode();
    }

    /**
     * Overridden by concrete implementors to return some reasonable identifier for
     * this section of XML.
     *
     * @return the identifier of this section
     */
    public abstract String getElementId();

    /**
     * Returns the text value of an element.
     *
     * @param element the element of wich the text value should be returned
     *
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
     * {@inheritDoc}
     * @see org.w3c.dom.Element#getTagName()
     */
    public String getTagName()
    {
        return getElement().getTagName();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#getAttribute(java.lang.String)
     */
    public String getAttribute(String name)
    {
        return getElement().getAttribute(name);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#setAttribute(java.lang.String, java.lang.String)
     */
    public void setAttribute(String name, String value) throws DOMException
    {
        getElement().setAttribute(name, value);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) throws DOMException
    {
        getElement().removeAttribute(name);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#getAttributeNode(java.lang.String)
     */
    public Attr getAttributeNode(String name)
    {
        return getElement().getAttributeNode(name);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#setAttributeNode(org.w3c.dom.Attr)
     */
    public Attr setAttributeNode(Attr newAttr) throws DOMException
    {
        return getElement().setAttributeNode(newAttr);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#removeAttributeNode(org.w3c.dom.Attr)
     */
    public Attr removeAttributeNode(Attr oldAttr) throws DOMException
    {
        return getElement().removeAttributeNode(oldAttr);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#getElementsByTagName(java.lang.String)
     */
    public NodeList getElementsByTagName(String name)
    {
        return getElement().getElementsByTagName(name);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#getAttributeNS(java.lang.String, java.lang.String)
     */
    public String getAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        return getElement().getAttributeNS(namespaceURI, localName);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#setAttributeNS(java.lang.String, java.lang.String, java.lang.String)
     */
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value)
        throws DOMException
    {
        getElement().setAttributeNS(namespaceURI, qualifiedName, value);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#removeAttributeNS(java.lang.String, java.lang.String)
     */
    public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        getElement().removeAttributeNS(namespaceURI, localName);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#getAttributeNodeNS(java.lang.String, java.lang.String)
     */
    public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException
    {
        return getElement().getAttributeNodeNS(namespaceURI, localName);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#setAttributeNodeNS(org.w3c.dom.Attr)
     */
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
    {
        return getElement().setAttributeNodeNS(newAttr);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#getElementsByTagNameNS(java.lang.String, java.lang.String)
     */
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
        throws DOMException
    {
        return getElement().getElementsByTagNameNS(namespaceURI, localName);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(String name)
    {
        return getElement().hasAttribute(name);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#hasAttributeNS(java.lang.String, java.lang.String)
     */
    public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException
    {
        return getElement().hasAttributeNS(namespaceURI, localName);
    }

    // From here on down are DOM level 3 methods. We have to include them,
    // otherwise compiling won't work on JDK1.5. But we can't proxy them,
    // because the methods won't be there on JDK1.4. And we can't throw
    // NotImplementedException, because the build doesn't like that package..

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#getSchemaTypeInfo()
     */
    public TypeInfo getSchemaTypeInfo()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#setIdAttribute(java.lang.String, boolean)
     */
    public void setIdAttribute(String name, boolean isId) throws DOMException
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#setIdAttributeNS(java.lang.String, java.lang.String, boolean)
     */
    public void setIdAttributeNS(String namespaceURI, String localName, boolean isId)
        throws DOMException
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Element#setIdAttributeNode(org.w3c.dom.Attr, boolean)
     */
    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
    {
        throw new RuntimeException("Not implemented");
    }
}
