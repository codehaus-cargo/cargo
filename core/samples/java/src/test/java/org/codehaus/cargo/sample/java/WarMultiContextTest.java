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
import java.net.URL;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;

/**
 * Test for WAR support: deployment to a multiple context (with many slashes).
 */
public class WarMultiContextTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public WarMultiContextTest()
    {
        super();
        this.addValidator(new HasWarSupportValidator());
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

        // We exclude containers that cannot deploy on a multiple context (with many slashes)
        return this.isNotContained(containerId,
            "jonas4x", "jonas5x",
            "jrun4x",
            "oc4j9x", "oc4j10x",
            "weblogic8x", "weblogic9x", "weblogic10x", "weblogic103x", "weblogic12x",
                "weblogic121x", "weblogic122x", "weblogic14x");
    }

    /**
     * Test deployment of a WAR with multi path.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testDeployWarDefinedWithMultipleContextPath() throws Exception
    {
        WAR war = (WAR) this.createDeployableFromTestdataFile("simple-war", DeployableType.WAR);
        war.setContext("/a/b");

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/a/b/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", "Sample page for testing",
            warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }

}
