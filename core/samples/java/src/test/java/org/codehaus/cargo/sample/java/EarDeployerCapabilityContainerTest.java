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
package org.codehaus.cargo.sample.java;

import java.lang.reflect.Method;
import java.net.URL;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.sample.java.validator.HasEarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasLocalDeployerValidator;

/**
 * Validates local hot deployment of EAR archives
 */
public class EarDeployerCapabilityContainerTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public EarDeployerCapabilityContainerTest()
    {
        this.addValidator(new HasEarSupportValidator());
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

        // We exclude containers that cannot hot deploy EARs
        return this.isNotContained(containerId,
            "liberty",
            "tomee1x", "tomee7x", "tomee8x", "tomee9x", "tomee10x",
            "weblogic122x", "weblogic14x", "weblogic15x");
    }

    /**
     * Test EAR hot deployment.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testEarHotDeployment() throws Exception
    {
        Deployable ear = this.createDeployableFromTestdataFile("simple-ear", DeployableType.EAR);
        ((EAR) ear).setName("ear-test-CARGO-1641");

        URL earPingURL =
            new URL("http://localhost:" + getTestData().port + "/simpleweb/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingFalse(
            "simple ear should not be present at this point", earPingURL, getLogger());

        Deployer deployer = createDeployer(getContainer());
        DeployableMonitor deployableMonitor = new URLDeployableMonitor(earPingURL);
        deployableMonitor.setLogger(this.getLogger());
        deployer.deploy(ear, deployableMonitor);
        PingUtils.assertPingTrue(
            "simple ear should have been deployed at this point", "Sample page for testing",
                earPingURL, getLogger());

        deployer.undeploy(ear, deployableMonitor);
        PingUtils.assertPingFalse("simple ear not correctly undeployed", earPingURL, getLogger());

        getLocalContainer().stop();
    }
}
