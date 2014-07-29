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
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.ResourcePropertySet;
import org.codehaus.cargo.sample.java.validator.HasResourceSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for XA datasource deployed as resource resource capabilities.
 * 
 * @version $Id$
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

        // glassfish3x and glassfish4x cannot deploy XA datasources as a resource
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("glassfish3x");
        excludedContainerIds.add("glassfish4x");

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
