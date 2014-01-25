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
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasXAEmulationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for datasource with XA transaction emulation capabilities.
 * 
 * @version $Id$
 */
public class XATransactionEmulationDataSourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public XATransactionEmulationDataSourceOnStandaloneConfigurationTest(String testName,
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

        // We exclude Geronimo, JBoss and WildFly containers as these don't support
        // XA transaction emulation the way CARGO tests it
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("geronimo2x");
        excludedContainerIds.add("geronimo3x");
        excludedContainerIds.add("jboss3x");
        excludedContainerIds.add("jboss4x");
        excludedContainerIds.add("jboss42x");
        excludedContainerIds.add("jboss5x");
        excludedContainerIds.add("jboss51x");
        excludedContainerIds.add("jboss6x");
        excludedContainerIds.add("jboss61x");
        excludedContainerIds.add("jboss7x");
        excludedContainerIds.add("jboss71x");
        excludedContainerIds.add("jboss72x");
        excludedContainerIds.add("jboss73x");
        excludedContainerIds.add("wildfly8x");

        suite.addTestSuite(XATransactionEmulationDataSourceOnStandaloneConfigurationTest.class,
            new Validator[] {
                new IsInstalledLocalContainerValidator(),
                new HasStandaloneConfigurationValidator(),
                new HasWarSupportValidator(),
                new HasDataSourceSupportValidator(ConfigurationType.STANDALONE),
                new HasXAEmulationValidator(ConfigurationType.STANDALONE)},
            excludedContainerIds);
        return suite;
    }

    /**
     * User configures java.sql.Driver -> container provides javax.sql.DataSource with emulated xa
     * transaction support
     * @throws MalformedURLException If servlet WAR URL cannot be created.
     */
    public void testUserConfiguresDriverAndRequestsDataSourceWithXaTransactionSupport()
        throws MalformedURLException
    {
        DataSourceFixture fixture =
            ConfigurationFixtureFactory.createDriverConfiguredDataSourceWithXaTransactionSupport();

        testServletThatIssuesGetConnectionFrom(fixture, "datasource-cmt-local");
    }
}
