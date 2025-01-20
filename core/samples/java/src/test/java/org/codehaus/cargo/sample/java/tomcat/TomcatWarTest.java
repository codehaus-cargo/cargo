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
import org.codehaus.cargo.sample.java.AbstractStandaloneLocalContainerTestCase;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;

/**
 * Test for Tomcat WARs.
 */
public class TomcatWarTest extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public TomcatWarTest()
    {
        this.addValidator(new HasWarSupportValidator());
        this.addValidator(new StartsWithContainerValidator("tomcat", "tomee"));
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
        // We exclude tomcat4x container as it does not support context.xml files
        return !"tomcat4x".equals(containerId);
    }

    /**
     * Test WAR with a <code>context.xml</code> file.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testWarWithContextXmlFile() throws Exception
    {
        // Copies the tomcat context war in order to rename it so that it matches the context
        // path defined in its context.xml file.
        String artifactFile = getFileHandler().append(
            getFileHandler().getParent(getTestData().configurationHome), "tomcat-context.war");
        getFileHandler().copyFile(
            getTestData().getTestDataFileFor("tomcatcontext-war"), artifactFile);

        WAR war = (WAR) this.createDeployable(artifactFile, DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/tomcat-context/");

        getLocalContainer().start();
        PingUtils.assertPingTrue("tomcat context war not started", "Test value is [test value]",
            warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse("tomcat context war not stopped", warPingURL, getLogger());
    }

    /**
     * Test expanded WAR with a <code>context.xml</code> file.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testExpandedWarWithContextXmlFile() throws Exception
    {
        String expandedWarDirectory = getFileHandler().append(
            getFileHandler().getParent(getTestData().configurationHome), "tomcat-context");
        getFileHandler().explode(getTestData().getTestDataFileFor("tomcatcontext-war"),
            expandedWarDirectory);

        WAR war = (WAR) this.createDeployable(expandedWarDirectory, DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/tomcat-context/");

        getLocalContainer().start();
        PingUtils.assertPingTrue("tomcat context war not started", "Test value is [test value]",
            warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse("tomcat context war not stopped", warPingURL, getLogger());
    }
}
