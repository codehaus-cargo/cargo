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

import java.net.URL;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;

/**
 * Test for system property support.
 */
public class SystemPropertyCapabilityTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public SystemPropertyCapabilityTest()
    {
        super();
        this.addValidator(new HasWarSupportValidator());
        this.addValidator(new IsInstalledLocalContainerValidator());
    }

    /**
     * Test whether setting system properties is working properly.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testSystemProperty() throws Exception
    {
        String random = UUID.randomUUID().toString();

        WAR war = (WAR) this.createDeployableFromTestdataFile(
            "systemproperty-war", DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL pingURL = new URL("http://localhost:" + getTestData().port
            + "/systemproperty-war/test?systemPropertyName=random");

        getInstalledLocalContainer().getSystemProperties().put("random", random);
        getLocalContainer().start();
        Assertions.assertEquals(State.STARTED, getContainer().getState());
        PingUtils.assertPingTrue(pingURL.getPath() + " not started", random, pingURL, getLogger());

        getLocalContainer().stop();
        Assertions.assertEquals(State.STOPPED, getContainer().getState());
        PingUtils.assertPingFalse(pingURL.getPath() + " not stopped", pingURL, getLogger());
    }

}
