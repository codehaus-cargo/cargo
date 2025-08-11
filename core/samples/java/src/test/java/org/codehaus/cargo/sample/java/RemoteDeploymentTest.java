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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.DeployerType;
import org.codehaus.cargo.container.internal.util.HttpUtils;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.User;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.sample.java.jboss.AbstractJBossCapabilityTestCase;
import org.codehaus.cargo.sample.java.validator.HasInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasRemoteContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasRemoteDeployerValidator;
import org.codehaus.cargo.sample.java.validator.HasRuntimeConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.util.ZipCompressor;

/**
 * Test for remote deployment.
 */
public class RemoteDeploymentTest extends AbstractCargoTestCase
{
    /**
     * Local container.
     */
    private InstalledLocalContainer localContainer;

    /**
     * Remote deployer.
     */
    private Deployer deployer;

    /**
     * WAR to deploy.
     */
    private WAR war;

    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public RemoteDeploymentTest()
    {
        this.addValidator(new HasRemoteContainerValidator());
        this.addValidator(new HasRuntimeConfigurationValidator());
        this.addValidator(new HasRemoteDeployerValidator());
        this.addValidator(new HasWarSupportValidator());

        // We cannot add the HasInstalledLocalContainerValidator and
        // HasStandaloneConfigurationValidator, else the Remote container would need to
        // implement a Standalone configuration, which doesn't make sense
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp(
        CargoTestCase.CargoTestcaseInvocationContext cargoContext, ExtensionContext testContext)
        throws Exception
    {
        super.setUp(cargoContext, testContext);

        // Create the remote container used by the tests
        setContainer(createContainer(createConfiguration(ConfigurationType.RUNTIME)));

        // Start the local container that this remote container will access
        this.startLocalContainer();

        // The GlassFish 3.x, 4.x and 5.x as well as Payara JSR88 containers
        // require a huge classpath
        List<File> filesToAddToClasspath = new ArrayList<File>();
        if (getTestData().containerId.equals("glassfish3x")
            || getTestData().containerId.equals("glassfish4x")
            || getTestData().containerId.equals("glassfish5x")
            || getTestData().containerId.equals("payara"))
        {
            for (File jar : new File(this.localContainer.getHome(),
                "glassfish/modules").listFiles())
            {
                if (jar.isFile())
                {
                    filesToAddToClasspath.add(jar);
                }
            }
        }
        // JBoss 5+ requires a huge classpath
        else if (getTestData().containerId.startsWith("jboss"))
        {
            int jbossVersion = Integer.parseInt(getTestData().containerId.substring(5,
                getTestData().containerId.length() - 1));

            if (jbossVersion < 10 && jbossVersion >= 5 || jbossVersion >= 50)
            {
                if (jbossVersion == 7 || jbossVersion >= 70 || jbossVersion == 8)
                {
                    AbstractJBossCapabilityTestCase.addAllJars(
                        new File(this.localContainer.getHome(), "modules"), filesToAddToClasspath);
                }
                else
                {
                    for (File jar : new File(this.localContainer.getHome(), "lib").listFiles())
                    {
                        if (jar.isFile())
                        {
                            filesToAddToClasspath.add(jar);
                        }
                    }
                    for (File jar : new File(this.localContainer.getHome(),
                        "common/lib").listFiles())
                    {
                        if (jar.isFile())
                        {
                            filesToAddToClasspath.add(jar);
                        }
                    }
                }
            }
        }
        // WildFly requires some delay between starting server (detected by pinging CPC)
        // and availability of management REST API.
        else if (getTestData().containerId.startsWith("wildfly"))
        {
            Thread.sleep(500);
        }
        URL[] urlsArray = new URL[filesToAddToClasspath.size()];
        for (int i = 0; i < filesToAddToClasspath.size(); i++)
        {
            urlsArray[i] = filesToAddToClasspath.get(i).toURI().toURL();
        }
        URLClassLoader classLoader = new URLClassLoader(urlsArray,
            this.getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);

        // GlassFish 3.x, 4.x and 5.x as well as Payara configuration generation cannot change
        // password. WebLogic needs password in specific format, we use the default value for tests
        if (!getTestData().containerId.equals("glassfish3x")
            && !getTestData().containerId.equals("glassfish4x")
            && !getTestData().containerId.equals("glassfish5x")
            && !getTestData().containerId.equals("payara")
            && !getTestData().containerId.startsWith("weblogic"))
        {
            // Set up deployment credentials
            getRemoteContainer().getConfiguration().setProperty(RemotePropertySet.USERNAME,
                "cargo");
            getRemoteContainer().getConfiguration().setProperty(RemotePropertySet.PASSWORD,
                "password");
        }
        else if (getTestData().containerId.startsWith("weblogic"))
        {
            getRemoteContainer().getConfiguration().setProperty(
                WebLogicPropertySet.LOCAL_WEBLOGIC_HOME, localContainer.getHome());
        }

        this.war = (WAR) this.createDeployableFromTestdataFile("simple-war", DeployableType.WAR);

        this.deployer = createDeployer(DeployerType.REMOTE, getRemoteContainer());
    }

    /**
     * Start the local container, on which we will remotely deploy.
     * @throws Exception If anything goes wrong.
     */
    private void startLocalContainer() throws Exception
    {
        // Variable externalized to keep checkstyle happy
        EnvironmentTestData testData = getTestData();

        final String message = "You have implemented the Remote container. Please also implement a "
            + "standalone local container for the CARGO samples to pass.";
        Assertions.assertTrue(new HasInstalledLocalContainerValidator().validate(
            getTestData().containerId, ContainerType.INSTALLED), message);
        Assertions.assertTrue(new HasStandaloneConfigurationValidator().validate(
            getTestData().containerId, ContainerType.INSTALLED), message);

        final ContainerType oldContainerType = getTestData().containerType;
        testData.containerType = ContainerType.INSTALLED;

        // First install a local container and start it. This is the container into which we'll
        // deploy into. It'll act as a remote container, already running.
        this.localContainer = (InstalledLocalContainer) createContainer(createConfiguration(
            ConfigurationType.STANDALONE));

        testData.containerType = oldContainerType;

        // Jetty requires its deployer application
        if (getTestData().containerId.startsWith("jetty"))
        {
            int jettyVersion = Integer.parseInt(getTestData().containerId.substring(5,
                getTestData().containerId.length() - 1));

            final Deployable jettyDeployerApplication;
            if (jettyVersion <= 6)
            {
                jettyDeployerApplication = (WAR) this.createDeployableFromTestdataFile(
                    "cargo-jetty-6-deployer", DeployableType.WAR);
            }
            else if (jettyVersion <= 9)
            {
                jettyDeployerApplication = (WAR) this.createDeployableFromTestdataFile(
                    "cargo-jetty-7-to-jetty-9-deployer", DeployableType.WAR);
            }
            else if (jettyVersion == 10)
            {
                jettyDeployerApplication = (WAR) this.createDeployableFromTestdataFile(
                    "cargo-jetty-10-deployer", DeployableType.WAR);
            }
            else if (jettyVersion == 11)
            {
                jettyDeployerApplication = (WAR) this.createDeployableFromTestdataFile(
                    "cargo-jetty-11-deployer", DeployableType.WAR);
            }
            else
            {
                throw new IllegalArgumentException("Jetty " + jettyVersion + " not supported");
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
                List<User> users = User.parseUsers("cargo:password:manager");
                this.localContainer.getConfiguration().getUsers().addAll(users);
            }
            else
            {
                List<User> users = User.parseUsers("cargo:password:manager-script");
                this.localContainer.getConfiguration().getUsers().addAll(users);
            }
        }
        // TomEE requires the servlet users to have a manager
        else if (getTestData().containerId.startsWith("tomee"))
        {
            List<User> users = User.parseUsers("cargo:password:manager-script");
            this.localContainer.getConfiguration().getUsers().addAll(users);
        }
        else if (getTestData().containerId.startsWith("wildfly"))
        {
            List<User> users = User.parseUsers("cargo:password:admin");
            this.localContainer.getConfiguration().getUsers().addAll(users);
        }

        this.localContainer.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @AfterEach
    protected void tearDown()
    {
        // Make sure the local container we started is shut down gracefully.
        setContainer(this.localContainer);
        super.tearDown();
    }

    /**
     * Verify that a WAR can be deployed, undeployed and redeployed remotely.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testDeployUndeployRedeployWarRemotely() throws Exception
    {
        URL warPingURL =
            new URL("http://localhost:" + getTestData().port + "/simple-war/index.jsp");

        deployer.deploy(this.war);
        PingUtils.assertPingTrue(
            "simple war not correctly deployed", "Sample page for testing",
                warPingURL, getLogger());

        deployer.undeploy(this.war);
        // Payara 7.2025.1.Alpha (and only that sub branch) returns empty HTTP 200
        // even when the deployable is not present
        if (this.localContainer.getName().startsWith("Payara 7"))
        {
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.setLogger(getLogger());
            HttpUtils.HttpResult result = new HttpUtils.HttpResult();
            httpUtils.ping(warPingURL, null, result, PingUtils.TIMEOUT);
            Assertions.assertEquals(
                "", result.responseBody, "simple war not correctly undeployed");
        }
        else
        {
            PingUtils.assertPingFalse(
                "simple war not correctly undeployed", warPingURL, getLogger());
        }

        // Redeploy a second time to ensure that the undeploy worked.
        deployer.deploy(this.war);
        PingUtils.assertPingTrue(
            "simple war not correctly redeployed", "Sample page for testing",
                warPingURL, getLogger());

        // Jetty 7.2.x (and only that sub branch) has trouble redeploying modified WARs
        if (this.localContainer.getName().startsWith("Jetty 7.2."))
        {
            return;
        }

        // JOnAS 4.x has trouble redeploying modified WARs,
        // applications indeed need to be EARs in order to be successfully redeployed
        if ("jonas4x".equals(getTestData().containerId))
        {
            return;
        }

        // Redeploy the WAR after modifying its content
        URL newWarPingURL =
            new URL("http://localhost:" + getTestData().port + "/simple-war/some.html");
        PingUtils.assertPingFalse("modified file already in war", newWarPingURL, getLogger());
        Deployable modifiedDeployable = modifyWar(this.war);
        File modifiedWar = new File(modifiedDeployable.getFile());
        if (!modifiedWar.isFile())
        {
            throw new FileNotFoundException("Modified WAR \"" + modifiedWar + "\" doesn't exist");
        }
        deployer.redeploy(modifiedDeployable);
        PingUtils.assertPingTrue(
            "simple war not correctly redeployed", "It works...", newWarPingURL, getLogger());
    }

    /**
     * Verify that WAR context change works.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testChangeWarContextAndDeployUndeployRemotely() throws Exception
    {
        this.war.setContext("simple");

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/"
            + this.war.getContext() + "/index.jsp");

        deployer.deploy(this.war);
        PingUtils.assertPingTrue(
            "simple war not correctly deployed", "Sample page for testing",
                warPingURL, getLogger());

        deployer.undeploy(this.war);
        // Payara 7.2025.1.Alpha (and only that sub branch) returns empty HTTP 200
        // even when the deployable is not present
        if (this.localContainer.getName().startsWith("Payara 7"))
        {
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.setLogger(getLogger());
            HttpUtils.HttpResult result = new HttpUtils.HttpResult();
            httpUtils.ping(warPingURL, null, result, PingUtils.TIMEOUT);
            Assertions.assertEquals(
                "", result.responseBody, "simple war not correctly undeployed");
        }
        else
        {
            PingUtils.assertPingFalse(
                "simple war not correctly undeployed", warPingURL, getLogger());
        }
    }

    /**
     * Modify the original simple WAR file to add a new HTML file which we will later ping to
     * verify the new WAR has been deployed.
     * @param originalDeployable {@link Deployable} to modify.
     * @return Modified {@link Deployable} (an HTML file is added).
     * @throws Exception If anything goes wrong.
     */
    private Deployable modifyWar(Deployable originalDeployable) throws Exception
    {
        // Create the HTML file that we'll add to the WAR
        String modifiedWarDirectory = getFileHandler().append(
            getFileHandler().getParent(getTestData().configurationHome), "modified-war");
        getFileHandler().mkdirs(modifiedWarDirectory);
        getFileHandler().explode(originalDeployable.getFile(), modifiedWarDirectory);
        String htmlFile = getFileHandler().append(modifiedWarDirectory, "some.html");
        getFileHandler().writeTextFile(htmlFile, "It works...", StandardCharsets.UTF_8);

        // Copy and update the WAR to add the HTML file
        String modifiedWar = getFileHandler().append(
            getFileHandler().getParent(getTestData().configurationHome),
                getFileHandler().getName(originalDeployable.getFile()));
        ZipCompressor compressor = new ZipCompressor(getFileHandler());
        compressor.compress(modifiedWarDirectory, modifiedWar);

        return this.createDeployable(modifiedWar, DeployableType.WAR);
    }
}
