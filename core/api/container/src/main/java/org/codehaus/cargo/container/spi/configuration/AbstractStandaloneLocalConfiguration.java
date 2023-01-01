/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.spi.configuration;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler.XmlReplacementDetails;
import org.codehaus.cargo.util.XmlReplacement;

/**
 * Base implementation for a standalone local configuration.
 */
public abstract class AbstractStandaloneLocalConfiguration extends AbstractLocalConfiguration
    implements StandaloneLocalConfiguration
{

    /**
     * The XML replacements for the configuration files. The first map's key is the file name,
     * the inner map's key is the {@link XmlReplacementDetails} and value the configuration
     * property.
     */
    private Map<String, Map<XmlReplacementDetails, String>> xmlReplacements;

    /**
     * {@inheritDoc}
     * @see AbstractLocalConfiguration#AbstractLocalConfiguration(String)
     */
    public AbstractStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        // Add all required properties that are common to all standalone configurations
        setProperty(GeneralPropertySet.LOGGING, LoggingLevel.MEDIUM.getLevel());
        this.xmlReplacements = new HashMap<String, Map<XmlReplacementDetails, String>>();
    }

    /**
     * Perform the XML replacements for the specified container.
     * @param container the container to configure
     */
    protected void performXmlReplacements(LocalContainer container)
    {
        boolean ignoreNonExistingProperties = Boolean.parseBoolean(
            getPropertyValue(GeneralPropertySet.IGNORE_NON_EXISTING_PROPERTIES));

        for (Map.Entry<String, Map<XmlReplacementDetails, String>> xmlReplacementDetails
            : this.xmlReplacements.entrySet())
        {
            Set<XmlReplacement> replacements = new HashSet<XmlReplacement>();
            String destinationFile = getFileHandler().append(
                container.getConfiguration().getHome(), xmlReplacementDetails.getKey());

            for (Map.Entry<XmlReplacementDetails, String> xmlReplacementDetail
                    : xmlReplacementDetails.getValue().entrySet())
            {
                String value = container.getConfiguration().getPropertyValue(
                    xmlReplacementDetail.getValue());

                if (value == null)
                {
                    value = xmlReplacementDetail.getValue();
                }

                XmlReplacementDetails key = xmlReplacementDetail.getKey();
                XmlReplacement xmlReplacement = new XmlReplacement(destinationFile,
                    key.getXpathExpression(), key.getAttributeName(), key.getReplacementBehavior(),
                        value);

                if (ignoreNonExistingProperties)
                {
                    xmlReplacement.setReplacementBehavior(
                            XmlReplacement.ReplacementBehavior.IGNORE_IF_NON_EXISTING);
                }

                replacements.add(xmlReplacement);
            }

            if (!replacements.isEmpty())
            {
                XmlReplacement[] replacementsArray = new XmlReplacement[replacements.size()];
                replacementsArray = replacements.toArray(replacementsArray);
                getFileHandler().replaceInXmlFile(replacementsArray);
            }
        }
    }

    /**
     * Set up the configuration directory (create it and clean it). We clean it because we want to
     * be sure the container starts with the same set up every time and there's no side effects
     * introduced by a previous run or someone modifying some files in there.
     * 
     * <p>
     * Note: We only clean the configuration directory if it's empty or if there is a Cargo
     * timestamp file. This is to prevent deleting not empty directories if the user has mistakenly
     * pointed the configuration dir to an existing location.
     * </p>
     * 
     * @throws IOException if the directory cannot be created
     */
    protected void setupConfigurationDir() throws IOException
    {
        String timestampFile = getFileHandler().append(getHome(), ".cargo");

        // Start by cleaning the configuration directory. Do it only if there's already a Cargo
        // timestamp or if the configuration directory exists but is empty or if the configuration
        // directory doesn't exist.
        boolean isEmpty = false;
        if (!getFileHandler().exists(getHome()))
        {
            isEmpty = true;
        }
        else if (!getFileHandler().isDirectory(getHome()))
        {
            isEmpty = true;
        }
        else if (getFileHandler().isDirectoryEmpty(getHome()))
        {
            isEmpty = true;
        }
        else if (getFileHandler().exists(timestampFile))
        {
            isEmpty = true;
        }

        if (isEmpty)
        {
            getFileHandler().delete(getHome());

            getFileHandler().mkdirs(getHome());

            // Create Cargo timestamp file
            getFileHandler().createFile(timestampFile);
        }
        else
        {
            throw new ContainerException("Invalid configuration dir [" + getHome() + "]. "
                + "When using standalone configurations, the configuration dir must point to an "
                + "empty directory - Except if the configuration was created by Cargo.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addXmlReplacement(XmlReplacement xmlReplacement)
    {
        Map<XmlReplacementDetails, String> fileReplacements =
            this.xmlReplacements.get(xmlReplacement.getFile());
        if (fileReplacements == null)
        {
            fileReplacements = new HashMap<XmlReplacementDetails, String>();
            this.xmlReplacements.put(xmlReplacement.getFile(), fileReplacements);
        }

        fileReplacements.put(new XmlReplacementDetails(xmlReplacement.getXpathExpression(),
            xmlReplacement.getAttributeName(), xmlReplacement.getReplacementBehavior()),
                xmlReplacement.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addXmlReplacement(String filename, String xpathExpression,
        String configurationPropertyName)
    {
        addXmlReplacement(filename, xpathExpression, null, configurationPropertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addXmlReplacement(String filename, String xpathExpression, String attributeName,
        String configurationPropertyName)
    {
        Map<XmlReplacementDetails, String> fileReplacements = this.xmlReplacements.get(filename);
        if (fileReplacements == null)
        {
            fileReplacements = new HashMap<XmlReplacementDetails, String>();
            this.xmlReplacements.put(filename, fileReplacements);
        }

        fileReplacements.put(new XmlReplacementDetails(xpathExpression, attributeName,
            XmlReplacement.ReplacementBehavior.THROW_EXCEPTION),
                configurationPropertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addXmlReplacement(String filename, String xpathExpression, String attributeName,
        String configurationPropertyName, XmlReplacement.ReplacementBehavior replacementBehavior)
    {
        Map<XmlReplacementDetails, String> fileReplacements = this.xmlReplacements.get(filename);
        if (fileReplacements == null)
        {
            fileReplacements = new HashMap<XmlReplacementDetails, String>();
            this.xmlReplacements.put(filename, fileReplacements);
        }

        fileReplacements.put(new XmlReplacementDetails(xpathExpression, attributeName,
            replacementBehavior), configurationPropertyName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeXmlReplacement(String filename, String xpathExpression)
    {
        removeXmlReplacement(filename, xpathExpression, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeXmlReplacement(String filename, String xpathExpression,
        String attributeName)
    {
        Map<XmlReplacementDetails, String> fileReplacements = this.xmlReplacements.get(filename);
        if (fileReplacements != null)
        {
            fileReplacements.remove(
                new XmlReplacementDetails(xpathExpression, attributeName,
                    XmlReplacement.ReplacementBehavior.THROW_EXCEPTION));

            if (fileReplacements.isEmpty())
            {
                this.xmlReplacements.remove(filename);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<XmlReplacement> getXmlReplacements()
    {
        List<XmlReplacement> xmlReplacements = new ArrayList<XmlReplacement>();

        for (Map.Entry<String, Map<XmlReplacementDetails, String>> xmlReplacementEntry
            : this.xmlReplacements.entrySet())
        {
            for (Map.Entry<XmlReplacementDetails, String> xmlReplacementDetail
                : xmlReplacementEntry.getValue().entrySet())
            {
                XmlReplacementDetails key = xmlReplacementDetail.getKey();
                XmlReplacement xmlReplacement = new XmlReplacement(xmlReplacementEntry.getKey(),
                    key.getXpathExpression(), key.getAttributeName(), key.getReplacementBehavior(),
                        xmlReplacementDetail.getValue());

                xmlReplacements.add(xmlReplacement);
            }
        }

        return xmlReplacements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify()
    {
        super.verify();

        // Verify that the logging level is a valid level
        verifyLogging();
    }

    /**
     * Verify that the logging level specified is a valid level.
     */
    private void verifyLogging()
    {
        String level = getPropertyValue(GeneralPropertySet.LOGGING);
        try
        {
            LoggingLevel.toLevel(level);
        }
        catch (IllegalArgumentException e)
        {
            throw new ContainerException("Invalid logging level [" + level
                + "]. Valid levels are {\"low\", \"medium\", " + "\"high\"}");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigurationType getType()
    {
        return ConfigurationType.STANDALONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureFiles(Map<String, String> replacements, LocalContainer container)
    {
        performXmlReplacements(container);
        super.configureFiles(replacements, container);
    }

    /**
     * Replaces using a map of replacements in a given file.
     * 
     * @param file File to replace in.
     * @param replacements Map containing replacements.
     * @param encoding The character encoding to use, may be {@code null} or empty to use the
     *            platform's default encoding.
     * @throws CargoException If anything fails, most notably if one of the replacements does not
     * exist in the file.
     */
    protected void replaceInFile(String file, Map<String, String> replacements, Charset encoding)
        throws CargoException
    {
        boolean ignoreNonExistingProperties = Boolean.parseBoolean(
            getPropertyValue(GeneralPropertySet.IGNORE_NON_EXISTING_PROPERTIES));

        if (replacements.isEmpty())
        {
            return;
        }
        String path = getHome() + "/" + file;
        getFileHandler().replaceInFile(path, replacements, encoding, ignoreNonExistingProperties);
    }
}
