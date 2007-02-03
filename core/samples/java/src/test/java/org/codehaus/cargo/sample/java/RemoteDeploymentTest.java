/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasRemoteDeployerValidator;
import org.codehaus.cargo.sample.java.validator.IsRemoteContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasRuntimeConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.types.FileSet;

import java.net.URL;
import java.io.File;
import java.io.FileWriter;

public class RemoteDeploymentTest extends AbstractCargoTestCase
{
    private FileHandler fileHandler = new DefaultFileHandler();

    private InstalledLocalContainer localContainer;
    private Deployable war;
    private Deployer deployer;
    private URL warPingURL;

    public RemoteDeploymentTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that perform remote deployments on remote containers");

        suite.addTestSuite(RemoteDeploymentTest.class, new Validator[] {
            new IsRemoteContainerValidator(),
            new HasRuntimeConfigurationValidator(),
            new HasRemoteDeployerValidator(),
            new HasWarSupportValidator(),

            // Ensure the container can be installed locally so that we can start it and consider
            // it as our remote container for the tests.
            new HasInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator()});

        return suite;
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        // First install a local container and start it. This is the container into which we'll
        // deploy into. It'll act as a remote container, already running.
        this.localContainer = (InstalledLocalContainer) createContainer(
            ContainerType.INSTALLED, createConfiguration(ConfigurationType.STANDALONE));

        // Set up credentials for securing the manager app in the host container. This is for
        // Tomcat.
        this.localContainer.getConfiguration().setProperty(ServletPropertySet.USERS,
            "cargo:password:manager");

        this.localContainer.start();

        // Now create the remote container used by the tests
        setContainer(createContainer(createConfiguration(ConfigurationType.RUNTIME)));

        // Set up deployment credentials
        getRemoteContainer().getConfiguration().setProperty(RemotePropertySet.USERNAME,
            "cargo");
        getRemoteContainer().getConfiguration().setProperty(RemotePropertySet.PASSWORD,
            "password");

        this.war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-war"), DeployableType.WAR);

        this.warPingURL = new URL("http://localhost:" + getTestData().port
            + "/simple-war-" + getTestData().version + "/index.jsp");

        this.deployer = createDeployer(DeployerType.REMOTE, getRemoteContainer());
    }

    protected void tearDown()
    {
        this.localContainer.stop();
    }

    /**
     * Verify that a WAR can be deployed, undeployed and redeployed remotely.
     */
    public void testDeployUndeployRedeployWarRemotely() throws Exception
    {
        deployer.deploy(this.war);
        PingUtils.assertPingTrue("simple war not correctly deployed", this.warPingURL, getLogger());

        deployer.undeploy(war);
        PingUtils.assertPingFalse("simple war not correctly undeployed", this.warPingURL,
            getLogger());

        // Redeploy a second time to ensure that the undeploy worked.
        deployer.deploy(war);
        PingUtils.assertPingTrue("simple war not correctly deployed", this.warPingURL, getLogger());

        // Redeploy the WAR after modifying its content
        deployer.redeploy(modifyWar(this.war));
        URL newWarPingURL = new URL("http://localhost:" + getTestData().port
            + "/simple-war-" + getTestData().version + "/some.html");
        PingUtils.assertPingTrue("simple war not correctly redeployed", newWarPingURL, getLogger());
    }

    /**
     * Modify the original simple WAR file to add a new HTML file which we will later ping to
     * verify the new WAR has been deployed.
     */
    private Deployable modifyWar(Deployable originalDeployable) throws Exception
    {
        // Create the HMTL file that we'll add to the WAR
        File tmpDir = new File(getTestData().targetDir);
        File htmlFile = new File(tmpDir, "some.html");
        FileWriter fw = new FileWriter(htmlFile);
        fw.write("It works...");
        fw.close();

        // Copy and update the WAR to add the HTML file
        File originalWar = new File(originalDeployable.getFile());
        File updatedWar = new File(tmpDir, originalWar.getName());
        this.fileHandler.copyFile(originalWar.getPath(), updatedWar.getPath());
        War warTask = (War) new AntUtils().createProject().createTask("war");
        warTask.setUpdate(true);
        warTask.setDestFile(updatedWar);
        FileSet fileSet = new FileSet();
        fileSet.setFile(htmlFile);
        warTask.addFileset(fileSet);
        warTask.execute();

        return new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            updatedWar.getPath(), DeployableType.WAR); 
    }
}
