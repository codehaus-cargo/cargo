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
package org.codehaus.cargo.sample.java.tomcat;

import junit.framework.Test;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Expand;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.sample.java.AbstractCargoTestCase;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.util.AntUtils;

import java.io.File;
import java.net.URL;

public class TomcatWarTest extends AbstractCargoTestCase
{   
    public TomcatWarTest(String testName, EnvironmentTestData testData) throws Exception
    {
        super(testName, testData);
    }

    public static Test suite() throws Exception
    {
        CargoTestSuite suite = new CargoTestSuite(
            "Tests that can run on Tomcat containers supporting META-INF/context.xml files");
        suite.addTestSuite(TomcatWarTest.class, new Validator[] {
            new StartsWithContainerValidator("tomcat5x"),
            new IsLocalContainerValidator(),
            new HasStandaloneConfigurationValidator()});
        return suite;
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    public void testWarWithContextXmlFile() throws Exception
    {
        // Copies the tomcat context war in order to rename it so that it matches the context
        // path defined in its context.xml file.
        File artifactDir = new File(getTestData().targetDir).getParentFile();
        Copy copyTask = (Copy) new AntUtils().createProject().createTask("copy");
        copyTask.setTofile(new File(artifactDir, "tomcat-context.war"));
        copyTask.setFile(new File(getTestData().getTestDataFileFor("tomcatcontext-war")));
        copyTask.execute();
        
        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            new File(artifactDir,"tomcat-context.war").getPath(), DeployableType.WAR);
            
        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/tomcat-context/");

        getLocalContainer().start();
        PingUtils.assertPingTrue("tomcat context war not started", "Test value is [test value]", 
            warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse("tomcat context war not stopped", warPingURL, getLogger());
    }
    
    public void testExpandedWarWithContextXmlFile() throws Exception
    {
        // Copy the war from the Maven local repository in order to expand it
        File artifactDir = new File(getTestData().targetDir).getParentFile();
        Expand expandTask = (Expand) new AntUtils().createProject().createTask("unwar");
        expandTask.setDest(new File(artifactDir, "tomcat-context"));
        expandTask.setSrc(new File(getTestData().getTestDataFileFor("tomcatcontext-war")));
        expandTask.execute();
        
        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            new File(artifactDir,"tomcat-context").getPath(), DeployableType.WAR);
        
        getLocalContainer().getConfiguration().addDeployable(war);
        
        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/tomcat-context/");

        getLocalContainer().start();
        PingUtils.assertPingTrue("tomcat context war not started", "Test value is [test value]", 
            warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse("tomcat context war not stopped", warPingURL, getLogger());
    }
}
