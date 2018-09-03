/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2018 Ali Tokmen.
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

import java.io.File;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Expand;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.AbstractCargoTestCase;
import org.codehaus.cargo.sample.java.CargoTestSuite;
import org.codehaus.cargo.sample.java.EnvironmentTestData;
import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;
import org.codehaus.cargo.util.AntUtils;
import org.codehaus.cargo.util.CargoException;

import junit.framework.Test;

/**
 * Test for WAR extra classpath support with a webapp who contains META-INF/context.xml file.
 */
public class WarExtraClasspathWithContextTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * 
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public WarExtraClasspathWithContextTest(String testName, EnvironmentTestData testData)
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
     * 
     * @return Test suite.
     * @throws Exception If anything goes wrong.
     */
    public static Test suite() throws Exception
    {
        CargoTestSuite suite =
            new CargoTestSuite("Tests that run on local containers to test extra classpath with "
                + " META-INF/context.xml file");

        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("tomcat4x");
        excludedContainerIds.add("tomcat5x");
        suite.addTestSuite(WarExtraClasspathWithContextTest.class, new Validator[] {
            new StartsWithContainerValidator("tomcat", "tomee"), 
            new HasWarSupportValidator(), new IsInstalledLocalContainerValidator(),
            new HasStandaloneConfigurationValidator()},
            excludedContainerIds);
        return suite;
    }
    
    /**
     * Tests that a servlet has access to a class in added to the extraclasspath
     * with WAR with a <code>context.xml</code> file.
     * @throws Exception If anything goes wrong.
     */
    public void testLoadClassOnWarWithContextXmlFile() throws Exception
    {
        // Copies the tomcat context war in order to rename it so that it matches the context
        // path defined in its context.xml file.
        File artifactDir = new File(getTestData().targetDir).getParentFile();
        Copy copyTask = (Copy) new AntUtils().createProject().createTask("copy");
        copyTask.setTofile(new File(artifactDir, "tomcat-context.war"));
        copyTask.setFile(new File(getTestData().getTestDataFileFor("tomcatcontext-war")));
        copyTask.execute();
        
        String simpleJar = System.getProperty("cargo.testdata.simple-jar");
        if (simpleJar == null)
        {
            throw new CargoException("Please set property [cargo.testdata.simple-jar] to a valid "
                + "location of simple-jar");
        }

        WAR war = (WAR) new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            new File(artifactDir, "tomcat-context.war").getPath(), DeployableType.WAR);
        war.setExtraClasspath(new String[] {simpleJar});

        getLocalContainer().getConfiguration().addDeployable(war);
        getLocalContainer().getConfiguration().setProperty(TomcatPropertySet.COPY_WARS, "false");

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/tomcat-context/");

        getLocalContainer().start();
        PingUtils.assertPingTrue("tomcat context war not started", "Test value is [test value]",
            warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse("tomcat context war not stopped", warPingURL, getLogger());
    }
    
    /**
     * Tests that a servlet has access to a class in added to the extraclasspath
     * with expanded WAR with a <code>context.xml</code> file.
     * @throws Exception If anything goes wrong.
     */
    public void testLoadClassOnExpandedWarWithContextXmlFile() throws Exception
    {
        // Copy the war from the Maven local repository in order to expand it
        File artifactDir = new File(getTestData().targetDir).getParentFile();
        Expand expandTask = (Expand) new AntUtils().createProject().createTask("unwar");
        expandTask.setDest(new File(artifactDir, "tomcat-context"));
        expandTask.setSrc(new File(getTestData().getTestDataFileFor("tomcatcontext-war-link-simple"
                + "-jar")));
        expandTask.execute();
        
        String simpleJar = System.getProperty("cargo.testdata.simple-jar");
        if (simpleJar == null)
        {
            throw new CargoException("Please set property [cargo.testdata.simple-jar] to a valid "
                + "location of simple-jar");
        }

        WAR war = (WAR) new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            new File(artifactDir, "tomcat-context").getPath(), DeployableType.WAR);
        war.setExtraClasspath(new String[] {simpleJar});

        getLocalContainer().getConfiguration().addDeployable(war);
        getLocalContainer().getConfiguration().setProperty(TomcatPropertySet.COPY_WARS, "false");

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/tomcat-context/");

        getLocalContainer().start();
        PingUtils.assertPingTrue("tomcat context war not started", "Test value is [test value]",
            warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse("tomcat context war not stopped", warPingURL, getLogger());
    }


}
