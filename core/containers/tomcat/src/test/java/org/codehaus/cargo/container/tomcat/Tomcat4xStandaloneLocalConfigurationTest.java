/*
 * ========================================================================
 *
 * Copyright 2005 Vincent Massol.
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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.resource.Resource;
import org.codehaus.cargo.container.tomcat.internal.AbstractTomcatStandaloneLocalConfigurationTest;
import org.custommonkey.xmlunit.XMLAssert;

/**
 *  Tests for the Tomcat 4 implementation of StandaloneLocalConfigurationTest 
 */
public class Tomcat4xStandaloneLocalConfigurationTest extends AbstractTomcatStandaloneLocalConfigurationTest {

    protected String getTestHome()
    {
        return "ram:/tomcat4xconfig";
    }
    
    protected void setUpContainerDefaults()
    {
        this.configuration = new Tomcat4xStandaloneLocalConfiguration(CONFIG_HOME);
        this.container = new Tomcat4xInstalledLocalContainer(configuration);
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        setUpManager();
    }

    protected void setUpManager()
    {
        fileHandler.mkdirs(CONTAINER_HOME + "/webapps");
        fileHandler.mkdirs(CONTAINER_HOME + "/server/lib");
        fileHandler.mkdirs(CONTAINER_HOME + "/server/webapps/manager");
        fileHandler.createFile(CONTAINER_HOME + "/webapps/manager.xml");
        fileHandler.createFile(CONTAINER_HOME + "/server/lib/catalina.jar");
    }

    public void testConfigureManager()
    {
        configuration.configure(container);
        assertTrue(fileHandler.exists(CONFIG_HOME + "/webapps/manager.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/server/lib/catalina.jar"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/server/webapps/manager"));
    }
    
    public void testConfigure() throws Exception
    {
        configuration.configure(container);

        assertTrue(fileHandler.exists(CONFIG_HOME + "/temp"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/logs"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/server.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/web.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/tomcat-users.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/webapps/cargocpc.war"));
        testConfigureManager();
    }
    
    public void testConfigureSetsCorrectAJPConnectorIdentifier() throws Exception
    {
        configuration.configure(container);
        String config = slurp(CONFIG_HOME + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo("org.apache.ajp.tomcat4.Ajp13Connector", "//Connector[2]/@className", config);

    }
    
    public void testConfigureSetsDefaultAJPPort() throws Exception
    {
        configuration.configure(container);
        String config = slurp(CONFIG_HOME + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(TomcatPropertySet.AJP_PORT), "//Connector[2]/@port", config);

    }

    public void testConfigureSetsAJPPort() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.AJP_PORT, AJP_PORT);
        configuration.configure(container);
        String config = slurp(CONFIG_HOME + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo(AJP_PORT,
            "//Connector[2]/@port",
            config);
    }

    public void testCreateWindowsHsqldbDataSource()
    {

        String realUrl = "jdbc:hsqldb:c:\\temp\\db/jira-home/database";

        String resourceProperty = 
            "cargo.datasource.url="+realUrl+"|\n"+
            "cargo.datasource.driver=org.hsqldb.jdbcDriver|\n"+
            "cargo.datasource.username=sa|"+
            "cargo.datasource.password=|"+
            "cargo.datasource.type=javax.sql.DataSource|"+
            "cargo.datasource.jndi=jdbc/JiraDS";
        Tomcat4xStandaloneLocalConfiguration conf =
            (Tomcat4xStandaloneLocalConfiguration) configuration;
        configuration.setProperty(DatasourcePropertySet.DATASOURCE, resourceProperty);
        String element = conf.createDatasourceTokenValue();
        assertTrue(element.indexOf(realUrl) >0);

    }   
    
	/**
	 * Test method for {@link org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfiguration#createResourceTokenValue()}.
	 */
	public void testCreateResourceTokenValue() {
        Tomcat4xStandaloneLocalConfiguration conf = 
            (Tomcat4xStandaloneLocalConfiguration)configuration;
		String expected = 
			"<Resource name=\"myDataSource\"\n" +
			"          type=\"javax.sql.DataSource\"\n" +
			"          auth=\"Container\"\n" +
			"/>\n" +
			"<ResourceParams name=\"myDataSource\">\n" +
			"  <parameter>\n" +
			"    <name>password</name>\n" +
			"    <value>pass</value>\n" +
			"  </parameter>\n" +
			"  <parameter>\n" +
			"    <name>username</name>\n" +
			"    <value>foo</value>\n" +
			"  </parameter>\n" +
			"</ResourceParams>\n";
		
		Resource resource = new Resource("myDataSource", "javax.sql.DataSource");
		resource.setParameter("password" , "pass");
		resource.setParameter("username", "foo");
		conf.addResource(resource);
		assertEquals("Resource string not correct", expected, 
		    conf.createResourceTokenValue());		
		
	}

}
