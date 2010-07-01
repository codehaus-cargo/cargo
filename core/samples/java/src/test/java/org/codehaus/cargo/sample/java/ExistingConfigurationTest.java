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
package org.codehaus.cargo.sample.java;

import junit.framework.Test;

import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.HasExistingConfigurationValidator;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;

public class ExistingConfigurationTest extends AbstractWarCapabilityContainerTestCase
{
    public ExistingConfigurationTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that verify that existing configuration work by doing local WAR deployments");

        suite.addTestSuite(ExistingConfigurationTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasExistingConfigurationValidator(),
            new HasWarSupportValidator()});
        return suite;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // For this test suite we need to have an existing configuration which means we need
        // a container configuration already set up. Thus we start by creating a standalone
        // configuration which we'll use as the existing configuration for the test. 
        LocalContainer tmpContainer = (LocalContainer) createContainer(
            createConfiguration(ConfigurationType.STANDALONE));
        tmpContainer.getConfiguration().configure(tmpContainer);

        setContainer(createContainer(createConfiguration(ConfigurationType.EXISTING)));
    }
}
