/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.lang.reflect.Method;
import java.net.MalformedURLException;

import org.junit.jupiter.api.extension.ExtensionContext;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.sample.java.validator.HasResourceSupportValidator;

/**
 * Test for JMS queue resource capabilities.
 */
public class JmsQueueResourceOnStandaloneConfigurationTest extends
    AbstractResourceOnStandaloneConfigurationTest
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public JmsQueueResourceOnStandaloneConfigurationTest()
    {
        this.addValidator(new HasResourceSupportValidator(ConfigurationType.STANDALONE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(String containerId, ContainerType containerType, Method testMethod)
    {
        if (!super.isSupported(containerId, containerType, testMethod))
        {
            return false;
        }

        // Jakarta EE versions of WildFly cannot deploy JMS topic resources
        if (containerId.startsWith("wildfly")
            && EnvironmentTestData.jakartaEeContainers.contains(containerId))
        {
            return false;
        }

        // JBoss 7.5.x, JRun, Resin, Tomcat as well as WildFly 10.x cannot deploy
        // JMS queue resources
        return this.isNotContained(containerId,
            "jboss75x",
            "resin3x", "resin31x", "resin4x",
            "tomcat4x", "tomcat5x", "tomcat6x", "tomcat7x", "tomcat8x", "tomcat9x", "tomcat10x",
                "tomcat11x",
            "wildfly10x");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp(
        CargoTestCase.CargoTestcaseInvocationContext cargoContext, ExtensionContext testContext)
        throws Exception
    {
        super.setUp(cargoContext, testContext);

        // WildFly needs to be run with full profile and configured JMS journal
        if (getTestData().containerId.startsWith("wildfly"))
        {
            getLocalContainer().getConfiguration().setProperty(
                "cargo.jboss.configuration", "standalone-full");

            if (getTestData().containerId.equals("wildfly9x"))
            {
                getLocalContainer().getConfiguration().setProperty(
                    "cargo.wildfly.script.cli.embedded.journal",
                        "target/test-classes/wildfly/wildfly9/jms-journal.cli");
            }
            else
            {
                getLocalContainer().getConfiguration().setProperty(
                    "cargo.wildfly.script.cli.embedded.journal",
                        "target/test-classes/wildfly/wildfly/jms-journal.cli");
            }
        }
    }

    /**
     * User configures JMS queue
     * @throws MalformedURLException If URL for the test WAR cannot be built.
     */
    @CargoTestCase
    public void testUserConfiguresJmsQueueAsResource() throws MalformedURLException
    {
        ResourceFixture fixture = ConfigurationFixtureFactory.createJmsQueueAsResource();

        addResourceToConfigurationViaProperty(fixture);

        testWar("jms-queue");
    }
}
