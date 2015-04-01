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

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

/**
 * Unit tests for {@link WebLogic10xStandaloneLocalConfiguration}.
 * 
 */
public class WebLogic10xStandaloneLocalConfigurationTest extends TestCase
{
    /**
     * BEA_HOME
     */
    private static final String BEA_HOME = "ram:/bea";

    /**
     * DOMAIN_HOME
     */
    private static final String DOMAIN_HOME = BEA_HOME + "/mydomain";

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
    private WebLogic10xInstalledLocalContainer container;

    /**
     * Configuration.
     */
    private WebLogic10xStandaloneLocalConfiguration configuration;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * Creates the test file system manager and the container. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // setup the namespace of the weblogic config.xml file
        Map<String, String> m = new HashMap<String, String>();
        m.put("weblogic", "http://www.bea.com/ns/weblogic/920/domain");
        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);

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
     * Closes the test file system manager. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void tearDown() throws Exception
    {
        if (fsManager != null)
        {
            fsManager.close();
        }

        super.tearDown();
    }

    /**
     * Test that all files are created correctly.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureCreatesFiles() throws Exception
    {
        configuration.doConfigure(container);

        assertTrue(fileHandler.exists(DOMAIN_HOME + "/config"));
        assertTrue(fileHandler.exists(DOMAIN_HOME + "/config/config.xml"));
        assertTrue(fileHandler.exists(DOMAIN_HOME + "/security"));
        assertTrue(fileHandler.exists(DOMAIN_HOME + "/security/DefaultAuthenticatorInit.ldift"));
        assertTrue(fileHandler.exists(DOMAIN_HOME + "/security/SerializedSystemIni.dat"));
        assertTrue(fileHandler.exists(DOMAIN_HOME + "/autodeploy/cargocpc.war"));
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
            "10.0.1.0");
        assertEquals(configuration.getPropertyValue(WebLogicPropertySet.DOMAIN_VERSION),
            "10.0.1.0");
    }

    /**
     * Test required elements.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureCreatesRequiredElements() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", "UTF-8");
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
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", "UTF-8");
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
        configuration.setProperty(WebLogicPropertySet.DOMAIN_VERSION, DOMAIN_VERSION);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(DOMAIN_VERSION, "//weblogic:domain-version", config);
    }

    /**
     * Test configuration version.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultConfigurationVersion() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", "UTF-8");
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
        configuration.setProperty(WebLogicPropertySet.CONFIGURATION_VERSION,
            CONFIGURATION_VERSION);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(CONFIGURATION_VERSION,
            "//weblogic:configuration-version", config);
    }

    /**
     * Test default admin server.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultAdminServer() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", "UTF-8");
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
        configuration.setProperty(WebLogicPropertySet.SERVER, SERVER);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(SERVER, "//weblogic:admin-server-name", config);
    }

    /**
     * Test default port.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsDefaultPort() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(configuration.getPropertyValue(ServletPropertySet.PORT),
            "//weblogic:listen-port", config);
    }

    /**
     * Test changed port.
     * @throws Exception If anything goes wrong.
     */
    public void testDoConfigureSetsPort() throws Exception
    {
        configuration.setProperty(ServletPropertySet.PORT, PORT);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(PORT, "//weblogic:listen-port", config);
    }

}
