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

import java.io.IOException;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfigurationTest;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4xConfigurationChecker;
import org.codehaus.cargo.util.Dom4JUtil;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.xml.sax.SAXException;

/**
 * Tests for the Tomcat 4 implementation of StandaloneLocalConfigurationTest
 */
public class Tomcat4xStandaloneLocalConfigurationTest extends
    AbstractCatalinaStandaloneLocalConfigurationTest
{

    protected String AJP_PORT = "8001";

    @Override
    public LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat4xStandaloneLocalConfiguration(home);
    }

    @Override
    public InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat4xInstalledLocalContainer(configuration);
    }

    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new Tomcat4xConfigurationChecker();
    }

    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return configuration.getHome() + "/conf/server.xml";
    }
    
    @Override
    protected void setUpResourceFile() throws Exception
    {
        Dom4JUtil xmlUtil = new Dom4JUtil(getFileHandler());
        String file = getResourceConfigurationFile(null);
        Document document = DocumentHelper.createDocument();
        document.addElement("Engine").addElement("DefaultContext");
        xmlUtil.saveXml(document, file);
    }
    
    @Override
    protected void setUpManager()
    {
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps");
        configuration.getFileHandler().mkdirs(container.getHome() + "/server/lib");
        configuration.getFileHandler().mkdirs(container.getHome() + "/server/webapps/manager");
        configuration.getFileHandler().createFile(container.getHome() + "/webapps/manager.xml");
        //seems copy needs to have a file present
        configuration.getFileHandler().createFile(container.getHome() + "/server/webapps/manager/touch.txt");
        configuration.getFileHandler().createFile(
        container.getHome() + "/server/lib/catalina.jar");
    }

    public void testConfigureManager()
    {
        configuration.configure(container);
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/webapps/manager.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/server/lib/catalina.jar"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/server/webapps/manager"));
    }

    public void testConfigure() throws Exception
    {
        configuration.configure(container);

        assertTrue(configuration.getFileHandler().exists(configuration.getHome() + "/temp"));
        assertTrue(configuration.getFileHandler().exists(configuration.getHome() + "/logs"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/server.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/web.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/tomcat-users.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/webapps/cargocpc.war"));
        testConfigureManager();
    }

    public void testConfigureSetsDefaultAJPPort() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(TomcatPropertySet.AJP_PORT), "//Connector[@className='org.apache.ajp.tomcat4.Ajp13Connector']/@port", config);

    }

    public void testConfigureSetsAJPPort() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.AJP_PORT, AJP_PORT);
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo(AJP_PORT, "//Connector[@className='org.apache.ajp.tomcat4.Ajp13Connector']/@port", config);
    }

    public void checkTransactionManagerToken(String xml) throws SAXException, IOException,
        XpathException
    {
        XMLAssert.assertXpathEvaluatesTo("javax.transaction.UserTransaction",
            "//Engine/DefaultContext/Resource[@name='UserTransaction']/@type", xml);
        XMLAssert.assertXpathEvaluatesTo("Container",
            "//Engine/DefaultContext/Resource[@name='UserTransaction']/@auth", xml);
        XMLAssert
            .assertXpathEvaluatesTo(
                "org.objectweb.jotm.UserTransactionFactory",
                "//Engine/DefaultContext/ResourceParams[@name='UserTransaction']/parameter[name='factory']/value",
                xml);
        XMLAssert
            .assertXpathEvaluatesTo(
                "60",
                "//Engine/DefaultContext/ResourceParams[@name='UserTransaction']/parameter[name='jotm.timeout']/value",
                xml);
    }

    public void testCreateMultipleResourceTokenValues() throws Exception
    {
        setUpResourceFile();
        Tomcat4xStandaloneLocalConfiguration conf =
            (Tomcat4xStandaloneLocalConfiguration) configuration;

        Resource resource = new Resource("myDataSource", "javax.sql.DataSource");
        resource.setParameter("password", "pass");
        resource.setParameter("username", "foo");

        Resource resource2 = new Resource("otherDataSource", "javax.sql.DataSource");
        resource2.setParameter("password", "bar");
        resource2.setParameter("username", "gazonk");

        conf.addResource(resource);
        conf.addResource(resource2);

        conf.configureResources(container);
        String xml =
            configuration.getFileHandler().readTextFile(getDataSourceConfigurationFile(null));

        XMLAssert.assertXpathEvaluatesTo("javax.sql.DataSource",
            "//Resource[@name='myDataSource']/@type", xml);
        XMLAssert.assertXpathEvaluatesTo("Container", "//Resource[@name='myDataSource']/@auth",
            xml);

        XMLAssert.assertXpathEvaluatesTo("foo",
            "//ResourceParams[@name='myDataSource']/parameter[name='username']/value", xml);
        XMLAssert.assertXpathEvaluatesTo("pass",
            "//ResourceParams[@name='myDataSource']/parameter[name='password']/value", xml);

        XMLAssert.assertXpathEvaluatesTo("javax.sql.DataSource",
            "//Resource[@name='otherDataSource']/@type", xml);
        XMLAssert.assertXpathEvaluatesTo("Container",
            "//Resource[@name='otherDataSource']/@auth", xml);

        XMLAssert.assertXpathEvaluatesTo("gazonk",
            "//ResourceParams[@name='otherDataSource']/parameter[name='username']/value", xml);
        XMLAssert.assertXpathEvaluatesTo("bar",
            "//ResourceParams[@name='otherDataSource']/parameter[name='password']/value", xml);
    }

}
