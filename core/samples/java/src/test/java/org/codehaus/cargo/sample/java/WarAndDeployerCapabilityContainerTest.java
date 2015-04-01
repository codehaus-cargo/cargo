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

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasLocalDeployerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Validates local hot deployment of WAR archives.
 * 
 */
public class WarAndDeployerCapabilityContainerTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public WarAndDeployerCapabilityContainerTest(String testName, EnvironmentTestData testData)
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
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that run on local containers supporting WAR deployments using a local Deployer");

        suite.addTestSuite(WarAndDeployerCapabilityContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasWarSupportValidator(),
            new HasLocalDeployerValidator()});
        return suite;
    }

    /**
     * Test WAR hot deployment.
     * @throws Exception If anything goes wrong.
     */
    public void testWarHotDeployment() throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        WAR war = (WAR) new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-war"), DeployableType.WAR);
        war.setContext("simple");

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/" + war.getContext()
            + "/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingFalse("simple war should not be started at this point", warPingURL,
            getLogger());

        Deployer deployer = createDeployer(getContainer());
        DeployableMonitor deployableMonitor = new URLDeployableMonitor(warPingURL);
        deployableMonitor.setLogger(this.getLogger());
        deployer.deploy(war, deployableMonitor);

        PingUtils.assertPingTrue("simple war should have been started at this point", warPingURL,
            getLogger());

        getLocalContainer().stop();
    }
}
