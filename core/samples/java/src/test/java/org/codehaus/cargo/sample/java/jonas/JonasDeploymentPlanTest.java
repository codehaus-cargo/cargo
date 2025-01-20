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
package org.codehaus.cargo.sample.java.jonas;

import java.lang.reflect.Method;
import java.net.URL;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.sample.java.AbstractStandaloneLocalContainerTestCase;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.sample.java.validator.HasLocalDeployerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;

/**
 * Test the deployment of JOnAS deployment plans.
 */
public class JonasDeploymentPlanTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public JonasDeploymentPlanTest()
    {
        this.addValidator(new StartsWithContainerValidator("jonas"));
        this.addValidator(new HasLocalDeployerValidator());
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
        // We exclude jonas4x as it doesn't support deployment plans
        return !"jonas4x".equals(containerId);
    }

    /**
     * Test XML deployment plan hot deployment.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testDeploymentPlanHotDeployment() throws Exception
    {
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));

        // Copies the simple war in order to rename it so that it matches the deployable path
        // defined in its jonas-deploymentplan.xml file.
        String urlInternal = this.getFileHandler().createDirectory(
            getInstalledLocalContainer().getHome(), "repositories/url-internal");
        this.getFileHandler().copyFile(getTestData().getTestDataFileFor("simple-war"),
            urlInternal + "/cargo-simple-war.war");

        Deployable deploymentPlan = this.createDeployable(
            "target/test-classes/jonas-deploymentplan.xml", DeployableType.FILE);

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
