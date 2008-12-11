/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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
import java.util.Properties;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.internal.util.ResourceUtils;
import org.codehaus.cargo.container.property.DataSource;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**
 * Unit tests for {@link WebLogic8xConfigXmlInstalledLocalDeployer}.
 * <p>
 * Note: These tests are using <a href="http://jakarta.apache.org/commons/vfs/">VFS</a> with a <a
 * href="http://jakarta.apache.org/commons/vfs/filesystems.html#ram">RAM file system</a> so that
 * files are only created in memory. This makes is easy to test file-based operations without having
 * to resort to creating files in the file system and deleting them afterwards.
 * </p>
 * 
 * @version $Id$
 */
public class WebLogic8xConfigXmlInstalledLocalDeployerTest extends TestCase
{

    private static final String DS_JNDI = "jdbc/CrowdDS";

    private static final String DS_TYPE_NONTX = "javax.sql.DataSource";

    private static final String DS_PASSWORD = "";

    private static final String DS_USER = "sa";

    private static final String DS_DRIVER = "org.hsqldb.jdbcDriver";

    private static final String DS_URL = "jdbc:hsqldb:mem:crowd_cargo";

    private static final String BEA_HOME = "ram:/bea";

    private static final String DOMAIN_HOME = BEA_HOME + "/mydomain";

    private static final String WL_HOME = BEA_HOME + "/weblogic8";

    protected static final String RESOURCE_PATH =
        "/org/codehaus/cargo/container/internal/resources/";

    private WebLogic8xInstalledLocalContainer container;

    private WebLogic8xConfigXmlInstalledLocalDeployer deployer;

    private StandardFileSystemManager fsManager;

    private ResourceUtils resourceUtils;

    private FileHandler fileHandler;

    private Element domain;

    private Properties dsProps;

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        this.dsProps = new Properties();
        this.dsProps.setProperty("cargo.datasource.url",DS_URL);
        this.dsProps.setProperty("cargo.datasource.driver",DS_DRIVER);
        this.dsProps.setProperty("cargo.datasource.username",DS_USER);
        this.dsProps.setProperty("cargo.datasource.password",DS_PASSWORD);
        this.dsProps.setProperty("cargo.datasource.type",DS_TYPE_NONTX);
        this.dsProps.setProperty("cargo.datasource.jndi",DS_JNDI);
        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        this.fileHandler.delete(BEA_HOME);

        LocalConfiguration configuration = new WebLogicStandaloneLocalConfiguration(DOMAIN_HOME);
        this.container = new WebLogic8xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.container.setFileHandler(this.fileHandler);
        this.deployer = new WebLogic8xConfigXmlInstalledLocalDeployer(container);
        resourceUtils = new ResourceUtils();
        Document document = DocumentHelper.createDocument();
        this.domain = document.addElement("Domain");

    }

    public void testConfigWar() throws IOException, XpathException, SAXException
    {
        this.resourceUtils.copyResource(RESOURCE_PATH + "cargocpc.war", this.fileHandler.append(
            DOMAIN_HOME, "cargocpc.war"), this.fileHandler);
        WAR war = new WAR("cargo.war");
        deployer.addWarToDomain(war, this.domain);
        String xml = domain.asXML();
        XMLAssert.assertXpathEvaluatesTo("cargo.war", "//WebAppComponent/@URI", xml);
    }

    public void testConfigDataSource() throws IOException, XpathException, SAXException
    {

        DataSource ds = new DataSource(dsProps);
        deployer.addDataSourceToDomain(ds, this.domain);
        String xml = domain.asXML();
        XMLAssert.assertXpathEvaluatesTo(DS_URL, "//JDBCConnectionPool/@URL", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_DRIVER, "//JDBCConnectionPool/@DriverName", xml);
        XMLAssert.assertXpathEvaluatesTo("user="+DS_USER, "//JDBCConnectionPool/@Properties", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_PASSWORD, "//JDBCConnectionPool/@Password", xml);
        XMLAssert.assertXpathEvaluatesTo("server", "//JDBCConnectionPool/@Targets", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//JDBCConnectionPool/@Name", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//JDBCDataSource/@Name", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//JDBCDataSource/@JNDIName", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//JDBCDataSource/@PoolName", xml);
        XMLAssert.assertXpathEvaluatesTo("server", "//JDBCDataSource/@Targets", xml);

    }

    public void testConfigXADataSource() throws IOException, XpathException, SAXException
    {
        this.resourceUtils.copyResource(RESOURCE_PATH + "cargocpc.war", this.fileHandler.append(
            DOMAIN_HOME, "cargocpc.war"), this.fileHandler);
        WAR war = new WAR("cargo.war");
        deployer.addWarToDomain(war, this.domain);
        String xml = domain.asXML();
        XMLAssert.assertXpathEvaluatesTo("cargo.war", "//WebAppComponent/@URI", xml);
    }

}
