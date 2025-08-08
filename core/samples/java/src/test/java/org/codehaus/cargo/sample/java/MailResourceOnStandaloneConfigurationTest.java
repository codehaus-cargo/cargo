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

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.sample.java.validator.HasResourceSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;

/**
 * Test for mail resource capabilities.
 */
public class MailResourceOnStandaloneConfigurationTest extends
    AbstractResourceOnStandaloneConfigurationTest
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public MailResourceOnStandaloneConfigurationTest()
    {
        this.addValidator(new HasResourceSupportValidator(ConfigurationType.STANDALONE));
        this.addValidator(new IsInstalledLocalContainerValidator());
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

        // Jakarta EE versions of WildFly cannot deploy mail sessions as a resource
        if (containerId.startsWith("wildfly")
            && EnvironmentTestData.jakartaEeContainers.contains(containerId))
        {
            return false;
        }

        // GlassFish 3.x, 4.x, 5.x, 6.x, 7.x and 8.x as well as Payara
        // cannot deploy mail sessions as a resource
        return this.isNotContained(containerId,
            "glassfish3x", "glassfish4x", "glassfish5x", "glassfish6x", "glassfish7x",
                "glassfish8x",
            "payara");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Container createContainer(ContainerType type, Configuration configuration)
    {
        return addMailJarsToExtraClasspath(
            (InstalledLocalContainer) super.createContainer(type, configuration));
    }

    /**
     * Add mail JARs to extra classpath.
     * @param container Container.
     * @return Updated container.
     */
    private Container addMailJarsToExtraClasspath(InstalledLocalContainer container)
    {
        String mail = System.getProperty("cargo.testdata.mail-jars");
        if (mail != null)
        {
            mail = container.getFileHandler().append(mail,
                EnvironmentTestData.jakartaEeContainers.contains(container.getId())
                    ? "jakarta" : "javax");
            String[] jars = container.getFileHandler().getChildren(mail);
            for (String jar : jars)
            {
                container.addExtraClasspath(jar);
            }
        }
        return container;
    }

    /**
     * User configures javax.mail.Session -&gt; container provides that same javax.mail.Session
     * @throws MalformedURLException If URL for the test WAR cannot be built.
     */
    @CargoTestCase
    public void testUserConfiguresMailSessionAsResource() throws MalformedURLException
    {
        ResourceFixture fixture = ConfigurationFixtureFactory.createMailSessionAsResource();

        addResourceToConfigurationViaProperty(fixture);

        testWar("mailsession", "Got mail session!");
    }
}
