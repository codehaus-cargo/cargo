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
package org.codehaus.cargo.container.weblogic;

import java.nio.charset.StandardCharsets;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.weblogic.internal.WebLogic8xConfigurationChecker;
import org.codehaus.cargo.util.XmlUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link WebLogic8xStandaloneLocalConfiguration}.
 */
public class WebLogic8xStandaloneLocalConfigurationTest extends
    AbstractWeblogicStandaloneConfigurationTest
{
    /**
     * Creates a {@link WebLogic8xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new WebLogic8xStandaloneLocalConfiguration(home);
    }

    /**
     * Creates a {@link WebLogic8xInstalledLocalContainer}. {@inheritDoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new WebLogic8xInstalledLocalContainer(configuration);
    }

    /**
     * @return {@link WebLogic8xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new WebLogic8xConfigurationChecker("server");
    }

    /**
     * {@inheritDoc}
     * @param fixture Ignored.
     * @return <code>config.xml</code> file in the configuration directory.
     */
    @Override
    protected String getDataSourceConfigurationFile(DataSourceFixture fixture)
    {
        return configuration.getHome() + "/config.xml";
    }

    /**
     * Test configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigure() throws Exception
    {
        configuration.configure(container);

        assertTrue(configuration.getFileHandler().exists(configuration.getHome() + "/config.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/DefaultAuthenticatorInit.ldift"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/applications/cargocpc.war"));
    }

    /**
     * Test default port.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultPort() throws Exception
    {
        configuration.configure(container);
        String config = configuration.getFileHandler().readTextFile(
            configuration.getHome() + "/config.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo(configuration.getPropertyValue(ServletPropertySet.PORT),
            "//Server/@ListenPort", config);
    }

    /**
     * Test changed port.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsPort() throws Exception
    {
        configuration.setProperty(ServletPropertySet.PORT, "123");
        configuration.configure(container);
        String config = configuration.getFileHandler().readTextFile(
            configuration.getHome() + "/config.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo("123", "//Server/@ListenPort", config);
    }

    /**
     * Test WAR creation.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureCreatesWar() throws Exception
    {
        configuration.addDeployable(new WAR("my.war"));
        configuration.configure(container);
        String config = configuration.getFileHandler().readTextFile(
            configuration.getHome() + "/config.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo("my.war", "//WebAppComponent/@URI", config);
    }

    /**
     * Test address.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultAddress() throws Exception
    {
        configuration.configure(container);
        String config = configuration.getFileHandler().readTextFile(
            configuration.getHome() + "/config.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(GeneralPropertySet.HOSTNAME), "//Server/@ListenAddress", config);
    }

    /**
     * Test changed address.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsAddress() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        configuration.configure(container);
        String config = configuration.getFileHandler().readTextFile(
            configuration.getHome() + "/config.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo("localhost", "//Server/@ListenAddress", config);
    }

    /**
     * {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUpDataSourceFile() throws Exception
    {
        XmlUtils xmlUtil = new XmlUtils(getFileHandler());
        String file = configuration.getHome() + "/config.xml";
        Document document = xmlUtil.createDocument();
        document.appendChild(document.createElement("Domain"));
        xmlUtil.saveXml(document, file);
    }

}
