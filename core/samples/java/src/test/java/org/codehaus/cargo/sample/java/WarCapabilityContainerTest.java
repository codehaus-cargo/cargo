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

import java.io.File;
import java.net.URL;

import junit.framework.Test;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.apache.tools.ant.taskdefs.Copy;

public class WarCapabilityContainerTest extends AbstractWarCapabilityContainerTestCase
{
    public WarCapabilityContainerTest(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

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

    public void testDeployWarDefinedWithRelativePath() throws Exception
    {
        // Copies the testdata artifact so that we can easily specify a relative path
        File artifactDir = new File(getTestData().targetDir).getParentFile();
        Copy copyTask = (Copy) new AntUtils().createProject().createTask("copy");
        copyTask.setTofile(new File(artifactDir, "simple.war"));
        copyTask.setFile(new File(getTestData().getTestDataFileFor("simple-war")));
        copyTask.execute();

        // Compute the relative path so that it works from anywhere where the tests are started
        // from.
        File rootPath = new File("");
        int pos = artifactDir.getCanonicalPath().indexOf(rootPath.getCanonicalPath());
        String relativePath = artifactDir.getCanonicalPath().substring(
            pos + rootPath.getCanonicalPath().length() + 1);

        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            new File(relativePath + "/simple.war").getPath(), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/simple/index.jsp");

        startAndStop(warPingURL);

    }

}
