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

import java.util.Set;
import java.util.TreeSet;
import junit.framework.Test;

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
import org.codehaus.cargo.sample.testdata.ejb.Sample;
import org.codehaus.cargo.sample.testdata.ejb.SampleHome;

/**
 * Test for JBoss EJB support.
 * 
 * @version $Id$
 */
public class EjbCapabilityContainerTest extends AbstractJBossCapabilityTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public EjbCapabilityContainerTest(String testName, EnvironmentTestData testData)
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
        // We exclude jboss7x as it doesn't support remote EJB lookup
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("jboss7x");

        CargoTestSuite suite = new CargoTestSuite(
            "Tests that can run on containers supporting EJB deployments");
        suite.addTestSuite(EjbCapabilityContainerTest.class, new Validator[] {
            new StartsWithContainerValidator("jboss"),
            new HasDeployableSupportValidator(DeployableType.EJB),
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator()}, excludedContainerIds);
        return suite;
    }

    /**
     * Test static EJB deployment.
     * @throws Exception If anything goes wrong.
     */
    public void testDeployEjbStatically() throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        Deployable ejb = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-ejb"), DeployableType.EJB);

        getLocalContainer().getConfiguration().addDeployable(ejb);

        getLocalContainer().start();

        SampleHome home = jndiLookup("SampleEJB");
        Sample sample = home.create();
        assertTrue("Should have returned true", sample.isWorking());

        getLocalContainer().stop();
    }
}
