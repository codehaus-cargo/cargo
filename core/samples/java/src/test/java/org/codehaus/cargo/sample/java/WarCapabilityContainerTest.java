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

import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Test for WAR support.
 */
public class WarCapabilityContainerTest extends AbstractWarCapabilityContainerTestCase
{
    /**
     * Test deployment of a WAR with relative path.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testDeployWarDefinedWithRelativePath() throws Exception
    {
        // Copies the testdata artifact, as we want to use a WAR with relative path
        String artifactFile = getFileHandler().append(
            getFileHandler().getParent(getTestData().configurationHome), "simple.war");
        getFileHandler().copyFile(
            getTestData().getTestDataFileFor("simple-war"), artifactFile);

        WAR war = (WAR) this.createDeployable(artifactFile, DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port + "/simple/index.jsp");

        startAndStop(warPingURL, "Sample page for testing");
    }

}
