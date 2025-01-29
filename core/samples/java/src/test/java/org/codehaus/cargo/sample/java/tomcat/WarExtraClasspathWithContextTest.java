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
package org.codehaus.cargo.sample.java.tomcat;

import java.lang.reflect.Method;
import java.net.URL;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.sample.java.AbstractStandaloneLocalContainerTestCase;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.util.CargoException;

/**
 * Test for WAR extra classpath support with a webapp who contains META-INF/context.xml file.
 */
public class WarExtraClasspathWithContextTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * String array holding the classpath for the simple jar file.
     */
    private String[] simpleJarExtraClasspath;

    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public WarExtraClasspathWithContextTest()
    {
        this.addValidator(new HasWarSupportValidator());
        this.addValidator(new StartsWithContainerValidator("tomcat", "tomee"));

        String simpleJar = System.getProperty("cargo.testdata.simple-jar");
        if (simpleJar == null)
        {
            throw new CargoException("Please set property [cargo.testdata.simple-jar] to a valid "
                + "location of simple-jar");
        }
        this.simpleJarExtraClasspath = new String[] {simpleJar};
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

        // Tomcat 4.x and 5.x don't support extra classpath
        return this.isNotContained(containerId,
            "tomcat4x", "tomcat5x");
    }

    /**
     * Tests that a servlet has access to a class in added to the extraclasspath
     * with WAR with a <code>context.xml</code> file.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testLoadClassOnWarWithContextXmlFile() throws Exception
    {
        // Copies the tomcat context war in order to rename it so that it matches the context
        // path defined in its context.xml file.
        String artifactFile = getFileHandler().append(
            getFileHandler().getParent(getTestData().configurationHome), "tomcat-context.war");
        getFileHandler().copyFile(
            getTestData().getTestDataFileFor("tomcatcontext-war"), artifactFile);

        WAR war = (WAR) this.createDeployable(artifactFile, DeployableType.WAR);

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
    @CargoTestCase
    public void testLoadClassOnExpandedWarWithContextXmlFile() throws Exception
    {
        String expandedWarDirectory = getFileHandler().append(
            getFileHandler().getParent(getTestData().configurationHome), "tomcat-context");
        getFileHandler().explode(getTestData().getTestDataFileFor("tomcatcontext-war"),
            expandedWarDirectory);

        WAR war = (WAR) this.createDeployable(expandedWarDirectory, DeployableType.WAR);

        war.setExtraClasspath(this.simpleJarExtraClasspath);

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
