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
package org.codehaus.cargo.sample.java.jetty;

import java.io.File;
import java.net.URL;

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.sample.java.AbstractWarTestCase;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.PingUtils;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.SupportsPropertyValidator;

/**
 * Test for Jetty TLS configuration options.
 */
public class JettyTLSTest extends AbstractWarTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public JettyTLSTest()
    {
        super();
        this.addValidator(new StartsWithContainerValidator("jetty"));
        this.addValidator(new SupportsPropertyValidator(
            ConfigurationType.STANDALONE, JettyPropertySet.CONNECTOR_HTTPS_PORT));
    }

    /**
     * Configure Jetty for HTTPS.
     * @throws Exception If anything goes wrong.
     */
    protected void configureHttps() throws Exception
    {
        File localhostJksFile = new File("target/test-classes/localhost.jks");
        Assertions.assertTrue(localhostJksFile.isFile());

        LocalConfiguration configuration = getLocalContainer().getConfiguration();
        configuration.setProperty(JettyPropertySet.CONNECTOR_KEY_STORE_FILE,
            localhostJksFile.getAbsolutePath());
        configuration.setProperty(JettyPropertySet.CONNECTOR_KEY_STORE_PASSWORD, "password");
        configuration.setProperty(JettyPropertySet.CONNECTOR_KEY_STORE_TYPE, "jks");
    }

    /**
     * Test Jetty with both HTTP and HTTPS ports active.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testJettyWithHttpAndHttps() throws Exception
    {
        configureHttps();

        LocalConfiguration configuration = getLocalContainer().getConfiguration();
        configuration.setProperty(JettyPropertySet.MODULES, configuration.getPropertyValue(
            JettyPropertySet.MODULES).replace("http,", "http,https,"));

        WAR war = (WAR) this.createDeployableFromTestdataFile("simple-war", DeployableType.WAR);
        configuration.addDeployable(war);

        URL warHttpPingURL =
            new URL("http://localhost:" + getTestData().port + "/simple-war/index.jsp");
        URL warHttpsPingURL = new URL("https://localhost:"
            + configuration.getPropertyValue(JettyPropertySet.CONNECTOR_HTTPS_PORT)
                + "/simple-war/index.jsp");

        getLocalContainer().start();
        PingUtils.assertPingTrue("simple war not started on HTTP", warHttpPingURL, getLogger());
        PingUtils.assertPingTrue("simple war not started on HTTPS", warHttpsPingURL, getLogger());
        getLocalContainer().stop();
        PingUtils.assertPingFalse("simple war not stopped on HTTP", warHttpPingURL, getLogger());
        PingUtils.assertPingFalse("simple war not stopped on HTTPS", warHttpsPingURL, getLogger());
    }

    /**
     * Test Jetty with only HTTPS active.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testJettyWithHttpsOnly() throws Exception
    {
        configureHttps();

        LocalConfiguration configuration = getLocalContainer().getConfiguration();
        configuration.setProperty(JettyPropertySet.MODULES, configuration.getPropertyValue(
            JettyPropertySet.MODULES).replace("http,", "https,"));
        configuration.setProperty(GeneralPropertySet.PROTOCOL, "https");
        configuration.setProperty(JettyPropertySet.CONNECTOR_HTTPS_PORT, "" + getTestData().port);

        testWar("simple");
    }
}
