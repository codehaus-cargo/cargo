/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
 * Test for mail resource capabilities.
 */
public class MailResourceOnStandaloneConfigurationTest extends
    AbstractResourceOnStandaloneConfigurationTest
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public MailResourceOnStandaloneConfigurationTest(String testName, EnvironmentTestData testData)
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
        addMailJarsToExtraClasspath(container);
        return container;
    }

    /**
     * Add mail JARs to extra classpath.
     * @param container Container.
     */
    private void addMailJarsToExtraClasspath(InstalledLocalContainer container)
    {
        String mail = System.getProperty("cargo.testdata.mail-jars");
        if (mail != null)
        {
            mail = container.getFileHandler().append(mail,
                EnvironmentTestData.JAKARTA_EE_CONTAINERS.contains(container.getId())
                    ? "jakarta" : "javax");
            String[] jars = container.getFileHandler().getChildren(mail);
            for (String jar : jars)
            {
                container.addExtraClasspath(jar);
            }
        }
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

        // GlassFish 3.x, 4.x, 5.x, 6.x and 7.x as well as Payara containers
        // cannot deploy mail sessions as a resource
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("glassfish3x");
        excludedContainerIds.add("glassfish4x");
        excludedContainerIds.add("glassfish5x");
        excludedContainerIds.add("glassfish6x");
        excludedContainerIds.add("glassfish7x");
        excludedContainerIds.add("payara");

        suite.addTestSuite(MailResourceOnStandaloneConfigurationTest.class,
            new Validator[] {
                new IsInstalledLocalContainerValidator(),
                new HasStandaloneConfigurationValidator(),
                new HasWarSupportValidator(),
                new HasResourceSupportValidator(ConfigurationType.STANDALONE)},
            excludedContainerIds);
        return suite;
    }

    /**
     * User configures javax.mail.Session -&gt; container provides that same javax.mail.Session
     * @throws MalformedURLException If URL for the test WAR cannot be built.
     */
    public void testUserConfiguresMailSessionAsResource() throws MalformedURLException
    {
        ResourceFixture fixture = ConfigurationFixtureFactory.createMailSessionAsResource();

        addResourceToConfigurationViaProperty(fixture);

        testWar("mailsession");
    }
}
