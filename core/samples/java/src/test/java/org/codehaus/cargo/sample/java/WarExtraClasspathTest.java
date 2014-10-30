/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.util.CargoException;

/**
 * Test for WAR extra classpath support.
 * 
 * @version $Id$
 */
public class WarExtraClasspathTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * 
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public WarExtraClasspathTest(String testName, EnvironmentTestData testData) throws Exception
    {
        super(testName, testData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    /**
     * Creates the test suite, using the {@link Validator}s.
     * 
     * @return Test suite.
     * @throws Exception If anything goes wrong.
     */
    public static Test suite() throws Exception
    {
        CargoTestSuite suite =
            new CargoTestSuite("Tests that run on local containers to test extra classpath");

        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("jetty4x");
        excludedContainerIds.add("jetty5x");
        excludedContainerIds.add("tomcat4x");
        excludedContainerIds.add("tomcat5x");
        suite.addTestSuite(WarExtraClasspathTest.class, new Validator[] {
            new StartsWithContainerValidator("jetty", "tomcat"), new HasWarSupportValidator(),
            new IsInstalledLocalContainerValidator(), new HasStandaloneConfigurationValidator()},
            excludedContainerIds);
        return suite;
    }

    /**
     * Tests that a servlet has access to a class in added to the extraclasspath
     * 
     * @throws MalformedURLException If the WAR URL cannot be built.
     */
    public void testLoadClass() throws MalformedURLException
    {
        String simpleJar = System.getProperty("cargo.testdata.simple-jar");
        if (simpleJar == null)
        {
            throw new CargoException("Please set property [cargo.testdata.simple-jar] to a valid "
                + "location of simple-jar");
        }

        WAR war =
            (WAR) new DefaultDeployableFactory().createDeployable(getContainer().getId(),
                getTestData().getTestDataFileFor("classpath-war"), DeployableType.WAR);
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
