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
package org.codehaus.cargo.container.orion.internal;

import java.util.Iterator;
import java.util.Properties;

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Contains XML logic used to validate the XML output of an Orion DataSource configuration.
 * 
 * @version $Id$
 */
public class OrionConfigurationChecker implements ConfigurationChecker
{

    /**
     * Get the id of a datasource fixture.
     * @param dataSourceFixture Datasource fixture.
     * @return Id for <code>fixture</code>.
     */
    private static String getDataSourceId(DataSourceFixture dataSourceFixture)
    {
        String id = dataSourceFixture.buildDataSource().getId();
        return id;
    }

    /**
     * Validate datasource.
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @param name Datasource name.
     * @throws Exception If anything goes wrong.
     */
    private static void validateDataSource(String configuration,
        DataSourceFixture dataSourceFixture, String name) throws Exception
    {
        if (dataSourceFixture.url == null)
        {
            XMLAssert.assertXpathNotExists("//data-source[@name='" + name + "']/@url",
                configuration);
        }
        else
        {
            XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.url, "//data-source[@name='"
                + name + "']/@url", configuration);
        }

        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.username, "//data-source[@name='"
            + name + "']/@username", configuration);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.password, "//data-source[@name='"
            + name + "']/@password", configuration);

        Properties driverProperties =
            dataSourceFixture.buildDataSource().getConnectionProperties();

        if (driverProperties != null)
        {
            Iterator<?> i = driverProperties.keySet().iterator();
            while (i.hasNext())
            {
                String propertyName = i.next().toString();
                XMLAssert.assertXpathEvaluatesTo(driverProperties.getProperty(propertyName),
                    "//data-source[@name='" + name + "']/property[@name='" + propertyName
                        + "']/@value", configuration);
            }
        }
        XMLAssert.assertXpathEvaluatesTo("30", "//data-source[@name='" + name
            + "']/@inactivity-timeout", configuration);
    }

    /**
     * Check XML for CMT datasource.
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @param className Class name.
     * @throws Exception If anything goes wrong.
     */
    private void checkXmlForCMTDataSourceBackedByImplementationClass(String configuration,
        DataSourceFixture dataSourceFixture, String className) throws Exception
    {
        String id = getDataSourceId(dataSourceFixture);

        String providedDataSourceId = id + "Provided";
        String providedDataSourceJndiName = dataSourceFixture.jndiLocation + "Provided";

        XMLAssert.assertXpathEvaluatesTo(className, "//data-source[@name='"
            + providedDataSourceId + "']/@class", configuration);
        XMLAssert.assertXpathEvaluatesTo(className, "//data-source[@name='"
            + providedDataSourceId + "']/@connection-driver", configuration);
        XMLAssert.assertXpathEvaluatesTo(providedDataSourceJndiName, "//data-source[@name='"
            + providedDataSourceId + "']/@location", configuration);
        validateDataSource(configuration, dataSourceFixture, providedDataSourceId);

        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, "//data-source[@name='"
            + id + "']/@location", configuration);
        if (dataSourceFixture.connectionType.equals(ConfigurationEntryType.XA_DATASOURCE))
        {
            XMLAssert.assertXpathEvaluatesTo(providedDataSourceJndiName, "//data-source[@name='"
                + id + "']/@xa-source-location", configuration);
            XMLAssert.assertXpathEvaluatesTo("com.evermind.sql.OrionCMTDataSource",
                "//data-source[@name='" + id + "']/@class", configuration);
        }
        else
        {
            XMLAssert.assertXpathEvaluatesTo(providedDataSourceJndiName, "//data-source[@name='"
                + id + "']/@source-location", configuration);
            XMLAssert.assertXpathEvaluatesTo("com.evermind.sql.OrionPooledDataSource",
                "//data-source[@name='" + id + "']/@class", configuration);
        }

    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    public void checkConfigurationForDriverConfiguredDSWithLocalTransactionSupportMatchesDSFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        String id = getDataSourceId(dataSourceFixture);
        XMLAssert.assertXpathEvaluatesTo("com.evermind.sql.DriverManagerDataSource",
            "//data-source[@name='" + id + "']/@class", configuration);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.driverClass, "//data-source[@name='"
            + id + "']/@connection-driver", configuration);

        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, "//data-source[@name='"
            + id + "']/@ejb-location", configuration);

        validateDataSource(configuration, dataSourceFixture, id);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    public void checkConfigurationForDataSourceMatchesDataSourceFixture(String configuration,
        DataSourceFixture dataSourceFixture) throws Exception
    {
        String id = getDataSourceId(dataSourceFixture);
        XMLAssert.assertXpathEvaluatesTo("com.evermind.sql.DriverManagerDataSource",
            "//data-source[@name='" + id + "']/@class", configuration);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.driverClass, "//data-source[@name='"
            + id + "']/@connection-driver", configuration);

        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, "//data-source[@name='"
            + id + "']/@location", configuration);

        validateDataSource(configuration, dataSourceFixture, id);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    public void checkConfigurationForDriverConfiguredDSWithXaTransactionSupportMatchesDSFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        String id = getDataSourceId(dataSourceFixture);
        XMLAssert.assertXpathEvaluatesTo("com.evermind.sql.DriverManagerDataSource",
            "//data-source[@name='" + id + "']/@class", configuration);
        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.driverClass, "//data-source[@name='"
            + id + "']/@connection-driver", configuration);

        XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.jndiLocation, "//data-source[@name='"
            + id + "']/@xa-location", configuration);

        validateDataSource(configuration, dataSourceFixture, id);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    public void checkConfigurationForXADataSourceConfiguredDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        checkXmlForCMTDataSourceBackedByImplementationClass(configuration, dataSourceFixture,
            dataSourceFixture.driverClass);
    }

    /**
     * {@inheritDoc}
     * @param dataSourceEntry Datasource entry.
     * @return Context with inserted configuration entry.
     */
    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        StringBuilder correctContext = new StringBuilder("<data-sources>");
        correctContext.append(dataSourceEntry);
        correctContext.append("</data-sources>");
        return correctContext.toString();
    }

    /**
     * Throws {@link RuntimeException}. {@inheritDoc}
     * @param configuration Ignored.
     * @param resourceFixture Ignored.
     * @throws Exception {@link RuntimeException}.
     */
    public void checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        throw new RuntimeException("TODO");
    }

    /**
     * Throws {@link RuntimeException}. {@inheritDoc}
     * @param configuration Ignored.
     * @param resourceFixture Ignored.
     * @throws Exception {@link RuntimeException}.
     */
    public void checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        throw new RuntimeException("TODO");
    }

}
