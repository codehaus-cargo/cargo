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
package org.codehaus.cargo.module.webapp.resin;

import org.codehaus.cargo.module.internal.util.xml.AbstractElement;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Wrapper class representing a resin-web resource-ref entry.
 * 
 * @version $Id: $
 */
public class ResourceRef extends AbstractElement implements Element
{
    /**
     * Constructor.
     * 
     * @param element the element that is a resource ref
     */
    public ResourceRef(Element element)
    {
        super(element);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.internal.util.xml.AbstractElement#getElementId()
     */
    public String getElementId()
    {
        NodeList list = getElementsByTagName("res-ref-name");

        // Check there was one
        if (list.getLength() == 0)
        {
            return null;
        }

        return getText((Element) list.item(0));
    }

    /**
     * Get the resource type that this is (eg JNDI).
     * 
     * @return the res-type value
     */
    public String getType()
    {
        NodeList list = getElementsByTagName("res-type");

        // Check there was one
        if (list.getLength() == 0)
        {
            return null;
        }

        return list.item(0).getNodeValue();
    }

}
