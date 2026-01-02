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
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.util.CargoException;

/**
 * Test for extra classpath support.
 */
public class ExtraClasspathOnStandaloneConfigurationTest
    extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public ExtraClasspathOnStandaloneConfigurationTest()
    {
        this.addValidator(new HasWarSupportValidator());
        this.addValidator(new IsInstalledLocalContainerValidator());
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

        // We exclude Geronimo 1.x as it doesn't support extra classpath
        return !"geronimo1x".equals(containerId);
    }

    /**
     * Tests that a servlet has access to a class in added to the extraclasspath
     * @throws MalformedURLException If the WAR URL cannot be built.
     */
    @CargoTestCase
    public void testLoadClass() throws MalformedURLException
    {
        WAR war = (WAR) this.createDeployableFromTestdataFile("classpath-war", DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL =
            new URL("http://localhost:" + getTestData().port + "/" + "classpath-war/test");

        getLocalContainer().start();

        PingUtils.assertPingTrue(
            "classpath war should have been started at this point", "Got class!",
                warPingURL, getLogger());

        getLocalContainer().stop();

        PingUtils.assertPingFalse(
            "classpath war should have been stopped at this point", warPingURL, getLogger());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Container createContainer(Configuration configuration)
    {
        InstalledLocalContainer container =
            (InstalledLocalContainer) super.createContainer(configuration);

        String simpleJar = System.getProperty("cargo.testdata.simple-jar");
        if (simpleJar != null)
        {
            container.addExtraClasspath(simpleJar);
        }
        else
        {
            throw new CargoException("Please set property [cargo.testdata.simple-jar] to a valid "
                + "location of simple-jar");
        }
        return container;
    }

}
