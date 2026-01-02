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

import java.io.File;
import java.net.URL;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.packager.Packager;
import org.codehaus.cargo.container.packager.PackagerType;
import org.codehaus.cargo.container.spi.packager.AbstractDirectoryPackager;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.packager.DefaultPackagerFactory;
import org.codehaus.cargo.generic.packager.PackagerFactory;
import org.codehaus.cargo.sample.java.validator.HasDirectoryPackagerValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;

/**
 * Test for packager.
 */
public class PackagerTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public PackagerTest()
    {
        this.addValidator(new HasDirectoryPackagerValidator());
        this.addValidator(new IsInstalledLocalContainerValidator());
    }

    /**
     * Create the packaging of a container and that it works when unpackaged.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testPackageContainer() throws Exception
    {
        // First, deploy one WAR into the configuration.
        WAR war = (WAR) this.createDeployableFromTestdataFile("simple-war", DeployableType.WAR);
        getLocalContainer().getConfiguration().addDeployable(war);
        getLocalContainer().getConfiguration().configure(getLocalContainer());

        File targetLocation =
            new File(new File(getTestData().configurationHome).getParentFile(), "package");

        PackagerFactory factory = new DefaultPackagerFactory();
        Packager packager = factory.createPackager(getTestData().containerId,
            PackagerType.DIRECTORY, targetLocation.getPath());
        ((AbstractDirectoryPackager) packager).setLogger(getLogger());
        packager.packageContainer(getInstalledLocalContainer());

        // Try to start and stop the container using the packaged configuration expanded as an
        // existing configuration.
        setContainer(new DefaultContainerFactory().createContainer(getTestData().containerId,
                getTestData().containerType, createConfiguration(ConfigurationType.EXISTING,
                    targetLocation.getPath())));
        getLocalContainer().setLogger(getLogger());
        getInstalledLocalContainer().setHome(targetLocation.getPath());
        URL warPingURL =
            new URL("http://localhost:" + getTestData().port + "/simple-war/index.jsp");

        // Jetty 9.2.x (and only that sub branch) has trouble restarting package
        if (getContainer().getName().startsWith("Jetty 9.2."))
        {
            return;
        }

        getLocalContainer().start();
        PingUtils.assertPingTrue(
            "simple war not started", "Sample page for testing", warPingURL, getLogger());
        getLocalContainer().stop();
        PingUtils.assertPingFalse("simple war not stopped", warPingURL, getLogger());
    }
}
