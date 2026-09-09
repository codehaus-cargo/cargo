/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Contains XML logic used to validate the XML output of a Tomcat 5.x, 6.x and 7.x DataSource
 * configuration.
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

        Assertions.assertFalse(xpathEngine.selectNodes(
            pathToResource, toSource(configuration)).iterator().hasNext());
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
                pathToResource + "/@" + propertyNameInTomcatXML, toSource(configuration)));
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
}
