/* 
 * ========================================================================
 * 
 * Copyright 2004-2005 Vincent Massol.
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

import org.jdom.Element;
import org.jdom.xpath.XPath;

/**
 * Class used to work out how to 'identify' instances of DescrtiptorTags.
 * 
 * @version $Id: $
 */
public class Identifier
{
    /**
     * String XPath of how to navigate to the identifier field.
     */
    private String xpath;

    /**
     * Constructor.
     * 
     * @param xpath XPath to use to identify field
     */
    public Identifier(String xpath)
    {
        this.xpath = xpath;
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
            Element e = (Element) XPath.newInstance(xpath).selectSingleNode(element);

            return e.getText();
        }
        catch (Exception ex)
        {
            return "";
        }
    }
}
