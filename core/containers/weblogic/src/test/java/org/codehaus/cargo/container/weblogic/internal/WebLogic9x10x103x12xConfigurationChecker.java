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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;

/**
 * Contains XML logic used to validate the XML output of a WebLogic 9.x - 10.3.x DataSource
 * configuration.
 */
public class WebLogic9x10x103x12xConfigurationChecker extends
    WebLogic8xConfigurationChecker
{

    /**
     * XML namespace prefix.
     */
    private static final String NS_PREFIX = "jdbc:";

    /**
     * Creates the WebLogic XML namespace. {@inheritDoc}
     * @param serverName Server name.
     */
    public WebLogic9x10x103x12xConfigurationChecker(String serverName)
    {
        super(serverName);

        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("jdbc", "http://www.bea.com/ns/weblogic/90");
        this.xpathEngine.setNamespaceContext(namespaces);
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
        checkDataSource(configuration, dataSourceFixture);
        Assertions.assertEquals(dataSourceFixture.driverClass, xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/" + NS_PREFIX
                + "driver-name", toSource(configuration)));
        Assertions.assertEquals("None", xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/"
                + NS_PREFIX + "global-transactions-protocol", toSource(configuration)));
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        checkDataSource(configuration, dataSourceFixture);
        Assertions.assertEquals(dataSourceFixture.driverClass, xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/"
                + NS_PREFIX + "driver-name", toSource(configuration)));
        Assertions.assertEquals("None", xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/"
                + NS_PREFIX + "global-transactions-protocol", toSource(configuration)));
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
        checkDataSource(configuration, dataSourceFixture);
        Assertions.assertEquals(dataSourceFixture.driverClass, xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/"
                + NS_PREFIX + "driver-name", toSource(configuration)));
        Assertions.assertEquals("EmulateTwoPhaseCommit", xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/"
                + NS_PREFIX + "global-transactions-protocol", toSource(configuration)));
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
        checkDataSource(configuration, dataSourceFixture);
        Assertions.assertEquals(dataSourceFixture.driverClass, xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/"
                + NS_PREFIX + "driver-name", toSource(configuration)));
        Assertions.assertEquals("TwoPhaseCommit", xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/"
                + NS_PREFIX + "global-transactions-protocol", toSource(configuration)));
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void checkDataSource(String configuration, DataSourceFixture dataSourceFixture)
        throws Exception
    {
        DataSource ds = dataSourceFixture.buildDataSource();

        Assertions.assertEquals(ds.getId(), xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "name", toSource(configuration)));
        if (dataSourceFixture.url != null)
        {
            Assertions.assertEquals(dataSourceFixture.url, xpathEngine.evaluate(
                "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/"
                    + NS_PREFIX + "url", toSource(configuration)));
        }
        Properties driverProperties = ds.getConnectionProperties();
        ds.getConnectionProperties().setProperty("user", ds.getUsername());

        Iterator<Object> i = driverProperties.keySet().iterator();
        while (i.hasNext())
        {
            String propertyName = i.next().toString();

            Assertions.assertEquals(driverProperties.getProperty(propertyName),
                xpathEngine.evaluate("/" + NS_PREFIX + "jdbc-data-source/"
                    + NS_PREFIX + "jdbc-driver-params/" + NS_PREFIX + "properties/"
                        + NS_PREFIX + "property[" + NS_PREFIX + "name='" + propertyName + "']/"
                            + NS_PREFIX + "value", toSource(configuration)));
        }
        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/"
                + NS_PREFIX + "jndi-name", toSource(configuration)));
    }

    /**
     * {@inheritDoc}
     * @param dataSourceEntry Datasource entry.
     * @return JDBC datasource XML entry with <code>dataSourceEntry</code>.
     */
    @Override
    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        return "<jdbc-data-source xmlns=\"http://www.bea.com/ns/weblogic/90\">" + dataSourceEntry
            + "</jdbc-data-source>";
    }
}
