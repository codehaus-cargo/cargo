/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Assertions;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;

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
     * XPath engine.
     */
    private XPathEngine xpathEngine;

    /**
     * Saves the server name.
     * @param serverName Server name.
     */
    public WebLogic8xConfigurationChecker(String serverName)
    {
        this.serverName = serverName;
        this.xpathEngine = new JAXPXPathEngine();
    }

    /**
     * Return given XML as Source. We need this as Source objects are single use.
     * @param xml XML String.
     * @return Source object.
     */
    private static Source toSource(String xml)
    {
        return new StreamSource(new StringReader(xml));
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
            Assertions.assertEquals(dataSourceFixture.url, xpathEngine.evaluate(
                pathToConnectionPool + "/@URL", toSource(configuration)));
        }
        Assertions.assertEquals(dataSourceFixture.driverClass, xpathEngine.evaluate(
                pathToConnectionPool + "/@DriverName", toSource(configuration)));

        Assertions.assertTrue(configuration.contains("user=" + dataSourceFixture.username));
        Assertions.assertEquals(dataSourceFixture.password, xpathEngine.evaluate(
                pathToConnectionPool + "/@Password", toSource(configuration)));
        Assertions.assertEquals("server", xpathEngine.evaluate(
            pathToConnectionPool + "/@Targets", toSource(configuration)));

        Assertions.assertEquals(serverName, xpathEngine.evaluate(
            pathToConnectionPool + "/@Targets", toSource(configuration)));
        DataSource ds = dataSourceFixture.buildDataSource();
        ds.getConnectionProperties().setProperty("user", ds.getUsername());
        Assertions.assertEquals(new DataSourceConverter()
            .getConnectionPropertiesAsASemicolonDelimitedString(ds), xpathEngine.evaluate(
                pathToConnectionPool + "/@Properties", toSource(configuration)));
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
        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
                pathToTxDataSource + "/@JNDIName", toSource(configuration)));
        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
                pathToTxDataSource + "/@PoolName", toSource(configuration)));
        Assertions.assertEquals(serverName, xpathEngine.evaluate(
            pathToTxDataSource + "/@Targets", toSource(configuration)));
        Assertions.assertEquals("server", xpathEngine.evaluate(
            pathToTxDataSource + "/@Targets", toSource(configuration)));
        Assertions.assertFalse(xpathEngine.selectNodes(
            "//JDBCDataSource", toSource(configuration)).iterator().hasNext());
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
        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
                pathToDataSource + "/@Name", toSource(configuration)));
        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
                pathToDataSource + "/@JNDIName", toSource(configuration)));
        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
                pathToDataSource + "/@PoolName", toSource(configuration)));
        Assertions.assertEquals(serverName, xpathEngine.evaluate(
            pathToDataSource + "/@Targets", toSource(configuration)));
        Assertions.assertEquals("server", xpathEngine.evaluate(
            pathToDataSource + "/@Targets", toSource(configuration)));
        Assertions.assertFalse(xpathEngine.selectNodes(
            pathToTxDataSource, toSource(configuration)).iterator().hasNext());
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
        Assertions.assertEquals("true", xpathEngine.evaluate(
            pathToTxDataSource + "/@EnableTwoPhaseCommit", toSource(configuration)));
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
