/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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

import junit.framework.Test;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasBundleSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for OSGi bundle support.
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

        suite.addTestSuite(BundleCapabilityContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasBundleSupportValidator()});
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
        String targetFile = System.getProperty("cargo.samples.bundle.targetFile");
        assertTrue("cargo.samples.bundle.targetFile not set!",
            targetFile != null && !targetFile.isEmpty());

        File bundleOutput = new File(targetFile);

        if (bundleOutput.exists())
        {
            bundleOutput.delete();
            assertFalse("Cannot delete " + bundleOutput, bundleOutput.exists());
        }

        Deployable bundle = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-bundle"), DeployableType.BUNDLE);

        getLocalContainer().getConfiguration().addDeployable(bundle);
        getInstalledLocalContainer().getSystemProperties().put(
            "cargo.samples.bundle.targetFile", targetFile);

        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());
        final long timeout = System.currentTimeMillis() + 30 * 1000;
        while (!bundleOutput.isFile() && System.currentTimeMillis() < timeout)
        {
            // Wait up to timeout while the bundle output is not here
            Thread.sleep(1000);
        }
        assertTrue(bundleOutput + " does not exist!", bundleOutput.isFile());
        BufferedReader reader = new BufferedReader(new FileReader(bundleOutput));
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
