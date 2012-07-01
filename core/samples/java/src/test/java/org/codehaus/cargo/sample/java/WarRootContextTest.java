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
package org.codehaus.cargo.sample.java;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for WAR support: deployment to the root context.
 * 
 * @version $Id$
 */
public class WarRootContextTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public WarRootContextTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
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
     * Creates the test suite, using the {@link Validator}s.
     * @return Test suite.
     * @throws Exception If anything goes wrong.
     */
    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that run on local containers supporting WAR deployments");

        // We exclude containers that cannot deploy on the root context
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("geronimo1x");
        excludedContainerIds.add("geronimo2x");
        excludedContainerIds.add("glassfish2x");
        excludedContainerIds.add("jo1x");
        excludedContainerIds.add("jonas4x");
        excludedContainerIds.add("jonas5x");

        suite.addTestSuite(WarRootContextTest.class,
            new Validator[] {
                new IsLocalContainerValidator(),
                new HasStandaloneConfigurationValidator(),
                new HasWarSupportValidator()},
            excludedContainerIds);
        return suite;
    }

    /**
     * Test deployment of a WAR with root path.
     * @throws Exception If anything goes wrong.
     */
    public void testDeployWarDefinedWithRootPath() throws Exception
    {
        WAR war = (WAR) new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-war"), DeployableType.WAR);
        war.setContext("/");

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", "Sample page for testing",
            warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }

}
