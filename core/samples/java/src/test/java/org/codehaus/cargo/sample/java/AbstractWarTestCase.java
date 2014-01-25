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

import java.net.MalformedURLException;
import java.net.URL;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;

/**
 * Abstract test case for testing WARs on a container.
 * 
 * @version $Id$
 */
public abstract class AbstractWarTestCase extends AbstractCargoTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public AbstractWarTestCase(String testName, EnvironmentTestData testData)
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
     * Tests servlet.
     * @param type WAR type.
     * @throws MalformedURLException If URL cannot be built.
     */
    protected void testWar(String type) throws MalformedURLException
    {
        Deployable war =
            new DefaultDeployableFactory().createDeployable(getContainer().getId(), getTestData()
                .getTestDataFileFor(type + "-war"), DeployableType.WAR);

        getLocalContainer().getConfiguration().addDeployable(war);

        URL warPingURL =
            new URL("http://localhost:" + getTestData().port + "/" + type + "-war/test");

        startAndStop(warPingURL);
    }

    /**
     * Start, test and stop WAR.
     * @param warPingURL WAR ping URL.
     */
    protected void startAndStop(URL warPingURL)
    {
        getLocalContainer().start();
        PingUtils.assertPingTrue(warPingURL.getPath() + " not started", warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }
}
