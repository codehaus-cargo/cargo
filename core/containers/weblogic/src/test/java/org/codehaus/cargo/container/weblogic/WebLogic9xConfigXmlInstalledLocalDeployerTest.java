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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
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
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link WebLogic9xConfigXmlInstalledLocalDeployer}.
 * <p>
 * Note: These tests are using <a href="http://jakarta.apache.org/commons/vfs/">VFS</a> with a <a
 * href="http://jakarta.apache.org/commons/vfs/filesystems.html#ram">RAM file system</a> so that
 * files are only created in memory. This makes is easy to test file-based operations without having
 * to resort to creating files in the file system and deleting them afterwards.
 * </p>
 * 
 * @version $Id$
 */
public class WebLogic9xConfigXmlInstalledLocalDeployerTest extends TestCase
{

    private static final String BEA_HOME = "ram:/bea";

    private static final String DOMAIN_HOME = BEA_HOME + "/mydomain";

    private static final String WL_HOME = BEA_HOME + "/weblogic9";

    protected static final String RESOURCE_PATH =
        "/org/codehaus/cargo/container/internal/resources/";

    private WebLogic9xInstalledLocalContainer container;

    private WebLogic9xConfigXmlInstalledLocalDeployer deployer;

    private StandardFileSystemManager fsManager;

    private ResourceUtils resourceUtils;

    private FileHandler fileHandler;

    private Element domain;

    private Document document;

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
        m.put("jdbc", "http://www.bea.com/ns/weblogic/90");
        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        this.fileHandler.delete(BEA_HOME);

        LocalConfiguration configuration =
            new WebLogic9xStandaloneLocalConfiguration(DOMAIN_HOME);
        this.container = new WebLogic9xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.container.setFileHandler(this.fileHandler);
        this.deployer = new WebLogic9xConfigXmlInstalledLocalDeployer(container);
        resourceUtils = new ResourceUtils();
        this.document = DocumentHelper.createDocument();
        this.domain = document.addElement("domain");
        document.setRootElement(domain);
        domain.addNamespace("", "http://www.bea.com/ns/weblogic/920/domain");
        QName configurationVersionQ =
            new QName("configuration-version", new Namespace("",
                "http://www.bea.com/ns/weblogic/920/domain"));
        domain.addElement(configurationVersionQ);
        QName adminServerNameQ =
            new QName("admin-server-name", new Namespace("",
                "http://www.bea.com/ns/weblogic/920/domain"));
        domain.addElement(adminServerNameQ);

    }

    public void testConfigWar() throws IOException, XpathException, SAXException
    {
        WAR war = createWar();
        String xml = domain.asXML();
        deployer.addDeployableToDomain(war, this.domain);
        xml = domain.asXML();
        XMLAssert.assertXpathEvaluatesTo("cargo", "//weblogic:app-deployment/weblogic:name", xml);
        XMLAssert.assertXpathEvaluatesTo(deployer.getAbsolutePath(war),
            "//weblogic:app-deployment/weblogic:source-path", xml);
    }

    public void testFindAppDeployments() throws Exception
    {
        WAR war = createWar();
        testConfigWar();
        List l = deployer.selectAppDeployments(war, domain);
        assertEquals(1, l.size());
        deployer.removeDeployableFromDomain(war, domain);
        l = deployer.selectAppDeployments(war, domain);
        assertEquals(0, l.size());
    }

    /**
     * @return
     * @throws IOException
     */
    protected WAR createWar() throws IOException
    {
        String sourcePath = this.fileHandler.append(DOMAIN_HOME, "cargocpc.war");
        this.resourceUtils.copyResource(RESOURCE_PATH + "cargocpc.war", sourcePath,
            this.fileHandler);
        WAR war = new WAR("cargo.war");
        return war;
    }

    public void testCreateIdForWAR() throws Exception
    {
        WAR war = createWar();
        String name = deployer.createIdForDeployable(war);
        assertEquals("cargo", name);
    }

    public void testCreateIdForEJB() throws Exception
    {
        EJB EJB = createEJB();
        String name = deployer.createIdForDeployable(EJB);
        assertEquals("cargo.war", name);
    }

    public void testCreateIdForRAR() throws Exception
    {
        RAR RAR = createRAR();
        String name = deployer.createIdForDeployable(RAR);
        assertEquals("cargo.war", name);
    }

    public void testCreateIdForEAR() throws Exception
    {
        // skipping until mocks are created
        // EAR ear = createEar();
        // String name = deployer.getNameFromDeployable(ear);
        // assertEquals("cargo", name);
    }

    public void testGetNameFromDeployableNotSupportedList() throws IOException
    {
        SAR sar = createSAR();
        testGetNameFromDeployableNotSupportedFor(sar);
        File file = createFile();
        testGetNameFromDeployableNotSupportedFor(file);
    }

    private RAR createRAR() throws IOException
    {
        // Current implementation of RAR does not validate
        // the type of file in any way. As such, we can re-use WAR
        // logic until this code is broken, or reimplemented as mocks
        WAR war = createWar();
        RAR rar = new RAR(war.getFile());
        return rar;
    }

    private SAR createSAR() throws IOException
    {
        // Current implementation of SAR does not validate
        // the type of file in any way. As such, we can re-use WAR
        // logic until this code is broken, or reimplemented as mocks
        WAR war = createWar();
        SAR SAR = new SAR(war.getFile());
        return SAR;
    }

    private EJB createEJB() throws IOException
    {
        // Current implementation of EJB does not validate
        // the type of file in any way. As such, we can re-use WAR
        // logic until this code is broken, or reimplemented as mocks
        WAR war = createWar();
        EJB EJB = new EJB(war.getFile());
        return EJB;
    }

    private File createFile() throws IOException
    {
        // Current implementation of File does not validate
        // the type of file in any way. As such, we can re-use WAR
        // logic until this code is broken, or reimplemented as mocks
        WAR war = createWar();
        File File = new File(war.getFile());
        return File;
    }

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
            fail("wrong exception type");
        }
    }

    /**
     * create an EAR that can be used for testing deployer capability.
     */
    private EAR createEar()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void testBuildDeployableNameFromFile() throws IOException
    {
        File file = createFile();
        assertEquals("cargo.war", deployer.createIdFromFileName(file));

    }

    public void testReorderAppDeploymentsAfterConfigurationVersionAndBeforeAdminServerName()
        throws IOException
    {
        WAR war = createWar();
        deployer.createElementForDeployableInDomain(war, domain);
        deployer.reorderAppDeploymentsAfterConfigurationVersion(domain);
        String xml = domain.asXML();
        int indexOfConfigurationVersion = xml.indexOf("configuration-version");
        int indexOfAppDeployment = xml.indexOf("app-deployment");
        int indexOfAdminServerName = xml.indexOf("admin-server-name");
        assertTrue(indexOfAppDeployment > indexOfConfigurationVersion);
        assertTrue(indexOfAppDeployment < indexOfAdminServerName);

    }

    public void testUnConfigWar() throws IOException, XpathException, SAXException
    {
        WAR war = createWar();
        testConfigWar();
        deployer.removeDeployableFromDomain(war, domain);
        String xml = domain.asXML();
        // contains is whitespace friendly
        XMLAssert.assertXpathNotExists(
            "//weblogic:app-deployment[contains(weblogic:name,'cargo')]", xml);
        XMLAssert.assertXpathNotExists(
            "//weblogic:app-deployment[contains(weblogic:source-path,'"
                + deployer.getAbsolutePath(war) + "')]", xml);
    }

    public void testGetAbsolutePathWithRelativePath() throws Exception
    {
        Deployable deployable = new WAR("path");
        String path = deployer.getAbsolutePath(deployable);

        assertEquals(System.getProperty("user.dir") + System.getProperty("file.separator")
            + "path", path);
    }

}
