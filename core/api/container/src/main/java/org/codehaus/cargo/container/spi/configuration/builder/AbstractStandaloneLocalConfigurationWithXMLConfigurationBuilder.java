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
package org.codehaus.cargo.container.spi.configuration.builder;

import java.util.Map;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.DataSourceSupport;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceSupport;
import org.codehaus.cargo.container.spi.configuration.AbstractStandaloneLocalConfiguration;
import org.codehaus.cargo.util.DefaultXmlFileBuilder;
import org.codehaus.cargo.util.XmlFileBuilder;

/**
 * {@inheritDoc} Convenience class that simplifies development of
 * {@link org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration} configurations
 * with {@link DataSourceSupport}. It does this by implementing {@link DataSourceSupport} and
 * instead asking for the inputs to this:
 * <ol>
 * <li> {@link createConfigurationBuilder}: how do we generate the xml element(s) representing the
 * datasource</li>
 * <li> {@link getXpathForDataSourcesParent}: under which element should datasource elements be
 * inserted?</li>
 * <li> {@link getNamespaces}: does this configuration file use namespaces?</li>
 * <li> {@link getOrCreateDataSourceConfigurationFile}: where do we load and store the datasource
 * configuration?</li>
 * </ol>
 */
public abstract class AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder extends
    AbstractStandaloneLocalConfiguration implements DataSourceSupport, ResourceSupport
{

    /**
     * {@inheritDoc}
     * 
     * @param dir configuration home
     */
    public AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder(String dir)
    {
        super(dir);
    }

    /**
     * Implementations should avoid passing null, and instead pass
     * <code>Collections.EMPTY_MAP</code>, if the document is DTD bound.
     * 
     * @return a map of prefixes to the url namespaces used in the datasource configuration file.
     */
    protected abstract Map<String, String> getNamespaces();

    /**
     * DataSource entries must be stored in the xml configuration file. Under which element do we
     * insert the entries? example: //Engine/DefaultContext
     * 
     * @return path the the parent element datasources should be inserted under.
     */
    protected abstract String getXpathForDataSourcesParent();

    /**
     * Resource entries must be stored in the xml configuration file. Under which element do we
     * insert the entries? example: //Engine/DefaultContext
     * 
     * @return path the the parent element Resources should be inserted under.
     */
    protected abstract String getXpathForResourcesParent();

    /**
     * note that this file could hold other configuration besides datasources.
     * 
     * @param ds the DataSource configuration you wish to install on the container.
     * @param container the container whose configuration you wish to affect.
     * @return the file that holds datasource configuration.
     */
    protected abstract String getOrCreateDataSourceConfigurationFile(DataSource ds,
        LocalContainer container);

    /**
     * note that this file could hold other configuration besides Resources.
     * 
     * @param resource the Resource configuration you wish to install on the container.
     * @param container the container whose configuration you wish to affect.
     * @return the file that holds Resource configuration.
     */
    protected abstract String getOrCreateResourceConfigurationFile(Resource resource,
        LocalContainer container);

    /**
     * @param container Container the dataSource will be configured on.
     * @return the object that produces xml entries for DataSource definitions.
     */
    protected abstract ConfigurationBuilder createConfigurationBuilder(LocalContainer container);

    /**
     * Configure the specified container.
     * 
     * @param container the container to configure
     */
    @Override
    public void configure(LocalContainer container)
    {
        super.configure(container);
        configureDataSources(container);
        configureResources(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureDataSources(LocalContainer container)
    {
        for (DataSource dataSource : getDataSources())
        {
            configure(dataSource, container);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Resource ds, LocalContainer container)
    {
        ConfigurationBuilder builder = this.createConfigurationBuilder(container);
        String xml = builder.toConfigurationEntry(ds);
        String file = getOrCreateResourceConfigurationFile(ds, container);
        writeConfigurationToXpath(file, xml, getXpathForResourcesParent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureResources(LocalContainer container)
    {
        for (Resource resource : getResources())
        {
            configure(resource, container);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(DataSource ds, LocalContainer container)
    {
        ConfigurationBuilder builder = this.createConfigurationBuilder(container);
        String file = getOrCreateDataSourceConfigurationFile(ds, container);
        String xml = builder.toConfigurationEntry(ds);
        writeConfigurationToXpath(file, xml, getXpathForDataSourcesParent());
    }

    /**
     * Utility method used to write XML to an appropriate place in the configuration file.
     * 
     * @param file where to write the datasource configuration to.
     * @param xml node you wish to write to the resources configuration file.
     * @param path where in the file to write the configuration.
     */
    protected void writeConfigurationToXpath(String file, String xml, String path)
    {
        XmlFileBuilder manager = new DefaultXmlFileBuilder(getFileHandler());
        manager.setNamespaces(getNamespaces());
        manager.setFile(file);
        manager.loadFile();
        manager.insertElementsUnderXPath(xml, path);
        manager.writeFile();
    }
}
