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

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.util.CargoException;

/**
 * Test for WAR extra classpath support.
 */
public class WarExtraClasspathTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public WarExtraClasspathTest()
    {
        this.addValidator(new HasWarSupportValidator());
        this.addValidator(new IsInstalledLocalContainerValidator());
        this.addValidator(new StartsWithContainerValidator("jetty", "tomcat", "liberty"));
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
        // Jetty 5.x, Tomcat 4.x and Tomcat 5.x do not support extra classpath.
        return this.isNotContained(containerId,
            "jetty5x",
            "tomcat4x", "tomcat5x");
    }

    /**
     * Tests that a servlet has access to a class in added to the extraclasspath
     * 
     * @throws MalformedURLException If the WAR URL cannot be built.
     */
    @CargoTestCase
    public void testLoadClass() throws MalformedURLException
    {
        String simpleJar = System.getProperty("cargo.testdata.simple-jar");
        if (simpleJar == null)
        {
            throw new CargoException("Please set property [cargo.testdata.simple-jar] to a valid "
                + "location of simple-jar");
        }

        WAR war = (WAR) this.createDeployableFromTestdataFile("classpath-war", DeployableType.WAR);
        war.setExtraClasspath(new String[] {simpleJar});

        getLocalContainer().getConfiguration().addDeployable(war);
        getLocalContainer().getConfiguration().setProperty(TomcatPropertySet.COPY_WARS, "false");

        URL warPingURL =
            new URL("http://localhost:" + getTestData().port + "/" + "classpath-war/test");

        getLocalContainer().start();

        PingUtils.assertPingTrue("simple war should have been started at this point", warPingURL,
            getLogger());

        getLocalContainer().stop();

        PingUtils.assertPingFalse("simple war should have been stopped at this point",
            warPingURL, getLogger());
    }
}
