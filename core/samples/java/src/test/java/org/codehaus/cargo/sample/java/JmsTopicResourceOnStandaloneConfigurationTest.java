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
 * Test for JMS topic resource capabilities.
 */
public class JmsTopicResourceOnStandaloneConfigurationTest extends
    AbstractResourceOnStandaloneConfigurationTest
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public JmsTopicResourceOnStandaloneConfigurationTest(String testName,
            EnvironmentTestData testData)
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

        // JBoss 7.5.x, Resin, Tomcat, WebLogic 12.1.x, 12.2.x and 14.x as well as
        // WildFly 10.x and WildFly 20.x onwards containers cannot deploy JMS topic resources
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("jboss75x");
        excludedContainerIds.add("resin3x");
        excludedContainerIds.add("resin31x");
        excludedContainerIds.add("resin4x");
        excludedContainerIds.add("tomcat4x");
        excludedContainerIds.add("tomcat5x");
        excludedContainerIds.add("tomcat6x");
        excludedContainerIds.add("tomcat7x");
        excludedContainerIds.add("tomcat8x");
        excludedContainerIds.add("tomcat9x");
        excludedContainerIds.add("tomcat10x");
        excludedContainerIds.add("tomcat11x");
        excludedContainerIds.add("weblogic121x");
        excludedContainerIds.add("weblogic122x");
        excludedContainerIds.add("weblogic14x");
        excludedContainerIds.add("wildfly10x");
        excludedContainerIds.add("wildfly20x");
        excludedContainerIds.add("wildfly21x");
        excludedContainerIds.add("wildfly22x");
        excludedContainerIds.add("wildfly23x");
        excludedContainerIds.add("wildfly24x");
        excludedContainerIds.add("wildfly25x");
        excludedContainerIds.add("wildfly26x");
        excludedContainerIds.add("wildfly27x");
        excludedContainerIds.add("wildfly28x");

        suite.addTestSuite(JmsTopicResourceOnStandaloneConfigurationTest.class,
            new Validator[] {
                new IsInstalledLocalContainerValidator(),
                new HasStandaloneConfigurationValidator(),
                new HasWarSupportValidator(),
                new HasResourceSupportValidator(ConfigurationType.STANDALONE)},
            excludedContainerIds);
        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // WildFly needs to be run with full profile and configured JMS journal
        if (getTestData().containerId.equals("wildfly9x"))
        {
            getLocalContainer().getConfiguration().setProperty("cargo.jboss.configuration",
                "standalone-full");
            getLocalContainer().getConfiguration().setProperty(
                "cargo.wildfly.script.cli.embedded.journal",
                    "target/test-classes/wildfly/wildfly9/jms-journal.cli");
        }
        else if (getTestData().containerId.startsWith("wildfly1"))
        {
            getLocalContainer().getConfiguration().setProperty("cargo.jboss.configuration",
                "standalone-full");
            getLocalContainer().getConfiguration().setProperty(
                "cargo.wildfly.script.cli.embedded.journal",
                    "target/test-classes/wildfly/wildfly10/jms-journal.cli");
        }
    }

    /**
     * User configures JMS queue
     * @throws MalformedURLException If URL for the test WAR cannot be built.
     */
    public void testUserConfiguresJmsTopicAsResource() throws MalformedURLException
    {
        ResourceFixture fixture = ConfigurationFixtureFactory.createJmsTopicAsResource();

        addResourceToConfigurationViaProperty(fixture);

        testWar("jms-topic");
    }
}
