/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasLocalDeployerValidator;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

/**
 * Validates local hot deployment of WAR archives.
 */
public class WarAndDeployerCapabilityContainerTest extends AbstractCargoTestCase
{   
    public WarAndDeployerCapabilityContainerTest(String testName, EnvironmentTestData testData) 
        throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that run on local containers supporting WAR deployments using a local Deployer");

        // We exclude glassfish3x container as it doesn't support hot deployment yet.
        // We exclude tomcat4x container as it doesn't support hot deployment.
        // We exclude tomcat7x container as because of Tomcat 7.0.0 bug 49536.
        Set excludedContainerIds = new TreeSet();
        excludedContainerIds.add("glassfish3x");
        excludedContainerIds.add("tomcat4x");
        excludedContainerIds.add("tomcat7x");

        suite.addTestSuite(WarAndDeployerCapabilityContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasWarSupportValidator(),
            new HasLocalDeployerValidator()}, excludedContainerIds);
        return suite;
    }
    
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
        deployableMonitor.setLogger(this.logger);
        deployer.deploy(war, deployableMonitor);

        PingUtils.assertPingTrue("simple war should have been started at this point", warPingURL,
            getLogger());

        getLocalContainer().stop();
    }
}
