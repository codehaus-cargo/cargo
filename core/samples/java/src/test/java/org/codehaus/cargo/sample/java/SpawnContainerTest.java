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

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.sample.java.validator.HasSpawnSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;

/**
 * Test for container spawn support.
 */
public class SpawnContainerTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public SpawnContainerTest()
    {
        this.addValidator(new HasWarSupportValidator());
        this.addValidator(new HasSpawnSupportValidator(ConfigurationType.STANDALONE));
    }

    /**
     * Start spawned container.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testStartSpawned() throws Exception
    {
        getLocalContainer().getConfiguration().setProperty(
            GeneralPropertySet.SPAWN_PROCESS, "true");

        // Disable container output to activate spawn.
        getLocalContainer().setOutput(null);

        WAR war = (WAR) this.createDeployableFromTestdataFile("simple-war", DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL pingURL = new URL("http://localhost:" + getTestData().port + "/simple-war/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue(pingURL.getPath() + " not started", pingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(pingURL.getPath() + " not stopped", pingURL, getLogger());
    }
}
