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

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

/**
 * Simple wrapped abstract node.
 *
 * @version $Id: $
 */
public abstract class AbstractNode implements Node
{
    /**
     * The wrapped node.
     */
    protected Node node;

    /**
     * @param node in the node to wrap
     */
    protected AbstractNode(Node node)
    {
        this.node = node;
    }

    /**
     * @return the underlying unwrapped node
     */
    public Node getNode()
    {
        return this.node;
    }

    /**
     * @param node in the possibly wrapped node
     * @return the underlying unwrapped node
     */
    protected Node getNode(Node node)
    {
        if (node instanceof AbstractNode)
        {
            return ((AbstractNode) node).getNode();
        }
        return node;
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getNodeName()
     */
    public String getNodeName()
    {
        return getNode().getNodeName();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getNodeValue()
     */
    public String getNodeValue() throws DOMException
    {
        return getNode().getNodeValue();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
     */
    public void setNodeValue(String nodeValue) throws DOMException
    {
        getNode().setNodeValue(nodeValue);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getNodeType()
     */
    public short getNodeType()
    {
        return getNode().getNodeType();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getParentNode()
     */
    public Node getParentNode()
    {
        return getNode().getParentNode();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getChildNodes()
     */
    public NodeList getChildNodes()
    {
        return getNode().getChildNodes();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getFirstChild()
     */
    public Node getFirstChild()
    {
        return getNode().getFirstChild();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getLastChild()
     */
    public Node getLastChild()
    {
        return getNode().getLastChild();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getPreviousSibling()
     */
    public Node getPreviousSibling()
    {
        return getNode().getPreviousSibling();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getNextSibling()
     */
    public Node getNextSibling()
    {
        return getNode().getNextSibling();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getAttributes()
     */
    public NamedNodeMap getAttributes()
    {
        return getNode().getAttributes();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getOwnerDocument()
     */
    public Document getOwnerDocument()
    {
        return getNode().getOwnerDocument();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    public Node insertBefore(Node newChild, Node refChild) throws DOMException
    {
        return getNode().insertBefore(getNode(newChild), getNode(refChild));
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException
    {
        return getNode().replaceChild(getNode(newChild), getNode(oldChild));
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
     */
    public Node removeChild(Node oldChild) throws DOMException
    {
        return getNode().removeChild(getNode(oldChild));
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
     */
    public Node appendChild(Node newChild) throws DOMException
    {
        return getNode().appendChild(getNode(newChild));
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#hasChildNodes()
     */
    public boolean hasChildNodes()
    {
        return getNode().hasChildNodes();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#cloneNode(boolean)
     */
    public Node cloneNode(boolean deep)
    {
        return getNode().cloneNode(deep);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#normalize()
     */
    public void normalize()
    {
        getNode().normalize();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
     */
    public boolean isSupported(String feature, String version)
    {
        return getNode().isSupported(feature, version);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getNamespaceURI()
     */
    public String getNamespaceURI()
    {
        return getNode().getNamespaceURI();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getPrefix()
     */
    public String getPrefix()
    {
        return getNode().getPrefix();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#setPrefix(java.lang.String)
     */
    public void setPrefix(String prefix) throws DOMException
    {
        getNode().setPrefix(prefix);
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getLocalName()
     */
    public String getLocalName()
    {
        return getNode().getLocalName();
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#hasAttributes()
     */
    public boolean hasAttributes()
    {
        return getNode().hasAttributes();
    }

    //  From here on down are DOM level 3 methods. We have to include them,
    // otherwise compiling won't work on JDK1.5. But we can't proxy them,
    // because the methods won't be there on JDK1.4. And we can't throw
    // NotImplementedException, because the build doesn't like that package..

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getBaseURI()
     */
    public String getBaseURI()
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#compareDocumentPosition(org.w3c.dom.Node)
     */
    public short compareDocumentPosition(Node other) throws DOMException
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getTextContent()
     */
    public String getTextContent() throws DOMException
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#setTextContent(java.lang.String)
     */
    public void setTextContent(String textContent) throws DOMException
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#isSameNode(org.w3c.dom.Node)
     */
    public boolean isSameNode(Node other)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#lookupPrefix(java.lang.String)
     */
    public String lookupPrefix(String namespaceURI)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#isDefaultNamespace(java.lang.String)
     */
    public boolean isDefaultNamespace(String namespaceURI)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#lookupNamespaceURI(java.lang.String)
     */
    public String lookupNamespaceURI(String prefix)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#isEqualNode(org.w3c.dom.Node)
     */
    public boolean isEqualNode(Node arg)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getFeature(java.lang.String, java.lang.String)
     */
    public Object getFeature(String feature, String version)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#setUserData(java.lang.String, java.lang.Object, org.w3c.dom.UserDataHandler)
     */
    public Object setUserData(String key, Object data, UserDataHandler handler)
    {
        throw new RuntimeException("Not implemented");
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.Node#getUserData(java.lang.String)
     */
    public Object getUserData(String key)
    {
        throw new RuntimeException("Not implemented");
    }
}
