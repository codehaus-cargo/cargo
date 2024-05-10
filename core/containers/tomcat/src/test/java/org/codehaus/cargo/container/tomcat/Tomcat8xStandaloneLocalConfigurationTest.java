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
package org.codehaus.cargo.container.tomcat;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.tomcat.internal.Tomcat8x9xConfigurationChecker;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Tests for the Tomcat 8 implementation of StandaloneLocalConfigurationTest
 */
public class Tomcat8xStandaloneLocalConfigurationTest extends
    Tomcat7xStandaloneLocalConfigurationTest
{

    /**
     * Default xpath expression for identifying the HTTP "Connector" element in the server.xml
     * file.
     */
    private static final String CONNECTOR_XPATH =
        "//Server/Service/Connector[not(@protocol) or @protocol='HTTP/1.1' "
            + "or @protocol='org.apache.coyote.http11.Http11Protocol' "
            + "or @protocol='org.apache.coyote.http11.Http11NioProtocol']";

    /**
     * Creates a {@link Tomcat8xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat8xStandaloneLocalConfiguration(home)
        {
            @Override
            protected void setupConfFiles(String confDir)
            {
                setupManager(container);
            }

            @Override
            protected void configureFiles(
                Map<String, String> replacements, LocalContainer container)
            {
                createServerXml();
                super.configureFiles(replacements, container);
            }
        };
    }

    /**
     * Creates a {@link Tomcat8xInstalledLocalContainer}. {@inheritDoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat8xInstalledLocalContainer(configuration);
    }

    /**
     * @return {@link Tomcat8x9xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new Tomcat8x9xConfigurationChecker();
    }

    /**
     * Assert that the attribute 'sslImplementationName' isn't added if the property isn't set.
     *
     * @throws Exception If anything does wrong.
     */
    public void testConfigureWithoutSslImplementationName() throws Exception
    {
        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
            configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathNotExists(
            "//Server/Service/Connector[@port='8080']/@sslImplementationName", config);
    }

    /**
     * Assert that the element 'UpgradeProtocol' isn't present if the property isn't set.
     *
     * @throws Exception If anything does wrong.
     */
    public void testConfigureWithoutHttpUpgradeProtocol() throws Exception
    {
        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
            configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathNotExists(
            "//Server/Service/Connector[@port='8080']/UpgradeProtocol",
                config);
    }

    /**
     * Assert that the element 'UpgradeProtocol' is added if the property is set.
     *
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureAddsHttpUpgradeProtocol() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.CONNECTOR_HTTP_UPGRADE_PROTOCOL, "true");

        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
            configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathExists(
            "//Server/Service/Connector[@port='8080']"
                + "/UpgradeProtocol[@className='org.apache.coyote.http2.Http2Protocol']",
                    config);
    }

    /**
     * Tests that combining changing the connector protocol class with adding the http upgrade
     * element works correctly.
     *
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureAddHttpUpgradeToNio2Protocol() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.CONNECTOR_PROTOCOL_CLASS,
            "org.apache.coyote.http11.Http11Nio2Protocol");
        configuration.setProperty(TomcatPropertySet.CONNECTOR_HTTP_UPGRADE_PROTOCOL, "true");

        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
            configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo("org.apache.coyote.http11.Http11Nio2Protocol",
            "//Server/Service/Connector[@port='8080']/@protocol", config);
        XMLAssert.assertXpathExists(
            "//Server/Service/Connector[@port='8080']"
                + "/UpgradeProtocol[@className='org.apache.coyote.http2.Http2Protocol']",
                    config);
    }
}
