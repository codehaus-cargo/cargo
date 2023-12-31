/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import java.net.URL;

import junit.framework.Test;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasSpawnSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for container spawn support.
 */
public class SpawnContainerTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public SpawnContainerTest(String testName, EnvironmentTestData testData)
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
            "Tests that run on containers supporting spawned process.");

        suite.addTestSuite(SpawnContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasSpawnSupportValidator(ConfigurationType.STANDALONE),
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

        Configuration configuration = createConfiguration(ConfigurationType.STANDALONE);
        configuration.setProperty(GeneralPropertySet.SPAWN_PROCESS, "true");

        Container container = createContainer(configuration);
        setContainer(container);

        // Disable container output to activate spawn.
        getLocalContainer().setOutput(null);
    }

    /**
     * Start spawned container.
     * @throws Exception If anything goes wrong.
     */
    public void testStartSpawned() throws Exception
    {
        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
                getTestData().getTestDataFileFor("simple-war"), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL pingURL = new URL("http://localhost:" + getTestData().port + "/simple-war/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue(pingURL.getPath() + " not started", pingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(pingURL.getPath() + " not stopped", pingURL, getLogger());
    }
}
