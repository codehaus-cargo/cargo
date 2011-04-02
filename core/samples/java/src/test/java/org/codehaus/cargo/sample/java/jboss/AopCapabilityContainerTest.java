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

public class AopCapabilityContainerTest extends AbstractJBossCapabilityTestCase
{
    private static final String JBOSSAOP_ASPECTMANAGER_OBJECT_NAME =
        "jboss.aop:service=AspectManager";

    public AopCapabilityContainerTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite =
            new CargoTestSuite("Test that verifies that deployment of AOP archive work on local "
                + "installed JBoss 5+ containers");

        suite.addTestSuite(AopCapabilityContainerTest.class, new Validator[] {
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

    public void testDeployAopStatically() throws Exception
    {
        Deployable aop =
            new DefaultDeployableFactory().createDeployable(getContainer().getId(), getTestData()
                .getTestDataFileFor("simple-aop"), DeployableType.AOP);

        getLocalContainer().getConfiguration().addDeployable(aop);

        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());

        // We're verifying that the AOP is successfully deployed by querying for the defined
        // pointcut name via jmx
        MBeanServerConnection server = createMBeanServerConnection(null, null);
        ObjectName objectName = ObjectName.getInstance(JBOSSAOP_ASPECTMANAGER_OBJECT_NAME);
        String pointcuts =
            (String) server.invoke(objectName, "pointcuts", new Object[] {}, new String[] {});
        getLogger().debug("Registered aop pointcuts: " + pointcuts.toString(), this.getClass().getName());
        assertTrue("Dummy cargo aop pointcut not found", pointcuts.contains("cargoTestDataSimpleAop"));

        getLocalContainer().stop();
    }
}
