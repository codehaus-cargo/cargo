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

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.sample.java.validator.HasResourceSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for JMS resource capabilities.
 * 
 */
public class JmsResourceOnStandaloneConfigurationTest extends
    AbstractResourceOnStandaloneConfigurationTest
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public JmsResourceOnStandaloneConfigurationTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Container createContainer(ContainerType type, Configuration configuration)
    {
        InstalledLocalContainer container =
            (InstalledLocalContainer) super.createContainer(type, configuration);
        return container;
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

        // JRun, Resin, Tomcat and TomEE containers cannot deploy JMS resources
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("jrun4x");
        excludedContainerIds.add("resin2x");
        excludedContainerIds.add("resin3x");
        excludedContainerIds.add("resin31x");
        excludedContainerIds.add("resin4x");
        excludedContainerIds.add("tomcat4x");
        excludedContainerIds.add("tomcat5x");
        excludedContainerIds.add("tomcat6x");
        excludedContainerIds.add("tomcat7x");
        excludedContainerIds.add("tomcat8x");
        excludedContainerIds.add("tomee1x");

        suite.addTestSuite(JmsResourceOnStandaloneConfigurationTest.class,
            new Validator[] {
                new IsInstalledLocalContainerValidator(),
                new HasStandaloneConfigurationValidator(),
                new HasWarSupportValidator(),
                new HasResourceSupportValidator(ConfigurationType.STANDALONE)},
            excludedContainerIds);
        return suite;
    }

    /**
     * User configures JMS queue
     * @throws MalformedURLException If URL for the test WAR cannot be built.
     */
    public void testUserConfiguresJmsQueueAsResource() throws MalformedURLException
    {
        ResourceFixture fixture = ConfigurationFixtureFactory.createJmsQueueAsResource();

        addResourceToConfigurationViaProperty(fixture);

        testWar("jms");
    }
}
