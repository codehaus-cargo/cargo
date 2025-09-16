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
package org.codehaus.cargo.sample.java;

import java.net.URL;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.sample.java.validator.HasLocalDeployerValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;

/**
 * Validates local hot deployment of WAR archives.
 */
public class WarDeployerCapabilityContainerTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public WarDeployerCapabilityContainerTest()
    {
        this.addValidator(new HasWarSupportValidator());
        this.addValidator(new HasLocalDeployerValidator());
    }

    /**
     * Test WAR hot deployment.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testWarHotDeployment() throws Exception
    {
        if ("jetty12x".equals(getTestData().containerId))
        {
            getLocalContainer().getConfiguration().setProperty(
                GeneralPropertySet.JVMARGS, "-Djetty.deploy.scanInterval=1");
        }

        WAR war = (WAR) this.createDeployableFromTestdataFile("simple-war", DeployableType.WAR);
        war.setContext("simple");

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/" + war.getContext()
            + "/index.jsp");

        getLocalContainer().start();
        // Payara 7.2025.1.Beta (and only that sub branch) returns empty HTTP 200
        // even when the deployable is not present
        if (!getLocalContainer().getName().startsWith("Payara 7.2025.1.Beta"))
        {
            PingUtils.assertPingFalse(
                "simple war should not be present at this point", warPingURL, getLogger());
        }

        Deployer deployer = createDeployer(getContainer());
        DeployableMonitor deployableMonitor = new URLDeployableMonitor(warPingURL);
        deployableMonitor.setLogger(this.getLogger());
        deployer.deploy(war, deployableMonitor);
        PingUtils.assertPingTrue(
            "simple war should have been deployed at this point", "Sample page for testing",
                warPingURL, getLogger());

        // We exclude containers that cannot hot undeploy WARs
        String containerId = getContainer().getId();
        if (this.isNotContained(containerId,
                "jetty6x", "jetty7x", "jetty8x", "jetty9x", "jetty10x", "jetty11x", "jetty12x",
                "jo1x",
                "liberty",
                "resin3x", "resin4x", "resin31x")
            && !getLocalContainer().getName().startsWith("Payara 7.2025.1.Beta")
            && !getContainer().getType().equals(ContainerType.EMBEDDED)
                && this.isNotContained(containerId,
                    "tomcat8x", "tomcat9x", "tomcat10x", "tomcat11x"))
        {
            deployer.undeploy(war, deployableMonitor);
            PingUtils.assertPingFalse(
                "simple war should have been undeployed at this point", warPingURL, getLogger());
        }

        getLocalContainer().stop();
    }
}
