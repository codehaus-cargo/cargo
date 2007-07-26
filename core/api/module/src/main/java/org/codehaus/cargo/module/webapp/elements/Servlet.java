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

import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.jdom.Element;

/**
 * @version $Id: $
 */
public class Servlet extends WebXmlElement
{
    /**
     * Constructor.
     * 
     * @param tag tag type
     */
    public Servlet(WebXmlTag tag)
    {
        super(tag);
    }

    /**
     * Constructor.
     * 
     * @param servletName name of the servlet
     * @param servletClass class of the servlet
     */
    public Servlet(WebXmlTag tag, String servletName, String servletClass)
    {
        super(tag);
        setServletName(servletName);
        setServletClass(servletClass);
    }

    /**
     * @return the servletClass
     */
    public String getServletClass()
    {
        Element e = child(WebXmlType.SERVLET_CLASS);
        return e.getText();
    }

    /**
     * @param servletClass the servletClass to set
     */
    public void setServletClass(String servletClass)
    {
        Element e = child(WebXmlType.SERVLET_CLASS);
        e.setText(servletClass);
    }

    /**
     * @return the servletName
     */
    public String getServletName()
    {
        Element e = child(WebXmlType.SERVLET_NAME);
        return e.getText();
    }

    /**
     * @param servletName the servletName to set
     */
    public void setServletName(String servletName)
    {
        Element e = child(WebXmlType.SERVLET_NAME);
        e.setText(servletName);
    }
}
