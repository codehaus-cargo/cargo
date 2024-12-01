/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Test;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.sample.java.validator.HasStandaloneConfigurationValidator;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.IsLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.Validator;

/**
 * Test for WAR support: deployment to a multiple context (with many slashes).
 */
public class WarMultiContextTest extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public WarMultiContextTest(String testName, EnvironmentTestData testData)
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

        // We exclude containers that cannot deploy on a multiple context (with many slashes)
        Set<String> excludedContainerIds = new TreeSet<String>();
        excludedContainerIds.add("geronimo1x");
        excludedContainerIds.add("jboss3x");
        excludedContainerIds.add("jboss4x");
        excludedContainerIds.add("jboss42x");
        excludedContainerIds.add("jboss5x");
        excludedContainerIds.add("jboss51x");
        excludedContainerIds.add("jboss6x");
        excludedContainerIds.add("jboss61x");
        excludedContainerIds.add("jboss7x");
        excludedContainerIds.add("jboss71x");
        excludedContainerIds.add("jboss72x");
        excludedContainerIds.add("jboss73x");
        excludedContainerIds.add("jboss74x");
        excludedContainerIds.add("jboss75x");
        excludedContainerIds.add("jonas4x");
        excludedContainerIds.add("jonas5x");
        excludedContainerIds.add("jrun4x");
        excludedContainerIds.add("oc4j9x");
        excludedContainerIds.add("oc4j10x");
        excludedContainerIds.add("resin3x");
        excludedContainerIds.add("resin31x");
        excludedContainerIds.add("resin4x");
        excludedContainerIds.add("tomcat4x");
        excludedContainerIds.add("weblogic8x");
        excludedContainerIds.add("weblogic9x");
        excludedContainerIds.add("weblogic10x");
        excludedContainerIds.add("weblogic103x");
        excludedContainerIds.add("weblogic12x");
        excludedContainerIds.add("weblogic121x");
        excludedContainerIds.add("weblogic122x");
        excludedContainerIds.add("weblogic14x");
        excludedContainerIds.add("wildfly8x");
        excludedContainerIds.add("wildfly9x");
        excludedContainerIds.add("wildfly10x");
        excludedContainerIds.add("wildfly11x");
        excludedContainerIds.add("wildfly12x");
        excludedContainerIds.add("wildfly13x");
        excludedContainerIds.add("wildfly14x");
        excludedContainerIds.add("wildfly15x");
        excludedContainerIds.add("wildfly16x");
        excludedContainerIds.add("wildfly17x");
        excludedContainerIds.add("wildfly18x");
        excludedContainerIds.add("wildfly19x");
        excludedContainerIds.add("wildfly20x");
        excludedContainerIds.add("wildfly21x");
        excludedContainerIds.add("wildfly22x");
        excludedContainerIds.add("wildfly23x");
        excludedContainerIds.add("wildfly24x");
        excludedContainerIds.add("wildfly25x");
        excludedContainerIds.add("wildfly26x");
        excludedContainerIds.add("wildfly27x");
        excludedContainerIds.add("wildfly28x");
        excludedContainerIds.add("wildfly29x");
        excludedContainerIds.add("wildfly30x");
        excludedContainerIds.add("wildfly31x");
        excludedContainerIds.add("wildfly32x");
        excludedContainerIds.add("wildfly33x");
        excludedContainerIds.add("wildfly34x");

        suite.addTestSuite(WarMultiContextTest.class,
            new Validator[] {
                new IsLocalContainerValidator(),
                new HasStandaloneConfigurationValidator(),
                new HasWarSupportValidator()},
            excludedContainerIds);
        return suite;
    }

    /**
     * Test deployment of a WAR with multi path.
     * @throws Exception If anything goes wrong.
     */
    public void testDeployWarDefinedWithMultipleContextPath() throws Exception
    {
        WAR war = (WAR) new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-war"), DeployableType.WAR);
        war.setContext("/a/b");

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/a/b/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", "Sample page for testing",
            warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }

}
