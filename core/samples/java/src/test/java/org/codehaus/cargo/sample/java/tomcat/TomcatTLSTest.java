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
package org.codehaus.cargo.sample.java.tomcat;

import java.io.File;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.sample.java.AbstractWarTestCase;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.SupportsPropertyValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for Tomcat TLS configuration options.
 */
public class TomcatTLSTest extends AbstractWarTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public TomcatTLSTest(String testName, EnvironmentTestData testData) throws Exception
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
        CargoTestSuite suite = new CargoTestSuite("Tests that can run on installed local Tomcat "
            + "containers supporting TLS configuration.");
        suite.addTestSuite(TomcatTLSTest.class, new Validator[] {
            new StartsWithContainerValidator("tomcat", "tomee"),
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new SupportsPropertyValidator(
                ConfigurationType.STANDALONE, TomcatPropertySet.CONNECTOR_KEY_STORE_FILE)});
        return suite;
    }

    /**
     * Create an package Tomcat container.
     * @throws Exception If anything goes wrong.
     */
    public void testTlsConfigContainer() throws Exception
    {
        File localhostJksFile = new File("target/test-classes/localhost.jks");
        assertTrue(localhostJksFile.isFile());

        LocalConfiguration configuration =
            (LocalConfiguration) createConfiguration(ConfigurationType.STANDALONE);
        setContainer(createContainer(configuration));

        // First, create a configuration using the SSL configuration options,
        // then put a WAR on it, finally start it and test for it to be running.
        configuration.setProperty(GeneralPropertySet.PROTOCOL, "https");
        configuration.setProperty(TomcatPropertySet.CONNECTOR_KEY_STORE_FILE,
            localhostJksFile.getAbsolutePath());
        configuration.setProperty(TomcatPropertySet.CONNECTOR_KEY_STORE_PASSWORD, "password");

        testWar("simple");
    }
}
