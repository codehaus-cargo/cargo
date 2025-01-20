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

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.sample.java.validator.HasEarSupportValidator;

/**
 * Test for EAR support.
 */
public class EarCapabilityContainerTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public EarCapabilityContainerTest()
    {
        this.addValidator(new HasEarSupportValidator());
    }

    /**
     * Start container with an empty EAR.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testStartWithOneEmptyEarDeployed() throws Exception
    {
        // The Apache Geronimo server doesn't like empty EARs
        if (getTestData().containerId.startsWith("geronimo"))
        {
            return;
        }

        Deployable ear = this.createDeployableFromTestdataFile("simple-ear", DeployableType.EAR);

        getLocalContainer().getConfiguration().addDeployable(ear);

        getLocalContainer().start();
        Assertions.assertEquals(State.STARTED, getContainer().getState());

        getLocalContainer().stop();
        Assertions.assertEquals(State.STOPPED, getContainer().getState());
    }

    /**
     * Start container with an EAR containing one WAR.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testStartWithOneEarWithOneWarDeployed() throws Exception
    {
        Deployable ear = this.createDeployableFromTestdataFile("simple-ear", DeployableType.EAR);

        getLocalContainer().getConfiguration().addDeployable(ear);

        URL earPingURL =
            new URL("http://localhost:" + getTestData().port + "/simpleweb/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue("simple ear not started", earPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse("simple ear not stopped", earPingURL, getLogger());
    }
}
