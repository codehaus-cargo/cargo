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
package org.codehaus.cargo.container.resin.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * Contains XML logic used to validate the XML output of a Resin 3.x DataSource configuration.
 * 
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
     * Adds the Resin namespaces to the XML namespace context.
     */
    public Resin3xConfigurationChecker()
    {
        // setup the namespace of the resin configuration file
        Map<String, String> m = new HashMap<String, String>();
        m.put("resin", NS_URL);

        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);
    }

    /**
     * {@inheritDoc}
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
            XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.driverClass, pathToDatabase + "/"
                + NS_PREFIX + "driver/" + NS_PREFIX + "type", configuration);

            if (dataSourceFixture.url == null)
            {
                XMLAssert.assertXpathNotExists(pathToDatabase + "/" + NS_PREFIX + "driver/"
                    + NS_PREFIX + "url", configuration);
            }
            else
            {
                XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.url, pathToDatabase + "/"
                    + NS_PREFIX + "driver/" + NS_PREFIX + "url", configuration);
            }

            XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.username, pathToDatabase + "/"
                + NS_PREFIX + "driver/" + NS_PREFIX + "user", configuration);
            XMLAssert.assertXpathEvaluatesTo(dataSourceFixture.password, pathToDatabase + "/"
                + NS_PREFIX + "driver/" + NS_PREFIX + "password", configuration);

            Properties driverProperties =
                dataSourceFixture.buildDataSource().getConnectionProperties();

            Iterator<Object> i = driverProperties.keySet().iterator();
            while (i.hasNext())
            {
                String propertyName = i.next().toString();
                XMLAssert.assertXpathEvaluatesTo(driverProperties.getProperty(propertyName),
                    pathToDatabase + "/" + NS_PREFIX + "driver/" + NS_PREFIX + "" + propertyName,
                    configuration);
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
        String pathToResource =
            "//" + NS_PREFIX + "resource[" + NS_PREFIX + "jndi-name='" + resource.getName()
                + "']";
        try
        {
            if (resource.getClassName() != null)
            {
                XMLAssert.assertXpathEvaluatesTo(resource.getClassName(), pathToResource + "/"
                    + NS_PREFIX + "type", configuration);
            }
            else
            {
                XMLAssert.assertXpathEvaluatesTo(resource.getType(), pathToResource + "/"
                    + NS_PREFIX + "type", configuration);
            }
            for (String propertyName : resource.getParameters().keySet())
            {
                XMLAssert.assertXpathEvaluatesTo(resource.getParameter(propertyName).toString(),
                    pathToResource + "/" + NS_PREFIX + "init/@" + propertyName, configuration);
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
        XMLAssert.assertXpathNotExists("//database[jndi-name='" + dataSourceFixture.jndiLocation
            + "']", configuration);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
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
