/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http//www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ========================================================================
 */
package org.codehaus.cargo.container.weblogic.internal;

import org.custommonkey.xmlunit.XMLAssert;

import junit.framework.Assert;

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.DataSourceConverter;

/**
 * Contains XML logic used to validate the XML output of a WebLogic 8.x DataSource configuration.
 */
public class WebLogic8xConfigurationChecker implements ConfigurationChecker
{

    /**
     * Server name.
     */
    private String serverName;

    /**
     * Path to connection pool.
     */
    private String pathToConnectionPool;

    /**
     * Path to transaction datasource.
     */
    private String pathToTxDataSource;

    /**
     * Path to datasource.
     */
    private String pathToDataSource;

    /**
     * Saves the server name.
     * @param serverName Server name.
     */
    public WebLogic8xConfigurationChecker(String serverName)
    {
        this.serverName = serverName;
    }

    /**
     * Initialize configuration.
     * @param jndiLocation JNDI location.
     */
    private void init(String jndiLocation)
    {
        pathToConnectionPool = "//JDBCConnectionPool[@Name='" + jndiLocation + "']";
        pathToTxDataSource = "//JDBCTxDataSource[@Name='" + jndiLocation + "']";
        pathToDataSource = "//JDBCDataSource[@Name='" + jndiLocation + "']";
    }

    /**
     * Check connection pool.
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource.
     * @throws Exception If anything goes wrong.
     */
    private void checkConnectionPool(String configuration, DataSourceFixture dataSourceFixture)
        throws Exception
    {
        if (dataSourceFixture.url != null)
        {
            XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.url, pathToConnectionPool
                + "/@URL", configuration);
        }
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.driverClass, pathToConnectionPool
            + "/@DriverName", configuration);

        Assert.assertTrue(configuration.contains("user=" + dataSourceFixture.username));
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.password, pathToConnectionPool
            + "/@Password", configuration);
        XMLAssert.assertXpathEvaluatesTo("server", pathToConnectionPool + "/@Targets",
            configuration);

        XMLAssert.assertXpathEvaluatesTo(serverName, pathToConnectionPool + "/@Targets",
            configuration);
        DataSource ds = dataSourceFixture.buildDataSource();
        ds.getConnectionProperties().setProperty("user", ds.getUsername());
        XMLAssert.assertXpathEvaluatesTo(new DataSourceConverter()
            .getConnectionPropertiesAsASemicolonDelimitedString(ds), pathToConnectionPool
            + "/@Properties", configuration);
    }

    /**
     * Check TX datasource.
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    protected void checkTxDataSource(String configuration, DataSourceFixture dataSourceFixture)
        throws Exception
    {
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, pathToTxDataSource
            + "/@JNDIName", configuration);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, pathToTxDataSource
            + "/@PoolName", configuration);
        XMLAssert.assertXpathEvaluatesTo(serverName, pathToTxDataSource + "/@Targets",
            configuration);
        XMLAssert.assertXpathEvaluatesTo("server", pathToTxDataSource + "/@Targets",
            configuration);
        XMLAssert.assertXpathNotExists("//JDBCDataSource", configuration);
    }

    /**
     * Check datasource.
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    protected void checkDataSource(String configuration, DataSourceFixture dataSourceFixture)
        throws Exception
    {
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, pathToDataSource
            + "/@Name", configuration);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, pathToDataSource
            + "/@JNDIName", configuration);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, pathToDataSource
            + "/@PoolName", configuration);
        XMLAssert.assertXpathEvaluatesTo(serverName, pathToDataSource + "/@Targets",
            configuration);
        XMLAssert.assertXpathEvaluatesTo("server", pathToDataSource + "/@Targets",
            configuration);
        XMLAssert.assertXpathNotExists(pathToTxDataSource, configuration);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDriverConfiguredDSWithLocalTransactionSupportMatchesDSFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        init(dataSourceFixture.jndiLocation);
        checkConnectionPool(configuration, dataSourceFixture);
        checkTxDataSource(configuration, dataSourceFixture);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDataSourceMatchesDataSourceFixture(String configuration,
        DataSourceFixture dataSourceFixture) throws Exception
    {
        init(dataSourceFixture.jndiLocation);
        checkConnectionPool(configuration, dataSourceFixture);
        checkDataSource(configuration, dataSourceFixture);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDriverConfiguredDSWithXaTransactionSupportMatchesDSFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        init(dataSourceFixture.jndiLocation);
        checkConnectionPool(configuration, dataSourceFixture);
        checkTxDataSource(configuration, dataSourceFixture);
        XMLAssert.assertXpathEvaluatesTo("true", pathToTxDataSource + "/@EnableTwoPhaseCommit",
            configuration);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForXADataSourceConfiguredDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        init(dataSourceFixture.jndiLocation);
        checkConnectionPool(configuration, dataSourceFixture);
        checkTxDataSource(configuration, dataSourceFixture);
    }

    /**
     * {@inheritDoc}
     * @param dataSourceEntry Datasource entry.
     * @return Domain XML with <code>dataSourceEntry</code>.
     */
    @Override
    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        return "<Domain>" + dataSourceEntry + "</Domain>";
    }

    /**
     * TODO: WebLogic container doesn't support Resources. {@inheritDoc}
     * @param configuration Configuration.
     * @param resourceFixture Resource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        // TODO
    }

    /**
     * TODO: WebLogic container doesn't support Resources. {@inheritDoc}
     * @param configuration Configuration.
     * @param resourceFixture Resource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        // TODO
    }
}
