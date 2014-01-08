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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.types.FilterChain;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.property.DataSourceConverter;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ResourceConverter;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.Logger;

/**
 * Base implementation of
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} that can be
 * specialized for standalone configuration, existing configuration or other local configurations.
 * 
 * @version $Id$
 */
public abstract class AbstractLocalConfiguration extends AbstractConfiguration implements
    LocalConfiguration
{
    /**
     * Property key to flag ports which have already an offset applied.
     */
    private static String PORT_OFFSET_APPLIED_PREFIX = LocalConfiguration.class.getName() + "_portOffsetApplied_";
    
    /**
     * The path under which the container resources are stored in the JAR.
     */
    public static final String RESOURCE_PATH =
        "org/codehaus/cargo/container/internal/resources/";

    /**
     * List of {@link Deployable}s to deploy into the container.
     */
    private List<Deployable> deployables;

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
     * The home directory for the configuration. This is where the associated container will be set
     * up to start and where it will deploy its deployables.
     */
    private String home;

    /**
     * Ant utility class.
     */
    private AntUtils antUtils;

    /**
     * Resource utility class.
     */
    private ResourceUtils resourceUtils;

    /**
     * File utility class.
     */
    private FileHandler fileHandler;

    /**
     * List of {@link Resource}s to add to a container.
     */
    private List<Resource> resources;

    /**
     * List of {@link DataSource}s to add to a container.
     */
    private List<DataSource> dataSources;

    /**
     * @param home the home directory where the container will be set up to start and where it will
     * deploy its deployables. <b>IMPORTANT</b>: While some containers can deal with this parameter
     * being set as a relative path, some others require this path to be set to an absolute
     * directory. Please refer to the documentation of the server to ensure you give the path in
     * the appropriate way. If in doubt, you can use absolute paths -that is known to work with all
     * containers.
     */
    public AbstractLocalConfiguration(String home)
    {
        super();

        this.deployables = new ArrayList<Deployable>();
        this.fileHandler = new DefaultFileHandler();
        this.antUtils = new AntUtils();
        this.resourceUtils = new ResourceUtils();
        this.resources = new ArrayList<Resource>();
        this.dataSources = new ArrayList<DataSource>();
        this.files = new ArrayList<FileConfig>();

        this.home = home;

        setProperty(GeneralPropertySet.PORT_OFFSET, "0");
        setProperty(GeneralPropertySet.SPAWN_PROCESS, "false");
    }

    /**
     * Overriden in order to set the logger on ancillary components.
     * 
     * @param logger the logger to set and set in the ancillary objects
     * @see org.codehaus.cargo.util.log.Loggable#setLogger(org.codehaus.cargo.util.log.Logger)
     */
    @Override
    public void setLogger(Logger logger)
    {
        super.setLogger(logger);
        this.fileHandler.setLogger(logger);
    }

    /**
     * @return the file utility class to use for performing all file I/O.
     */
    public FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the file utility class to use for performing all file I/O.
     */
    public void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
    }

    /**
     * @return the Ant utility class
     */
    protected final AntUtils getAntUtils()
    {
        return this.antUtils;
    }

    /**
     * @return the Resource utility class
     */
    protected final ResourceUtils getResourceUtils()
    {
        return this.resourceUtils;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#addConfigfile(org.codehaus.cargo.container.configuration.FileConfig)
     */
    public void setFileProperty(FileConfig fileConfig)
    {
        this.files.add(fileConfig);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#addConfigfile(org.codehaus.cargo.container.configuration.FileConfig)
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
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#addDeployable(org.codehaus.cargo.container.deployable.Deployable)
     */
    public synchronized void addDeployable(Deployable newDeployable)
    {
        this.deployables.add(newDeployable);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#getDeployables()
     */
    public List<Deployable> getDeployables()
    {
        return this.deployables;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.configuration.LocalConfiguration#getHome()
     */
    public String getHome()
    {
        return this.home;
    }

    /**
     * {@inheritDoc}
     * 
     * @see LocalConfiguration#configure(LocalContainer)
     */
    public void configure(LocalContainer container)
    {
        if (getPropertyValue(GeneralPropertySet.JAVA_HOME) == null)
        {
            setProperty(GeneralPropertySet.JAVA_HOME, System.getProperty("java.home"));
        }

        parsePropertiesForPendingConfiguration();
        verify();

        try
        {
            doConfigure(container);
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to create a " + container.getName() + " "
                + getType().getType() + " configuration", e);
        }

        configureFiles(getFilterChain(), container);
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
     * {@inheritDoc}
     */
    protected FilterChain getFilterChain()
    {
        if (this.filterChain == null)
        {
            this.filterChain = createFilterChain();
        }
        return this.filterChain;
    }

    /**
     * Copy the customized configuration files into the cargo home directory.
     * @param filterChain the filter chain to use during the copy
     * @param container local container
     */
    protected void configureFiles(FilterChain filterChain, LocalContainer container)
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
     * {@inheritDoc}
     * 
     * @see ContainerConfiguration#verify()
     */
    @Override
    public void verify()
    {
        collectUnsupportedResourcesAndThrowException();
        collectUnsupportedDataSourcesAndThrowException();
        super.verify();
    }

    /**
     * Warn user and throw an Exception if any unsupported {@link Resource}s are setup for this
     * configuration.
     */
    public void collectUnsupportedResourcesAndThrowException()
    {
        if (!getResources().isEmpty()
            && !this.getCapability().supportsProperty(ResourcePropertySet.RESOURCE))
        {
            StringBuilder errorMessage = new StringBuilder();
            for (Resource resource : getResources())
            {
                String message =
                    "This configuration does not support Resource configuration! JndiName: "
                        + resource.getName();
                getLogger().warn(message, getClass().getName());
                if (!errorMessage.toString().equals(""))
                {
                    errorMessage.append("\n");
                }
                errorMessage.append(message);
            }
            throw new CargoException(errorMessage.toString());
        }
    }

    /**
     * Warn user and throw an Exception if any unsupported {@link DataSource}s are setup for this
     * configuration.
     */
    public void collectUnsupportedDataSourcesAndThrowException()
    {
        StringBuilder errorMessage = new StringBuilder();

        for (DataSource dataSource : getDataSources())
        {
            String reason = null;
            if (!this.getCapability().supportsProperty(DatasourcePropertySet.DATASOURCE))
            {
                reason = "This configuration does not support DataSource configuration! ";
            }
            else if (ConfigurationEntryType.XA_DATASOURCE.toString().equals(
                dataSource.getConnectionType())
                && !this.getCapability().supportsProperty(DatasourcePropertySet.CONNECTION_TYPE))
            {
                reason =
                    "This configuration does not support XADataSource configured DataSources! ";
            }
            else if (!ConfigurationEntryType.XA_DATASOURCE.toString()
                .equals(dataSource.getConnectionType())
                && !TransactionSupport.NO_TRANSACTION.equals(dataSource.getTransactionSupport())
                && !this.getCapability().supportsProperty(
                    DatasourcePropertySet.TRANSACTION_SUPPORT))
            {
                reason =
                    "This configuration does not support Transactions on Driver configured "
                        + "DataSources! ";
            }
            if (reason != null)
            {
                String message = reason + "JndiName: " + dataSource.getJndiLocation();
                if (!errorMessage.toString().equals(""))
                {
                    errorMessage.append("\n");
                }
                errorMessage.append(message);
                getLogger().warn(message, getClass().getName());
            }
        }
        if (!errorMessage.toString().equals(""))
        {
            throw new CargoException(errorMessage.toString());
        }
    }

    /**
     * Some configuration can be specified as encoded properties. Parse properties and apply what is
     * found to the appropriate pending configuration list.
     */
    public void parsePropertiesForPendingConfiguration()
    {
        addResourcesFromProperties();
        addDataSourcesFromProperties();
    }

    /**
     * Parse properties and add any Resources to pending configuration. Resources will be found if
     * their property name starts with: {@link ResourcePropertySet#RESOURCE}
     */
    protected void addResourcesFromProperties()
    {
        getLogger().debug("Searching properties for Resource definitions",
            this.getClass().getName());
        for (Map.Entry<String, String> property : getProperties().entrySet())
        {
            String propertyName = property.getKey();
            if (propertyName.startsWith(ResourcePropertySet.RESOURCE))
            {
                String resourceProperty = property.getValue();
                getLogger().debug("Found Resource definition: value [" + resourceProperty + "]",
                    this.getClass().getName());
                Resource resource = new ResourceConverter().fromPropertyString(resourceProperty);
                getResources().add(resource);
            }
        }
    }

    /**
     * Parse properties and add any DataSources to pending configuration. DataSources will be found
     * if their property name starts with: {@link DatasourcePropertySet#DATASOURCE}
     */
    protected void addDataSourcesFromProperties()
    {
        getLogger().debug("Searching properties for DataSource definitions",
            this.getClass().getName());
        for (Map.Entry<String, String> property : getProperties().entrySet())
        {
            String propertyName = property.getKey();
            if (propertyName.startsWith(DatasourcePropertySet.DATASOURCE))
            {
                String dataSourceProperty = property.getValue();
                getLogger().debug(
                    "Found DataSource definition: value [" + dataSourceProperty + "]",
                    this.getClass().getName());
                DataSource dataSource =
                    new DataSourceConverter().fromPropertyString(dataSourceProperty);
                getDataSources().add(dataSource);
            }
        }
    }

    /**
     * Implementation of {@link LocalConfiguration#configure(LocalContainer)} that all local
     * configuration using this class must implement. This provides the ability to perform generic
     * actions before and after the container-specific implementation. Another way would be to use
     * AOP...
     * 
     * @param container the container to configure
     * @throws Exception if any error is raised during the configuration
     */
    protected abstract void doConfigure(LocalContainer container) throws Exception;

    /**
     * {@inheritDoc}
     * 
     * @see LocalConfiguration#addResource(Resource)
     */
    public void addResource(Resource resource)
    {
        this.resources.add(resource);
    }

    /**
     * @return the configured resources for this container.
     */
    public List<Resource> getResources()
    {
        return this.resources;
    }

    /**
     * {@inheritDoc}
     * 
     * @see LocalConfiguration#addDataSource(DataSource)
     */
    public void addDataSource(DataSource dataSource)
    {
        this.dataSources.add(dataSource);
    }

    /**
     * {@inheritDoc}
     * 
     * @see LocalConfiguration#getDataSources()
     */
    public List<DataSource> getDataSources()
    {
        return this.dataSources;
    }

    /**
     * {@inheritDoc}
     * 
     * @see LocalConfiguration#applyPortOffset()
     * 
     * This method should only be called once all the properties has been set.
     */
    public void applyPortOffset() 
    {
        if (this.getPropertyValue(GeneralPropertySet.PORT_OFFSET) != null 
            && !this.getPropertyValue(GeneralPropertySet.PORT_OFFSET).equals("0")) 
        {
            // Since the properties hashmap is impacted by the revert we must 
            // use a copy of the keys
            Set<String> keysCopy = new HashSet<String>(this.getProperties().keySet());
            for (String key : keysCopy) 
            {
                if (key.endsWith(".port")) 
                {
                    this.applyPortOffset(key);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see LocalConfiguration#revertPortOffset()
     * 
     * This method should only be called once all the properties has been set.
     */
    public void revertPortOffset() 
    {
        if (this.getPropertyValue(GeneralPropertySet.PORT_OFFSET) != null 
            && !this.getPropertyValue(GeneralPropertySet.PORT_OFFSET).equals("0")) 
        {
            // We need to shift the ports
            
            // Since the properties hashmap is impacted by the revert we must 
            // use a copy of the keys
            Set<String> keysCopy = new HashSet<String>(this.getProperties().keySet());
            for (String key : keysCopy) 
            {
                if (key.endsWith(".port")) 
                {
                    this.revertPortOffset(key);
                }
            }
        }
    }

    /**
     * Apply the port offset on the specified property
     * @param name the property name
     */
    protected void applyPortOffset(String name) 
    {
        if (this.getPropertyValue(GeneralPropertySet.PORT_OFFSET) != null
            && this.getPropertyValue(name) != null 
            && !isOffsetApplied(name)) 
        {
            try 
            {
                int portOffset = Integer.parseInt(this.getPropertyValue(
                    GeneralPropertySet.PORT_OFFSET));
                int value = Integer.parseInt(this.getPropertyValue(name));
                this.setProperty(name, Integer.toString(value + portOffset));
                flagOffestApplied(name, true);
            }
            catch (NumberFormatException e) 
            {
                // We do nothing
            }
        }
    }

    /**
     * Revert the port offset on the specified property
     * @param name the property name
     */
    protected void revertPortOffset(String name) 
    {
        if (this.getPropertyValue(GeneralPropertySet.PORT_OFFSET) != null
                && this.getPropertyValue(name) != null
                && isOffsetApplied(name)) 
        {
            try 
            {
                int portOffset = Integer.parseInt(this.getPropertyValue(
                    GeneralPropertySet.PORT_OFFSET));
                int value = Integer.parseInt(this.getPropertyValue(name));
                this.setProperty(name, Integer.toString(value - portOffset));
                flagOffestApplied(name, false);
            }
            catch (NumberFormatException e) 
            {
                // We do nothing
            }
        }
    }

    /**
     * Checks whether the offset is already applied or not 
     * @param name the name of the property to be checked
     * @return <code>true</code> if the offset is already applied
     */
    protected boolean isOffsetApplied(String name) {
        return this.getPropertyValue(PORT_OFFSET_APPLIED_PREFIX + name) != null;
    }

    /**
     * Flags the 
     * @param name the name of the property to be flagged.
     * @param offsetApplied
     */
    protected void flagOffestApplied(String name, boolean offsetApplied) {
        this.setProperty(PORT_OFFSET_APPLIED_PREFIX + name,
                offsetApplied ? String.valueOf(offsetApplied) : null);
    }
}
