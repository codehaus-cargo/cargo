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

import org.apache.tools.ant.taskdefs.Expand;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.util.AntUtils;

/**
 * Abstract test case for container with WAR capabilities.
 * 
 * @version $Id$
 */
public abstract class AbstractWarCapabilityContainerTestCase extends AbstractCargoTestCase
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
     * {@inheritDoc}
     */
    @Override
    public void tearDown()
    {
        try
        {
            getLocalContainer().stop();
        }
        finally
        {
            super.tearDown();
        }
    }

    /**
     * Deploy WAR statically.
     * @throws Exception If anything goes wrong.
     */
    public void testDeployWarStatically() throws Exception
    {
        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-war"), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port
            + "/simple-war-" + getTestData().version + "/index.jsp");

        startAndStop(warPingURL);
    }

    /**
     * Test start with one expanded WAR.
     * @throws Exception If anything goes wrong.
     */
    public void testStartWithOneExpandedWarDeployed() throws Exception
    {
        if (getContainer().getId().startsWith("geronimo"))
        {
            // The Apache Geronimo server doesn't support expanded WARs
            return;
        }

        // Copy the war from the Maven local repository in order to expand it
        File artifactDir = new File(getTestData().targetDir).getParentFile();
        Expand expandTask = (Expand) new AntUtils().createProject().createTask("unwar");
        expandTask.setDest(new File(artifactDir, "expanded-war"));
        expandTask.setSrc(new File(getTestData().getTestDataFileFor("expanded-war")));
        expandTask.execute();

        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            new File(artifactDir, "expanded-war").getPath(), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port
            + "/expanded-war" + "/index.html");

        startAndStop(warPingURL);
    }

    /**
     * Start, test and stop WAR.
     * @param warPingURL WAR ping URL.
     * @throws Exception If anything goes wrong.
     */
    public void startAndStop(URL warPingURL) throws Exception
    {
        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", warPingURL, getLogger());

        if ("jboss71x".equals(this.getContainer().getId()))
        {
            // Some versions of JBoss 7.1.x need a short rest before getting shut down
            Thread.sleep(2000);
            System.gc();
            Thread.sleep(3000);
            System.gc();
        }

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }
}
