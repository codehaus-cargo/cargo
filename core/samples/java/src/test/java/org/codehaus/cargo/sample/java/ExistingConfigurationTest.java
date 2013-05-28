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

import java.io.File;
import java.net.URL;
import junit.framework.Test;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.sample.java.validator.HasExistingConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for existing configuration.
 * 
 * @version $Id$
 */
public class ExistingConfigurationTest extends AbstractWarCapabilityContainerTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public ExistingConfigurationTest(String testName, EnvironmentTestData testData)
        throws Exception
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
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that verify that existing configuration work by doing local WAR deployments");

        suite.addTestSuite(ExistingConfigurationTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasExistingConfigurationValidator(),
            new HasWarSupportValidator()});
        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // For this test suite we need to have an existing configuration which means we need
        // a container configuration already set up. Thus we start by creating a standalone
        // configuration which we'll use as the existing configuration for the test.
        LocalContainer tmpContainer = (LocalContainer) createContainer(
            createConfiguration(ConfigurationType.STANDALONE));
        tmpContainer.getConfiguration().configure(tmpContainer);

        setContainer(createContainer(createConfiguration(ConfigurationType.EXISTING)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAndStop(URL warPingURL)
    {
        String testFileName = "cargo-test/test.file";
        File testFile = new File(getLocalContainer().getConfiguration().getHome(), testFileName);
        assertFalse("File " + testFile + " already exists", testFile.exists());

        FileConfig fileConfig = new FileConfig();
        fileConfig.setFile(getTestData().getTestDataFileFor("simple-war"));
        fileConfig.setToFile(testFileName);
        getLocalContainer().getConfiguration().setConfigFileProperty(fileConfig);

        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", warPingURL, getLogger());

        // CARGO-1195: DeployableFiles should be setup for ExistingLocalConfiguration
        assertTrue("File " + testFile + " was not configured", testFile.exists());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }
}
