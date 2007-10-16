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

import java.util.List;

import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.jdom.Element;

/**
 * @version $Id: $
 */
public class FilterMapping extends WebXmlElement
{
    /**
     * Constructor.
     * @param tag Web Xml Tag definition
     */
    public FilterMapping(WebXmlTag tag)
    {
        super(tag);
    }

    /**
     * Get the URL Pattern.
     * @return URL Pattern
     */
    public String getUrlPattern()
    {
        Element e = getChild(WebXmlType.URL_PATTERN, this.getNamespace());
        if (e == null)
        {
            return null;
        }
        return e.getText();
    }

    /**
     * Set the URL Pattern.
     * @param urlPattern The URL Pattern
     */
    public void setUrlPattern(String urlPattern)
    {
        Element e = child(WebXmlType.URL_PATTERN);
        e.setText(urlPattern);
    }

    /**
     * Get the filter name.
     * @return The filter name
     */
    public String getFilterName()
    {
        Element e = getChild(WebXmlType.FILTER_NAME, this.getNamespace());
        return e.getText();
    }

    /**
     * Set the filter name.
     * @param filterName The filter name
     */
    public void setFilterName(String filterName)
    {
        Element e = child(WebXmlType.FILTER_NAME);
        e.setText(filterName);
    }
    
    /**
     * Get the servlet name.
     * @return The filter name
     */
    public String getServletName()
    {
        Element e = getChild(WebXmlType.SERVLET_NAME, this.getNamespace());
        return (e==null)?null:e.getText();
    }

    /**
     * Set the servlet name.
     * @param servletName The filter name
     */
    public void setServletName(String servletName)
    {
        Element e = child(WebXmlType.SERVLET_NAME);
        e.setText(servletName);
    }
     
    /**
     * Add a dispatcher element.
     * @param dispatcherName name of the dispatcher.
     */
    public void addDispatcher(String dispatcherName)
    {                
        Element child = new Element(WebXmlType.DISPATCHER, this.getNamespace());
        child.setText(dispatcherName);
        this.getChildren().add(child);      
    }

    /**
     * @return a list of dispatcher names.
     */
    public String[] getDispatchers()
    {
      List l = getChildren(WebXmlType.DISPATCHER, getTag().getTagNamespace());
      
      String[] items = new String[l.size()];
      
      for(int i=0; i<l.size(); i++)
      {
        items[i] = ((Element)l.get(i)).getText();
      }
      
      return items;
    }
}
