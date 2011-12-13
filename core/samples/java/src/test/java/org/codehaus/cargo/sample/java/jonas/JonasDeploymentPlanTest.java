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
package org.codehaus.cargo.sample.java.jonas;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.AbstractCargoTestCase;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.sample.java.validator.HasLocalDeployerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Test the deployment of JOnAS deployment plans.
 * 
 * @version $Id$
 */
public class JonasDeploymentPlanTest extends AbstractCargoTestCase
{
    /**
     * File handler.
     */
    private FileHandler fileHandler = new DefaultFileHandler();

    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public JonasDeploymentPlanTest(String testName, EnvironmentTestData testData)
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
        // We exclude jonas4x as it doesn't support deployment plans
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("jonas4x");

        CargoTestSuite suite =
            new CargoTestSuite("Test that verifies JOnAS-specific standalone local configuration "
                + "options");

        suite.addTestSuite(JonasStandaloneConfigurationTest.class, new Validator[] {
            new StartsWithContainerValidator("jonas"),
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasLocalDeployerValidator()
        }, excludedContainerIds);

        return suite;
    }

    /**
     * Test XML deployment plan hot deployment.
     * @throws Exception If anything goes wrong.
     */
    public void testDeploymentPlanHotDeployment() throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        String urlInternal = this.fileHandler.createDirectory(
            getLocalContainer().getConfiguration().getHome(), "repositories/url-internal");
        this.fileHandler.copyFile(getTestData().getTestDataFileFor("simple-war"),
            urlInternal + "/cargo-simple-war.war");

        Deployable deploymentPlan = new DefaultDeployableFactory().createDeployable(
            getContainer().getId(), "target/test-classes/jonas-deploymentplan.xml",
            DeployableType.FILE);

        URL warPingURL = new URL("http://localhost:" + getTestData().port
            + "/cargo-simple-war/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingFalse("simple war should not be started at this point", warPingURL,
            getLogger());

        Deployer deployer = createDeployer(getContainer());
        DeployableMonitor deployableMonitor = new URLDeployableMonitor(warPingURL);
        deployableMonitor.setLogger(this.getLogger());
        deployer.deploy(deploymentPlan, deployableMonitor);
        PingUtils.assertPingTrue("simple war should have been started at this point", warPingURL,
            getLogger());

        deployer.undeploy(deploymentPlan, deployableMonitor);
        PingUtils.assertPingFalse("simple war should have been stopped at this point", warPingURL,
            getLogger());

        getLocalContainer().stop();
    }
}
