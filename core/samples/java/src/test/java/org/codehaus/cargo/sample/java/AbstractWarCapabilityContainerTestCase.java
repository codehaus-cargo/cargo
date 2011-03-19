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

public abstract class AbstractWarCapabilityContainerTestCase extends AbstractCargoTestCase
{
    public AbstractWarCapabilityContainerTestCase(String testName, EnvironmentTestData testData)
        throws Exception
    {
        super(testName, testData);
    }

    /**
     * make sure we always stop the container
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

    public void testDeployWarStatically() throws Exception
    {
        Deployable war = new DefaultDeployableFactory().createDeployable(getContainer().getId(),
            getTestData().getTestDataFileFor("simple-war"), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL = new URL("http://localhost:" + getTestData().port
            + "/simple-war-" + getTestData().version + "/index.jsp");

        startAndStop(warPingURL);

    }

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
     * @param warPingURL
     */
    public void startAndStop(URL warPingURL)
    {
        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }
}
