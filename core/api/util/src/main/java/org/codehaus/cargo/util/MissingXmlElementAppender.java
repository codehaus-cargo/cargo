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
package org.codehaus.cargo.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class detects missing nodes of a DOM document based on an XPath expression and
 * appends the missing branch.
 */
public class MissingXmlElementAppender
{
    /**
     * Document that may have missing nodes
     */
    private final Document document;

    /**
     * XPath, split up into path elements
     */
    private final LinkedList<String> expressionNodes = new LinkedList<String>();

    /**
     * 
     * @param document Document that may have missing nodes
     * @param xPathString XPath that needs to be present in the document
     */
    public MissingXmlElementAppender(Document document, String xPathString)
    {
        this.document = document;

        String regex = "/(?=(?:[^']*'[^']*')*[^']*$)";
        this.expressionNodes.addAll(Arrays.asList(xPathString.split(regex)));
    }

    /**
     * Getter
     * @return documnet given via the constructor
     */
    public Document getDocument()
    {
        return document;
    }

    /**
     * Appends the missing nodes.
     * 
     * @return the common node of document and XPath
     * @throws XPathExpressionException if anything goes wrong
     */
    public Node append() throws XPathExpressionException
    {
        LinkedList<String> missingNodes = new LinkedList<String>();
        Node evaluate = getMostCommonNode(missingNodes);

        if (evaluate != null && !missingNodes.isEmpty())
        {
            evaluate = appendMissingElements(missingNodes, evaluate);
        }

        return evaluate;
    }

    /**
     * Find the common node of the given document and the XPath.
     * Based on this node the hierarchy will be appended.
     * 
     * @param missingNodes as a side effect the node elements are collected
     * @return common node of the given document and the XPath
     * @throws XPathExpressionException if anything goes wrong
     */
    private Node getMostCommonNode(LinkedList<String> missingNodes) throws XPathExpressionException
    {
        XPath xPath = XPathFactory.newInstance().newXPath();

        Object evaluate;
        do
        {
            String expression = toXPathString(expressionNodes);
            XPathExpression xPathExpression = xPath.compile(expression);
            evaluate = xPathExpression.evaluate(document, XPathConstants.NODE);

            if (evaluate == null)
            {
                String lastNode = expressionNodes.removeLast();
                missingNodes.addFirst(lastNode);
            }
        }
        while (evaluate == null && !expressionNodes.isEmpty());

        if (evaluate != null)
        {
            return (Node) evaluate;
        }
        return null;
    }

    /**
     * Assemble a XPath string based on a list of elements
     * @param expressionNodes split up XPath elements
     * @return XPath string
     */
    private String toXPathString(List<String> expressionNodes)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (Iterator<String> iterator = expressionNodes.iterator(); iterator.hasNext();)
        {
            String expressionNode = iterator.next();
            stringBuilder.append(expressionNode);
            if (iterator.hasNext())
            {
                stringBuilder.append("/");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Adds the elements of missingNodes as element hierarchy to the node
     * @param missingNodes collected list of the missing elements
     * @param node target of the additions
     * @return the last node added to the document
     */
    private Node appendMissingElements(List<String> missingNodes, Node node)
    {
        Pattern elementOnlyPattern = Pattern.compile("([^\\[]+)\\[.+");
        Node result = node;

        for (String missingNode : missingNodes)
        {
            if (!missingNode.startsWith("@"))
            {
                Matcher matcher = elementOnlyPattern.matcher(missingNode);
                if (matcher.matches())
                {
                    missingNode = matcher.group(1);
                }

                Element newElement = document.createElement(missingNode);
                result.appendChild(newElement);
                result = newElement;
            }
        }

        return result;
    }
}
