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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Assertions;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.DataSourceConverter;

/**
 * Contains XML logic used to validate the XML output of Tomcat DataSource configuration.
 */
public class Tomcat4xConfigurationChecker implements ConfigurationChecker
{
    /**
     * XPath engine.
     */
    protected XPathEngine xpathEngine;

    /**
     * Datasource converter.
     */
    private DataSourceConverter converter = new DataSourceConverter();

    /**
     * Creates the XPathEngine.
     */
    public Tomcat4xConfigurationChecker()
    {
        this.xpathEngine = new JAXPXPathEngine();
    }

    /**
     * Return given XML as Source. We need this as Source objects are single use.
     * @param xml XML String.
     * @return Source object.
     */
    protected static Source toSource(String xml)
    {
        return new StreamSource(new StringReader(xml));
    }

    /**
     * Check that a configuration matches a given resource.
     * @param configuration Configuration name.
     * @param resource Resource.
     * @throws Exception If anything goes wrong.
     */
    protected void checkConfigurationMatchesResource(String configuration, Resource resource)
        throws Exception
    {
        String pathToResource =
            "//Engine/DefaultContext/Resource[@name='" + resource.getName() + "']";

        String pathToResourceParams =
            "//Engine/DefaultContext/ResourceParams[@name='" + resource.getName() + "']";

        Assertions.assertEquals("Container", xpathEngine.evaluate(
            pathToResource + "/@auth", toSource(configuration)));

        if (resource.getClassName() != null)
        {
            Assertions.assertEquals(resource.getClassName(), xpathEngine.evaluate(
                pathToResource + "/@type", toSource(configuration)));
        }
        else
        {
            Assertions.assertEquals(resource.getType(), xpathEngine.evaluate(
                pathToResource + "/@type", toSource(configuration)));
        }

        for (String propertyName : resource.getParameterNames())
        {
            String propertyNameInTomcatXML = propertyName;
            if ("user".equals(propertyName))
            {
                // see: https://codehaus-cargo.atlassian.net/browse/CARGO-705
                propertyNameInTomcatXML = "username";
            }

            Assertions.assertEquals(resource.getParameter(propertyName), xpathEngine.evaluate(
                pathToResourceParams + "/parameter[name='" + propertyNameInTomcatXML + "']/value",
                    toSource(configuration)));
        }
    }

    /**
     * Check that a configuration matches a given datasource.
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource fixture.
     * @param resourceType Resource type.
     * @throws Exception If anything goes wrong.
     */
    private void convertToResourceAndCheckConfigurationMatches(String configuration,
        DataSourceFixture dataSourceFixture, String resourceType) throws Exception
    {
        Resource resource = converter.convertToResource(
            dataSourceFixture.buildDataSource(), resourceType, "driverClassName");
        resource.setParameter("factory", getDataSourceFactory());
        checkConfigurationMatchesResource(configuration, resource);
    }

    /**
     * @return Datasource factory class:
     * <code>org.apache.commons.dbcp.BasicDataSourceFactory</code>
     */
    protected String getDataSourceFactory()
    {
        return "org.apache.commons.dbcp.BasicDataSourceFactory";
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
        notExists(configuration, dataSourceFixture);
    }

    /**
     * Check if a configuration contains a given datasource.
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource.
     * @throws Exception If anything goes wrong.
     */
    protected void notExists(String configuration, DataSourceFixture dataSourceFixture)
        throws Exception
    {
        String pathToResource =
            "//Engine/DefaultContext/Resource[@name='" + dataSourceFixture.jndiLocation + "']";

        Assertions.assertFalse(xpathEngine.selectNodes(
            pathToResource, toSource(configuration)).iterator().hasNext());

        String pathToResourceParams =
            "//Engine/DefaultContext/ResourceParams[@name='" + dataSourceFixture.jndiLocation
                + "']";
        Assertions.assertFalse(xpathEngine.selectNodes(
            pathToResourceParams, toSource(configuration)).iterator().hasNext());
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
        convertToResourceAndCheckConfigurationMatches(configuration, dataSourceFixture,
            ConfigurationEntryType.DATASOURCE);
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
        notExists(configuration, dataSourceFixture);
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
        notExists(configuration, dataSourceFixture);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param resourceFixture Resource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        Resource resource = resourceFixture.buildResource();
        resource.setParameter("factory", "org.apache.naming.factory.BeanFactory");
        checkConfigurationMatchesResource(configuration, resource);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param resourceFixture Resource fixture.
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        Resource resource = resourceFixture.buildResource();
        resource.setParameter("factory", "org.apache.naming.factory.MailSessionFactory");
        checkConfigurationMatchesResource(configuration, resource);
    }

    /**
     * {@inheritDoc}
     * @param dataSourceEntry Datasource entry.
     * @return Context with inserted configuration entry.
     */
    @Override
    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        return "<Server><Engine><DefaultContext>" + dataSourceEntry
            + "</DefaultContext></Engine></Server>";
    }

}
