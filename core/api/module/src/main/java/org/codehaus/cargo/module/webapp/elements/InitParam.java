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

package org.codehaus.cargo.module.webapp.elements;

import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.WebXmlType;

/**
 * Context Parameter class for accessing context parameters in a <code>web.xml</code> file.
 * 
 * @version $Id: $
 */
public class InitParam extends WebXmlElement 
{
    /**
     * Constructor.
     * 
     * @param tag Web XML Tag definition
     */
    public InitParam(WebXmlTag tag)
    {
      super(tag);      
    }        
    
    /**
     * Constructor.
     * 
     * @param name param name
     * @param value param value
     */
    public InitParam(WebXmlTag tag, String name, String value)
    {
        this(tag);
        setParamName(name);
        setParamValue(value);
    }
    
    /**
     * @return param name
     */
    public String getParamName()
    {
        return getChild("param-name", getTag().getTagNamespace()).getText();
    }
    
    /**
     * 
     * @return param value
     */
    public String getParamValue()
    {
        return getChild("param-value", getTag().getTagNamespace()).getText();
    }
    
    /**
     * 
     * @param paramName param name
     */
    public void setParamName(String paramName)
    {
        child("param-name").setText(paramName);
    }    

    /**
     * 
     * @param paramValue param value
     */
    public void setParamValue(String paramValue)
    {
        child("param-value").setText(paramValue);
    }
    
}
