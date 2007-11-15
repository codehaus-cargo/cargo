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

import java.util.Iterator;
import java.util.List;

import org.codehaus.cargo.module.webapp.WebXmlTag;
import org.codehaus.cargo.module.webapp.WebXmlType;
import org.jdom.Element;

/**
 * @version $Id: $
 */
public class Filter extends WebXmlElement
{
    /**
     * Constructor.
     * @param tag XML tag definition
     */
    public Filter(WebXmlTag tag)
    {
        super(tag);
    }

    /**
     * Constructor.
     * @param tag XML tag definition
     * @param filterName name of the filter
     * @param filterClass filter class
     */
    public Filter(WebXmlTag tag, String filterName, String filterClass)
    {
        super(tag);
        setFilterName(filterName);
        setFilterClass(filterClass);
    }

    /**
     * Get the filter class.
     * @return filter class
     */
    public String getFilterClass()
    {
        Element e = child(WebXmlType.FILTER_CLASS);
        return e.getText();
    }

    /**
     * Set the filter class.
     * @param filterClass The classname to use
     */
    public void setFilterClass(String filterClass)
    {
        Element e = child(WebXmlType.FILTER_CLASS);
        e.setText(filterClass);
    }
    /**
     * Get the filter name.
     * @return The filter name
     */
    public String getFilterName()
    {
        Element e = child(WebXmlType.FILTER_NAME);
        return e.getText();
    }

    /**
     * @param filterName Name of the filter to use
     */
    public void setFilterName(String filterName)
    {
        Element e = child(WebXmlType.FILTER_NAME);
        e.setText(filterName);
    }

    /**
     * @return List of init params
     */
    public List getInitParams()
    {
        return getChildren("init-param", getTag().getTagNamespace());
    }

    /**
     * @param name Name of the Init param
     * @return InitParam
     */
    public InitParam getInitParam(String name)
    {
        for (Iterator i = getInitParams().iterator(); i.hasNext();)
        {
            InitParam initParam = (InitParam) i.next();
            if (initParam.getParamName().equals(name))
            {
                return initParam;
            }
        }
        return null;
    }
}
