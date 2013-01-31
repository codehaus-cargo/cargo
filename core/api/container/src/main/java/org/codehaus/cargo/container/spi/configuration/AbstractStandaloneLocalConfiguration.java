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
package org.codehaus.cargo.container.spi.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.types.FilterChain;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler.XmlReplacement;

/**
 * Base implementation for a standalone local configuration.
 * 
 * @version $Id$
 */
public abstract class AbstractStandaloneLocalConfiguration extends AbstractLocalConfiguration
    implements StandaloneLocalConfiguration
{

    /**
     * List of {@link FileConfig}s to use for the container.
     */
    private List<FileConfig> files;

    /**
     * The filterChain for the configuration files. This contains the tokens and what values they
     * should be replaced with.
     */
    private FilterChain filterChain;

    /**
     * The XML replacements for the configuration files. The first map's key is the file name,
     * the inner map's key is the {@link XmlReplacement} and value the configuration property.
     */
    private Map<String, Map<XmlReplacement, String>> xmlReplacements;

    /**
     * {@inheritDoc}
     * @see AbstractLocalConfiguration#AbstractLocalConfiguration(String)
     */
    public AbstractStandaloneLocalConfiguration(String dir)
    {
        super(dir);

        // Add all required properties that are common to all standalone configurations
        setProperty(GeneralPropertySet.LOGGING, LoggingLevel.MEDIUM.getLevel());
        setProperty(GeneralPropertySet.IGNORE_NON_EXISTING_PROPERTIES, "false");
        this.files = new ArrayList<FileConfig>();
        this.xmlReplacements = new HashMap<String, Map<XmlReplacement, String>>();
    }

    /**
     * Configure the specified container.
     * @param container the container to configure
     */
    @Override
    public void configure(LocalContainer container)
    {
        super.configure(container);
        performXmlReplacements(container);
        configureFiles(getFilterChain());
    }

    /**
     * Perform the XML replacements for the specified container.
     * @param container the container to configure
     */
    protected void performXmlReplacements(LocalContainer container)
    {
        boolean ignoreNonExistingProperties = Boolean.valueOf(
            getPropertyValue(GeneralPropertySet.IGNORE_NON_EXISTING_PROPERTIES)).booleanValue();

        for (Map.Entry<String, Map<XmlReplacement, String>> xmlReplacements
            : this.xmlReplacements.entrySet())
        {
            Map<XmlReplacement, String> replacements = new HashMap<XmlReplacement, String>();

            for (Map.Entry<XmlReplacement, String> xmlReplacement
                    : xmlReplacements.getValue().entrySet())
            {
                String value = container.getConfiguration().getPropertyValue(
                    xmlReplacement.getValue());

                if (value != null)
                {
                    replacements.put(xmlReplacement.getKey(), value);
                }
                else
                {
                    replacements.put(xmlReplacement.getKey(), xmlReplacement.getValue());
                }
            }

            if (!replacements.isEmpty())
            {
                String destinationFile = getFileHandler().append(
                    container.getConfiguration().getHome(), xmlReplacements.getKey());
                getFileHandler().replaceInXmlFile(destinationFile, replacements,
                    ignoreNonExistingProperties);
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
        if (getFileHandler().exists(timestampFile)
            || getFileHandler().exists(getHome())
                && getFileHandler().isDirectoryEmpty(getHome())
            || !getFileHandler().exists(getHome()))
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
                + "empty directory. Note that everything in that dir will get deleted by Cargo.");
        }
    }

    /**
     * Creates the default filter chain that should be applied while copying container configuration
     * files to the working directory from which the container is started.
     * 
     * @return The default filter chain
     */
    protected final FilterChain createFilterChain()
    {
        this.filterChain = new FilterChain();

        // add all the token specified in the containers configuration into the filterchain
        getAntUtils().addTokensToFilterChain(filterChain, getProperties());

        return filterChain;
    }

    /**
     * Adds an XML replacement.
     * 
     * @param filename File in which to replace.
     * @param xpathExpression XPath expression to look for.
     * @param configurationPropertyName Name of the configuration property to set. The XML
     * replacement will be ignored if the property is set to <code>null</code>.
     */
    protected void addXmlReplacement(String filename, String xpathExpression,
        String configurationPropertyName)
    {
        addXmlReplacement(filename, xpathExpression, null, configurationPropertyName);
    }

    /**
     * Adds an XML replacement.
     * 
     * @param filename File in which to replace.
     * @param xpathExpression XPath expression to look for.
     * @param attributeName Attribute name to modify. If <code>null</code>, the node's contents
     * will be modified.
     * @param configurationPropertyName Name of the configuration property to set. The XML
     * replacement will be ignored if the property is set to <code>null</code>.
     */
    protected void addXmlReplacement(String filename, String xpathExpression, String attributeName,
        String configurationPropertyName)
    {
        Map<XmlReplacement, String> fileReplacements = this.xmlReplacements.get(filename);
        if (fileReplacements == null)
        {
            fileReplacements = new HashMap<XmlReplacement, String>();
            this.xmlReplacements.put(filename, fileReplacements);
        }

        fileReplacements.put(new XmlReplacement(xpathExpression, attributeName),
            configurationPropertyName);
    }

    /**
     * Removes an XML replacement.
     * 
     * @param filename File in which to replace.
     * @param xpathExpression XPath expression to look for.
     */
    protected void removeXmlReplacement(String filename, String xpathExpression)
    {
        removeXmlReplacement(filename, xpathExpression, null);
    }

    /**
     * Removes an XML replacement.
     * 
     * @param filename File in which to replace.
     * @param xpathExpression XPath expression to look for.
     * @param attributeName Attribute name to modify. If <code>null</code>, the node's contents
     * will be modified.
     */
    protected void removeXmlReplacement(String filename, String xpathExpression,
        String attributeName)
    {
        Map<XmlReplacement, String> fileReplacements = this.xmlReplacements.get(filename);
        if (fileReplacements != null)
        {
            fileReplacements.remove(new XmlReplacement(xpathExpression, attributeName));

            if (fileReplacements.isEmpty())
            {
                this.xmlReplacements.remove(filename);
            }
        }
    }

    /**
     * {@inheritDoc}
     * @see ContainerConfiguration#verify()
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
     * @see org.codehaus.cargo.container.configuration.Configuration#getType()
     */
    public ConfigurationType getType()
    {
        return ConfigurationType.STANDALONE;
    }

    /**
     * {@inheritDoc}
     */
    public FilterChain getFilterChain()
    {
        if (this.filterChain == null)
        {
            this.filterChain = createFilterChain();
        }
        return this.filterChain;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration#addConfigfile(org.codehaus.cargo.container.configuration.FileConfig)
     */
    public void setFileProperty(FileConfig fileConfig)
    {
        this.files.add(fileConfig);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration#addConfigfile(org.codehaus.cargo.container.configuration.FileConfig)
     */
    public void setConfigFileProperty(FileConfig fileConfig)
    {
        // a configuration file should always overwrite the previous file if it exists
        // since the token value could have changed during.
        fileConfig.setOverwrite(true);
        fileConfig.setConfigfile(true);
        this.setFileProperty(fileConfig);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration#getConfigfiles()
     */
    public List<FileConfig> getFileProperties()
    {
        return this.files;
    }

    /**
     * Copy the customized configuration files into the cargo home directory.
     * @param filterChain the filter chain to use during the copy
     */
    protected void configureFiles(FilterChain filterChain)
    {
        List<FileConfig> files = this.files;

        for (FileConfig fileConfig : files)
        {
            boolean isDirectory = false;

            if (fileConfig.getFile() == null)
            {
                throw new RuntimeException("File cannot be null");
            }

            File origFile = new File(fileConfig.getFile());
            if (origFile.isDirectory())
            {
                isDirectory = true;
            }

            String destFile = getDestFileLocation(fileConfig.getFile(),
                    fileConfig.getToDir(), fileConfig.getToFile());

            // we don't want to do anything if the file exists and overwrite is false
            if (!origFile.exists() || fileConfig.getOverwrite())
            {
                if (isDirectory)
                {
                    String destDir = getDestDirectoryLocation(fileConfig.getFile(), fileConfig
                            .getToDir());

                    if (fileConfig.getConfigfile())
                    {
                        getFileHandler().copyDirectory(fileConfig.getFile(), destDir, filterChain,
                            fileConfig.getEncoding());
                    }
                    else
                    {
                        getFileHandler().copyDirectory(fileConfig.getFile(), destDir);
                    }
                }
                else
                {
                    if (fileConfig.getConfigfile())
                    {
                        getFileHandler().copyFile(fileConfig.getFile(), destFile, filterChain,
                            fileConfig.getEncoding());
                    }
                    else
                    {
                        getFileHandler().copyFile(fileConfig.getFile(), destFile,
                                fileConfig.getOverwrite());
                    }
                }
            }
        }
    }

    /**
     * Determines the correct path for the destination file.
     * @param file The path of the original file
     * @param toDir The directory for the copied file
     * @param toFile The file name for the copied file
     * @return The path for the destination file
     */
    protected String getDestFileLocation(String file, String toDir, String toFile)
    {
        String fileName = file;
        String finalFile = null;

        if (fileName == null)
        {
            throw new RuntimeException("file cannot be null");
        }
        else if (toFile == null && toDir != null)
        {
            // get the filename and add it in the todir directory name
            String filename = new File(fileName).getName();
            finalFile = getHome() + "/" + toDir + "/" + filename;
        }
        else if (toFile != null && toDir == null)
        {
            // just use the tofile filename as the final file
            finalFile = getHome() + "/" + toFile;
        }
        else if (toFile == null && toDir == null)
        {
            // use the filename and add it into the conf directory
            String filename = new File(fileName).getName();
            finalFile = getHome() + "/" + filename;
        }
        else if (toFile != null && toDir != null)
        {
            // tofile means what name to call the file in the todir directory
            finalFile = getHome() + "/" + toDir + "/" + toFile;
        }

        // replace all double slashes with a single slash
        while (finalFile.contains("//"))
        {
            finalFile = finalFile.replaceAll("//", "/");
        }

        return finalFile;
    }

    /**
     * Determines the correct path for the destination directory.
     * @param file The path of the original file
     * @param toDir The directory for the copied file
     * @return The path for the destination file
     */
    protected String getDestDirectoryLocation(String file, String toDir)
    {
        String fileName = file;
        String finalDir = null;

        if (fileName == null)
        {
            throw new RuntimeException("file cannot be null");
        }
        else if (toDir != null)
        {
            finalDir = getHome() + "/" + toDir;
        }
        else if (toDir == null)
        {
            finalDir = getHome();
        }
        // replace all double slashes with a single slash
        while (finalDir.contains("//"))
        {
            finalDir = finalDir.replaceAll("//", "/");
        }

        return finalDir;
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
    protected void replaceInFile(String file, Map<String, String> replacements, String encoding)
        throws CargoException
    {
        boolean ignoreNonExistingProperties = Boolean.valueOf(
            getPropertyValue(GeneralPropertySet.IGNORE_NON_EXISTING_PROPERTIES)).booleanValue();

        if (replacements.isEmpty())
        {
            return;
        }
        String path = getHome() + "/" + file;
        getFileHandler().replaceInFile(path, replacements, encoding, ignoreNonExistingProperties);
    }
}
