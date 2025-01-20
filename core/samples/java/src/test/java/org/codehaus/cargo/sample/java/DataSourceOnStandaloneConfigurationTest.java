/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.sample.java;

import java.lang.reflect.Method;
import java.net.MalformedURLException;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.sample.java.validator.HasDataSourceSupportValidator;

/**
 * Test for datasource capabilities.
 */
public class DataSourceOnStandaloneConfigurationTest
    extends AbstractDataSourceWarCapabilityContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public DataSourceOnStandaloneConfigurationTest()
    {
        this.addValidator(new HasDataSourceSupportValidator(ConfigurationType.STANDALONE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(String containerId, ContainerType containerType, Method testMethod)
    {
        if (!super.isSupported(containerId, containerType, testMethod))
        {
            return false;
        }
        // We exclude geronimo2x and liberty as they don't support datasource setup the way
        // Codehaus Cargo tests it
        return this.isNotContained(containerId, "geronimo2x", "liberty");
    }

    /**
     * User configures java.sql.Driver -&gt; container provides javax.sql.DataSource with no
     * transaction support
     * @throws MalformedURLException If servlet WAR URL cannot be created.
     */
    @CargoTestCase
    public void testUserConfiguresDriverAndRequestsDataSource() throws MalformedURLException
    {
        DataSourceFixture fixture = ConfigurationFixtureFactory.createDataSource();
        if ("glassfish4x".equals(getContainer().getId()))
        {
            fixture.jndiLocation = "jdbc/__default";
        }
        testServletThatIssuesGetConnectionFrom(fixture, "datasource");
    }

    /**
     * Test multiple datasources.
     * @throws MalformedURLException If servlet WAR URL cannot be created.
     */
    @CargoTestCase
    public void testMultipleDataSources() throws MalformedURLException
    {
        DataSourceFixture fixture = ConfigurationFixtureFactory.createDataSource();
        if ("glassfish4x".equals(getContainer().getId()))
        {
            fixture.jndiLocation = "jdbc/__default";
        }

        Configuration config = getLocalContainer().getConfiguration();
        config.setProperty(DatasourcePropertySet.DATASOURCE + ".1", fixture
            .buildDataSourcePropertyString());
        fixture = ConfigurationFixtureFactory.createAnotherDataSource();
        testServletThatIssuesGetConnectionFrom(fixture, "two-datasources");
    }
}
