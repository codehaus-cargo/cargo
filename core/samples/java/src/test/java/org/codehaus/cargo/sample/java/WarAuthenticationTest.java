/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import java.util.Map;
import java.util.HashMap;
import java.net.URL;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasAuthenticationSupportValidator;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.apache.commons.codec.binary.Base64;

public class WarAuthenticationTest extends AbstractCargoTestCase
{
    public WarAuthenticationTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

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

    public void testExecutionWithAuthenticatedWar() throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        // TODO: Find a better way to exclude a test if a configuration doesn't support a property.
        // The way it is implemented here is not ideal as this test will be shown as executed in the
        // JUnit report whereas it won't be for container who do not support the
        // ServletPropertySet.USERS property.
        if (getLocalContainer().getConfiguration().getCapability().supportsProperty(
            ServletPropertySet.USERS))
        {
            Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
                getTestData().getTestDataFileFor("authentication-war"), DeployableType.WAR);

            getLocalContainer().getConfiguration().addDeployable(war);

            // Add authentication data
            getLocalContainer().getConfiguration().setProperty(ServletPropertySet.USERS,
                "someone:password:cargo");

            URL warPingURL = new URL("http://localhost:" + getTestData().port
                + "/authentication-war-" + getTestData().version + "/test");

            getLocalContainer().start();

            Map requestProperties = new HashMap();
            requestProperties.put("Authorization", "Basic "
                + new String(Base64.encodeBase64("someone:password".getBytes())));

            PingUtils.assertPingTrue("Failed authentication", "Principal name [someone], "
                + "Is user in \"cargo\" role [true]", warPingURL, requestProperties, getLogger());

            getLocalContainer().stop();
            PingUtils.assertPingFalse("Authentication war not stopped", warPingURL, getLogger());
        }
    }
}
