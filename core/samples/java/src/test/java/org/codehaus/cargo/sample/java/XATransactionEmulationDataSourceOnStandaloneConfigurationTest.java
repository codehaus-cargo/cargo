/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

        // We exclude Geronimo, JBoss, WildFly 10.x and WilFly 14.x onwards and GlassFish 7.x and
        // 8.x as these doesn't support XA transaction emulation the way Codehaus Cargo tests it
        // (using an old version of Spring)
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("geronimo2x");
        excludedContainerIds.add("geronimo3x");
        excludedContainerIds.add("glassfish7x");
        excludedContainerIds.add("glassfish8x");
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
        excludedContainerIds.add("jboss74x");
        excludedContainerIds.add("jboss75x");
        excludedContainerIds.add("wildfly10x");
        excludedContainerIds.add("wildfly14x");
        excludedContainerIds.add("wildfly15x");
        excludedContainerIds.add("wildfly16x");
        excludedContainerIds.add("wildfly17x");
        excludedContainerIds.add("wildfly18x");
        excludedContainerIds.add("wildfly19x");
        excludedContainerIds.add("wildfly20x");
        excludedContainerIds.add("wildfly21x");
        excludedContainerIds.add("wildfly22x");
        excludedContainerIds.add("wildfly23x");
        excludedContainerIds.add("wildfly24x");
        excludedContainerIds.add("wildfly25x");
        excludedContainerIds.add("wildfly26x");
        excludedContainerIds.add("wildfly27x");
        excludedContainerIds.add("wildfly28x");
        excludedContainerIds.add("wildfly29x");
        excludedContainerIds.add("wildfly30x");
        excludedContainerIds.add("wildfly31x");

        // Jakarta EE versions of Payara do not support XA transaction emulation
        // the way Codehaus Cargo tests it
        if (EnvironmentTestData.jakartaEeContainers.contains("payara"))
        {
            excludedContainerIds.add("payara");
        }

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
     * User configures java.sql.Driver -&gt; container provides javax.sql.DataSource with emulated
     * XA transaction support
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
