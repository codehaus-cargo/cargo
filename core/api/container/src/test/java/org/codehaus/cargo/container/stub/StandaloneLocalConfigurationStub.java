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
package org.codehaus.cargo.container.stub;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.util.XmlReplacement;

/**
 * Mock for {@link StandaloneLocalConfiguration}. We need a static mock rather than a dynamic mock
 * (which we could get using JMock for example) because we're testing factory classes which create
 * an object out of a class name.
 */
public class StandaloneLocalConfigurationStub extends AbstractLocalConfigurationStub
    implements StandaloneLocalConfiguration
{
    /**
     * {@inheritDoc}
     * @param home Configuration home.
     */
    public StandaloneLocalConfigurationStub(String home)
    {
        super(home);
    }

    /**
     * {@inheritDoc}
     * @return {@link ConfigurationType#STANDALONE}
     */
    @Override
    public ConfigurationType getType()
    {
        return ConfigurationType.STANDALONE;
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     * 
     * @param xmlReplacement Ignored.
     */
    @Override
    public void addXmlReplacement(XmlReplacement xmlReplacement)
    {
        // Nothing
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     * 
     * @param filename Ignored.
     * @param xpathExpression Ignored.
     * @param configurationPropertyName Ignored.
     */
    @Override
    public void addXmlReplacement(String filename, String xpathExpression,
        String configurationPropertyName)
    {
        // Nothing
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     * 
     * @param filename Ignored.
     * @param xpathExpression Ignored.
     * @param attributeName Ignored.
     * @param configurationPropertyName Ignored.
     */
    @Override
    public void addXmlReplacement(String filename, String xpathExpression, String attributeName,
        String configurationPropertyName)
    {
        // Nothing
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     * 
     * @param filename File in which to replace.
     * @param xpathExpression XPath expression to look for.
     * @param attributeName Attribute name to modify. If <code>null</code>, the node's contents
     * will be modified.
     * @param configurationPropertyName Name of the configuration property to set. The XML
     * replacement will be ignored if the property is set to <code>null</code>.
     * @param replacementBehavior Behavior if XPath expression doesn't match anything.
     */
    @Override
    public void addXmlReplacement(String filename, String xpathExpression, String attributeName,
        String configurationPropertyName, XmlReplacement.ReplacementBehavior replacementBehavior)
    {
        // Nothing
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     * 
     * @param filename Ignored.
     * @param xpathExpression Ignored.
     */
    @Override
    public void removeXmlReplacement(String filename, String xpathExpression)
    {
        // Nothing
    }

    /**
     * Doesn't do anything. {@inheritDoc}
     * 
     * @param filename Ignored.
     * @param xpathExpression Ignored.
     * @param attributeName Ignored.
     */
    @Override
    public void removeXmlReplacement(String filename, String xpathExpression,
        String attributeName)
    {
        // Nothing
    }

    /**
     * Returns an empty array. {@inheritDoc}
     */
    @Override
    public List<XmlReplacement> getXmlReplacements()
    {
        return new ArrayList<XmlReplacement>(0);
    }

}
