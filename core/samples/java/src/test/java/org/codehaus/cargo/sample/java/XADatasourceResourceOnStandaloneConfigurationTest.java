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

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.sample.java.validator.HasResourceSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for XA datasource deployed as resource resource capabilities.
 */
public class XADatasourceResourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public XADatasourceResourceOnStandaloneConfigurationTest(String testName,
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
                "Tests that run on local containers supporting Resource and WAR deployments");

        // JBoss 7.5.x, GlassFish 3.x, GlassFish 4.x, GlassFish 5.x, GlassFish 6.x, GlassFish 7.x,
        // Payara, the WebLogic WSLT deployer and WildFly cannot deploy XA datasources as resource
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("jboss75x");
        excludedContainerIds.add("glassfish3x");
        excludedContainerIds.add("glassfish4x");
        excludedContainerIds.add("glassfish5x");
        excludedContainerIds.add("glassfish6x");
        excludedContainerIds.add("glassfish7x");
        excludedContainerIds.add("payara");
        excludedContainerIds.add("weblogic121x");
        excludedContainerIds.add("weblogic122x");
        excludedContainerIds.add("weblogic14x");
        excludedContainerIds.add("wildfly8x");
        excludedContainerIds.add("wildfly9x");
        excludedContainerIds.add("wildfly10x");
        excludedContainerIds.add("wildfly11x");
        excludedContainerIds.add("wildfly12x");
        excludedContainerIds.add("wildfly13x");
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

        suite.addTestSuite(XADatasourceResourceOnStandaloneConfigurationTest.class,
            new Validator[] {
                new IsInstalledLocalContainerValidator(),
                new HasStandaloneConfigurationValidator(),
                new HasWarSupportValidator(),
                new HasResourceSupportValidator(ConfigurationType.STANDALONE)},
            excludedContainerIds);
        return suite;
    }

    /**
     * User configures javax.sql.XADataSource -&gt; container provides that same
     * javax.sql.XADataSource
     * @throws MalformedURLException If URL for the test WAR cannot be built.
     */
    public void testUserConfiguresXADataSourceAsResource() throws MalformedURLException
    {
        ResourceFixture fixture = ConfigurationFixtureFactory.createXADataSourceAsResource();

        addResourceToConfigurationViaProperty(fixture);

        testWar("xadatasource");
    }

    /**
     * Add resource to configuration using properties.
     * @param fixture Container.
     */
    protected void addResourceToConfigurationViaProperty(ResourceFixture fixture)
    {
        Configuration config = getLocalContainer().getConfiguration();
        config.setProperty(ResourcePropertySet.RESOURCE, fixture.buildResourcePropertyString());
    }
}
