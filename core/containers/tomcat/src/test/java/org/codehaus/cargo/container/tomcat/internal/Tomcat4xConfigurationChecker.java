/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import java.util.Iterator;
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
 * @version $Id $
 */
public class Tomcat4xConfigurationChecker implements ConfigurationChecker
{
    private DataSourceConverter converter = new DataSourceConverter();

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

        Iterator i = resource.getParameterNames().iterator();
        while (i.hasNext())
        {
            String propertyName = i.next().toString();

            XMLAssert.assertXpathEvaluatesTo(resource.getParameter(propertyName),
                pathToResourceParams + "/parameter[name='" + propertyName + "']/value",
                configuration);
        }
    }

    private void convertToResourceAndCheckConfigurationMatches(String configuration,
        DataSourceFixture dataSourceFixture, String resourceType) throws Exception
    {
        Resource resource =
            converter.convertToResource(dataSourceFixture.buildDataSource(), resourceType,
                "driverClassName");
        resource.setParameter("factory", getDataSourceFactory());
        checkConfigurationMatchesResource(configuration, resource);
    }

    protected String getDataSourceFactory()
    {
        return "org.apache.commons.dbcp.BasicDataSourceFactory";
    }

    public void checkConfigurationForDriverConfiguredDataSourceWithLocalTransactionSupportMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        notExists(configuration, dataSourceFixture);
    }

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

    public void checkConfigurationForDataSourceMatchesDataSourceFixture(String configuration,
        DataSourceFixture dataSourceFixture) throws Exception
    {
        convertToResourceAndCheckConfigurationMatches(configuration, dataSourceFixture,
            ConfigurationEntryType.DATASOURCE);
    }

    public void checkConfigurationForDriverConfiguredDataSourceWithXaTransactionSupportMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        notExists(configuration, dataSourceFixture);
    }

    public void checkConfigurationForXADataSourceConfiguredDataSourceMatchesDataSourceFixture(
        String configuration, DataSourceFixture dataSourceFixture) throws Exception
    {
        notExists(configuration, dataSourceFixture);
    }

    public void checkConfigurationForXADataSourceConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        Resource resource = resourceFixture.buildResource();
        resource.setParameter("factory", "org.apache.naming.factory.BeanFactory");
        checkConfigurationMatchesResource(configuration, resource);
    }

    public void checkConfigurationForMailSessionConfiguredResourceMatchesResourceFixture(
        String configuration, ResourceFixture resourceFixture) throws Exception
    {
        Resource resource = resourceFixture.buildResource();
        resource.setParameter("factory", "org.apache.naming.factory.MailSessionFactory");
        checkConfigurationMatchesResource(configuration, resource);
    }

    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        return "<Server><Engine><DefaultContext>" + dataSourceEntry
            + "</DefaultContext></Engine></Server>";
    }

}
