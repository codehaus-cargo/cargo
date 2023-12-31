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

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.custommonkey.xmlunit.XMLAssert;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Tests for the Tomcat 6 implementation of StandaloneLocalConfigurationTest
 */
public class Tomcat6xStandaloneLocalConfigurationTest extends
    Tomcat5xStandaloneLocalConfigurationTest
{

    /**
     * Creates a {@link Tomcat6xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat6xStandaloneLocalConfiguration(home)
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
     * Creates a {@link Tomcat6xInstalledLocalContainer}. {@inheritDoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat6xInstalledLocalContainer(configuration);
    }

    @Override
    protected void setUpManager()
    {
        configuration.getFileHandler().mkdirs(container.getHome() + "/conf");
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps/manager");
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps/host-manager");
    }

    /**
     * note that manager is under webapps, not server/webapps in 5x.
     */
    @Override
    public void testConfigureManager()
    {
        configuration.configure(container);
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/webapps/manager"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/webapps/host-manager"));
    }

    /**
     * Assert that the attribute 'protocol' isn't added if the property isn't set.
     * @throws Exception If anything does wrong.
     */
    public void testConfigureWithoutConnectorProtocol() throws Exception
    {
        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathNotExists(
                "//Server/Service/Connector[@port='8080']/@protocol", config);
    }

    /**
     * Assert that the attribute 'protocol' is overidden with the property's value.
     * @throws Exception If anything does wrong.
     */
    public void testConfigureSetsConnectorProtocol() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.CONNECTOR_PROTOCOL_CLASS,
                "org.apache.coyote.http11.Http11NioProtocol");

        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo("org.apache.coyote.http11.Http11NioProtocol",
                "//Server/Service/Connector[@port='8080']/@protocol", config);
    }

    /**
     * Assert that the attribute 'protocol' is overidden with the property's APR implementation
     * value.
     * @throws Exception If anything does wrong.
     */
    public void testConfigureSetsAprConnectorProtocol() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.CONNECTOR_PROTOCOL_CLASS,
                "org.apache.coyote.http11.Http11AprProtocol");

        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo("org.apache.coyote.http11.Http11AprProtocol",
                "//Server/Service/Connector[@port='8080']/@protocol", config);
    }

    /**
     * Assert that the attribute 'protocol' is overidden with the property's APR implementation
     * value, when setting any Tomcat 5+ optional xml replacements.
     * @throws Exception If anything does wrong.
     */
    public void testConfigurationSetsAprConnectorProtocolWithSslProtocol() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.CONNECTOR_PROTOCOL_CLASS,
                "org.apache.coyote.http11.Http11AprProtocol");
        configuration.setProperty(TomcatPropertySet.CONNECTOR_SSL_PROTOCOL, "TLSv1.2");

        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo("org.apache.coyote.http11.Http11AprProtocol",
                "//Server/Service/Connector[@port='8080']/@protocol", config);
        XMLAssert.assertXpathEvaluatesTo("TLSv1.2",
                "//Server/Service/Connector[@port='8080']/@sslProtocol", config);
    }
}
