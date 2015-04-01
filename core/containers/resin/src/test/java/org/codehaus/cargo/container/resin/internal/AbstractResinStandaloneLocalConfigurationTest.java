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
package org.codehaus.cargo.container.resin.internal;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.spi.configuration.builder.AbstractLocalConfigurationWithConfigurationBuilderTest;

/**
 * Unit tests for standalone Resin configurations.
 * 
 */
public abstract class AbstractResinStandaloneLocalConfigurationTest extends
    AbstractLocalConfigurationWithConfigurationBuilderTest
{

    /**
     * {@inheritDoc}
     * @param fixture Datasource fixture.
     * @return <code>conf/resin.conf</code> in the configuration home.
     */
    @Override
    protected String getDataSourceConfigurationFile(DataSourceFixture fixture)
    {
        return configuration.getHome() + "/conf/resin.conf";
    }

    /**
     * {@inheritDoc}
     * @param fixture Resource fixture.
     * @return <code>conf/resin.conf</code> in the configuration home.
     */
    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return getDataSourceConfigurationFile(null);
    }

    /**
     * Test configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigure() throws Exception
    {
        configuration.configure(container);

        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/resin.conf"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/webapps/cargocpc.war"));
    }

    /**
     * Set up datasource file.
     * @throws Exception If anything goes wrong.
     */
    protected abstract void setUpDataSourceFile() throws Exception;

    /**
     * Setup the datasource file and call parent. {@inheritDoc}
     * @param fixture Datasource fixture.
     * @return Configuration file for <code>fixture</code>.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceViaPropertyAndRetrieveConfigurationFile(fixture);
    }

    /**
     * Setup the datasource file and call parent. {@inheritDoc}
     * @param fixture Datasource fixture.
     * @return Configuration file for <code>fixture</code>.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        setUpDataSourceFile();
        return super.configureDataSourceAndRetrieveConfigurationFile(fixture);
    }

    /**
     * Checks that creating datasource configuration entries with local transaction support
     * throws an exception with message
     * {@link Resin2xConfigurationBuilder#TRANSACTIONS_WITH_XA_OR_JCA_ONLY}. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesDataSourceForDriverConfiguredDSWithLocalTransactionSupport()
        throws Exception
    {
        try
        {
            super
                .testConfigureCreatesDataSourceForDriverConfiguredDSWithLocalTransactionSupport();
            fail("should have received an exception");

        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(Resin2xConfigurationBuilder.TRANSACTIONS_WITH_XA_OR_JCA_ONLY,
                e.getMessage());
        }
    }

    /**
     * Checks that creating datasource configuration entries with driver-configured XA transaction
     * support throws an exception with message
     * {@link Resin2xConfigurationBuilder#TRANSACTIONS_WITH_XA_OR_JCA_ONLY}. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesDataSourceForDriverConfiguredDSWithXaTransactionSupport()
        throws Exception
    {
        try
        {
            super
                .testConfigureCreatesDataSourceForDriverConfiguredDSWithXaTransactionSupport();
            fail("should have received an exception");

        }
        catch (UnsupportedOperationException e)
        {
            assertEquals(Resin2xConfigurationBuilder.TRANSACTIONS_WITH_XA_OR_JCA_ONLY,
                e.getMessage());
        }
    }

    /**
     * Resource support is currently unimplemented.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureCreatesResourceForXADataSource() throws Exception
    {
        // Nothing
    }

    /**
     * Resource support is currently unimplemented.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesResource() throws Exception
    {
        // Nothing
    }

    /**
     * Resource support is currently unimplemented.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void testConfigureCreatesTwoResourcesViaProperties() throws Exception
    {
        // Nothing
    }

}
