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
package org.codehaus.cargo.sample.java.jboss;

import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.validator.HasDeployableSupportValidator;

/**
 * Test for JBoss HAR support.
 */
public class HarCapabilityContainerTest extends AbstractJBossCapabilityTestCase
{
    /**
     * MBean name for the deployed HAR.
     */
    private static final String SIMPLE_HAR_OBJECT_NAME = "cargo.testdata:name=simple-har";

    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public HarCapabilityContainerTest()
    {
        this.addValidator(new HasDeployableSupportValidator(DeployableType.HAR));
    }

    /**
     * Test HAR deployment.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testDeployHarStatically() throws Exception
    {
        Deployable har = this.createDeployableFromTestdataFile("simple-har", DeployableType.HAR);

        getLocalContainer().getConfiguration().addDeployable(har);

        getLocalContainer().start();
        Assertions.assertEquals(State.STARTED, getContainer().getState());

        // We're verifying that the HAR is successfully deployed by querying it via jmx
        MBeanServerConnection server = createMBeanServerConnection();
        ObjectName objectName = ObjectName.getInstance(SIMPLE_HAR_OBJECT_NAME);
        // getMBeanInfo will throw exception if not found
        MBeanInfo mbeanInfo = server.getMBeanInfo(objectName);
        getLogger().debug("The HAR MBean found: " + mbeanInfo.getDescription(),
            this.getClass().getName());
        Assertions.assertNotNull(mbeanInfo.getDescription(), "MBean description is null");

        getLocalContainer().stop();
    }
}
