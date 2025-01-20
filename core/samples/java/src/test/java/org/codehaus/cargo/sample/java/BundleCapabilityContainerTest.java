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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.sample.java.validator.HasBundleSupportValidator;

/**
 * Test for OSGi bundle support.
 */
public class BundleCapabilityContainerTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public BundleCapabilityContainerTest()
    {
        this.addValidator(new HasBundleSupportValidator());
    }

    /**
     * Test bundle deployment.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testStartWithBundleDeployed() throws Exception
    {
        String targetFile = System.getProperty("cargo.samples.bundle.targetFile");
        Assertions.assertTrue(targetFile != null && !targetFile.isEmpty(),
            "cargo.samples.bundle.targetFile not set!");

        File bundleOutput = new File(targetFile);

        if (bundleOutput.exists())
        {
            bundleOutput.delete();
            Assertions.assertFalse(bundleOutput.exists(), "Cannot delete " + bundleOutput);
        }

        Deployable bundle =
            this.createDeployableFromTestdataFile("simple-bundle", DeployableType.BUNDLE);

        getLocalContainer().getConfiguration().addDeployable(bundle);
        getInstalledLocalContainer().getSystemProperties().put(
            "cargo.samples.bundle.targetFile", targetFile);

        getLocalContainer().start();
        Assertions.assertEquals(State.STARTED, getContainer().getState());
        final long timeout = System.currentTimeMillis() + 30 * 1000;
        while (!bundleOutput.isFile() && System.currentTimeMillis() < timeout)
        {
            // Wait up to timeout while the bundle output is not here
            Thread.sleep(1000);
        }
        Assertions.assertTrue(bundleOutput.isFile(), bundleOutput + " does not exist!");
        BufferedReader reader = new BufferedReader(new FileReader(bundleOutput));
        Assertions.assertEquals("Hello, World", reader.readLine());
        reader.close();
        reader = null;
        System.gc();

        if (getTestData().containerId.startsWith("geronimo"))
        {
            Deployer deployer = createDeployer(getContainer());
            deployer.undeploy(bundle);
        }

        getLocalContainer().stop();
        Assertions.assertEquals(State.STOPPED, getContainer().getState());
        reader = new BufferedReader(new FileReader(bundleOutput));
        Assertions.assertEquals("Goodbye, World", reader.readLine());
        reader.close();
        reader = null;
        System.gc();
    }
}
