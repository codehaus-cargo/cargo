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
package org.codehaus.cargo.container.tomcat.internal;

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.DataSourceConverter;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Contains XML logic used to validate the XML output of Tomcat DataSource configuration.
 * 
 */
public class Tomcat4xConfigurationChecker implements ConfigurationChecker
{
    /**
     * Datasource converyer.
     */
    private DataSourceConverter converter = new DataSourceConverter();

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

        XMLAssert.assertXpathEvaluatesTo("Container", pathToResource + "/@auth", configuration);

        if (resource.getClassName() != null)
        {
            XMLAssert.assertXpathEvaluatesTo(resource.getClassName(), pathToResource + "/@type",
                configuration);
        }
        else
        {
            XMLAssert.assertXpathEvaluatesTo(resource.getType(), pathToResource + "/@type",
                configuration);
        }

        for (String propertyName : resource.getParameterNames())
        {
            String propertyNameInTomcatXML = propertyName;
            if ("user".equals(propertyName))
            {
                // see: http://jira.codehaus.org/browse/CARGO-705
                propertyNameInTomcatXML = "username";
            }

            XMLAssert.assertXpathEvaluatesTo(resource.getParameter(propertyName),
                pathToResourceParams + "/parameter[name='" + propertyNameInTomcatXML + "']/value",
                configuration);
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
        Resource resource =
            converter.convertToResource(dataSourceFixture.buildDataSource(), resourceType,
                "driverClassName");
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

        XMLAssert.assertXpathNotExists(pathToResource, configuration);

        String pathToResourceParams =
            "//Engine/DefaultContext/ResourceParams[@name='" + dataSourceFixture.jndiLocation
                + "']";
        XMLAssert.assertXpathNotExists(pathToResourceParams, configuration);

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
        convertToResourceAndCheckConfigurationMatches(configuration, dataSourceFixture,
            ConfigurationEntryType.DATASOURCE);
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
        notExists(configuration, dataSourceFixture);
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
        notExists(configuration, dataSourceFixture);
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param resourceFixture Resource fixture.
     * @throws Exception If anything goes wrong.
     */
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
    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        return "<Server><Engine><DefaultContext>" + dataSourceEntry
            + "</DefaultContext></Engine></Server>";
    }

}
