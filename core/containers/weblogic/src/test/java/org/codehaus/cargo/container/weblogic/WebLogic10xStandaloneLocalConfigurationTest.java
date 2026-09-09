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
package org.codehaus.cargo.container.weblogic;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;

import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.weblogic.internal.AbstractWebLogicInstalledLocalContainer;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;

/**
 * Unit tests for {@link WebLogic10xStandaloneLocalConfiguration}.
 */
public class WebLogic10xStandaloneLocalConfigurationTest
{
    /**
     * BEA_HOME
     */
    protected static final String BEA_HOME = "ram:/bea";

    /**
     * DOMAIN_HOME
     */
    protected static final String DOMAIN_HOME = BEA_HOME + "/mydomain";

    /**
     * WL_HOME
     */
    private static final String WL_HOME = BEA_HOME + "/weblogic10";

    /**
     * Default port.
     */
    private static final String PORT = "7001";

    /**
     * Configuration version.
     */
    private static final String CONFIGURATION_VERSION = "10.0.9.0";

    /**
     * Domain version.
     */
    private static final String DOMAIN_VERSION = "10.0.9.1";

    /**
     * Server name.
     */
    private static final String SERVER = "myserver";

    /**
     * Container.
     */
    protected AbstractWebLogicInstalledLocalContainer container;

    /**
     * Configuration.
     */
    protected WebLogic10xStandaloneLocalConfiguration configuration;

    /**
     * File handler.
     */
    protected FileHandler fileHandler;

    /**
     * XPath engine.
     */
    protected XPathEngine xpathEngine;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * Return given XML as Source. We need this as Source objects are single use.
     * @param xml XML String.
     * @return Source object.
     */
    protected static Source toSource(String xml)
    {
        return new StreamSource(new StringReader(xml));
    }

    /**
     * Creates the test file system manager and the container.
     * @throws Exception If anything goes wrong.
     */
    @BeforeEach
    protected void setUp() throws Exception
    {
        this.xpathEngine = new JAXPXPathEngine();
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("weblogic", "http://www.bea.com/ns/weblogic/920/domain");
        this.xpathEngine.setNamespaceContext(namespaces);

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        this.fileHandler.mkdirs(DOMAIN_HOME);
        this.fileHandler.mkdirs(WL_HOME);
        this.configuration = new WebLogic10xStandaloneLocalConfiguration(DOMAIN_HOME);
        this.configuration.setFileHandler(this.fileHandler);

        this.container = new WebLogic10xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.container.setFileHandler(this.fileHandler);
    }

    /**
     * Closes the test file system manager.
     */
    @AfterEach
    protected void tearDown()
    {
        if (fsManager != null)
        {
            fsManager.close();
        }
    }

    /**
     * Test that all files are created correctly.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureCreatesFiles() throws Exception
    {
        configuration.doConfigure(container);

        Assertions.assertTrue(fileHandler.exists(DOMAIN_HOME + "/config"));
        Assertions.assertTrue(fileHandler.exists(DOMAIN_HOME + "/config/config.xml"));
        Assertions.assertTrue(fileHandler.exists(DOMAIN_HOME + "/security"));
        Assertions.assertTrue(
            fileHandler.exists(DOMAIN_HOME + "/security/DefaultAuthenticatorInit.ldift"));
        Assertions.assertTrue(
            fileHandler.exists(DOMAIN_HOME + "/security/SerializedSystemIni.dat"));
        Assertions.assertTrue(fileHandler.exists(DOMAIN_HOME + "/autodeploy/cargocpc.war"));
    }

    /**
     * Test default values.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testConstructorSetsPropertyDefaults() throws Exception
    {
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.ADMIN_USER), "weblogic");
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.ADMIN_PWD), "weblogic");
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.SERVER), "server");
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.CONFIGURATION_VERSION), "10.0.1.0");
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.DOMAIN_VERSION), "10.0.1.0");
    }

    /**
     * Test required elements.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureCreatesRequiredElements() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(configuration.getPropertyValue(WebLogicPropertySet.DOMAIN_VERSION),
            xpathEngine.evaluate("//weblogic:domain-version", toSource(config)));
    }

    /**
     * Test domain version.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureSetsDefaultDomainVersion() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertTrue(xpathEngine.selectNodes(
            "//weblogic:domain-version", toSource(config)).iterator().hasNext());
        Assertions.assertTrue(xpathEngine.selectNodes(
            "//weblogic:configuration-version", toSource(config)).iterator().hasNext());
        Assertions.assertTrue(xpathEngine.selectNodes(
            "//weblogic:server", toSource(config)).iterator().hasNext());
        Assertions.assertTrue(xpathEngine.selectNodes(
            "//weblogic:server/weblogic:name", toSource(config)).iterator().hasNext());
        Assertions.assertTrue(xpathEngine.selectNodes(
            "//weblogic:security-configuration", toSource(config)).iterator().hasNext());
        Assertions.assertTrue(xpathEngine.selectNodes(
            "//weblogic:security-configuration/weblogic:credential-encrypted",
                toSource(config)).iterator().hasNext());
        Assertions.assertTrue(xpathEngine.selectNodes(
            "//weblogic:embedded-ldap", toSource(config)).iterator().hasNext());
        Assertions.assertTrue(xpathEngine.selectNodes(
            "//weblogic:embedded-ldap/weblogic:credential-encrypted",
                toSource(config)).iterator().hasNext());
        Assertions.assertTrue(xpathEngine.selectNodes(
            "//weblogic:admin-server-name", toSource(config)).iterator().hasNext());
    }

    /**
     * Test changing domain version.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureSetsDomainVersion() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.DOMAIN_VERSION, DOMAIN_VERSION);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(DOMAIN_VERSION, xpathEngine.evaluate(
            "//weblogic:domain-version", toSource(config)));
    }

    /**
     * Test configuration version.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureSetsDefaultConfigurationVersion() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.CONFIGURATION_VERSION),
                xpathEngine.evaluate("//weblogic:configuration-version", toSource(config)));
    }

    /**
     * Test changing configuration version.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureSetsConfigurationVersion() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.CONFIGURATION_VERSION,
            CONFIGURATION_VERSION);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(CONFIGURATION_VERSION, xpathEngine.evaluate(
            "//weblogic:configuration-version", toSource(config)));
    }

    /**
     * Test default admin server.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureSetsDefaultAdminServer() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(configuration.getPropertyValue(WebLogicPropertySet.SERVER),
            xpathEngine.evaluate("//weblogic:admin-server-name", toSource(config)));
    }

    /**
     * Test changed admin server.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureSetsAdminServer() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.SERVER, SERVER);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(SERVER, xpathEngine.evaluate(
            "//weblogic:admin-server-name", toSource(config)));
    }

    /**
     * Test default port.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureSetsDefaultPort() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(configuration.getPropertyValue(ServletPropertySet.PORT),
            xpathEngine.evaluate("//weblogic:listen-port", toSource(config)));
    }

    /**
     * Test changed port.
     * @throws Exception If anything goes wrong.
     */
    @Test
    public void testDoConfigureSetsPort() throws Exception
    {
        configuration.setProperty(ServletPropertySet.PORT, PORT);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(PORT, xpathEngine.evaluate(
            "//weblogic:listen-port", toSource(config)));
    }

}
