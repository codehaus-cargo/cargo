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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasAuthenticationSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Validates WAR archives with authentication.
 */
public class WarAuthenticationTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public WarAuthenticationTest(String testName, EnvironmentTestData testData)
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
        CargoTestSuite suite = new CargoTestSuite("Tests that run on local containers supporting "
            + "WAR deployments and which support authentication");

        suite.addTestSuite(WarAuthenticationTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasWarSupportValidator(),
            new HasAuthenticationSupportValidator(ConfigurationType.STANDALONE)});
        return suite;
    }

    /**
     * Test authenticated WAR.
     * @throws Exception If anything goes wrong.
     */
    public void testExecutionWithAuthenticatedWar() throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("authentication-war"), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        // Add authentication data
        List<User> users = User.parseUsers("someone:p@ssw0rd:cargo");
        getLocalContainer().getConfiguration().getUsers().addAll(users);

        URL warPingURL =
            new URL("http://localhost:" + getTestData().port + "/authentication-war/test");

        getLocalContainer().start();

        Map<String, String> requestProperties = new HashMap<String, String>();
        requestProperties.put("Authorization", "Basic "
            + Base64.getEncoder().encodeToString(
                "someone:p@ssw0rd".getBytes(StandardCharsets.UTF_8)));

        PingUtils.assertPingTrue("Failed authentication", "Principal name [someone], "
            + "Is user in \"cargo\" role [true]", warPingURL, requestProperties, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse("Authentication war not stopped", warPingURL, getLogger());
    }
}
