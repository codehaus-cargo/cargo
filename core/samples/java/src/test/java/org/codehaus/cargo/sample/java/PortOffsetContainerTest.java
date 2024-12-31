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

import java.net.URL;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasPortOffsetValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for port offset support.
 */
public class PortOffsetContainerTest extends AbstractCargoTestCase
{
    /**
     * Offset.
     */
    private static final String OFFSET = "20";

    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public PortOffsetContainerTest(String testName, EnvironmentTestData testData)
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
            "Tests that run on containers supporting offset configuration.");

        suite.addTestSuite(PortOffsetContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasPortOffsetValidator(ConfigurationType.STANDALONE),
            new HasWarSupportValidator()});
        return suite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        System.setProperty(GeneralPropertySet.PORT_OFFSET, OFFSET);
        Configuration configuration = createConfiguration(ConfigurationType.STANDALONE);
        System.clearProperty(GeneralPropertySet.PORT_OFFSET);
        setContainer(createContainer(configuration));
    }

    /**
     * Start container with port offset.
     * @throws Exception If anything goes wrong.
     */
    public void testStartWithPortOffset() throws Exception
    {
        int offsetValue = Integer.valueOf(OFFSET);
        int portWithOffset = getTestData().port + offsetValue;

        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
                getTestData().getTestDataFileFor("simple-war"), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + portWithOffset + "/simple-war/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }
}
