/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Contains XML logic used to validate the XML output of a Tomcat 5.x, 6.x and 7.x DataSource
 * configuration.
 * 
 */
public class Tomcat5x6x7xConfigurationChecker extends Tomcat4xConfigurationChecker
{
    /**
     * {@inheritDoc}
     * @return Datasource factory class:
     * <code>org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory</code>
     */
    @Override
    protected String getDataSourceFactory()
    {
        return "org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory";
    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param dataSourceFixture Datasource.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void notExists(String configuration, DataSourceFixture dataSourceFixture)
        throws Exception
    {
        String pathToResource =
            "//Context/Resource[@name='" + dataSourceFixture.jndiLocation + "']";

        XMLAssert.assertXpathNotExists(pathToResource, configuration);

    }

    /**
     * {@inheritDoc}
     * @param configuration Configuration name.
     * @param resource Resource.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void checkConfigurationMatchesResource(String configuration, Resource resource)
        throws Exception
    {

        String pathToResource = "//Context/Resource[@name='" + resource.getName() + "']";

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

            XMLAssert.assertXpathEvaluatesTo(resource.getParameter(propertyName), pathToResource
                + "/@" + propertyNameInTomcatXML, configuration);
        }
    }

    /**
     * {@inheritDoc}
     * @param dataSourceEntry Datasource entry.
     * @return Context with inserted configuration entry.
     */
    @Override
    public String insertConfigurationEntryIntoContext(String dataSourceEntry)
    {
        return "<Context>" + dataSourceEntry + "</Context>";
    }

    /**
     * Check transaction manager token in XML configuration.
     * @param xml XML configuration.
     * @throws Exception If anything goes wrong.
     */
    public void checkTransactionManagerToken(String xml) throws Exception
    {
        XMLAssert
            .assertXpathEvaluatesTo(
                "60",
                "//Context/Transaction[@factory='org.objectweb.jotm.UserTransactionFactory']"
                    + "/@jotm.timeout",
                xml);
    }
}
