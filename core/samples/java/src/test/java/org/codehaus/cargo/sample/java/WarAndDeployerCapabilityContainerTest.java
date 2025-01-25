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
public class WarAndDeployerCapabilityContainerTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public WarAndDeployerCapabilityContainerTest()
    {
        super();
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
        PingUtils.assertPingFalse("simple war should not be present at this point", warPingURL,
            getLogger());

        Deployer deployer = createDeployer(getContainer());
        DeployableMonitor deployableMonitor = new URLDeployableMonitor(warPingURL);
        deployableMonitor.setLogger(this.getLogger());
        deployer.deploy(war, deployableMonitor);
        PingUtils.assertPingTrue("simple war should have been deployed at this point", warPingURL,
            getLogger());

        getLocalContainer().stop();
    }
}
