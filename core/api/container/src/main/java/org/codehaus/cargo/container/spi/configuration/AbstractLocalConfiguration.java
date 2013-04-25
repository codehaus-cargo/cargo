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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.LocalContainer;
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
     * The path under which the container resources are stored in the JAR.
     */
    protected static final String RESOURCE_PATH =
        "org/codehaus/cargo/container/internal/resources/";

    /**
     * List of {@link Deployable}s to deploy into the container.
     */
    private List<Deployable> deployables;

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
            && this.getPropertyValue(name) != null) 
        {
            try 
            {
                int portOffset = Integer.parseInt(this.getPropertyValue(
                    GeneralPropertySet.PORT_OFFSET));
                int value = Integer.parseInt(this.getPropertyValue(name));
                this.setProperty(name, Integer.toString(value + portOffset));
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
                && this.getPropertyValue(name) != null) 
        {
            try 
            {
                int portOffset = Integer.parseInt(this.getPropertyValue(
                    GeneralPropertySet.PORT_OFFSET));
                int value = Integer.parseInt(this.getPropertyValue(name));
                this.setProperty(name, Integer.toString(value - portOffset));
            }
            catch (NumberFormatException e) 
            {
                // We do nothing
            }
        }
    }
}
