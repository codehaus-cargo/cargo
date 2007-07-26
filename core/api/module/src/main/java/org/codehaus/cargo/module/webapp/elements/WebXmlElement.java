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
package org.codehaus.cargo.module.webapp.elements;

import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.jdom.Element;

/**
 * @version $Id: $
 */
public class WebXmlElement extends DescriptorElement
{    
  
    /**
     * Constructor.
     * @param tag Web Xml Tag Definition
     */
    public WebXmlElement(WebXmlTag tag)
    {
        super(tag);
    }
  
    /**
     * Constructor.
     * @param tag Web Xml Tag Definition
     * @param element XML Element
     */
    public WebXmlElement(WebXmlTag tag, Element element)
    {    
        super(tag, element);
    
        this.addContent(element.detach());
    }
    
  /**
   * @param string Child name
   * @return child element
   */
    protected Element child(String string)
    {
        Element child = this.getChild(string, this.getNamespace());
        if (child == null)
        {
            child = new Element(string, this.getNamespace());
            this.getChildren().add(child);
        }
        return child;
    } 

}
