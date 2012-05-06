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

import java.io.File;
import java.net.URL;
import java.util.UUID;

import junit.framework.Test;

import org.apache.tools.ant.taskdefs.Copy;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.util.AntUtils;

/**
 * Test for system property support.
 * 
 * @version $Id$
 */
public class SystemPropertyCapabilityTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public SystemPropertyCapabilityTest(String testName, EnvironmentTestData testData)
        throws Exception
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
     * @return Test suite.
     * @throws Exception If anything goes wrong.
     */
    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that run on local containers supporting system properties");

        suite.addTestSuite(SystemPropertyCapabilityTest.class, new Validator[] {
            new IsInstalledLocalContainerValidator(),
            new HasWarSupportValidator()});
        return suite;
    }

    /**
     * Test whether setting system properties is working properly.
     * @throws Exception If anything goes wrong.
     */
    public void testSystemProperty() throws Exception
    {
        String random = UUID.randomUUID().toString();

        // Copies the testdata artifact
        File artifactDir = new File(getTestData().targetDir).getParentFile();
        File artifactFile = new File(artifactDir, "systemproperty.war").getAbsoluteFile();
        Copy copyTask = (Copy) new AntUtils().createProject().createTask("copy");
        copyTask.setTofile(artifactFile);
        copyTask.setFile(new File(getTestData().getTestDataFileFor("systemproperty-war")));
        copyTask.execute();

        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            artifactFile.getAbsolutePath(), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL pingURL = new URL("http://localhost:" + getTestData().port
            + "/systemproperty/test?systemPropertyName=random");

        getInstalledLocalContainer().getSystemProperties().put("random", random);
        getLocalContainer().start();
        assertEquals(State.STARTED, getContainer().getState());
        PingUtils.assertPingTrue(pingURL.getPath() + " not started", random, pingURL, getLogger());

        getLocalContainer().stop();
        assertEquals(State.STOPPED, getContainer().getState());
        PingUtils.assertPingFalse(pingURL.getPath() + " not stopped", pingURL, getLogger());
    }

}
