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
package org.codehaus.cargo.module.webapp.resin.elements;

import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.webapp.resin.ResinWebXmlTag;
import org.codehaus.cargo.module.webapp.resin.ResinWebXmlType;
import org.jdom.Attribute;

/**
 * Wrapper Class representing a resin-web.xml system property.
 * 
 * @version $Id: $
 */
public class SystemProperty extends DescriptorElement
{
  
    /**
     * Constructor.
     * 
     * @param tag Resin Web XML Tag type
     */
    public SystemProperty(ResinWebXmlTag tag)
    {
        super(tag);      
    }
  
  /**
   * Constructor.
   */
    public SystemProperty()
    {
        this((ResinWebXmlTag) ResinWebXmlType.getInstance().getTagByName(
            ResinWebXmlTag.SYSTEM_PROPERTY));
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.IdentifiableElement#getElementId()
     */
    public String getElementId()
    {
        return ((Attribute) getAttributes().get(0)).getName();
    }

    /**
     * Get the system property value.
     * 
     * @return the value
     */
    public String getValue()
    {
        return ((Attribute) getAttributes().get(0)).getValue();
    }

    /**
     * Set the system property value.
     * 
     * @param value to be set
     */
    public void setValue(String value)
    {
        ((Attribute) getAttributes().get(0)).setValue(value);        
    }
}
