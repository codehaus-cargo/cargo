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
package org.codehaus.cargo.container.weblogic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableException;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.EJB;
import org.codehaus.cargo.container.deployable.File;
import org.codehaus.cargo.container.deployable.RAR;
import org.codehaus.cargo.container.deployable.SAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.spi.configuration.AbstractLocalConfiguration;
import org.codehaus.cargo.util.XmlUtils;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for {@link WebLogic9x10x12x14xConfigXmlInstalledLocalDeployer}.
 * <p>
 * Note: These tests are using <a href="http://jakarta.apache.org/commons/vfs/">VFS</a> with a <a
 * href="http://jakarta.apache.org/commons/vfs/filesystems.html#ram">RAM file system</a> so that
 * files are only created in memory. This makes is easy to test file-based operations without having
 * to resort to creating files in the file system and deleting them afterwards.
 * </p>
 */
public class WebLogic9x10x12x14xConfigXmlInstalledLocalDeployerTest extends TestCase
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
    private static final String WL_HOME = BEA_HOME + "/weblogic9";

    /**
     * Container.
     */
    private WebLogic9xInstalledLocalContainer container;

    /**
     * Deployer.
     */
    private WebLogic9x10x12x14xConfigXmlInstalledLocalDeployer deployer;

    /**
     * File system manager.
     */
    private StandardFileSystemManager fsManager;

    /**
     * Resource utilities.
     */
    private ResourceUtils resourceUtils;

    /**
     * File handler.
     */
    private FileHandler fileHandler;

    /**
     * XML element for domain.
     */
    private Element domain;

    /**
     * XML utilities.
     */
    private XmlUtils xmlUtil;

    /**
     * XML document.
     */
    private Document document;

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
        m.put("jdbc", "http://www.bea.com/ns/weblogic/90");
        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        this.fileHandler.delete(BEA_HOME);
        this.fileHandler.createDirectory(DOMAIN_HOME, "");
        this.xmlUtil = new XmlUtils(this.fileHandler);

        LocalConfiguration configuration =
            new WebLogic9xStandaloneLocalConfiguration(DOMAIN_HOME);
        this.container = new WebLogic9xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.container.setFileHandler(this.fileHandler);
        this.deployer = new WebLogic9x10x12x14xConfigXmlInstalledLocalDeployer(container);
        this.resourceUtils = new ResourceUtils();
        this.document = xmlUtil.createDocument();
        this.domain = document.createElement("domain");
        domain.setAttribute("xmlns", "http://www.bea.com/ns/weblogic/920/domain");
        domain.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        domain.setAttribute("xsi:schemaLocation", "http://www.bea.com/ns/weblogic/920/domain "
            + "http://www.bea.com/ns/weblogic/920/domain.xsd");
        this.document.appendChild(this.domain);
        Element configurationVersion = document.createElement("configuration-version");
        this.domain.appendChild(configurationVersion);
        Element adminServerName = document.createElement("admin-server-name");
        this.domain.appendChild(adminServerName);
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
     * Test WAR in config.xml.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigWar() throws Exception
    {
        WAR war = createWar();
        deployer.addDeployableToDomain(war, this.domain);
        String xml = this.xmlUtil.toString(domain);
        try
        {
            XMLAssert.assertXpathEvaluatesTo("cargo.war",
                "//weblogic:app-deployment/weblogic:name", xml);
            XMLAssert.assertXpathEvaluatesTo(deployer.getAbsolutePath(war),
                "//weblogic:app-deployment/weblogic:source-path", xml);
        }
        catch (Throwable t)
        {
            XMLAssert.assertXpathEvaluatesTo("cargo.war",
                "//app-deployment/name", xml);
            XMLAssert.assertXpathEvaluatesTo(deployer.getAbsolutePath(war),
                "//app-deployment/source-path", xml);
        }
    }

    /**
     * Test deployments analysis in config.xml.
     * @throws Exception If anything goes wrong.
     */
    public void testFindAppDeployments() throws Exception
    {
        WAR war = createWar();
        testConfigWar();
        List<Element> l = deployer.selectAppDeployments(war, domain);
        assertEquals(1, l.size());
        deployer.removeDeployableFromDomain(war, domain);
        l = deployer.selectAppDeployments(war, domain);
        assertEquals(0, l.size());
    }

    /**
     * @return WAR file.
     * @throws Exception If anything goes wrong.
     */
    protected WAR createWar() throws Exception
    {
        String sourcePath = this.fileHandler.append(DOMAIN_HOME, "cargocpc.war");
        this.resourceUtils.copyResource(AbstractLocalConfiguration.RESOURCE_PATH + "cargocpc.war",
            sourcePath, this.fileHandler);
        WAR war = new WAR("cargo.war");
        return war;
    }

    /**
     * Test WAR identifier creation.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateIdForWAR() throws Exception
    {
        WAR war = createWar();
        String name = deployer.createIdForDeployable(war);
        assertEquals("cargo.war", name);
    }

    /**
     * Test EJB identifier creation.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateIdForEJB() throws Exception
    {
        EJB ejb = createEJB();
        String name = deployer.createIdForDeployable(ejb);
        assertEquals("cargo.war", name);
    }

    /**
     * Test EAR identifier creation.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateIdForEAR() throws Exception
    {
        EAR ear = createEAR();
        String name = deployer.createIdForDeployable(ear);
        assertEquals("cargo.ear", name);
    }

    /**
     * Test RAR identifier creation.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateIdForRAR() throws Exception
    {
        RAR rar = createRAR();
        String name = deployer.createIdForDeployable(rar);
        assertEquals("cargo.rar", name);
    }

    /**
     * Test name getter when deployable not listed.
     * @throws Exception If anything goes wrong.
     */
    public void testGetNameFromDeployableNotSupportedList() throws Exception
    {
        SAR sar = createSAR();
        testGetNameFromDeployableNotSupportedFor(sar);
        File file = createFile();
        testGetNameFromDeployableNotSupportedFor(file);
    }

    /**
     * @return RAR file.
     * @throws Exception If anything goes wrong.
     */
    private RAR createRAR() throws Exception
    {
        // Current implementation of RAR does not validate
        // the type of file in any way. As such, we can re-use WAR
        // logic until this code is broken, or reimplemented as mocks
        WAR war = createWar();
        RAR rar = new RAR(war.getFile());
        return rar;
    }

    /**
     * @return SAR file.
     * @throws Exception If anything goes wrong.
     */
    private SAR createSAR() throws Exception
    {
        // Current implementation of SAR does not validate
        // the type of file in any way. As such, we can re-use WAR
        // logic until this code is broken, or reimplemented as mocks
        WAR war = createWar();
        SAR sar = new SAR(war.getFile());
        return sar;
    }

    /**
     * @return EJB file.
     * @throws Exception If anything goes wrong.
     */
    private EJB createEJB() throws Exception
    {
        // Current implementation of EJB does not validate
        // the type of file in any way. As such, we can re-use WAR
        // logic until this code is broken, or reimplemented as mocks
        WAR war = createWar();
        EJB ejb = new EJB(war.getFile());
        return ejb;
    }

    /**
     * @return EAR file.
     * @throws Exception If anything goes wrong.
     */
    private EAR createEAR() throws Exception
    {
        // Current implementation of EAR does not validate
        // the type of file in any way. As such, we can re-use WAR
        // logic until this code is broken, or reimplemented as mocks
        WAR war = createWar();
        EAR ear = new EAR(war.getFile());
        return ear;
    }

    /**
     * @return Any file.
     * @throws Exception If anything goes wrong.
     */
    private File createFile() throws Exception
    {
        // Current implementation of File does not validate
        // the type of file in any way. As such, we can re-use WAR
        // logic until this code is broken, or reimplemented as mocks
        WAR war = createWar();
        File file = new File(war.getFile());
        return file;
    }

    /**
     * Test name getter when deployable not listed.
     * @param deployable Deployable to test.
     */
    private void testGetNameFromDeployableNotSupportedFor(Deployable deployable)
    {
        try
        {
            deployer.createIdForDeployable(deployable);
            fail("should have gotten an exception");
        }
        catch (DeployableException e)
        {
            assertEquals("name extraction for " + deployable.getType()
                + " not currently supported", e.getMessage());
        }
        catch (Exception e)
        {
            fail("wrong exception type: " + e);
        }
    }

    /**
     * Test application deployment reordering.
     * @throws Exception If anything goes wrong.
     */
    public void testReorderAppDeploymentsAfterConfigurationVersionAndBeforeAdminServerName()
        throws Exception
    {
        WAR war = createWar();
        deployer.createElementForDeployableInDomain(war, domain);
        deployer.reorderAppDeploymentsAfterConfigurationVersion(domain);
        String xml = this.xmlUtil.toString(domain);
        int indexOfConfigurationVersion = xml.indexOf("configuration-version");
        int indexOfAppDeployment = xml.indexOf("app-deployment");
        int indexOfAdminServerName = xml.indexOf("admin-server-name");
        assertTrue(indexOfAppDeployment > indexOfConfigurationVersion);
        assertTrue(indexOfAppDeployment < indexOfAdminServerName);
    }

    /**
     * Test WAR unconfiguration.
     * @throws Exception If anything goes wrong.
     */
    public void testUnConfigWar() throws Exception
    {
        WAR war = createWar();
        testConfigWar();
        deployer.removeDeployableFromDomain(war, domain);
        String xml = this.xmlUtil.toString(domain);
        // contains is whitespace friendly
        XMLAssert.assertXpathNotExists(
            "//weblogic:app-deployment[contains(weblogic:name,'cargo')]", xml);
        XMLAssert.assertXpathNotExists(
            "//weblogic:app-deployment[contains(weblogic:source-path,'"
                + deployer.getAbsolutePath(war) + "')]", xml);
    }

    /**
     * Test path getter.
     * @throws Exception If anything goes wrong.
     */
    public void testGetAbsolutePathWithRelativePath() throws Exception
    {
        Deployable deployable = new WAR("path");
        String path = deployer.getAbsolutePath(deployable);

        assertEquals(System.getProperty("user.dir") + System.getProperty("file.separator")
            + "path", path);
    }

}
