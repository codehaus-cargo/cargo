/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasEarSupportValidator;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

public class EarCapabilityContainerTest extends AbstractCargoTestCase
{
    public EarCapabilityContainerTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that run on containers supporting EAR deployments");

        // We exclude geronimo1x container as it doesn't support static deployments yet.
        // We exclude glassfish3x container as it doesn't support hot deployment yet.
        Set excludedContainerIds = new TreeSet();
        excludedContainerIds.add("geronimo1x");
        excludedContainerIds.add("glassfish3x");

        suite.addTestSuite(EarCapabilityContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasEarSupportValidator()}, excludedContainerIds);
        return suite;
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    public void testStartWithOneEmptyEarDeployed() throws Exception
    {
        Deployable ear = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("empty-ear"), DeployableType.EAR);

        getLocalContainer().getConfiguration().addDeployable(ear);

        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());

        getLocalContainer().stop();
        assertEquals(State.STOPPED, getContainer().getState());
    }

    public void testStartWithOneEarWithOneWarDeployed() throws Exception
    {
        Deployable ear = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-ear"), DeployableType.EAR);

        getLocalContainer().getConfiguration().addDeployable(ear);

        URL earPingURL =
            new URL("http://localhost:" + getTestData().port + "/simpleweb/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue("simple ear not started", earPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse("simple ear not stopped", earPingURL, getLogger());
    }
}
