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
package org.codehaus.cargo.container.spi.configuration.builder;

import junit.framework.TestCase;

import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilderTests;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;

/**
 * Abstract test for any {@link ConfigurationBuilder} and {@link ConfigurationChecker}.
 * 
 * @version $Id$
 */
public abstract class AbstractConfigurationBuilderTest extends TestCase implements
    ConfigurationBuilderTests
{
    /**
     * Configuration builder.
     */
    private ConfigurationBuilder builder;

    /**
     * Configuration checked.
     */
    private ConfigurationChecker checker;

    /**
     * Creates the configuration builder and checker. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        builder = createConfigurationBuilder();
        checker = createConfigurationChecker();
    }

    /**
     * @return Configuration builder.
     */
    protected abstract ConfigurationBuilder createConfigurationBuilder();

    /**
     * @return Configuration checker.
     */
    protected abstract ConfigurationChecker createConfigurationChecker();

    /**
     * Test configuration with driver configured with local transaction support.
     * @throws Exception If anything goes wrong.
     */
    public void testBuildConfigurationEntryForDriverConfiguredDSWithLocalTransactionSupport()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithLocalTransactionSupport();
        String dataSourceEntry =
            builder.toConfigurationEntry(dataSourceFixture.buildDataSource());
        String configuration = checker.insertConfigurationEntryIntoContext(dataSourceEntry);
        checker
            .checkConfigurationForDriverConfiguredDSWithLocalTransactionSupportMatchesDSFixture(
                configuration, dataSourceFixture);

    }

    /**
     * Test configuration with datasource entries.
     * @throws Exception If anything goes wrong.
     */
    public void testBuildConfigurationEntryForDataSource() throws Exception
    {
        DataSourceFixture dataSourceFixture = ConfigurationFixtureFactory.createDataSource();
        String dataSourceEntry =
            builder.toConfigurationEntry(dataSourceFixture.buildDataSource());
        String configuration = checker.insertConfigurationEntryIntoContext(dataSourceEntry);
        checker.checkConfigurationForDataSourceMatchesDataSourceFixture(configuration,
            dataSourceFixture);

    }

    /**
     * Test configuration with driver configured with XA transaction support.
     * @throws Exception If anything goes wrong.
     */
    public void testBuildConfigurationEntryForDriverConfiguredDataSourceWithXaTransactionSupport()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithXaTransactionSupport();
        String dataSourceEntry =
            builder.toConfigurationEntry(dataSourceFixture.buildDataSource());
        String configuration = checker.insertConfigurationEntryIntoContext(dataSourceEntry);
        checker
            .checkConfigurationForDriverConfiguredDSWithXaTransactionSupportMatchesDSFixture(
                configuration, dataSourceFixture);
    }

    /**
     * Test configuration with XA datasource.
     * @throws Exception If anything goes wrong.
     */
    public void testBuildConfigurationEntryForXADataSourceConfiguredDataSource() throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory.createXADataSourceConfiguredDataSource();
        String dataSourceEntry =
            builder.toConfigurationEntry(dataSourceFixture.buildDataSource());
        String configuration = checker.insertConfigurationEntryIntoContext(dataSourceEntry);

        checker.checkConfigurationForXADataSourceConfiguredDataSourceMatchesDataSourceFixture(
            configuration, dataSourceFixture);

    }

    /**
     * Test configuration with XA datasource configured as resource.
     * @throws Exception If anything goes wrong.
     */
    public void testBuildConfigurationEntryForXADataSourceConfiguredResource() throws Exception
    {
        ResourceFixture resourceFixture =
            ConfigurationFixtureFactory.createXADataSourceAsResource();
        String resourceEntry = builder.toConfigurationEntry(resourceFixture.buildResource());
        String configuration = checker.insertConfigurationEntryIntoContext(resourceEntry);
        checker.checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
            configuration, resourceFixture);
    }

    /**
     * Test configuration with mail resource.
     * @throws Exception If anything goes wrong.
     */
    public void testBuildConfigurationEntryForMailSessionConfiguredResource() throws Exception
    {
        ResourceFixture resourceFixture =
            ConfigurationFixtureFactory.createMailSessionAsResource();
        String resourceEntry = builder.toConfigurationEntry(resourceFixture.buildResource());
        String configuration = checker.insertConfigurationEntryIntoContext(resourceEntry);
        checker.checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(
            configuration, resourceFixture);
    }

}
