/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * Contains XML logic used to validate the XML output of a WebLogic 9-10.3x DataSource
 * configuration.
 * 
 * @version $Id$
 */
public class WebLogic9x10xAnd103xConfigurationChecker extends
    WebLogic8xConfigurationChecker
{

    /**
     * XML namespace prefix.
     */
    private static final String NS_PREFIX = "jdbc:";

    /**
     * Creates the WebLogic XML namespace. {@inheritdoc}
     * @param serverName Server name.
     */
    public WebLogic9x10xAnd103xConfigurationChecker(String serverName)
    {
        super(serverName);
        Map<String, String> m = new HashMap<String, String>();
        m.put("jdbc", "http://www.bea.com/ns/weblogic/90");
        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);
    }

    /**
     * {@inheritdoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDriverConfiguredDSWithLocalTransactionSupportMatchesDSFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        checkDataSource(configuration, dataSourceFixture);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.driverClass,
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/" + NS_PREFIX
                + "driver-name", configuration);
        XMLAssert.assertXpathEvaluatesTo("None",
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/"
                + NS_PREFIX + "global-transactions-protocol", configuration);
    }

    /**
     * {@inheritdoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        checkDataSource(configuration, dataSourceFixture);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.driverClass,
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/" + NS_PREFIX
                + "driver-name", configuration);
        XMLAssert.assertXpathEvaluatesTo("None",
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/"
                + NS_PREFIX + "global-transactions-protocol", configuration);
    }

    /**
     * {@inheritdoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDriverConfiguredDSWithXaTransactionSupportMatchesDSFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        checkDataSource(configuration, dataSourceFixture);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.driverClass,
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/" + NS_PREFIX
                + "driver-name", configuration);
        XMLAssert.assertXpathEvaluatesTo("EmulateTwoPhaseCommit", "/" + NS_PREFIX
            + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/" + NS_PREFIX
            + "global-transactions-protocol", configuration);
    }

    /**
     * {@inheritdoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForXADataSourceConfiguredDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        checkDataSource(configuration, dataSourceFixture);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.driverClass,
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/" + NS_PREFIX
                + "driver-name", configuration);
        XMLAssert.assertXpathEvaluatesTo("TwoPhaseCommit",
            "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/"
                + NS_PREFIX + "global-transactions-protocol", configuration);
    }

    /**
     * {@inheritdoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void checkDataSource(String configuration, DataSourceFixture dataSourceFixture)
        throws Exception
    {
        DataSource ds = dataSourceFixture.buildDataSource();

        XMLAssert.assertXpathEvaluatesTo(ds.getId(), "/" + NS_PREFIX + "jdbc-data-source/"
            + NS_PREFIX + "name", configuration);
        if (dataSourceFixture.url != null)
        {
            XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.url,
                "/" + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/"
                    + NS_PREFIX + "url", configuration);
        }
        Properties driverProperties = ds.getConnectionProperties();
        ds.getConnectionProperties().setProperty("user", ds.getUsername());

        Iterator<Object> i = driverProperties.keySet().iterator();
        while (i.hasNext())
        {
            String propertyName = i.next().toString();

            XMLAssert.assertXpathEvaluatesTo(driverProperties.getProperty(propertyName), "/"
                + NS_PREFIX + "jdbc-data-source/" + NS_PREFIX + "jdbc-driver-params/"
                + NS_PREFIX + "properties/" + NS_PREFIX + "property[" + NS_PREFIX + "name='"
                + propertyName + "']/" + NS_PREFIX + "value", configuration);
        }
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, "/" + NS_PREFIX
            + "jdbc-data-source/" + NS_PREFIX + "jdbc-data-source-params/" + NS_PREFIX
            + "jndi-name", configuration);
    }

    /**
     * {@inheritdoc}
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
