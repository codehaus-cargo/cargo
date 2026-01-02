/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
     * Get the dispatchers.
     * @return Dispatchers
     */
    public List<String> getDispatchers()
    {
        List<Element> e = getChildren(WebXmlType.DISPATCHER, this.getNamespace());
        List<String> result = new ArrayList<String>(e.size());
        for (Element ee : e)
        {
            result.add(ee.getText());
        }
        return result;
    }

    /**
     * Add a dispatcher.
     * @param dispatcher The dispatcher
     */
    public void addDispatcher(String dispatcher)
    {
        if (!getDispatchers().contains(dispatcher))
        {
            Element e = child(WebXmlType.DISPATCHER);
            e.setText(dispatcher);
        }
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
}
