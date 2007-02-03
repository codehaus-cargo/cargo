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
package org.codehaus.cargo.module.webapp;

import java.lang.reflect.Constructor;

import org.codehaus.cargo.module.DescriptorTag;
import org.codehaus.cargo.module.internal.util.xml.AbstractNodeList;
import org.codehaus.cargo.util.CargoException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * TagNodeList is a nodelist constructed from a known tag and creates wrapped
 * nodes of a particular class automatically.
 *
 * @version $Id $
 */
public class TagNodeList extends AbstractNodeList
{
    /**
     * The class of wrapper.
     */
    private Class clazz;

    /**
     * @param rootElement the root element for the node list
     * @param tag the tag we are looking for
     * @param clazz the class of abstractelement wrapper to construct
     */
    public TagNodeList(Element rootElement, DescriptorTag tag, Class clazz)
    {
        super(rootElement, rootElement.getElementsByTagName(tag.getTagName()));
        this.clazz = clazz;
    }

    /**
     * {@inheritDoc}
     * @see org.w3c.dom.NodeList#item(int)
     */
    public Node item(int index)
    {
        try
        {
            Constructor cons = this.clazz.getConstructor(new Class[] {Element.class});
            return (Node) cons.newInstance(new Object[] {this.nodeList.item(index)});
        }
        catch (Exception e)
        {
            throw new CargoException("Couldn't create wrapped node item ", e);
        }
    }

}
