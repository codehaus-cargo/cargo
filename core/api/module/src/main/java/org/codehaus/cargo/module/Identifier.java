/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.module;

import java.util.Map;

import org.codehaus.cargo.util.CargoException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

/**
 * Class used to work out how to 'identify' instances of DescrtiptorTags.
 * 
 * @version $Id$
 */
public class Identifier
{
    /**
     * String XPath of how to navigate to the identifier field.
     */
    private XPath xpath;

    /**
     * String XPath of how to navigate to the identifier field.<br><br>
     * This is to avoid having namespace problems with XPath (see
     * <a href="https://jira.codehaus.org/browse/CARGO-1175">CARGO-1175</a>)
     */
    private XPath xpathWithoutNamespace;

    /**
     * Constructor.
     * 
     * @param xpath XPath to use to identify field
     */
    public Identifier(String xpath)
    {
        try
        {
            this.xpath = XPath.newInstance(xpath);
        }
        catch (JDOMException ex)
        {
            throw new CargoException("Unexpected xpath error", ex);
        }
    }

    /**
     * 
     * @param namespaceMap The namespaceMap
     * @param xpath The xpath
     */
    public Identifier(Map<String, String> namespaceMap, String xpath)
    {
        try
        {
            this.xpath = XPath.newInstance(xpath);
            for (Map.Entry<String, String> namespaceEntry : namespaceMap.entrySet())
            {
                String ns = namespaceEntry.getKey();
                String uri = namespaceEntry.getValue();
                this.xpath.addNamespace(ns, uri);
            }
            this.xpathWithoutNamespace = XPath.newInstance(xpath);
        }
        catch (JDOMException ex)
        {
            throw new CargoException("Unexpected xpath error", ex);
        }
    }

    /**
     * Get the value of the identifier for a particular element.
     * 
     * @param element element to use
     * @return the value of the identifier
     */
    public String getIdentifier(Element element)
    {
        try
        {
            String identifier = xpath.valueOf(element);
            if (identifier == null || identifier.length() == 0)
            {
                identifier = this.xpathWithoutNamespace.valueOf(element);
            }
            return identifier;
        }
        catch (Exception ex)
        {
            return "";
        }
    }
}
