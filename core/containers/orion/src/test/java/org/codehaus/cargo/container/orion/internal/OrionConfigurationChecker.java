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
package org.codehaus.cargo.container.orion.internal;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Assertions;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;

/**
 * Contains XML logic used to validate the XML output of an Orion DataSource configuration.
 */
public class OrionConfigurationChecker implements ConfigurationChecker
{

    /**
     * XPath engine.
     */
    private XPathEngine xpathEngine;

    /**
     * Creates the various XML test elements.
     */
    public OrionConfigurationChecker()
    {
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
    private void validateDataSource(String configuration,
        DataSourceFixture dataSourceFixture, String name) throws Exception
    {
        if (dataSourceFixture.url == null)
        {
            Assertions.assertFalse(
                xpathEngine.selectNodes("//data-source[@name='" + name + "']/@url",
                    toSource(configuration)).iterator().hasNext());
        }
        else
        {
            Assertions.assertEquals(dataSourceFixture.url, xpathEngine.evaluate(
                "//data-source[@name='" + name + "']/@url", toSource(configuration)));
        }

        Assertions.assertEquals(dataSourceFixture.username, xpathEngine.evaluate(
            "//data-source[@name='" + name + "']/@username", toSource(configuration)));
        Assertions.assertEquals(dataSourceFixture.password, xpathEngine.evaluate(
            "//data-source[@name='" + name + "']/@password", toSource(configuration)));

        Properties driverProperties =
            dataSourceFixture.buildDataSource().getConnectionProperties();

        if (driverProperties != null)
        {
            Iterator<?> i = driverProperties.keySet().iterator();
            while (i.hasNext())
            {
                String propertyName = i.next().toString();
                Assertions.assertEquals(driverProperties.getProperty(propertyName),
                    xpathEngine.evaluate(
                        "//data-source[@name='" + name
                            + "']/property[@name='" + propertyName + "']/@value",
                                toSource(configuration)));
            }
        }
        Assertions.assertEquals("30", xpathEngine.evaluate(
            "//data-source[@name='" + name + "']/@inactivity-timeout", toSource(configuration)));
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

        Assertions.assertEquals(className, xpathEngine.evaluate(
            "//data-source[@name='" + providedDataSourceId + "']/@class",
                toSource(configuration)));
        Assertions.assertEquals(className, xpathEngine.evaluate(
            "//data-source[@name='" + providedDataSourceId + "']/@connection-driver",
                toSource(configuration)));
        Assertions.assertEquals(providedDataSourceJndiName, xpathEngine.evaluate(
            "//data-source[@name='" + providedDataSourceId + "']/@location",
                toSource(configuration)));
        validateDataSource(configuration, dataSourceFixture, providedDataSourceId);

        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@location", toSource(configuration)));
        if (dataSourceFixture.connectionType.equals(ConfigurationEntryType.XA_DATASOURCE))
        {
            Assertions.assertEquals(providedDataSourceJndiName, xpathEngine.evaluate(
                "//data-source[@name='" + id + "']/@xa-source-location", toSource(configuration)));
            Assertions.assertEquals("com.evermind.sql.OrionCMTDataSource", xpathEngine.evaluate(
                "//data-source[@name='" + id + "']/@class", toSource(configuration)));
        }
        else
        {
            Assertions.assertEquals(providedDataSourceJndiName, xpathEngine.evaluate(
                "//data-source[@name='" + id + "']/@source-location", toSource(configuration)));
            Assertions.assertEquals("com.evermind.sql.OrionPooledDataSource", xpathEngine.evaluate(
                "//data-source[@name='" + id + "']/@class", toSource(configuration)));
        }

    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDriverConfiguredDSWithLocalTransactionSupportMatchesDSFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        String id = getDataSourceId(dataSourceFixture);

        Assertions.assertEquals("com.evermind.sql.DriverManagerDataSource", xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@class", toSource(configuration)));
        Assertions.assertEquals(dataSourceFixture.driverClass, xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@connection-driver", toSource(configuration)));

        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@ejb-location", toSource(configuration)));

        validateDataSource(configuration, dataSourceFixture, id);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDataSourceMatchesDataSourceFixture(String configuration,
        DataSourceFixture dataSourceFixture) throws Exception
    {
        String id = getDataSourceId(dataSourceFixture);

        Assertions.assertEquals("com.evermind.sql.DriverManagerDataSource", xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@class", toSource(configuration)));
        Assertions.assertEquals(dataSourceFixture.driverClass, xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@connection-driver", toSource(configuration)));

        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@location", toSource(configuration)));

        validateDataSource(configuration, dataSourceFixture, id);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForDriverConfiguredDSWithXaTransactionSupportMatchesDSFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        String id = getDataSourceId(dataSourceFixture);

        Assertions.assertEquals("com.evermind.sql.DriverManagerDataSource", xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@class", toSource(configuration)));
        Assertions.assertEquals(dataSourceFixture.driverClass, xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@connection-driver", toSource(configuration)));

        Assertions.assertEquals(dataSourceFixture.jndiLocation, xpathEngine.evaluate(
            "//data-source[@name='" + id + "']/@xa-location", toSource(configuration)));

        validateDataSource(configuration, dataSourceFixture, id);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        throw new RuntimeException("TODO");
    }

}
