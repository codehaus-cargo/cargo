/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.validator.HasDeployableSupportValidator;

/**
 * Test for JBoss AOP support.
 */
public class AopCapabilityContainerTest extends AbstractJBossCapabilityTestCase
{
    /**
     * MBean name for the JBoss AOP aspect manager.
     */
    private static final String JBOSSAOP_ASPECTMANAGER_OBJECT_NAME =
        "jboss.aop:service=AspectManager";

    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public AopCapabilityContainerTest()
    {
        this.addValidator(new HasDeployableSupportValidator(DeployableType.AOP));
    }

    /**
     * Test static AOP deployment.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testDeployAopStatically() throws Exception
    {
        Deployable aop = this.createDeployableFromTestdataFile("simple-aop", DeployableType.AOP);

        getLocalContainer().getConfiguration().addDeployable(aop);

        getLocalContainer().start();
        Assertions.assertEquals(State.STARTED, getContainer().getState());

        // We're verifying that the AOP is successfully deployed by querying for the defined
        // pointcut name via jmx
        MBeanServerConnection server = createMBeanServerConnection();
        ObjectName objectName = ObjectName.getInstance(JBOSSAOP_ASPECTMANAGER_OBJECT_NAME);
        String pointcuts =
            (String) server.invoke(objectName, "pointcuts", new Object[] {}, new String[] {});
        getLogger().debug("Registered aop pointcuts: " + pointcuts,
            this.getClass().getName());
        Assertions.assertTrue(
            pointcuts.contains("cargoTestDataSimpleAop"), "Dummy cargo aop pointcut not found");

        getLocalContainer().stop();
    }
}
