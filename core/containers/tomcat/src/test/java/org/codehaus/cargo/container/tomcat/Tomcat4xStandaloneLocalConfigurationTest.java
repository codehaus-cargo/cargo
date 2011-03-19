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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfigurationTest;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4xConfigurationChecker;
import org.codehaus.cargo.util.Dom4JUtil;
import org.custommonkey.xmlunit.XMLAssert;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

/**
 * Tests for the Tomcat 4 implementation of StandaloneLocalConfigurationTest
 * 
 * @version $Id$
 */
public class Tomcat4xStandaloneLocalConfigurationTest extends
    AbstractCatalinaStandaloneLocalConfigurationTest
{

    /**
     * Default AJP port.
     */
    protected static final String AJP_PORT = "8001";

    /**
     * Creates a {@link Tomcat4xStandaloneLocalConfiguration}. {@inheritdoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat4xStandaloneLocalConfiguration(home);
    }

    /**
     * Creates a {@link Tomcat4xInstalledLocalContainer}. {@inheritdoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat4xInstalledLocalContainer(configuration);
    }

    /**
     * @return {@link Tomcat4xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new Tomcat4xConfigurationChecker();
    }

    /**
     * {@inheritdoc}.
     * @param fixture Resource fixture.
     * @return <code>conf/server.xml</code> in the configuration's home.
     */
    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return configuration.getHome() + "/conf/server.xml";
    }

    /**
     * {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUpResourceFile() throws Exception
    {
        Dom4JUtil xmlUtil = new Dom4JUtil(getFileHandler());
        String file = getResourceConfigurationFile(null);
        Document document = DocumentHelper.createDocument();
        document.addElement("Engine").addElement("DefaultContext");
        xmlUtil.saveXml(document, file);
    }

    /**
     * {@inheritdoc}
     */
    @Override
    protected void setUpManager()
    {
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps");
        configuration.getFileHandler().mkdirs(container.getHome() + "/server/lib");
        configuration.getFileHandler().mkdirs(container.getHome() + "/server/webapps/manager");
        configuration.getFileHandler().createFile(container.getHome() + "/webapps/manager.xml");
        // seems copy needs to have a file present
        configuration.getFileHandler().createFile(
            container.getHome() + "/server/webapps/manager/touch.txt");
        configuration.getFileHandler().createFile(
            container.getHome() + "/server/lib/catalina.jar");
    }

    /**
     * Test {@link
     * Tomcat4xStandaloneLocalConfiguration#configure(org.codehaus.cargo.container.LocalContainer)}
     * and check manager.
     */
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

    /**
     * Test {@link
     * Tomcat4xStandaloneLocalConfiguration#configure(org.codehaus.cargo.container.LocalContainer)}
     * @throws Exception If anything goes wrong.
     */
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

    /**
     * Test AJP configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureSetsDefaultAJPPort() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(TomcatPropertySet.AJP_PORT),
            "//Connector[@className='org.apache.ajp.tomcat4.Ajp13Connector']/@port", config);
    }

    /**
     * Test AJP configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureSetsAJPPort() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.AJP_PORT, AJP_PORT);
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo(AJP_PORT,
            "//Connector[@className='org.apache.ajp.tomcat4.Ajp13Connector']/@port", config);
    }

    /**
     * Check transaction manager token in XML contents.
     * @param xml XML contents.
     * @throws Exception If anything goes wrong.
     */
    public void checkTransactionManagerToken(String xml) throws Exception
    {
        XMLAssert.assertXpathEvaluatesTo("javax.transaction.UserTransaction",
            "//Engine/DefaultContext/Resource[@name='UserTransaction']/@type", xml);
        XMLAssert.assertXpathEvaluatesTo("Container",
            "//Engine/DefaultContext/Resource[@name='UserTransaction']/@auth", xml);
        XMLAssert
            .assertXpathEvaluatesTo(
                "org.objectweb.jotm.UserTransactionFactory",
                "//Engine/DefaultContext/ResourceParams[@name='UserTransaction']"
                    + "/parameter[name='factory']/value",
                xml);
        XMLAssert
            .assertXpathEvaluatesTo(
                "60",
                "//Engine/DefaultContext/ResourceParams[@name='UserTransaction']"
                    + "/parameter[name='jotm.timeout']/value",
                xml);
    }

    /**
     * Test the creation of multiple resource token values.
     * @throws Exception If anything goes wrong.
     */
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
            configuration.getFileHandler().readTextFile(getDataSourceConfigurationFile(null),
                "UTF-8");

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
