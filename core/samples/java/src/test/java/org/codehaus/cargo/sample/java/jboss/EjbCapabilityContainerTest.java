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

import java.lang.reflect.Method;

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.validator.HasDeployableSupportValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.testdata.ejb.Sample;
import org.codehaus.cargo.sample.testdata.ejb.SampleHome;

/**
 * Test for JBoss EJB support.
 */
public class EjbCapabilityContainerTest extends AbstractJBossCapabilityTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public EjbCapabilityContainerTest()
    {
        super();
        this.addValidator(new HasDeployableSupportValidator(DeployableType.EJB));

        // We don't include any WildFly containers as these don't support remote EJB lookup
        this.addValidator(new StartsWithContainerValidator("jboss"));
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

        // We exclude JBoss 7.x, JBoss 7.1.x, JBoss 7.2.x, JBoss 7.3.x, JBoss 7.4.x and JBoss 7.5.x
        // as these don't support remote EJB lookup
        return this.isNotContained(containerId,
            "jboss7x", "jboss71x", "jboss72x", "jboss73x", "jboss74x", "jboss75x");
    }

    /**
     * Test static EJB deployment.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testDeployEjbStatically() throws Exception
    {
        Deployable ejb = this.createDeployableFromTestdataFile("simple-ejb", DeployableType.EJB);

        getLocalContainer().getConfiguration().addDeployable(ejb);

        getLocalContainer().start();

        SampleHome home = jndiLookup("SampleEJB");
        Sample sample = home.create();
        Assertions.assertTrue(sample.isWorking(), "Sample EJB not working");

        getLocalContainer().stop();
    }
}
