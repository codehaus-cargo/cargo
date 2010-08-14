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

import java.net.URL;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasRemoteContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasRemoteDeployerValidator;
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
            new HasRemoteContainerValidator(),
            new HasRuntimeConfigurationValidator(),
            new HasRemoteDeployerValidator(),
            new HasWarSupportValidator(),

            // We cannot add the HasInstalledLocalContainerValidator and
            // HasStandaloneConfigurationValidator, else the Remote container would need to
            // implement a Standalone configration, which doesn't make sense
        });

        return suite;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Create the remote container used by the tests
        setContainer(createContainer(createConfiguration(ConfigurationType.RUNTIME)));

        // Start the local container that this remote container will access
        this.startLocalContainer();

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

    private void startLocalContainer() throws Exception
    {
        final String message = "You have implemented the Remote container. Please also implement a "
            + "standalone local container for the CARGO samples to pass.";
        assertTrue(message, new HasInstalledLocalContainerValidator().validate(
            getTestData().containerId, ContainerType.INSTALLED));
        assertTrue(message, new HasStandaloneConfigurationValidator().validate(
            getTestData().containerId, ContainerType.INSTALLED));

        final ContainerType oldContainerType = getTestData().containerType;
        getTestData().containerType = ContainerType.INSTALLED;

        // First install a local container and start it. This is the container into which we'll
        // deploy into. It'll act as a remote container, already running.
        this.localContainer = (InstalledLocalContainer) createContainer(createConfiguration(
            ConfigurationType.STANDALONE));

        getTestData().containerType = oldContainerType;

        // JBoss 5+ requires a huge classpath
        if (getTestData().containerId.startsWith("jboss"))
        {
            int jbossVersion = Integer.parseInt(getTestData().containerId.substring(5,
                getTestData().containerId.length() - 1));

            if (jbossVersion >= 5)
            {
                List<URL> urls = new ArrayList<URL>();

                if (jbossVersion == 5)
                {
                    urls.add(new File(getTestData().getTestDataFileFor(
                        "cargo-core-tools-jboss-deployer-5")).toURI().toURL());
                }
                else
                {
                    urls.add(new File(getTestData().getTestDataFileFor(
                        "cargo-core-tools-jboss-deployer-5.1-and-onwards")).toURI().toURL());
                }

                for (File jar : new File(this.localContainer.getHome(), "lib").listFiles())
                {
                    if (jar.isFile())
                    {
                        urls.add(jar.toURI().toURL());
                    }
                }
                for (File jar : new File(this.localContainer.getHome(), "common/lib").listFiles())
                {
                    if (jar.isFile())
                    {
                        urls.add(jar.toURI().toURL());
                    }
                }

                URL[] urlsArray = new URL[urls.size()];
                urlsArray = urls.toArray(urlsArray);
                URLClassLoader classLoader = new URLClassLoader(urlsArray,
                    Thread.currentThread().getContextClassLoader());
                Thread.currentThread().setContextClassLoader(classLoader);
            }
        }
        // Jetty requires its deployer application
        else if (getTestData().containerId.startsWith("jetty"))
        {
            int jettyVersion = Integer.parseInt(getTestData().containerId.substring(5,
                getTestData().containerId.length() - 1));

            final Deployable jettyDeployerApplication;
            if (jettyVersion <= 6)
            {
                jettyDeployerApplication = new DefaultDeployableFactory().createDeployable(
                    this.localContainer.getId(), getTestData().getTestDataFileFor(
                    "cargo-jetty-6-and-earlier-deployer"), DeployableType.WAR);
            }
            else
            {
                jettyDeployerApplication = new DefaultDeployableFactory().createDeployable(
                    this.localContainer.getId(), getTestData().getTestDataFileFor(
                    "cargo-jetty-7-and-onwards-deployer"), DeployableType.WAR);
            }

            this.localContainer.getConfiguration().addDeployable(jettyDeployerApplication);

            // As of CARGO-820, the Jetty remote deployer is on context /cargo-jetty-deployer
        }
        // Tomcat requires the servlet users to have a manager
        // Tomcat 7 needs the manager to be a manager-script
        else if (getTestData().containerId.startsWith("tomcat"))
        {
            int tomcatVersion = Integer.parseInt(getTestData().containerId.substring(6,
                getTestData().containerId.length() - 1));

            if (tomcatVersion < 7)
            {
                this.localContainer.getConfiguration().setProperty(ServletPropertySet.USERS,
                    "cargo:password:manager");
            }
            else
            {
                this.localContainer.getConfiguration().setProperty(ServletPropertySet.USERS,
                    "cargo:password:manager-script");
            }
        }

        this.localContainer.start();
    }

    @Override
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

        deployer.undeploy(this.war);
        PingUtils.assertPingFalse("simple war not correctly undeployed", "", this.warPingURL,
            getLogger());

        // Redeploy a second time to ensure that the undeploy worked.
        deployer.deploy(this.war);
        PingUtils.assertPingTrue("simple war not correctly redeployed", this.warPingURL, getLogger());

        if ("jonas4x".equals(getTestData().containerId))
        {
            // JOnAS 4.x has trouble redeploying modified WARs,
            // applications indeed need to be EARs in order to be successfully redeployed
            return;
        }

        // Redeploy the WAR after modifying its content
        Deployable modifiedDeployable = modifyWar(this.war);
        File modifiedWar = new File(modifiedDeployable.getFile());
        if (!modifiedWar.isFile())
        {
            throw new FileNotFoundException("Modified WAR \"" + modifiedWar + "\" doesn't exist");
        }
        deployer.redeploy(modifiedDeployable);
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
        // Create the HTML file that we'll add to the WAR
        File tmpDir = new File(new File(getTestData().targetDir).getParent(), "modified-war");
        tmpDir.mkdirs();
        if (!tmpDir.isDirectory())
        {
            throw new FileNotFoundException("Cannot create modified WAR temporary directory \""
                + tmpDir + "\"");
        }
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
