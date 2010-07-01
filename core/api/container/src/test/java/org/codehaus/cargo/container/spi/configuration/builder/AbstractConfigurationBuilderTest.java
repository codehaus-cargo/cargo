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

import junit.framework.TestCase;

import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilder;
import org.codehaus.cargo.container.configuration.builder.ConfigurationBuilderTests;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;

public abstract class AbstractConfigurationBuilderTest extends TestCase implements
    ConfigurationBuilderTests
{
    ConfigurationBuilder builder;

    ConfigurationChecker checker;

    @Override
    public void setUp()
    {
        builder = createConfigurationBuilder();
        checker = createConfigurationChecker();
    }

    protected abstract ConfigurationChecker createConfigurationChecker();

    protected abstract ConfigurationBuilder createConfigurationBuilder();

    public void testBuildConfigurationEntryForDriverConfiguredDataSourceWithLocalTransactionSupport()
        throws Exception
    {
        DataSourceFixture dataSourceFixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithLocalTransactionSupport();
        String dataSourceEntry =
            builder.toConfigurationEntry(dataSourceFixture.buildDataSource());
        String configuration = checker.insertConfigurationEntryIntoContext(dataSourceEntry);
        checker
            .checkConfigurationForDriverConfiguredDataSourceWithLocalTransactionSupportMatchesDataSourceFixture(
                configuration, dataSourceFixture);

    }

    public void testBuildConfigurationEntryForDataSource() throws Exception
    {
        DataSourceFixture dataSourceFixture = ConfigurationFixtureFactory.createDataSource();
        String dataSourceEntry =
            builder.toConfigurationEntry(dataSourceFixture.buildDataSource());
        String configuration = checker.insertConfigurationEntryIntoContext(dataSourceEntry);
        checker.checkConfigurationForDataSourceMatchesDataSourceFixture(configuration,
            dataSourceFixture);

    }

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
            .checkConfigurationForDriverConfiguredDataSourceWithXaTransactionSupportMatchesDataSourceFixture(
                configuration, dataSourceFixture);

    }

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

    public void testBuildConfigurationEntryForXADataSourceConfiguredResource() throws Exception
    {
        ResourceFixture ResourceFixture =
            ConfigurationFixtureFactory.createXADataSourceAsResource();
        String ResourceEntry = builder.toConfigurationEntry(ResourceFixture.buildResource());
        String configuration = checker.insertConfigurationEntryIntoContext(ResourceEntry);
        checker.checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
            configuration, ResourceFixture);
    }

    public void testBuildConfigurationEntryForMailSessionConfiguredResource() throws Exception
    {
        ResourceFixture ResourceFixture =
            ConfigurationFixtureFactory.createMailSessionAsResource();
        String ResourceEntry = builder.toConfigurationEntry(ResourceFixture.buildResource());
        String configuration = checker.insertConfigurationEntryIntoContext(ResourceEntry);
        checker.checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(
            configuration, ResourceFixture);
    }

}
