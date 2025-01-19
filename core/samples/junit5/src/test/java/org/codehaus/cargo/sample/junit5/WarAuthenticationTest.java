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
package org.codehaus.cargo.sample.junit5;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;

/**
 * Validates deployment of WAR archives with authentication.
 */
public class WarAuthenticationTest extends AbstractStandaloneLocalContainerTestCase
{
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
        return AbstractCargoTestCase.CONTAINER_CAPABILITY_FACTORY.
            createContainerCapability(containerId).supportsDeployableType(DeployableType.WAR)
            &&
            AbstractCargoTestCase.CONFIGURATION_CAPABILITY_FACTORY.
            createConfigurationCapability(containerId, containerType, ConfigurationType.STANDALONE)
                .supportsProperty(ServletPropertySet.USERS);
    }

    /**
     * Test authenticated WAR.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testExecutionWithAuthenticatedWar() throws Exception
    {
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
