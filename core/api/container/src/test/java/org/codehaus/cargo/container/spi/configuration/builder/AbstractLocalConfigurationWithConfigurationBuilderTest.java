/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.builder.LocalConfigurationWithConfigurationBuilderTests;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.DataSourceSupport;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceSupport;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.container.spi.configuration.AbstractLocalConfigurationTest;

/**
 * Abstract test for any {@link org.codehaus.cargo.container.configuration.LocalConfiguration} with
 * a {@link ConfigurationChecker}.
 * 
 * @version $Id$
 */
public abstract class AbstractLocalConfigurationWithConfigurationBuilderTest extends
    AbstractLocalConfigurationTest implements LocalConfigurationWithConfigurationBuilderTests
{

    /**
     * Configuration checker.
     */
    private ConfigurationChecker configurationChecker = null;

    /**
     * Empty constructor.
     */
    public AbstractLocalConfigurationWithConfigurationBuilderTest()
    {
        super();
    }

    /**
     * Constructor with container name.
     * @param name Container name.
     */
    public AbstractLocalConfigurationWithConfigurationBuilderTest(String name)
    {
        super(name);
    }

    /**
     * Creates the configuration checker. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        configurationChecker = createConfigurationChecker();
    }

    /**
     * @return Configuration checker.
     */
    protected abstract ConfigurationChecker createConfigurationChecker();

    /**
     * Return the datasource configuration for a given datasource fixture.
     * @param fixture Datasource fixture.
     * @return The datasource configuration for <code>fixture</code>.
     */
    protected abstract String getDataSourceConfigurationFile(DataSourceFixture fixture);

    /**
     * Return the resource configuration for a given resource fixture.
     * @param fixture Resource fixture.
     * @return The resource configuration for <code>fixture</code>.
     */
    protected abstract String getResourceConfigurationFile(ResourceFixture fixture);

    /**
     * Configure datasource and retrieve the configuration file.
     * @param fixture Datasource fixture.
     * @return Configuration file for <code>fixture</code>.
     * @throws Exception If anything goes wrong.
     */
    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        DataSource ds = fixture.buildDataSource();
        ((DataSourceSupport) configuration).configure(ds, container);
        return configuration.getFileHandler().readTextFile(
            getDataSourceConfigurationFile(fixture), "UTF-8");
    }

    /**
     * Configure datasource via property and retrieve the configuration file.
     * @param fixture Datasource fixture.
     * @return Configuration file for <code>fixture</code>.
     * @throws Exception If anything goes wrong.
     */
    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        configuration.setProperty(DatasourcePropertySet.DATASOURCE, fixture
            .buildDataSourcePropertyString());
        ((AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder) configuration)
            .parsePropertiesForPendingConfiguration();
        ((DataSourceSupport) configuration).configureDataSources(container);
        return configuration.getFileHandler().readTextFile(
            getDataSourceConfigurationFile(fixture), "UTF-8");
    }

    /**
     * Configure resource and retrieve the configuration file.
     * @param fixture Resource fixture.
     * @return Configuration file for <code>fixture</code>.
     * @throws Exception If anything goes wrong.
     */
    protected String configureResourceAndRetrieveConfigurationFile(ResourceFixture fixture)
        throws Exception
    {
        Resource resource = fixture.buildResource();
        ((ResourceSupport) configuration).configure(resource, container);
        return configuration.getFileHandler().readTextFile(getResourceConfigurationFile(fixture),
            "UTF-8");
    }

    /**
     * Configure resource via property and retrieve the configuration file.
     * @param fixture Resource fixture.
     * @return Configuration file for <code>fixture</code>.
     * @throws Exception If anything goes wrong.
     */
    protected String configureResourceViaPropertyAndRetrieveConfigurationFile(
        ResourceFixture fixture) throws Exception
    {
        configuration.setProperty(ResourcePropertySet.RESOURCE, fixture
            .buildResourcePropertyString());
        ((AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder) configuration)
            .parsePropertiesForPendingConfiguration();
        ((ResourceSupport) configuration).configureResources(container);
        return configuration.getFileHandler().readTextFile(getResourceConfigurationFile(fixture),
            "UTF-8");
    }

    /**
     * Test datasource configuration on local container if property with Windows path on
     * datasource.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureDSOnLocalContainerIfPropertyIsPresentOnDataSourceWithWindowsPath()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory.createDataSourceWithWindowsPath();
        String configuration =
            configureDataSourceViaPropertyAndRetrieveConfigurationFile(dataSourceFixture);
        configurationChecker.checkConfigurationForDataSourceMatchesDataSourceFixture(
            configuration, dataSourceFixture);
    }

    /**
     * Test datasource configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesDataSource() throws Exception
    {
        DataSourceFixture dataSourceFixture = ConfigurationFixtureFactory.createDataSource();
        String configuration = configureDataSourceAndRetrieveConfigurationFile(dataSourceFixture);
        configurationChecker.checkConfigurationForDataSourceMatchesDataSourceFixture(
            configuration, dataSourceFixture);
    }

    /**
     * Test datasource configuration with two datasources.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesTwoDataSources() throws Exception
    {
        DataSourceFixture dataSourceFixture1 = ConfigurationFixtureFactory.createDataSource();
        String configuration =
            configureDataSourceAndRetrieveConfigurationFile(dataSourceFixture1);
        configurationChecker.checkConfigurationForDataSourceMatchesDataSourceFixture(
            configuration, dataSourceFixture1);

        DataSourceFixture dataSourceFixture2 =
            ConfigurationFixtureFactory.createAnotherDataSource();
        String configuration2 =
            configureDataSourceViaPropertyAndRetrieveConfigurationFile(dataSourceFixture2);
        configurationChecker.checkConfigurationForDataSourceMatchesDataSourceFixture(
            configuration2, dataSourceFixture2);
    }

    /**
     * Test datasource configuration with a driver configured with local transaction support.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesDataSourceForDriverConfiguredDSWithLocalTransactionSupport()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithLocalTransactionSupport();
        String configuration = configureDataSourceAndRetrieveConfigurationFile(dataSourceFixture);
        configurationChecker
            .checkConfigurationForDriverConfiguredDSWithLocalTransactionSupportMatchesDSFixture(
                configuration, dataSourceFixture);
    }

    /**
     * Test datasource configuration with a driver configured with XA transaction support.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesDataSourceForDriverConfiguredDSWithXaTransactionSupport()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithXaTransactionSupport();
        String configuration = configureDataSourceAndRetrieveConfigurationFile(dataSourceFixture);
        configurationChecker
            .checkConfigurationForDriverConfiguredDSWithXaTransactionSupportMatchesDSFixture(
                configuration, dataSourceFixture);
    }

    /**
     * Test XA datasource configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesDataSourceForXADataSourceConfiguredDataSource()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory.createXADataSourceConfiguredDataSource();
        String configuration = configureDataSourceAndRetrieveConfigurationFile(dataSourceFixture);
        configurationChecker
            .checkConfigurationForXADataSourceConfiguredDataSourceMatchesDataSourceFixture(
                configuration, dataSourceFixture);
    }

    /**
     * Test resource configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesResource() throws Exception
    {
        ResourceFixture resourceFixture =
            ConfigurationFixtureFactory.createXADataSourceAsResource();
        String configuration = configureResourceAndRetrieveConfigurationFile(resourceFixture);
        configurationChecker
            .checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
                configuration, resourceFixture);
    }

    /**
     * Test resource configuration with two resources.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
        ResourceFixture resourceFixture1 =
            ConfigurationFixtureFactory.createXADataSourceAsResource();
        configuration.setProperty(ResourcePropertySet.RESOURCE + ".1", resourceFixture1
            .buildResourcePropertyString());
        ResourceFixture resourceFixture2 =
            ConfigurationFixtureFactory.createConnectionPoolDataSourceAsResource();
        String configuration =
            configureResourceViaPropertyAndRetrieveConfigurationFile(resourceFixture2);
        configurationChecker
            .checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
                configuration,
                resourceFixture1);
        configurationChecker
            .checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
                configuration,
                resourceFixture2);
    }
}
