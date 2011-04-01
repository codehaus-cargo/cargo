/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import junit.framework.Test;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.validator.ContainerIdRegExValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

public class HarCapabilityContainerTest extends AbstractJBossCapabilityTestCase
{
    private static final String SIMPLE_HAR_OBJECT_NAME = "cargo.testdata:name=simple-har";

    public HarCapabilityContainerTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite =
            new CargoTestSuite("Test that verifies that deployment of HAR archive work on local "
                + "installed JBoss 5+ containers");

        suite.addTestSuite(HarCapabilityContainerTest.class, new Validator[] {
        new ContainerIdRegExValidator("^jboss[5-9].*"), // the verification of the deployment via
                                                        // jmx only works with JBoss 5+
        new IsInstalledLocalContainerValidator(), new HasStandaloneConfigurationValidator()});

        return suite;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    public void testDeployHarStatically() throws Exception
    {
        Deployable har =
            new DefaultDeployableFactory().createDeployable(getContainer().getId(), getTestData()
                .getTestDataFileFor("simple-har"), DeployableType.HAR);

        getLocalContainer().getConfiguration().addDeployable(har);

        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());

        // We're verifying that the HAR is successfully deployed by querying it via jmx
        MBeanServerConnection server = createMBeanServerConnection(null, null);
        ObjectName objectName = ObjectName.getInstance(SIMPLE_HAR_OBJECT_NAME);
        MBeanInfo mbeanInfo = server.getMBeanInfo(objectName); // will throw exception if not found
        getLogger().debug("The HAR MBean found: " + mbeanInfo.getDescription(),
            this.getClass().getName());
        assertNotNull("MBean description is null", mbeanInfo.getDescription());

        getLocalContainer().stop();
    }
}
