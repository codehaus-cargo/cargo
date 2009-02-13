/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
import org.codehaus.cargo.container.spi.configuration.builder.AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder;

public abstract class AbstractLocalConfigurationWithConfigurationBuilderTest extends
    AbstractLocalConfigurationTest implements LocalConfigurationWithConfigurationBuilderTests
{

    ConfigurationChecker configurationChecker = null;

    public AbstractLocalConfigurationWithConfigurationBuilderTest()
    {
        super();
    }

    public AbstractLocalConfigurationWithConfigurationBuilderTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        configurationChecker = createConfigurationChecker();
    }

    protected abstract ConfigurationChecker createConfigurationChecker();

    protected abstract String getDataSourceConfigurationFile(DataSourceFixture fixture);

    protected abstract String getResourceConfigurationFile(ResourceFixture fixture);

    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        DataSource ds = fixture.buildDataSource();
        ((DataSourceSupport) configuration).configure(ds, container);
        return configuration.getFileHandler().readTextFile(
            getDataSourceConfigurationFile(fixture));
    }

    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        configuration.setProperty(DatasourcePropertySet.DATASOURCE, fixture
            .buildDataSourcePropertyString());
        ((AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder) configuration)
            .parsePropertiesForPendingConfiguration();
        ((DataSourceSupport) configuration).configureDataSources(container);
        return configuration.getFileHandler().readTextFile(
            getDataSourceConfigurationFile(fixture));
    }

    protected String configureResourceAndRetrieveConfigurationFile(ResourceFixture fixture)
        throws Exception
    {
        Resource resource = fixture.buildResource();
        ((ResourceSupport) configuration).configure(resource, container);
        return configuration.getFileHandler().readTextFile(getResourceConfigurationFile(fixture));
    }

    protected String configureResourceViaPropertyAndRetrieveConfigurationFile(
        ResourceFixture fixture) throws Exception
    {
        configuration.setProperty(ResourcePropertySet.RESOURCE, fixture
            .buildResourcePropertyString());
        ((AbstractStandaloneLocalConfigurationWithXMLConfigurationBuilder) configuration)
            .parsePropertiesForPendingConfiguration();
        ((ResourceSupport) configuration).configureResources(container);
        return configuration.getFileHandler().readTextFile(getResourceConfigurationFile(fixture));
    }

    public void testConfigureDataSourceOnLocalContainerIfPropertyIsPresentOnDataSourceWithWindowsPath()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory.createDataSourceWithWindowsPath();
        String configuration =
            configureDataSourceViaPropertyAndRetrieveConfigurationFile(dataSourceFixture);
        configurationChecker.checkConfigurationForDataSourceMatchesDataSourceFixture(
            configuration, dataSourceFixture);
    }

    public void testConfigureCreatesDataSource() throws Exception
    {
        DataSourceFixture dataSourceFixture = ConfigurationFixtureFactory.createDataSource();
        String configuration = configureDataSourceAndRetrieveConfigurationFile(dataSourceFixture);
        configurationChecker.checkConfigurationForDataSourceMatchesDataSourceFixture(
            configuration, dataSourceFixture);
    }

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

    public void testConfigureCreatesDataSourceForDriverConfiguredDataSourceWithLocalTransactionSupport()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithLocalTransactionSupport();
        String configuration = configureDataSourceAndRetrieveConfigurationFile(dataSourceFixture);
        configurationChecker
            .checkConfigurationForDriverConfiguredDataSourceWithLocalTransactionSupportMatchesDataSourceFixture(
                configuration, dataSourceFixture);
    }

    public void testConfigureCreatesDataSourceForDriverConfiguredDataSourceWithXaTransactionSupport()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithXaTransactionSupport();
        String configuration = configureDataSourceAndRetrieveConfigurationFile(dataSourceFixture);
        configurationChecker
            .checkConfigurationForDriverConfiguredDataSourceWithXaTransactionSupportMatchesDataSourceFixture(
                configuration, dataSourceFixture);
    }

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

    public void testConfigureCreatesResource() throws Exception
    {
        ResourceFixture resourceFixture =
            ConfigurationFixtureFactory.createXADataSourceAsResource();
        String configuration = configureResourceAndRetrieveConfigurationFile(resourceFixture);
        configurationChecker.checkConfigurationMatchesResourceFixture(configuration,
            resourceFixture);
    }

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
        configurationChecker.checkConfigurationMatchesResourceFixture(configuration,
            resourceFixture1);
        configurationChecker.checkConfigurationMatchesResourceFixture(configuration,
            resourceFixture2);
    }
}
