/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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
package org.codehaus.cargo.container.orion.internal;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.orion.Orion1xInstalledLocalContainer;
import org.codehaus.cargo.container.orion.OrionStandaloneLocalConfiguration;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link org.codehaus.cargo.container.orion.OrionStandaloneLocalConfiguration}.
 * 
 * @version $Id$
 */
public class OrionStandaloneLocalConfigurationTest extends TestCase
{
    private static final String ORION_HOME = "ram:/Orion";

    private static final String CONFIG_HOME = ORION_HOME + "/config";

    private static final String CONTAINER_HOME = ORION_HOME + "/container";

    private static final String DS_JNDI = "jdbc/CargoDS";

    private static final String DS_TYPE_NONTX = "javax.sql.DataSource";

    private static final String DS_PASSWORD = "";

    private static final String DS_USER = "APP";

    private static final String DS_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String DS_URL = "jdbc:derby:derbyDB;create=true";

    private String dataSourceProperty;

    private Orion1xInstalledLocalContainer container;

    private OrionStandaloneLocalConfiguration configuration;

    private StandardFileSystemManager fsManager;

    private FileHandler fileHandler;

    /**
     * {@inheritDoc}
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        StringBuffer dataSource = new StringBuffer();
        dataSource.append("cargo.datasource.url=" + DS_URL + "|\n");
        dataSource.append("cargo.datasource.driver=" + DS_DRIVER + "|\n");
        dataSource.append("cargo.datasource.username=" + DS_USER + "|\n");
        dataSource.append("cargo.datasource.password=" + DS_PASSWORD + "|\n");
        dataSource.append("cargo.datasource.type=" + DS_TYPE_NONTX + "|\n");
        dataSource.append("cargo.datasource.jndi=" + DS_JNDI);
        this.dataSourceProperty = dataSource.toString();

        this.fsManager = new StandardFileSystemManager();
        this.fsManager.init();
        this.fileHandler = new VFSFileHandler(this.fsManager);
        fileHandler.mkdirs(CONFIG_HOME);
        fileHandler.mkdirs(CONTAINER_HOME);
        this.configuration = new OrionStandaloneLocalConfiguration(CONFIG_HOME);
        this.configuration.setFileHandler(this.fileHandler);

        this.container = new Orion1xInstalledLocalContainer(configuration);
        this.container.setHome(CONTAINER_HOME);
        this.container.setFileHandler(this.fileHandler);

    }

    // public void testConfigure() throws Exception
    // {
    // configuration.configure(container);
    //
    // assertTrue(fileHandler.exists(CONFIG_HOME + "/app-default.xml"));
    // assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/resin.conf"));
    // assertTrue(fileHandler.exists(CONFIG_HOME + "/webapps/cargocpc.war"));
    //
    // }

    public void testCreateDatasourceTokenValue() throws XpathException, SAXException, IOException
    {
        configuration.setProperty(DatasourcePropertySet.DATASOURCE, this.dataSourceProperty);
        String xml = configuration.createDatasourceTokenValue();
        checkStandardDataSource(xml);
    }

    private void checkStandardDataSource(String xml) throws SAXException, IOException,
        XpathException
    {
        XMLAssert.assertXpathEvaluatesTo("30", "//data-source/@inactivity-timeout", xml);
        XMLAssert.assertXpathEvaluatesTo("com.evermind.sql.DriverManagerDataSource",
            "//data-source/@class", xml);
        XMLAssert.assertXpathEvaluatesTo("Cargo-Datasource", "//data-source/@name", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_DRIVER, "//data-source/@connection-driver", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//data-source/@location", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI+"EJB", "//data-source/@ejb-location", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI+"XA", "//data-source/@xa-location", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_URL, "//data-source/@url", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_USER, "//data-source/@username", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_PASSWORD, "//data-source/@password", xml);
    }

    public void testConfigureMakesDataSource() throws IOException, XpathException, SAXException
    {
        configuration.setProperty(DatasourcePropertySet.DATASOURCE, this.dataSourceProperty);
        configuration.configure(container);
        String xml = slurp(CONFIG_HOME + "/conf/data-sources.xml");
        checkStandardDataSource(xml);
    }

    public void testCreateDatasourceTokenValueReturnsSpaceWhenNull()
    {
        String ds = configuration.createDatasourceTokenValue();
        assertEquals(" ", ds);
    }

    /**
     * reads a file into a String
     * 
     * @param in - what to read
     * @return String contents of the file
     * @throws IOException
     */
    public String slurp(String file) throws IOException
    {
        InputStream in = this.fsManager.resolveFile(file).getContent().getInputStream();
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;)
        {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    public void testGetRoleToken()
    {
        OrionStandaloneLocalConfiguration configuration =
            new OrionStandaloneLocalConfiguration("something");
        configuration.setProperty(ServletPropertySet.USERS, "u1:p1:r1,r2|u2:p2:r2,r3");

        String token = configuration.getRoleToken();
        assertTrue(token.indexOf("<security-role-mapping name=\"r1\">"
            + "<user name=\"u1\"/></security-role-mapping>") > -1);
        assertTrue(token.indexOf("<security-role-mapping name=\"r2\">"
            + "<user name=\"u1\"/><user name=\"u2\"/></security-role-mapping>") > -1);
        assertTrue(token.indexOf("<security-role-mapping name=\"r3\">"
            + "<user name=\"u2\"/></security-role-mapping>") > -1);
    }

    public void testGetUserToken()
    {
        OrionStandaloneLocalConfiguration configuration =
            new OrionStandaloneLocalConfiguration("something");
        configuration.setProperty(ServletPropertySet.USERS, "u1:p1:r1,r2|u2:p2:r2,r3");

        String token = configuration.getUserToken();
        assertEquals(" " + "<user deactivated=\"false\" username=\"u1\" password=\"p1\"/>"
            + "<user deactivated=\"false\" username=\"u2\" password=\"p2\"/>", token);
    }
}
