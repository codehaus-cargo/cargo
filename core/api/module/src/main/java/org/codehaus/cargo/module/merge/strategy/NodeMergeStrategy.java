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
package org.codehaus.cargo.module.merge.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.codehaus.cargo.module.AbstractDescriptorIo;
import org.codehaus.cargo.module.merge.AbstractMergeSet;
import org.codehaus.cargo.module.merge.MergePair;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.xpath.XPathAPI;

/**
 * A merging strategy that can be used to combine two XML documents together.
 * 
 * @version $Id $
 */
public class NodeMergeStrategy extends MergeStrategy
{
    /**
     * The template for the output.
     */
    private Element template;

    /**
     * Constructor.
     * @param template in the template to use in the merge
     */
    public NodeMergeStrategy(Element template)
    {
        this.template = template;
    }

    /**
     * Constructor.
     * @param template in the template to use in the merge (XML)
     * @throws ParserConfigurationException if parser error
     * @throws IOException if IO Exception
     * @throws SAXException if SAX Exception
     */
    public NodeMergeStrategy(InputStream template) throws SAXException, IOException,
        ParserConfigurationException
    {
        this.template = AbstractDescriptorIo.createDocumentBuilder().parse(template)
            .getDocumentElement();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.strategy.MergeStrategy#inRight(org.codehaus.cargo.module.merge.AbstractMergeSet, org.w3c.dom.Element)
     */
    public int inRight(AbstractMergeSet set, Element element)
    {
        set.add(element);
        return 1;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.module.merge.strategy.MergeStrategy#inBoth(org.codehaus.cargo.module.merge.AbstractMergeSet, org.codehaus.cargo.module.merge.MergePair)
     */
    public int inBoth(AbstractMergeSet set, MergePair pair)
    {
        // Merge common items by overwriting them
        set.remove(pair.left);

        Element newElement = (Element) this.template.cloneNode(true);
        applyValues(newElement, pair);
        set.add(newElement);

        return 1;
    }

    /**
     * Apply some merge parameters on the element passed in, using the
     * left and write elements in the merge pair.
     * 
     * @param element the element to search and replace on
     * @param pair the mergepair containing left and right
     */
    private void applyValues(Element element, MergePair pair)
    {        
        NodeList childNodes = element.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++)
        {
            Node n = childNodes.item(i);
            if (n instanceof Element)
            {
                applyValues((Element) n, pair);
            }
        }

        String str = element.getFirstChild().getNodeValue();        

        Pattern pat = Pattern.compile("\\$(left:|right:)[^ \\t]*");

        Matcher m = pat.matcher(str);

        while (m.find())
        {            
            str = m.replaceFirst(replaceValue(m.group(), pair));
            m = pat.matcher(str);            
        }

        element.getFirstChild().setNodeValue(str);
    }

    /**
     * Replace a value.
     * @param string - the expression
     * @param pair - the pair of nodes
     * @return the replaced string
     */
    private String replaceValue(String string, MergePair pair)
    {
        String xPath;
        Element element;
        
        String result = "";
        
        if (string.startsWith("$left:"))
        {
            xPath = string.substring(6);
            element = pair.getLeftElement();
        }
        else if (string.startsWith("$right:"))
        {
            xPath = string.substring(7);
            element = pair.getRightElement();
        }
        else
        {
            // Make sure we don't loop forever!
            return "";
        }
        try
        {

            Node nestedText = XPathAPI.selectSingleNode(element, xPath).getFirstChild();
            if (nestedText != null)
            {
                result = nestedText.getNodeValue();
            }                                   
        }
        catch (TransformerException e)
        {
            result = e.getMessage();
        }
        return result;
    }

}
