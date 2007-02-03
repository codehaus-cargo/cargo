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

import org.codehaus.cargo.module.internal.util.xml.AbstractElement;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Context Parameter class for accessing context parameters in a <code>web.xml</code> file.
 * 
 * @version $Id: $
 */
public class ContextParam extends AbstractElement implements Element
{
    /**
     * Constructor.
     * 
     * @param element that is the system property
     */
    public ContextParam(Element element)
    {
        super(element);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.internal.util.xml.AbstractElement#getElementId()
     */
    public String getElementId()
    {
        NodeList list = getElementsByTagName("param-name");

        // Check there was one
        if (list.getLength() == 0)
        {
            return null;
        }

        return getText((Element) list.item(0));
    }

}
