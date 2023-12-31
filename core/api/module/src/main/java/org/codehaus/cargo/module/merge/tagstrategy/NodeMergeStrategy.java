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
package org.codehaus.cargo.module.merge.tagstrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.cargo.module.Descriptor;
import org.codehaus.cargo.module.DescriptorElement;
import org.codehaus.cargo.module.DescriptorType;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.xpath.XPath;

/**
 * A merging strategy that can be used to combine two XML documents together.
 */
public class NodeMergeStrategy implements MergeStrategy
{
    /**
     * The template for the output.
     */
    private Element template;

    /**
     * Constructor.
     * 
     * @param type the descriptor type
     * @param template in the template to use in the merge
     */
    public NodeMergeStrategy(DescriptorType type, Element template)
    {
        if (template == null)
        {
            throw new IllegalArgumentException("Template must not be null");
        }
        this.template = template;
    }

    /**
     * Constructor.
     * 
     * @param type Descriptor Type
     * @param stream content input stream
     * @throws IOException if IO Exception
     * @throws JDOMException if parser error
     */
    public NodeMergeStrategy(DescriptorType type, InputStream stream) throws
        IOException, JDOMException
    {
        this.template = type.getDescriptorIo().parseXml(stream).getRootElement();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inBoth(Descriptor target, DescriptorElement left, DescriptorElement right)
    {
        // Merge common items by overwriting them
        int idx = target.getDocument().getRootElement().getContent().indexOf(left);
        target.getDocument().getRootElement().removeContent(left);

        Element newElement = (Element) this.template.clone();
        applyValues(newElement, left, right);
        target.getRootElement().addContent(idx, newElement);

        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inLeft(Descriptor target, DescriptorElement left)
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int inRight(Descriptor target, DescriptorElement right)
    {
        target.addElement(right.getTag(), right, target.getRootElement());
        return 1;
    }

    /**
     * Apply some merge parameters on the element passed in, using the left and write elements in
     * the merge pair.
     * 
     * @param content the content to apply to
     * @param left The Left hand element to use
     * @param right The Right hand element to use
     */
    private void applyValues(Content content, DescriptorElement left, DescriptorElement right)
    {
        if (content instanceof Element)
        {
            Element element = (Element) content;
            List<Content> childNodes = element.getContent();

            for (Content n : childNodes)
            {
                applyValues(n, left, right);
            }
        }
        else if (content instanceof Text)
        {
            Pattern pat = Pattern.compile("\\$(left:|right:)[^ ,\\t]*");

            Text element = (Text) content;
            Matcher m = pat.matcher(element.getText());
            StringBuffer sb = new StringBuffer();
            while (m.find())
            {
                String repl = replaceValue(m.group(), left, right).replaceAll("\\$", "\\\\\\$");
                m.appendReplacement(sb, repl);
            }
            m.appendTail(sb);

            element.setText(sb.toString());
        }
    }

    /**
     * Replace a value.
     * 
     * @param string - the expression
     * @param left The left hand node
     * @param right The right hand node
     * @return the replaced string
     */
    private String replaceValue(String string, DescriptorElement left, DescriptorElement right)
    {
        String xPath;
        Element element;

        String result = "";

        if (string.startsWith("$left:"))
        {
            xPath = string.substring(6);
            element = left;
        }
        else if (string.startsWith("$right:"))
        {
            xPath = string.substring(7);
            element = right;
        }
        else
        {
            // Make sure we don't loop forever!
            return "";
        }
        try
        {
            // CARGO-1175: Avoid XPath and namespace problems
            String nsPrefix = null;
            if (!element.getNamespaceURI().isEmpty())
            {
                nsPrefix = element.getNamespacePrefix();
                if (nsPrefix.isEmpty() || !xPath.startsWith(nsPrefix))
                {
                    if (nsPrefix.isEmpty())
                    {
                        nsPrefix = "cargo-xs";
                    }
                    xPath = nsPrefix + ":" + xPath;
                }
            }
            XPath xp = XPath.newInstance(xPath);
            if (nsPrefix != null)
            {
                xp.addNamespace(nsPrefix, element.getNamespaceURI());
            }
            Element nestedText = (Element) xp.selectSingleNode(element);
            if (nestedText != null)
            {
                result = nestedText.getText();
            }
        }
        catch (JDOMException e)
        {
            result = e.getMessage();
        }
        return result;
    }

}
