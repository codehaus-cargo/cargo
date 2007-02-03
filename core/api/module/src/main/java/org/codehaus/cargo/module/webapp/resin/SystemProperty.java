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
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Wrapper Class representing a resin-web.xml system property.
 * 
 * @version $Id: $
 */
public class SystemProperty extends AbstractElement
{
    /**
     * Constructor.
     * 
     * @param element that is the system property
     */
    public SystemProperty(Element element)
    {
        super(element);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.internal.util.xml.AbstractElement#getElementId()
     */
    public String getElementId()
    {
        return ((Attr) getAttributes().item(0)).getName();
    }

    /**
     * Get the system property value.
     * 
     * @return the value
     */
    public String getValue()
    {
        return ((Attr) getAttributes().item(0)).getValue();
    }

    /**
     * Set the system property value.
     * 
     * @param value to be set
     */
    public void setValue(String value)
    {
        ((Attr) getAttributes().item(0)).setValue(value);
    }
}
