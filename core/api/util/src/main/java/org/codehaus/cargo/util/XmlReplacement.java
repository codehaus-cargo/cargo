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
package org.codehaus.cargo.util;

/**
 * XML replacement for the {@link FileHandler}.
 */
public class XmlReplacement
{
    /**
     * Define how the XmlReplacement behaves XPath expression doesn't match anything.
     */
    public enum ReplacementBehavior
    {
        /**
         * Continue, if the XPath can't be found
         */
        IGNORE_IF_NON_EXISTING,

        /**
         * Throw exception, if the XPath can't be found
         */
        THROW_EXCEPTION,

        /**
         * Add missing nodes, if the XPath only matches on any upper level
         */
        ADD_MISSING_NODES;
    }

    /**
     * File name.
     */
    private String file;

    /**
     * XPath expression.
     */
    private String xpathExpression;

    /**
     * XML attribute name.
     */
    private String attributeName;

    /**
     * Behavior if XPath expression doesn't match anything.
     */
    private ReplacementBehavior replacementBehavior = ReplacementBehavior.THROW_EXCEPTION;

    /**
     * Value or property name.
     */
    private String value;

    /**
     * Empty constructor.
     */
    public XmlReplacement()
    {
        // Nothing
    }

    /**
     * Saves the attributes for this XML replacement.
     * 
     * @param file File name.
     * @param xpathExpression XPath expression.
     * @param attributeName XML attribute name.
     * @param replacementBehavior Behavior if XPath expression doesn't match anything.
     * @param value Value or property name.
     */
    public XmlReplacement(String file, String xpathExpression, String attributeName,
        ReplacementBehavior replacementBehavior, String value)
    {
        this.file = file;
        this.xpathExpression = xpathExpression;
        this.attributeName = attributeName;
        this.replacementBehavior = replacementBehavior;
        this.value = value;
    }

    /**
     * @return File name.
     */
    public String getFile()
    {
        return file;
    }

    /**
     * @param file File name.
     */
    public void setFile(String file)
    {
        this.file = file;
    }

    /**
     * @return XPath expression.
     */
    public String getXpathExpression()
    {
        return xpathExpression;
    }

    /**
     * @param xpathExpression XPath expression.
     */
    public void setXpathExpression(String xpathExpression)
    {
        this.xpathExpression = xpathExpression;
    }

    /**
     * @return XML attribute name.
     */
    public String getAttributeName()
    {
        return attributeName;
    }

    /**
     * @param attributeName XML attribute name.
     */
    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }

    /**
     * @return Behavior if XPath expression doesn't match anything.
     */
    public ReplacementBehavior getReplacementBehavior()
    {
        return replacementBehavior;
    }

    /**
     * @param replacementBehavior Behavior if XPath expression doesn't match anything.
     */
    public void setReplacementBehavior(ReplacementBehavior replacementBehavior)
    {
        this.replacementBehavior = replacementBehavior;
    }

    /**
     * @return Value or property name.
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value Value or property name.
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "XmlReplacement[fileName='" + file
            + "',xpathExpression='" + xpathExpression
            + "',attributeName='" + attributeName
            + "',value='" + value + "']";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        final XmlReplacement other = (XmlReplacement) obj;
        return this.toString().equals(other.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
    }
}
