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

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;

/**
 * Abstract test case for container with WAR capabilities.
 */
public abstract class AbstractWarCapabilityContainerTestCase extends AbstractWarTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public AbstractWarCapabilityContainerTestCase(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    /**
     * Deploy WAR statically.
     * @throws Exception If anything goes wrong.
     */
    public void testDeployWarStatically() throws Exception
    {
        testWar("simple");
    }

    /**
     * Test start with one expanded WAR.
     * @throws Exception If anything goes wrong.
     */
    public void testStartWithOneExpandedWarDeployed() throws Exception
    {
        // The Apache Geronimo server doesn't support expanded WARs
        if (getContainer().getId().startsWith("geronimo"))
        {
            return;
        }

        String expandedWarDirectory = getFileHandler().append(
            getFileHandler().getParent(getTestData().configurationHome), "expanded-war");
        getFileHandler().explode(getTestData().getTestDataFileFor("expanded-war"),
            expandedWarDirectory);

        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            expandedWarDirectory, DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port
            + "/expanded-war" + "/index.html");

        startAndStop(warPingURL);
    }
}
