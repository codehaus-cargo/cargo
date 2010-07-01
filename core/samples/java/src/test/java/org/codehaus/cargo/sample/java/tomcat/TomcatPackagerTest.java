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
package org.codehaus.cargo.sample.java.tomcat;

import org.codehaus.cargo.sample.java.AbstractCargoTestCase;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.sample.java.validator.*;
import org.codehaus.cargo.generic.packager.DefaultPackagerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.container.packager.PackagerType;
import org.codehaus.cargo.container.packager.Packager;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.StandaloneLocalConfiguration;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import junit.framework.Test;

import java.io.File;
import java.net.URL;

/**
 */
public class TomcatPackagerTest extends AbstractCargoTestCase
{
    public TomcatPackagerTest(String testName, EnvironmentTestData testData) throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite("Tests that can run on installed local Tomcat "
            + "containers supporting directory Packagers");
        suite.addTestSuite(TomcatPackagerTest.class, new Validator[] {
            new StartsWithContainerValidator("tomcat"),
            new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasDirectoryPackagerValidator()});
        return suite;
    }

    public void testPackageContainer() throws Exception
    {
        // First, create a configuration and deploy one WAR in it.
        StandaloneLocalConfiguration configuration =
            (StandaloneLocalConfiguration) createConfiguration(ConfigurationType.STANDALONE);
        InstalledLocalContainer container =
            (InstalledLocalContainer) createContainer(configuration);
        Deployable war = new DefaultDeployableFactory().createDeployable(container.getId(),
            getTestData().getTestDataFileFor("simple-war"), DeployableType.WAR);
        configuration.addDeployable(war);
        configuration.configure(container);

        File targetLocation =
            new File(new File(getTestData().targetDir).getParentFile(), "package");

        PackagerFactory factory = new DefaultPackagerFactory();
        Packager packager = factory.createPackager(getTestData().containerId,
            PackagerType.DIRECTORY, targetLocation.getPath());
        packager.packageContainer(container);

        // Try to start and stop the container using an existing configuration. This doesn't really
        // validate that the container can be started/stopped using the native start/stop script
        // but that's the best we can do to validate it works.

        InstalledLocalContainer assertContainer = (InstalledLocalContainer)
            new DefaultContainerFactory().createContainer(getTestData().containerId,
            getTestData().containerType, createConfiguration(ConfigurationType.EXISTING,
                targetLocation.getPath()));
        assertContainer.setHome(targetLocation.getPath());
        URL warPingURL = new URL("http://localhost:" + getTestData().port
            + "/simple-war-" + getTestData().version + "/index.jsp");

        assertContainer.start();
        PingUtils.assertPingTrue("simple war not started", warPingURL, getLogger());
        assertContainer.stop();
        PingUtils.assertPingFalse("simple war not stopped", warPingURL, getLogger());
    }
}
