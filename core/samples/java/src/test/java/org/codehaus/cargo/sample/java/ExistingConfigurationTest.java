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
import java.lang.reflect.Method;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.FileConfig;
import org.codehaus.cargo.sample.java.validator.HasExistingConfigurationValidator;

/**
 * Test for existing configuration.
 */
public class ExistingConfigurationTest extends AbstractWarCapabilityContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public ExistingConfigurationTest()
    {
        super();
        this.addValidator(new HasExistingConfigurationValidator());
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

        if (testMethod != null
            && "testStartWithOneExpandedWarDeployed".equals(testMethod.getName()))
        {
            // The WebLogic 12.x container get confused when expanded WARs are redeployed
            if ("weblogic12x".equals(containerId))
            {
                return false;
            }
        }
        return true;
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

        // For this test suite we need to have an existing configuration which means we need
        // a container configuration already set up. Thus we start by creating a standalone
        // configuration which we'll use as the existing configuration for the test.
        LocalContainer tmpContainer = (LocalContainer) createContainer(
            createConfiguration(ConfigurationType.STANDALONE));
        tmpContainer.getConfiguration().configure(tmpContainer);

        setContainer(createContainer(createConfiguration(ConfigurationType.EXISTING)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAndStop(URL warPingURL)
    {
        String testFileName = "cargo-test/test.file";
        File testFile = new File(getLocalContainer().getConfiguration().getHome(), testFileName);
        Assertions.assertFalse(testFile.exists(), "File " + testFile + " already exists");

        FileConfig fileConfig = new FileConfig();
        fileConfig.setFile(getTestData().getTestDataFileFor("simple-war"));
        fileConfig.setToFile(testFileName);
        getLocalContainer().getConfiguration().setConfigFileProperty(fileConfig);

        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", warPingURL, getLogger());

        // CARGO-1195: DeployableFiles should be setup for ExistingLocalConfiguration
        Assertions.assertTrue(testFile.exists(), "File " + testFile + " was not configured");

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }
}
