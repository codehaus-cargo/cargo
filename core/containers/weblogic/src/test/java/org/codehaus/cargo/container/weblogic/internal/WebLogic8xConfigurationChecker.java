/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import junit.framework.Assert;

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.DataSourceConverter;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Contains XML logic used to validate the XML output of a WebLogic 8.x DataSource configuration.
 * 
 * @version $Id $
 */
public class WebLogic8xConfigurationChecker implements ConfigurationChecker
{

    private String serverName;

    private String pathToConnectionPool;

    private String pathToTxDataSource;

    private String pathToDataSource;

    public WebLogic8xConfigurationChecker(String serverName)
    {
        this.serverName = serverName;
    }

    private void init(String jndiLocation)
    {
        pathToConnectionPool = "//JDBCConnectionPool[@Name='" + jndiLocation + "']";
        pathToTxDataSource = "//JDBCTxDataSource[@Name='" + jndiLocation + "']";
        pathToDataSource = "//JDBCDataSource[@Name='" + jndiLocation + "']";
    }

    private void checkConnectionPool(String configuration, DataSourceFixture dataSourceFixture)
    {

        try
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
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    protected void notConfigured(String configuration, ResourceFixture dataSourceFixture)
    {
        // TODO
    }

    protected void checkTxDataSource(String configuration, DataSourceFixture dataSourceFixture)

    {

        try
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
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void checkDataSource(String configuration, DataSourceFixture dataSourceFixture)
    {

        try
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
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    public void checkConfigurationForDriverConfiguredDataSourceWithLocalTransactionSupportMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        init(dataSourceFixture.jndiLocation);
        checkConnectionPool(configuration, dataSourceFixture);
        checkTxDataSource(configuration, dataSourceFixture);
    }

    public void checkConfigurationForDataSourceMatchesDataSourceFixture(String configuration,
        DataSourceFixture dataSourceFixture) throws Exception
    {
        init(dataSourceFixture.jndiLocation);
        checkConnectionPool(configuration, dataSourceFixture);
        checkDataSource(configuration, dataSourceFixture);
    }

    public void checkConfigurationForDriverConfiguredDataSourceWithXaTransactionSupportMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        init(dataSourceFixture.jndiLocation);
        checkConnectionPool(configuration, dataSourceFixture);
        checkTxDataSource(configuration, dataSourceFixture);
        XMLAssert.assertXpathEvaluatesTo("true", pathToTxDataSource + "/@EnableTwoPhaseCommit",
            configuration);
    }

    public void checkConfigurationForXADataSourceConfiguredDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        init(dataSourceFixture.jndiLocation);
        checkConnectionPool(configuration, dataSourceFixture);
        checkTxDataSource(configuration, dataSourceFixture);
    }

    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        return "<Domain>" + dataSourceEntry + "</Domain>";
    }

    public void checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(String configuration,
        ResourceFixture resourceFixture) throws Exception
    {
        notConfigured(configuration, resourceFixture);
    }

    public void checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        notConfigured(configuration, resourceFixture);       
    }
}
