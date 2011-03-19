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

import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.DataSourceConverter;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Contains XML logic used to validate the XML output of a Resin 2.x DataSource configuration.
 * 
 * @version $Id$
 */
public class Resin2xConfigurationChecker implements ConfigurationChecker
{

    /**
     * Datasource converter.
     */
    private DataSourceConverter converter = new DataSourceConverter();

    /**
     * Check that a configuration matches a given resource.
     * @param configuration Configuration.
     * @param resource Resource definition.
     */
    private void checkConfigurationMatchesResource(String configuration, Resource resource)
    {
        try
        {
            String pathToResourceRef =
                "//resource-ref[res-ref-name='" + resource.getName() + "']";

            if (resource.getClassName() != null)
            {
                XMLAssert.assertXpathEvaluatesTo(resource.getClassName(), pathToResourceRef
                    + "/res-type", configuration);
            }
            else
            {
                XMLAssert.assertXpathEvaluatesTo(resource.getType(), pathToResourceRef
                    + "/res-type", configuration);
            }

            for (String propertyName : resource.getParameterNames())
            {
                XMLAssert.assertXpathEvaluatesTo(resource.getParameter(propertyName),
                    pathToResourceRef + "/init-param/@" + propertyName, configuration);
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
        XMLAssert.assertXpathNotExists("//resource-ref[res-ref-name='"
            + dataSourceFixture.jndiLocation + "']", configuration);

    }

    /**
     * Convert to resource and check configuration.
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @param resourceType Resource type.
     */
    private void convertToResourceAndCheckConfigurationMatches(String configuration,
        DataSourceFixture dataSourceFixture, String resourceType)
    {
        Resource resource =
            converter.convertToResource(dataSourceFixture.buildDataSource(), resourceType,
                "driver-name");
        checkConfigurationMatchesResource(configuration, resource);
    }

    /**
     * {@inheritdoc}
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
     * {@inheritdoc}
     * @param configuration Configuration.
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
     * {@inheritdoc}
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
     * {@inheritdoc}
     * @param configuration Configuration.
     * @param dataSourceFixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    public void checkConfigurationForXADataSourceConfiguredDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        // per resin docs, res-type doesn't necessarily reflect the object returned from JNDI
        convertToResourceAndCheckConfigurationMatches(configuration, dataSourceFixture,
            ConfigurationEntryType.XA_DATASOURCE);
    }

    /**
     * {@inheritdoc}
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
     * {@inheritdoc}
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
     * {@inheritdoc}
     * @param dataSourceEntry Entry to insert.
     * @return <code>dataSourceEntry</code>.
     */
    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        return dataSourceEntry;
    }

}
