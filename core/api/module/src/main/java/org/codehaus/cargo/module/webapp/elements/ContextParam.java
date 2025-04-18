/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

/**
 * Context Parameter class for accessing context parameters in a <code>web.xml</code> file.
 */
public class ContextParam extends WebXmlElement
{
    /**
     * Constructor.
     * 
     * @param tag web xml tag
     */
    public ContextParam(WebXmlTag tag)
    {
        super(tag);
    }

    /**
     * Constructor.
     * 
     * @param tag web xml tag
     * @param name Name of the parameter
     * @param value Value for the parameter
     */
    public ContextParam(WebXmlTag tag, String name, String value)
    {
        this(tag);
        setParamName(name);
        setParamValue(value);
    }

    /**
     * @return String of the parameter name
     */
    public String getParamName()
    {
        return getChild("param-name", getTag().getTagNamespace()).getText();
    }

    /**
     * @return String of the parameter value
     */
    public String getParamValue()
    {
        return getChild("param-value", getTag().getTagNamespace()).getText();
    }

    /**
     * Set the parameter name.
     * @param paramName Name of the parameter
     */
    public void setParamName(String paramName)
    {
        child("param-name").setText(paramName);
    }

    /**
     * Set the parameter value.
     * @param paramValue Value for the parameter
     */
    public void setParamValue(String paramValue)
    {
        child("param-value").setText(paramValue);
    }

}
