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
package org.codehaus.cargo.sample.java;

import java.net.MalformedURLException;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.sample.java.validator.HasDataSourceSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for datasource capabilities.
 * 
 */
public class DataSourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public DataSourceOnStandaloneConfigurationTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    /**
     * Creates the test suite, using the {@link Validator}s.
     * @return Test suite.
     * @throws Exception If anything goes wrong.
     */
    public static Test suite() throws Exception
    {
        CargoTestSuite suite =
            new CargoTestSuite(
                "Tests that run on local containers supporting DataSource and WAR deployments");

        // We exclude geronimo2x as it doesn't support datasource setup the way CARGO tests it
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("geronimo2x");

        suite.addTestSuite(DataSourceOnStandaloneConfigurationTest.class, new Validator[] {
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasWarSupportValidator(),
            new HasDataSourceSupportValidator(ConfigurationType.STANDALONE)},
            excludedContainerIds);
        return suite;
    }

    /**
     * User configures java.sql.Driver -&gt; container provides javax.sql.DataSource with no
     * transaction support
     * @throws MalformedURLException If servlet WAR URL cannot be created.
     */
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
