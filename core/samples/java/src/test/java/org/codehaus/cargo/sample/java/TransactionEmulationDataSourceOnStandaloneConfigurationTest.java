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
 * Test for datasource with transaction emulation capabilities.
 */
public class TransactionEmulationDataSourceOnStandaloneConfigurationTest extends
    AbstractDataSourceWarCapabilityContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public TransactionEmulationDataSourceOnStandaloneConfigurationTest()
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

        // Jakarta EE versions of Payara do not support transaction emulation
        // the way Codehaus Cargo tests it
        if ("payara".equals(containerId)
            && EnvironmentTestData.jakartaEeContainers.contains("payara"))
        {
            return false;
        }

        // We exclude Geronimo 2.x and GlassFish 7.x and 8.x as these don't support
        // transaction emulation the way Codehaus Cargo tests it (using an old version of Spring)
        return this.isNotContained(containerId,
            "geronimo2x",
            "glassfish7x", "glassfish8x");
    }

    /**
     * User configures java.sql.Driver -&gt; container provides javax.sql.DataSource with local
     * transaction support
     * @throws MalformedURLException If servlet WAR URL cannot be created.
     */
    @CargoTestCase
    public void testUserConfiguresDriverAndRequestsDataSourceWithLocalTransactionSupport()
        throws MalformedURLException
    {
        DataSourceFixture fixture =
            ConfigurationFixtureFactory
                .createDriverConfiguredDataSourceWithLocalTransactionSupport();

        testServletThatIssuesGetConnectionFrom(fixture, "datasource-cmt-local");
    }
}
