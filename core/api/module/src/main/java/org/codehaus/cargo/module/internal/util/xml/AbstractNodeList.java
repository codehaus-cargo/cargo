/*
 * ========================================================================
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
package org.codehaus.cargo.module.internal.util.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Wrapped NodeList that provides accessors and iterators.
 *
 * @version $Id: $
 */
public abstract class AbstractNodeList implements NodeList
{
    /**
     * The wrapped nodelist.
     */
    protected NodeList nodeList;

    /**
     * The root (parent) element for all of the nodes in the list.
     */
    protected Element rootElement;

    /**
     * Constructor.
     *
     * Create a wrapped nodelist
     *
     * @param rootElement the root element of all the nodelist items
     * @param nodeList the nodelist to wrap
     */
    public AbstractNodeList(Element rootElement, NodeList nodeList)
    {
        this.rootElement = rootElement;
        this.nodeList    = nodeList;
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.NodeList#item(int)
     */
    public abstract Node item(int index);

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.NodeList#getLength()
     */
    public int getLength()
    {
        return this.nodeList.getLength();
    }

    /**
     * Create an iterator over the list.
     * @return the iterator
     */
    public Iterator iterator()
    {
        List elements = new ArrayList();

        for (int i = 0; i < this.nodeList.getLength(); i++)
        {
            elements.add(item(i));
        }
        return elements.iterator();
    }

    /**
     *
     * Get an element by comparing the provided name to each element in turn.
     *
     * @param name in the name of the element
     * @return the found element, or null if not found
     */
    public AbstractElement getByElementId(String name)
    {
        for (int i = 0; i < this.getLength(); i++)
        {
            AbstractElement sp = (AbstractElement) item(i);

            if (sp.getElementId().equals(name))
            {
                return sp;
            }
        }
        return null;
    }

    /**
     * Add an item to the list.
     * @param item in the item to add
     */
    public void add(Element item)
    {
        Node n = this.rootElement.getOwnerDocument().importNode(item, true);
        if (this.getLength() == 0)
        {
            this.rootElement.appendChild(n);
        }
        else
        {
            // Keep alike items together
            AbstractNode an = (AbstractNode) item(getLength() - 1);
            this.rootElement.insertBefore(n, an.getNode());
        }
    }

    /**
     * Remove an item from the list.
     * @param item in the item to remove
     */
    public void remove(Element item)
    {
        Element itemToRemove = item;
        if (item instanceof AbstractNode)
        {
            itemToRemove = (Element) ((AbstractNode) item).getNode();
        }
        this.rootElement.removeChild(itemToRemove);
    }
}
