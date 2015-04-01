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
package org.codehaus.cargo.container.weblogic;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.LoggingLevel;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.weblogic.internal.WebLogic9x10x103x12xConfigurationChecker;
import org.codehaus.cargo.util.Dom4JUtil;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

/**
 * Unit tests for {@link WebLogic9xStandaloneLocalConfiguration}.
 * 
 */
public class WebLogic9xStandaloneLocalConfigurationTest extends
    AbstractWeblogicStandaloneConfigurationTest
{

    /**
     * Document checked.
     */
    private Document document;

    /**
     * XML domain.
     */
    private Element domain;

    /**
     * XML utilities.
     */
    private Dom4JUtil xmlUtil;

    /**
     * Creates a {@link WebLogic9xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new WebLogic9xStandaloneLocalConfiguration(home);
    }

    /**
     * Creates a {@link WebLogic9xInstalledLocalContainer}. {@inheritDoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new WebLogic9xInstalledLocalContainer(configuration);
    }

    /**
     * @return {@link WebLogic9x10x103x12xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new WebLogic9x10x103x12xConfigurationChecker("server");
    }

    /**
     * {@inheritDoc}
     * @param ds Datasource.
     * @return Datasource configuration file with <code>ds</code>.
     */
    @Override
    protected String getDataSourceConfigurationFile(DataSourceFixture ds)
    {
        return configuration.getHome() + "/config/jdbc/" + ds.buildDataSource().getId()
            + "-jdbc.xml";
    }

    /**
     * Initialize all XML tools. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        Map<String, String> m = new HashMap<String, String>();
        m.put("weblogic", "http://www.bea.com/ns/weblogic/920/domain");
        m.put("jdbc", "http://www.bea.com/ns/weblogic/90");
        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);

        this.xmlUtil = new Dom4JUtil(getFileHandler());
        this.document = DocumentHelper.createDocument();
        this.domain = document.addElement("domain");
        this.document.setRootElement(domain);
        this.domain.addNamespace("", "http://www.bea.com/ns/weblogic/920/domain");
        QName configurationVersionQ =
            new QName("configuration-version", new Namespace("",
                "http://www.bea.com/ns/weblogic/920/domain"));
        this.domain.addElement(configurationVersionQ);
        QName adminServerNameQ =
            new QName("admin-server-name", new Namespace("",
                "http://www.bea.com/ns/weblogic/920/domain"));
        this.domain.addElement(adminServerNameQ);
    }

    /**
     * Call parent and check link in configuration XML. {@inheritDoc}
     * @param fixture Datasource fixture.
     * @return Configured datasource.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureDataSourceViaPropertyAndRetrieveConfigurationFile(
        DataSourceFixture fixture) throws Exception
    {
        String toReturn =
            super.configureDataSourceViaPropertyAndRetrieveConfigurationFile(fixture);
        checkLinkToDataSourceInConfigXml(fixture);
        return toReturn;
    }

    /**
     * Call parent and check link in configuration XML. {@inheritDoc}
     * @param fixture Datasource fixture.
     * @return Configured datasource.
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected String configureDataSourceAndRetrieveConfigurationFile(DataSourceFixture fixture)
        throws Exception
    {
        String toReturn = super.configureDataSourceAndRetrieveConfigurationFile(fixture);
        checkLinkToDataSourceInConfigXml(fixture);
        return toReturn;
    }

    /**
     * Check link in configuration XML.
     * @param fixture Datasource fixture.
     * @throws Exception If anything goes wrong.
     */
    private void checkLinkToDataSourceInConfigXml(DataSourceFixture fixture) throws Exception
    {
        String domainXml =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(fixture.buildDataSource().getId(),
            "//weblogic:jdbc-system-resource/weblogic:name", domainXml);
        XMLAssert.assertXpathEvaluatesTo("server",
            "//weblogic:jdbc-system-resource/weblogic:target", domainXml);
    }

    /**
     * {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUpDataSourceFile() throws Exception
    {
        configuration.getFileHandler().mkdirs(configuration.getHome() + "/config/jdbc");
        String file = configuration.getHome() + "/config/config.xml";
        xmlUtil.saveXml(document, file);
    }

    /**
     * Test file creation.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureCreatesFiles() throws Exception
    {
        configuration.configure(container);

        assertTrue(configuration.getFileHandler().exists(configuration.getHome() + "/config"));
        assertTrue(configuration.getFileHandler()
            .exists(configuration.getHome() + "/config/jdbc"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/config/config.xml"));
        assertTrue(configuration.getFileHandler().exists(configuration.getHome() + "/security"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/security/DefaultAuthenticatorInit.ldift"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/security/SerializedSystemIni.dat"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/autodeploy/cargocpc.war"));
    }

    /**
     * Test default values.
     * @throws Exception If anything goes wrong.
     */
    public void testConstructorSetsPropertyDefaults() throws Exception
    {
        assertEquals(configuration.getPropertyValue(WebLogicPropertySet.ADMIN_USER), "weblogic");
        assertEquals(configuration.getPropertyValue(WebLogicPropertySet.ADMIN_PWD), "weblogic");
        assertEquals(configuration.getPropertyValue(WebLogicPropertySet.SERVER), "server");
        assertEquals(configuration.getPropertyValue(WebLogicPropertySet.CONFIGURATION_VERSION),
            "9.2.3.0");
        assertEquals(configuration.getPropertyValue(WebLogicPropertySet.DOMAIN_VERSION),
            "9.2.3.0");
    }

    /**
     * Test required elements.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureCreatesRequiredElements() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(WebLogicPropertySet.DOMAIN_VERSION), "//weblogic:domain-version",
            config);
    }

    /**
     * Test domain version.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultDomainVersion() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathExists("//weblogic:domain-version", config);
        XMLAssert.assertXpathExists("//weblogic:configuration-version", config);
        XMLAssert.assertXpathExists("//weblogic:server", config);
        XMLAssert.assertXpathExists("//weblogic:server/weblogic:name", config);
        XMLAssert.assertXpathExists("//weblogic:security-configuration", config);
        XMLAssert.assertXpathExists(
            "//weblogic:security-configuration/weblogic:credential-encrypted", config);
        XMLAssert.assertXpathExists("//weblogic:embedded-ldap", config);
        XMLAssert.assertXpathExists("//weblogic:embedded-ldap/weblogic:credential-encrypted",
            config);
        XMLAssert.assertXpathExists("//weblogic:admin-server-name", config);
    }

    /**
     * Test changing domain version.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDomainVersion() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.DOMAIN_VERSION, "1.2.2.1");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("1.2.2.1", "//weblogic:domain-version", config);
    }

    /**
     * Test configuration version.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultConfigurationVersion() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(WebLogicPropertySet.CONFIGURATION_VERSION),
            "//weblogic:configuration-version", config);
    }

    /**
     * Test changing configuration version.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsConfigurationVersion() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.CONFIGURATION_VERSION, "1.2.2.1");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("1.2.2.1", "//weblogic:configuration-version", config);
    }

    /**
     * Test default admin server.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultAdminServer() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert
            .assertXpathEvaluatesTo(configuration.getPropertyValue(WebLogicPropertySet.SERVER),
                "//weblogic:admin-server-name", config);
    }

    /**
     * Test changed admin server.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsAdminServer() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.SERVER, "asda");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("asda", "//weblogic:admin-server-name", config);
    }

    /**
     * Test default port.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultPort() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(configuration.getPropertyValue(ServletPropertySet.PORT),
            "//weblogic:listen-port", config);
    }

    /**
     * Test changed port.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsPort() throws Exception
    {
        configuration.setProperty(ServletPropertySet.PORT, "1001");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("1001", "//weblogic:listen-port", config);
    }

    /**
     * Test log levels.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultLogging() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("Info", "//weblogic:log-file-severity", config);
    }

    /**
     * Test changed log levels.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsHighLogging() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.LOGGING, LoggingLevel.HIGH.getLevel());
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("Debug", "//weblogic:log-file-severity", config);
    }

    /**
     * Test changed log levels.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsMediumLogging() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.LOGGING, LoggingLevel.MEDIUM.getLevel());
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("Info", "//weblogic:log-file-severity", config);
    }

    /**
     * Test changed log levels.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsLowLogging() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.LOGGING, LoggingLevel.LOW.getLevel());
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("Warning", "//weblogic:log-file-severity", config);
    }

    /**
     * Test address.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultAddress() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(GeneralPropertySet.HOSTNAME), "//weblogic:listen-address", config);
    }

    /**
     * Test changed address.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsAddress() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.HOSTNAME, "loc");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("loc", "//weblogic:listen-address", config);
    }

}
