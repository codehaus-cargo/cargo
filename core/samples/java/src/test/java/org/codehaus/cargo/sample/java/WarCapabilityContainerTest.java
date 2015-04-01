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

import junit.framework.Test;

import org.apache.tools.ant.taskdefs.Copy;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.util.AntUtils;

/**
 * Test for WAR support.
 * 
 */
public class WarCapabilityContainerTest extends AbstractWarCapabilityContainerTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public WarCapabilityContainerTest(String testName, EnvironmentTestData testData)
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
            "Tests that run on local containers supporting WAR deployments");

        suite.addTestSuite(WarCapabilityContainerTest.class, new Validator[] {
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator(),
            new HasWarSupportValidator()});
        return suite;
    }

    /**
     * Test deployment of a WAR with relative path.
     * @throws Exception If anything goes wrong.
     */
    public void testDeployWarDefinedWithRelativePath() throws Exception
    {
        // Copies the testdata artifact
        File artifactDir = new File(getTestData().targetDir).getParentFile();
        File artifactFile = new File(artifactDir, "simple.war").getAbsoluteFile();
        Copy copyTask = (Copy) new AntUtils().createProject().createTask("copy");
        copyTask.setTofile(artifactFile);
        copyTask.setFile(new File(getTestData().getTestDataFileFor("simple-war")));
        copyTask.execute();

        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            artifactFile.getAbsolutePath(), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/simple/index.jsp");

        startAndStop(warPingURL);
    }

}
