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
package org.codehaus.cargo.container.resin;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.util.FileHandler;
import org.codehaus.cargo.util.VFSFileHandler;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class Resin3xStandaloneLocalConfigurationTest extends TestCase
{
    private static final String RESIN_HOME = "ram:/resin3x";

    private static final String CONFIG_HOME = RESIN_HOME + "/config";

    private static final String CONTAINER_HOME = RESIN_HOME + "/container";

    private static final String DS_JNDI = "jdbc/CargoDS";

    private static final String DS_TYPE_NONTX = "javax.sql.DataSource";

    private static final String DS_PASSWORD = "";

    private static final String DS_USER = "APP";

    private static final String DS_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String DS_URL = "jdbc:derby:derbyDB;create=true";

    private static final String CONF_NS = "http://caucho.com/ns/resin";

    private String dataSourceProperty;

    private Resin3xInstalledLocalContainer container;

    private Resin3xStandaloneLocalConfiguration configuration;

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
        this.configuration = new Resin3xStandaloneLocalConfiguration(CONFIG_HOME);
        this.configuration.setFileHandler(this.fileHandler);

        this.container = new Resin3xInstalledLocalContainer(configuration);
        this.container.setHome(CONTAINER_HOME);
        this.container.setFileHandler(this.fileHandler);

        // setup the namespace of the weblogic config.xml file
        Map m = new HashMap();
        m.put("resin", CONF_NS);

        NamespaceContext ctx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(ctx);
    }

    public void testConfigure() throws Exception
    {
        configuration.configure(container);

        assertTrue(fileHandler.exists(CONFIG_HOME + "/app-default.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/resin.conf"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/webapps/cargocpc.war"));

    }

    public void testCreateDatasourceTokenValue() throws XpathException, SAXException, IOException
    {
        configuration.setProperty(DatasourcePropertySet.DATASOURCE, this.dataSourceProperty);
        String xml = configuration.createDatasourceTokenValue();
        XMLAssert.assertXpathEvaluatesTo(DS_DRIVER, "//database/driver/@type", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//database/@jndi-name", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_URL, "//database/driver/url", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_USER, "//database/driver/user", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_PASSWORD, "//database/driver/password", xml);
    }

    public void testConfigureMakesDataSource() throws IOException, XpathException, SAXException
    {
        configuration.setProperty(DatasourcePropertySet.DATASOURCE, this.dataSourceProperty);
        configuration.configure(container);
        String xml = slurp(CONFIG_HOME + "/conf/resin.conf");

        XMLAssert.assertXpathEvaluatesTo(DS_DRIVER, "//resin:database/resin:driver/@type", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_JNDI, "//resin:database/@jndi-name", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_URL, "//resin:database/resin:driver/resin:url", xml);
        XMLAssert
            .assertXpathEvaluatesTo(DS_USER, "//resin:database/resin:driver/resin:user", xml);
        XMLAssert.assertXpathEvaluatesTo(DS_PASSWORD,
            "//resin:database/resin:driver/resin:password", xml);
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
}
