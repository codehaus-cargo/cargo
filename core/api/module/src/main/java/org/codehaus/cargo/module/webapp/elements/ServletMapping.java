/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.jdom2.Element;

/**
 */
public class ServletMapping extends WebXmlElement
{
    /**
     * Constructor.
     * @param tag Web Xml Tag definition
     */
    public ServletMapping(WebXmlTag tag)
    {
        super(tag);
    }

    /**
     * Get the URL Patterns.
     * @return URL Patterns
     */
    public List<String> getUrlPatterns()
    {
        List<Element> e = getChildren(WebXmlType.URL_PATTERN, this.getNamespace());
        List<String> result = new ArrayList<String>(e.size());
        for (Element ee : e)
        {
            result.add(ee.getText());
        }
        return result;
    }

    /**
     * Add a URL Pattern.
     * @param urlPattern The URL Pattern
     */
    public void addUrlPattern(String urlPattern)
    {
        if (!getUrlPatterns().contains(urlPattern))
        {
            Element e = child(WebXmlType.URL_PATTERN);
            e.setText(urlPattern);
        }
    }

    /**
     * Get the servlet name.
     * @return The servlet name
     */
    public String getServletName()
    {
        Element e = getChild(WebXmlType.SERVLET_NAME, this.getNamespace());
        return e == null ? null : e.getText();
    }

    /**
     * Set the servlet name.
     * @param servletName The servlet name
     */
    public void setServletName(String servletName)
    {
        Element e = child(WebXmlType.SERVLET_NAME);
        e.setText(servletName);
    }
}
