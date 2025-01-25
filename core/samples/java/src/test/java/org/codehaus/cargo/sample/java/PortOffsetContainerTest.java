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
import org.codehaus.cargo.sample.java.validator.HasPortOffsetValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;

/**
 * Test for port offset support.
 */
public class PortOffsetContainerTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Offset.
     */
    private static final String OFFSET = "20";

    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public PortOffsetContainerTest()
    {
        super();
        this.addValidator(new HasWarSupportValidator());
        this.addValidator(new HasPortOffsetValidator(ConfigurationType.STANDALONE));
    }

    /**
     * Start container with port offset.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testStartWithPortOffset() throws Exception
    {
        getLocalContainer().getConfiguration().setProperty(GeneralPropertySet.PORT_OFFSET, OFFSET);

        int offsetValue = Integer.valueOf(OFFSET);
        int portWithOffset = getTestData().port + offsetValue;

        WAR war = (WAR) this.createDeployableFromTestdataFile("simple-war", DeployableType.WAR);
        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + portWithOffset + "/simple-war/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }
}
