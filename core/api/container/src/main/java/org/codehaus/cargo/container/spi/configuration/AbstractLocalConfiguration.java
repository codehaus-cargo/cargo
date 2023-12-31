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
package org.codehaus.cargo.container.spi.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.TransactionSupport;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.log.Logger;

/**
 * Base implementation of
 * {@link org.codehaus.cargo.container.spi.configuration.ContainerConfiguration} that can be
 * specialized for standalone configuration, existing configuration or other local configurations.
 */
public abstract class AbstractLocalConfiguration extends AbstractConfiguration implements
    LocalConfiguration
{
    /**
     * The path under which the container resources are stored in the JAR.
     */
    public static final String RESOURCE_PATH = "org/codehaus/cargo/container/internal/resources/";

    /**
     * Property key to flag ports which have already an offset applied.
     */
    private static final String PORT_OFFSET_APPLIED_PREFIX = LocalConfiguration.class.getName()
        + "_portOffsetApplied_";

    /**
     * List of {@link Deployable}s to deploy into the container.
     */
    private List<Deployable> deployables;

    /**
     * List of {@link FileConfig}s to use for the container.
     */
    private List<FileConfig> files;

    /**
     * The replacements for the configuration files. This contains the tokens and what values they
     * should be replaced with.
     */
    private Map<String, String> replacements;

    /**
     * The home directory for the configuration. This is where the associated container will be set
     * up to start and where it will deploy its deployables.
     */
    private String home;

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
     * List of {@link User}s to add to a container.
     */
    private List<User> users;

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
        this.resourceUtils = new ResourceUtils();
        this.resources = new ArrayList<Resource>();
        this.dataSources = new ArrayList<DataSource>();
        this.files = new ArrayList<FileConfig>();
        this.users = new ArrayList<User>();

        this.home = home;

        setProperty(GeneralPropertySet.PORT_OFFSET, "0");
        setProperty(GeneralPropertySet.SPAWN_PROCESS, "false");
    }

    /**
     * Overriden in order to set the logger on ancillary components.
     * {@inheritDoc}
     * 
     * @param logger the logger to set and set in the ancillary objects
     */
    @Override
    public void setLogger(Logger logger)
    {
        super.setLogger(logger);
        this.fileHandler.setLogger(logger);
        this.resourceUtils.setLogger(logger);
    }

    /**
     * @return the file utility class to use for performing all file I/O.
     */
    @Override
    public FileHandler getFileHandler()
    {
        return this.fileHandler;
    }

    /**
     * @param fileHandler the file utility class to use for performing all file I/O.
     */
    @Override
    public void setFileHandler(FileHandler fileHandler)
    {
        this.fileHandler = fileHandler;
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
     */
    @Override
    public void setFileProperty(FileConfig fileConfig)
    {
        this.files.add(fileConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
     */
    @Override
    public List<FileConfig> getFileProperties()
    {
        return this.files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addDeployable(Deployable newDeployable)
    {
        this.deployables.add(newDeployable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Deployable> getDeployables()
    {
        return this.deployables;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHome()
    {
        return this.home;
    }

    @Override
    public String getPropertyValue(String name)
    {
        if (isOffsetApplied(name))
        {
            return super.getProperties().get(name);
        }
        else
        {
            return super.getPropertyValue(name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
                + getType().getType() + " configuration: " + e.getMessage(), e);
        }

        configureFiles(getReplacements(), container);
    }

    /**
     * Returns (while, if necessary, creating) the default filter chain that should be applied
     * while copying container configuration files to the working directory from which the
     * container is started.
     * 
     * @return The default filter chain
     */
    protected Map<String, String> getReplacements()
    {
        if (this.replacements == null)
        {
            this.replacements = new HashMap<String, String>(getProperties());
        }
        return this.replacements;
    }

    /**
     * Copy the customized configuration files into the cargo home directory.
     * @param replacements the replacements to use during the copy
     * @param container local container
     */
    protected void configureFiles(Map<String, String> replacements, LocalContainer container)
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
                        getFileHandler().copyDirectory(fileConfig.getFile(), destDir, replacements,
                            fileConfig.getEncodingAsCharset());
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
                        getFileHandler().copyFile(fileConfig.getFile(), destFile, replacements,
                            fileConfig.getEncodingAsCharset());
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
                if (!errorMessage.toString().isEmpty())
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
            else if (ConfigurationEntryType.XA_DATASOURCE.equals(dataSource.getConnectionType())
                && !this.getCapability().supportsProperty(DatasourcePropertySet.CONNECTION_TYPE))
            {
                reason =
                    "This configuration does not support XADataSource configured DataSources! ";
            }
            else if (!ConfigurationEntryType.XA_DATASOURCE.equals(dataSource.getConnectionType())
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
                if (errorMessage.length() > 0)
                {
                    errorMessage.append("\n");
                }
                errorMessage.append(message);
                getLogger().warn(message, getClass().getName());
            }
        }
        if (errorMessage.length() > 0)
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
        addUsersFromProperties();
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
     * Parse properties and add any users to pending configuration. Users will be retrieved from
     * their property: {@link ServletPropertySet#USERS}
     */
    protected void addUsersFromProperties()
    {
        getLogger().debug("Searching properties for User definition",
            this.getClass().getName());

        String usersProperty = getPropertyValue(ServletPropertySet.USERS);

        if (usersProperty != null)
        {
            getLogger().debug("Found User definition: value [" + usersProperty + "]",
                    this.getClass().getName());

            List<User> usersFromProp = User.parseUsers(usersProperty);
            getUsers().addAll(usersFromProp);
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
     */
    @Override
    public void addResource(Resource resource)
    {
        this.resources.add(resource);
    }

    /**
     * @return the configured resources for this container.
     */
    @Override
    public List<Resource> getResources()
    {
        return this.resources;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addUser(User user)
    {
        this.users.add(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getUsers()
    {
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDataSource(DataSource dataSource)
    {
        this.dataSources.add(dataSource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataSource> getDataSources()
    {
        return this.dataSources;
    }

    /**
     * This method should only be called once all the properties has been set. {@inheritDoc}
     */
    @Override
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
                // CARGO-1438: Only update numbers for properties prefixed with "cargo."
                if (key.startsWith("cargo.") && key.endsWith(".port"))
                {
                    this.applyPortOffset(key);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * This method should only be called once all the properties has been set.
     */
    @Override
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
                // CARGO-1438: Only update numbers for properties prefixed with "cargo."
                if (key.startsWith("cargo.") && key.endsWith(".port"))
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
                flagOffsetApplied(name, true);
            }
            catch (NumberFormatException e)
            {
                // We do nothing
            }
        }
    }

    /**
     * Revert the port offset on the specified property
     * 
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
                flagOffsetApplied(name, false);
            }
            catch (NumberFormatException e)
            {
                // We do nothing
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOffsetApplied()
    {
        // Servlet port should be available in all containers, so we can use it
        // to check if offset was applied.
        return isOffsetApplied(ServletPropertySet.PORT);
    }

    /**
     * Checks whether the offset is already applied or not
     * @param name the name of the property to be checked
     * @return <code>true</code> if the offset is already applied
     */
    protected boolean isOffsetApplied(String name)
    {
        return super.getPropertyValue(PORT_OFFSET_APPLIED_PREFIX + name) != null;
    }

    /**
     * Flags whether offset has been applied to a given property or not.
     * @param name name of the property to be flagged.
     * @param offsetApplied <code>true</code> if the offset is applied, else <code>false</code>.
     */
    protected void flagOffsetApplied(String name, boolean offsetApplied)
    {
        this.setProperty(PORT_OFFSET_APPLIED_PREFIX + name,
            offsetApplied ? String.valueOf(offsetApplied) : null);
    }
}
