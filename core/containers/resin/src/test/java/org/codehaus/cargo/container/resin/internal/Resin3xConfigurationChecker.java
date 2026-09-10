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
package org.codehaus.cargo.container.resin.internal;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Assertions;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;

/**
 * Contains XML logic used to validate the XML output of a Resin 3.x DataSource configuration.
 */
public class Resin3xConfigurationChecker implements ConfigurationChecker
{
    /**
     * Resin XML namespace URL.
     */
    private static final String NS_URL = "http://caucho.com/ns/resin";

    /**
     * Resin XML namespace prefix.
     */
    private static final String NS_PREFIX = "resin:";

    /**
     * XPath engine.
     */
    private XPathEngine xpathEngine;

    /**
     * Adds the Resin namespaces to the XML namespace context.
     */
    public Resin3xConfigurationChecker()
    {
        this.xpathEngine = new JAXPXPathEngine();
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("resin", NS_URL);
        this.xpathEngine.setNamespaceContext(namespaces);
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
     * Check whether the configuration matches a certain datasource fixture.
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     */
    public void checkConfigurationMatchesDataSourceFixture(String configuration,
        DataSourceFixture dataSourceFixture)
    {
        String pathToDatabase =
            "//" + NS_PREFIX + "database[" + NS_PREFIX + "jndi-name='"
                + dataSourceFixture.jndiLocation + "']";

        try
        {
            Assertions.assertEquals(dataSourceFixture.driverClass, xpathEngine.evaluate(
                pathToDatabase + "/" + NS_PREFIX + "driver/" + NS_PREFIX + "type",
                    toSource(configuration)));

            if (dataSourceFixture.url == null)
            {
                Assertions.assertFalse(xpathEngine.selectNodes(
                    pathToDatabase + "/" + NS_PREFIX + "driver/" + NS_PREFIX + "url",
                        toSource(configuration)).iterator().hasNext());
            }
            else
            {
                Assertions.assertEquals(dataSourceFixture.url, xpathEngine.evaluate(
                    pathToDatabase + "/" + NS_PREFIX + "driver/" + NS_PREFIX + "url",
                        toSource(configuration)));
            }

            Assertions.assertEquals(dataSourceFixture.username, xpathEngine.evaluate(
                pathToDatabase + "/" + NS_PREFIX + "driver/" + NS_PREFIX + "user",
                    toSource(configuration)));
            Assertions.assertEquals(dataSourceFixture.password, xpathEngine.evaluate(
                pathToDatabase + "/" + NS_PREFIX + "driver/" + NS_PREFIX + "password",
                    toSource(configuration)));

            Properties driverProperties =
                dataSourceFixture.buildDataSource().getConnectionProperties();

            Iterator<Object> i = driverProperties.keySet().iterator();
            while (i.hasNext())
            {
                String propertyName = i.next().toString();
                Assertions.assertEquals(
                    driverProperties.getProperty(propertyName), xpathEngine.evaluate(
                        pathToDatabase + "/" + NS_PREFIX + "driver/"
                            + NS_PREFIX + "" + propertyName, toSource(configuration)));
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check configuration for a matching resource.
     * @param configuration Configuration.
     * @param resource Resource.
     */
    private void checkConfigurationMatchesResource(String configuration, Resource resource)
    {
        String pathToResource = "//" + NS_PREFIX + "resource["
            + NS_PREFIX + "jndi-name='" + resource.getName() + "']";
        try
        {
            if (resource.getClassName() != null)
            {
                Assertions.assertEquals(resource.getClassName(), xpathEngine.evaluate(
                    pathToResource + "/" + NS_PREFIX + "type", toSource(configuration)));
            }
            else
            {
                Assertions.assertEquals(resource.getType(), xpathEngine.evaluate(
                    pathToResource + "/" + NS_PREFIX + "type", toSource(configuration)));
            }
            for (String propertyName : resource.getParameters().keySet())
            {
                Assertions.assertEquals(resource.getParameter(propertyName), xpathEngine.evaluate(
                    pathToResource + "/" + NS_PREFIX + "init/@" + propertyName,
                        toSource(configuration)));
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    /**
     * Check that XML path doesn't exist for a datasource fixture.
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    private void notExists(String configuration, DataSourceFixture dataSourceFixture)
        throws Exception
    {
        Assertions.assertFalse(xpathEngine.selectNodes(
            "//database[jndi-name='" + dataSourceFixture.jndiLocation + "']",
                toSource(configuration)).iterator().hasNext());
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
        checkConfigurationMatchesDataSourceFixture(configuration, dataSourceFixture);
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
        notExists(configuration, dataSourceFixture);
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
        notExists(configuration, dataSourceFixture);
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
        checkConfigurationMatchesDataSourceFixture(configuration, dataSourceFixture);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration.
     * @param resourceFixture Resource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        Resource resource = resourceFixture.buildResource();
        checkConfigurationMatchesResource(configuration, resource);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration.
     * @param resourceFixture Resource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        Resource resource = resourceFixture.buildResource();
        checkConfigurationMatchesResource(configuration, resource);
    }

    /**
     * {@inheritDoc}
     * @param dataSourceEntry Entry to insert.
     * @return Context with new entry.
     */
    @Override
    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        StringBuilder configurationContext = new StringBuilder();
        configurationContext.append("<resin xmlns=\"http://caucho.com/ns/resin\"\n");
        configurationContext.append("       xmlnsresin=\"http://caucho.com/ns/resin/core\">");
        configurationContext.append(dataSourceEntry);
        configurationContext.append("</resin>");
        return configurationContext.toString();
    }

}
