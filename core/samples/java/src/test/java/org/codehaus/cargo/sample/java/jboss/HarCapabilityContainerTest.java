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
import org.codehaus.cargo.sample.java.validator.HasDeployableSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for JBoss HAR support.
 * 
 */
public class HarCapabilityContainerTest extends AbstractJBossCapabilityTestCase
{
    /**
     * MBean name for the deployed HAR.
     */
    private static final String SIMPLE_HAR_OBJECT_NAME = "cargo.testdata:name=simple-har";

    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public HarCapabilityContainerTest(String testName, EnvironmentTestData testData)
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
        CargoTestSuite suite =
            new CargoTestSuite("Test that verifies that deployment of HAR archive work on local "
                + "installed JBoss containers");

        suite.addTestSuite(HarCapabilityContainerTest.class, new Validator[] {
            new StartsWithContainerValidator("jboss"),
            new HasDeployableSupportValidator(DeployableType.HAR),
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator()
        });

        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    /**
     * Test HAR deployment.
     * @throws Exception If anything goes wrong.
     */
    public void testDeployHarStatically() throws Exception
    {
        Deployable har =
            new DefaultDeployableFactory().createDeployable(getContainer().getId(), getTestData()
                .getTestDataFileFor("simple-har"), DeployableType.HAR);

        getLocalContainer().getConfiguration().addDeployable(har);

        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());

        // We're verifying that the HAR is successfully deployed by querying it via jmx
        MBeanServerConnection server = createMBeanServerConnection();
        ObjectName objectName = ObjectName.getInstance(SIMPLE_HAR_OBJECT_NAME);
        // getMBeanInfo will throw exception if not found
        MBeanInfo mbeanInfo = server.getMBeanInfo(objectName);
        getLogger().debug("The HAR MBean found: " + mbeanInfo.getDescription(),
            this.getClass().getName());
        assertNotNull("MBean description is null", mbeanInfo.getDescription());

        getLocalContainer().stop();
    }
}
