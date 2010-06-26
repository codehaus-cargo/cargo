/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasDataSourceSupportValidator;

public class DataSourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    public DataSourceOnStandaloneConfigurationTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite =
            new CargoTestSuite("Tests that run on local containers supporting DataSource and WAR deployments");

        suite.addTestSuite(DataSourceOnStandaloneConfigurationTest.class, new Validator[] {
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasWarSupportValidator(),
            new HasDataSourceSupportValidator(ConfigurationType.STANDALONE)});
        return suite;
    }

    /**
     * User configures java.sql.Driver -> container provides javax.sql.DataSource with no
     * transaction support
     */
    public void testUserConfiguresDriverAndRequestsDataSource() throws MalformedURLException
    {
        DataSourceFixture fixture = ConfigurationFixtureFactory.createDataSource();
        _testServletThatIssuesGetConnectionFrom(fixture, "datasource");
    }

    public void testMultipleDataSources() throws MalformedURLException
    {
        DataSourceFixture fixture = ConfigurationFixtureFactory.createDataSource();

        Configuration config = getLocalContainer().getConfiguration();
        config.setProperty(DatasourcePropertySet.DATASOURCE + ".1", fixture
            .buildDataSourcePropertyString());
        fixture = ConfigurationFixtureFactory.createAnotherDataSource();
        _testServletThatIssuesGetConnectionFrom(fixture, "two-datasources");
    }
}
