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
package org.codehaus.cargo.util;

import org.dom4j.Element;

/**
 * Exception raised when an XPath search returns no results.
 * 
 */
public class ElementNotFoundException extends RuntimeException
{

    /**
     * XPath query that failed to match.
     */
    private final String xpath;

    /**
     * Base element for search.
     */
    private final Element searched;

    /**
     * Constructor that provides a default message based on the XPath and element search.
     * 
     * @param xpath query that failed
     * @param searched context under which the query failed to match
     */
    public ElementNotFoundException(String xpath, Element searched)
    {
        super("XPath: " + xpath + " not found in element: " + searched.getName());
        this.xpath = xpath;
        this.searched = searched;
    }

    /**
     * Getter that returns the XML Element which didn't match on the given XPath.
     * 
     * @return context of the XPath query.
     */
    public Element getSearched()
    {
        return searched;
    }

    /**
     * Getter for the XPath that didn't match.
     * 
     * @return the XPath query that failed
     */
    public String getXpath()
    {
        return xpath;
    }

}
