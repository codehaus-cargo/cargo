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

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.sample.java.validator.HasEarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasXAEmulationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasDataSourceSupportValidator;

public class TransactionEmulationDataSourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    public TransactionEmulationDataSourceOnStandaloneConfigurationTest(String testName,
        EnvironmentTestData testData) throws Exception
    {
        super(testName, testData);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite =
            new CargoTestSuite("Tests that run on local containers supporting DataSource and WAR deployments");

        suite.addTestSuite(TransactionEmulationDataSourceOnStandaloneConfigurationTest.class,
            new Validator[] {
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasEarSupportValidator(),
            new HasDataSourceSupportValidator(ConfigurationType.STANDALONE),
            new HasXAEmulationValidator(ConfigurationType.STANDALONE)});
        return suite;
    }

    /**
     * User configures java.sql.Driver -> container provides javax.sql.DataSource with local
     * transaction support
     */
    public void testUserConfiguresDriverAndRequestsDataSourceWithLocalTransactionSupport()
        throws MalformedURLException
    {
        DataSourceFixture fixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithLocalTransactionSupport();

        _testServletThatIssuesGetConnectionFrom(fixture, "datasource-cmt-local");
    }

    /**
     * User configures java.sql.Driver -> container provides javax.sql.DataSource with emulated xa
     * transaction support
     */
    public void testUserConfiguresDriverAndRequestsDataSourceWithXaTransactionSupport()
        throws MalformedURLException
    {
        DataSourceFixture fixture =
            ConfigurationFixtureFactory.createDriverConfiguredDataSourceWithXaTransactionSupport();

        _testServletThatIssuesGetConnectionFrom(fixture, "datasource-cmt-local");
    }
}
