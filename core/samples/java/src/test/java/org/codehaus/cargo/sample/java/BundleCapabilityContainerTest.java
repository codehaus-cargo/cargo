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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.glassfish.GlassFishPropertySet;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasBundleSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for OSGi bundle support.
 * 
 * @version $Id$
 */
public class BundleCapabilityContainerTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public BundleCapabilityContainerTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    /**
     * Creates the test suite, using the {@link Validator}s.
     * @return Test suite.
     * @throws Exception If anything goes wrong.
     */
    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that run on containers supporting OSGi deployments");

        // We exclude WildFly as the default standalone.xml has OSGi support disabled
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("wildfly8x");

        suite.addTestSuite(BundleCapabilityContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasBundleSupportValidator()},
            excludedContainerIds);
        return suite;
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
     * Test bundle deployment.
     * @throws Exception If anything goes wrong.
     */
    public void testStartWithBundleDeployed() throws Exception
    {
        BufferedReader reader;
        File bundleOutput;
        if (getContainer().getId().startsWith("glassfish"))
        {
            // In GlassFish, the server runs in the domain's "config" directory
            LocalConfiguration configuration = getLocalContainer().getConfiguration();
            bundleOutput = new File(configuration.getHome() + "/"
                + configuration.getPropertyValue(GlassFishPropertySet.DOMAIN_NAME) + "/config",
                "bundle-output.txt");
        }
        else
        {
            bundleOutput = new File(getLocalContainer().getConfiguration().getHome(),
                "bundle-output.txt");
        }
        assertFalse(bundleOutput + " already exists!", bundleOutput.isFile());

        Deployable bundle = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-bundle"), DeployableType.BUNDLE);

        getLocalContainer().getConfiguration().addDeployable(bundle);

        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());
        final long timeout = System.currentTimeMillis() + 30 * 1000;
        while (!bundleOutput.isFile() && System.currentTimeMillis() < timeout)
        {
            // Wait up to timeout while the bundle output is not here
            Thread.sleep(1000);
        }
        assertTrue(bundleOutput + " does not exist!", bundleOutput.isFile());
        reader = new BufferedReader(new FileReader(bundleOutput));
        assertEquals("Hello, World", reader.readLine());
        reader.close();
        reader = null;
        System.gc();

        if (getContainer().getId().startsWith("geronimo"))
        {
            Deployer deployer = createDeployer(getContainer());
            deployer.undeploy(bundle);
        }

        getLocalContainer().stop();
        assertEquals(State.STOPPED, getContainer().getState());
        reader = new BufferedReader(new FileReader(bundleOutput));
        assertEquals("Goodbye, World", reader.readLine());
        reader.close();
        reader = null;
        System.gc();
    }
}
