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

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ConfigurationFixtureFactory;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.sample.java.validator.HasXAEmulationValidator;

/**
 * Test for datasource with XA transaction emulation capabilities.
 */
public class XATransactionEmulationDataSourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public XATransactionEmulationDataSourceOnStandaloneConfigurationTest()
    {
        this.addValidator(new HasXAEmulationValidator(ConfigurationType.STANDALONE));
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

        // Jakarta EE versions of Payara do not support XA transaction emulation
        // the way Codehaus Cargo tests it
        if (EnvironmentTestData.jakartaEeContainers.contains("payara"))
        {
            return false;
        }

        // We exclude Geronimo, JBoss, WildFly 10.x and WilFly 14.x onwards and GlassFish 7.x and
        // 8.x as these doesn't support XA transaction emulation the way Codehaus Cargo tests it
        // (using an old version of Spring)
        return this.isNotContained(containerId,
            "geronimo2x", "geronimo3x",
            "glassfish7x", "glassfish8x",
            "jboss3x", "jboss4x", "jboss42x", "jboss5x", "jboss51x", "jboss6x", "jboss61x",
                "jboss7x", "jboss71x", "jboss72x", "jboss73x", "jboss74x", "jboss75x",
            "wildfly10x", "wildfly14x", "wildfly15x", "wildfly16x", "wildfly17x", "wildfly18x",
                "wildfly19x", "wildfly20x", "wildfly21x", "wildfly22x", "wildfly23x", "wildfly24x",
                "wildfly25x", "wildfly26x", "wildfly27x", "wildfly28x", "wildfly29x", "wildfly30x",
                "wildfly31x", "wildfly32x", "wildfly33x", "wildfly34x", "wildfly35x");
    }

    /**
     * User configures java.sql.Driver -&gt; container provides javax.sql.DataSource with emulated
     * XA transaction support
     * @throws MalformedURLException If servlet WAR URL cannot be created.
     */
    @CargoTestCase
    public void testUserConfiguresDriverAndRequestsDataSourceWithXaTransactionSupport()
        throws MalformedURLException
    {
        DataSourceFixture fixture =
            ConfigurationFixtureFactory.createDriverConfiguredDataSourceWithXaTransactionSupport();

        testServletThatIssuesGetConnectionFrom(fixture, "datasource-cmt-local");
    }
}
