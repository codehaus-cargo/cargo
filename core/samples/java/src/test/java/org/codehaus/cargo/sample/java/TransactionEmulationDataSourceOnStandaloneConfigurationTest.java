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

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.sample.java.validator.HasDataSourceSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasEarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasXAEmulationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for datasource with transaction emulation capabilities.
 * 
 * @version $Id$
 */
public class TransactionEmulationDataSourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public TransactionEmulationDataSourceOnStandaloneConfigurationTest(String testName,
        EnvironmentTestData testData) throws Exception
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

        // We exclude geronimo2x, jboss7x, jboss71x, jboss72x, jboss73x, jboss74x and wildfly8x
        // as these don't support transaction emulation the way CARGO tests it
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("geronimo2x");
        excludedContainerIds.add("jboss7x");
        excludedContainerIds.add("jboss71x");
        excludedContainerIds.add("jboss72x");
        excludedContainerIds.add("jboss73x");
        excludedContainerIds.add("jboss74x");
        excludedContainerIds.add("wildfly8x");

        suite.addTestSuite(TransactionEmulationDataSourceOnStandaloneConfigurationTest.class,
            new Validator[] {
                new IsInstalledLocalContainerValidator(),
                new HasStandaloneConfigurationValidator(),
                new HasEarSupportValidator(),
                new HasDataSourceSupportValidator(ConfigurationType.STANDALONE),
                new HasXAEmulationValidator(ConfigurationType.STANDALONE)},
            excludedContainerIds);
        return suite;
    }

    /**
     * User configures java.sql.Driver -> container provides javax.sql.DataSource with local
     * transaction support
     * @throws MalformedURLException If servlet WAR URL cannot be created.
     */
    public void testUserConfiguresDriverAndRequestsDataSourceWithLocalTransactionSupport()
        throws MalformedURLException
    {
        DataSourceFixture fixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithLocalTransactionSupport();

        testServletThatIssuesGetConnectionFrom(fixture, "datasource-cmt-local");
    }
}
