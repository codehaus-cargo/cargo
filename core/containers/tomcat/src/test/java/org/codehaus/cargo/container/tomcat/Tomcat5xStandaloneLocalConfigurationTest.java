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
import org.codehaus.cargo.container.tomcat.Tomcat5xStandaloneLocalConfiguration;
import org.custommonkey.xmlunit.XMLAssert;

/**
 *  Tests for the Tomcat 5 implementation of StandaloneLocalConfigurationTest 
 */
public class Tomcat5xStandaloneLocalConfigurationTest extends Tomcat4xStandaloneLocalConfigurationTest {

    protected String getTestHome()
    {
        return "ram:/tomcat5xconfig";
    }
    
    protected void setUpContainerDefaults()
    {
        this.configuration = new Tomcat5xStandaloneLocalConfiguration(CONFIG_HOME);
        this.container = new Tomcat5xInstalledLocalContainer(configuration);
    }
    

    public void testConfigure() throws Exception
    {
        super.testConfigure();
        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/catalina.properties"));
    }

    protected void setUpManager()
    {
        fileHandler.mkdirs(CONTAINER_HOME + "/webapps");
        fileHandler.mkdirs(CONTAINER_HOME + "/server/lib");
        fileHandler.mkdirs(CONTAINER_HOME + "/server/webapps/manager");
        fileHandler.createFile(CONTAINER_HOME + "/conf/Catalina/localhost/manager.xml");
        fileHandler.createFile(CONTAINER_HOME + "/server/lib/catalina.jar");
    }

    public void testConfigureManager()
    {
        configuration.configure(container);
        assertTrue(fileHandler.exists(CONFIG_HOME + "/conf/Catalina/localhost/manager.xml"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/server/lib/catalina.jar"));
        assertTrue(fileHandler.exists(CONFIG_HOME + "/server/webapps/manager"));
    }

    public void testConfigureSetsCorrectAJPConnectorIdentifier() throws Exception
    {
        //check protocol instead of classname, as class is not required.
        configuration.configure(container);
        String config = slurp(CONFIG_HOME + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo("AJP/1.3", "//Connector[2]/@protocol", config);
    }
    
	/**
	 * Test method for {@link org.codehaus.cargo.container.tomcat.Tomcat5xStandaloneLocalConfiguration#createResourceTokenValue()}.
	 */
	public void testCreateResourceTokenValue() {
		String expected = 
			"<Resource name=\"myDataSource\"\n" +
			"          type=\"javax.sql.DataSource\"\n" +
			"          password=\"pass\"\n" +
			"          username=\"foo\"\n" +
			"/>\n";
		
        Tomcat5xStandaloneLocalConfiguration conf = (Tomcat5xStandaloneLocalConfiguration)configuration;		
        Resource resource = new Resource("myDataSource", "javax.sql.DataSource");
        resource.setParameter("password", "pass");
		resource.setParameter("username", "foo");
		
		conf.addResource(resource);
		
		assertEquals("Resource string not correct", expected, conf.createResourceTokenValue());		
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
        Tomcat5xStandaloneLocalConfiguration conf =
            (Tomcat5xStandaloneLocalConfiguration) configuration;
        configuration.setProperty(DatasourcePropertySet.DATASOURCE, resourceProperty);
        String element = conf.createDatasourceTokenValue();
        assertTrue(element.indexOf(realUrl) >0);

    }
	
	public void testCreateMultipleResourceTokenValues() {
		String expected = 
			"<Resource name=\"myDataSource\"\n" +
			"          type=\"javax.sql.DataSource\"\n" +
			"          password=\"pass\"\n" +
			"          username=\"foo\"\n" +
			"/>\n" +
			"<Resource name=\"otherDataSource\"\n" +
			"          type=\"javax.sql.DataSource\"\n" +
			"          password=\"bar\"\n" +
			"          username=\"gazonk\"\n" +
			"/>\n";
		
		Tomcat5xStandaloneLocalConfiguration conf =
            (Tomcat5xStandaloneLocalConfiguration) configuration;

        Resource resource = new Resource("myDataSource", "javax.sql.DataSource");
        resource.setParameter("password", "pass");
        resource.setParameter("username", "foo");

        Resource resource2 = new Resource("otherDataSource", "javax.sql.DataSource");
        resource2.setParameter("password", "bar");
        resource2.setParameter("username", "gazonk");

        conf.addResource(resource);
        conf.addResource(resource2);

        assertEquals("Resource string not correct", expected, conf.createResourceTokenValue());				
	}

}
