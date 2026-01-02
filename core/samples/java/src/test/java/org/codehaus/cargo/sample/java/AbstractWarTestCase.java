/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.sample.java.validator.HasWarSupportValidator;

/**
 * Abstract test case for testing WARs on a container.
 */
public abstract class AbstractWarTestCase extends AbstractStandaloneLocalContainerTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public AbstractWarTestCase()
    {
        this.addValidator(new HasWarSupportValidator());
    }

    /**
     * Tests servlet.
     * @param type WAR type.
     * @param expectedMessage Expected message when WAR URL pinged successfully
     * @throws MalformedURLException If URL cannot be built.
     */
    protected void testWar(String type, String expectedMessage) throws MalformedURLException
    {
        WAR war = (WAR) this.createDeployableFromTestdataFile(type + "-war", DeployableType.WAR);

        String page;
        if ("simple".equals(type))
        {
            page = "index.jsp";
        }
        else
        {
            page = "test";
        }

        LocalConfiguration configuration = getLocalContainer().getConfiguration();
        configuration.addDeployable(war);
        URL warPingURL = new URL(configuration.getPropertyValue(GeneralPropertySet.PROTOCOL)
            + "://localhost:" + getTestData().port + "/" + type + "-war/" + page);

        startAndStop(warPingURL, expectedMessage);
    }

    /**
     * Start, test and stop WAR.
     * @param warPingURL WAR ping URL.
     * @param expectedMessage Expected message when WAR URL pinged successfully
     */
    protected void startAndStop(URL warPingURL, String expectedMessage)
    {
        getLocalContainer().start();
        PingUtils.assertPingTrue(
            warPingURL.getPath() + " not started", expectedMessage, warPingURL, getLogger());

        getLocalContainer().stop();
        PingUtils.assertPingFalse(warPingURL.getPath() + " not stopped", warPingURL, getLogger());
    }
}
