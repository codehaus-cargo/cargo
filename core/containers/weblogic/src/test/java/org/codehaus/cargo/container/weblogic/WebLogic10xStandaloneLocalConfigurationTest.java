/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.custommonkey.xmlunit.SimpleNamespaceContext;

/**
 * Unit tests for {@link WebLogic9xStandaloneLocalConfiguration}.
 */
public class WebLogic10xStandaloneLocalConfigurationTest extends TestCase
{
    private static final String BEA_HOME = "ram:/bea";

    private static final String DOMAIN_HOME = BEA_HOME + "/mydomain";

    private static final String WL_HOME = BEA_HOME + "/weblogic10";

    private static final String PORT = "7001";

    private static final String CONFIGURATION_VERSION = "10.0.9.0";

    private static final String DOMAIN_VERSION = "10.0.9.1";

    private static final String SERVER = "myserver";

    private WebLogic10xInstalledLocalContainer container;

    private WebLogic10xStandaloneLocalConfiguration configuration;

    private StandardFileSystemManager fsManager;

    private FileHandler fileHandler;

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // setup the namespace of the weblogic config.xml file
        Map m = new HashMap();
        m.put("weblogic", "http://www.bea.com/ns/weblogic/920/domain");
        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        fileHandler.mkdirs(DOMAIN_HOME);
        fileHandler.mkdirs(WL_HOME);
        this.configuration = new WebLogic10xStandaloneLocalConfiguration(DOMAIN_HOME);
        this.configuration.setFileHandler(this.fileHandler);

        this.container = new WebLogic10xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.container.setFileHandler(this.fileHandler);

    }

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

    public void testDoConfigureCreatesRequiredElements() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(DOMAIN_HOME + "/config/config.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(WebLogicPropertySet.DOMAIN_VERSION), "//weblogic:domain-version",
            config);
    }

    
    public void testDoConfigureSetsDefaultDomainVersion() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(DOMAIN_HOME + "/config/config.xml");
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

    public void testDoConfigureSetsDomainVersion() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.DOMAIN_VERSION, DOMAIN_VERSION);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(DOMAIN_HOME + "/config/config.xml");
        XMLAssert.assertXpathEvaluatesTo(DOMAIN_VERSION, "//weblogic:domain-version", config);

    }

    public void testDoConfigureSetsDefaultConfigurationVersion() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(DOMAIN_HOME + "/config/config.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(WebLogicPropertySet.CONFIGURATION_VERSION),
            "//weblogic:configuration-version", config);

    }

    public void testDoConfigureSetsConfigurationVersion() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.CONFIGURATION_VERSION,
            CONFIGURATION_VERSION);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(DOMAIN_HOME + "/config/config.xml");
        XMLAssert.assertXpathEvaluatesTo(CONFIGURATION_VERSION,
            "//weblogic:configuration-version", config);

    }

    public void testDoConfigureSetsDefaultAdminServer() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(DOMAIN_HOME + "/config/config.xml");
        XMLAssert
            .assertXpathEvaluatesTo(configuration.getPropertyValue(WebLogicPropertySet.SERVER),
                "//weblogic:admin-server-name", config);

    }

    public void testDoConfigureSetsAdminServer() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.SERVER, SERVER);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(DOMAIN_HOME + "/config/config.xml");
        XMLAssert.assertXpathEvaluatesTo(SERVER, "//weblogic:admin-server-name", config);

    }

    public void testDoConfigureSetsDefaultServer() throws Exception
    {
        // TODO
    }

    public void testDoConfigureSetsServer() throws Exception
    {
        // TODO
    }

    public void testDoConfigureSetsDefaultPort() throws Exception
    {
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(DOMAIN_HOME + "/config/config.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration.getPropertyValue(ServletPropertySet.PORT),
            "//weblogic:listen-port", config);

    }

    public void testDoConfigureSetsPort() throws Exception
    {
        configuration.setProperty(ServletPropertySet.PORT, PORT);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(DOMAIN_HOME + "/config/config.xml");
        XMLAssert.assertXpathEvaluatesTo(PORT, "//weblogic:listen-port", config);

    }

}
