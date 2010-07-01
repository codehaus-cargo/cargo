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
package org.codehaus.cargo.container.tomcat;

import java.io.IOException;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfigurationTest;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5And6xConfigurationChecker;
import org.codehaus.cargo.util.Dom4JUtil;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.xml.sax.SAXException;

/**
 * Tests for the Tomcat 5 implementation of StandaloneLocalConfigurationTest
 */
public class Tomcat5xStandaloneLocalConfigurationTest extends
AbstractCatalinaStandaloneLocalConfigurationTest
{
    private Tomcat5And6xConfigurationChecker checker = new Tomcat5And6xConfigurationChecker();
    
    @Override
    public LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat5xStandaloneLocalConfiguration(home);
    }

    @Override
    public InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat5xInstalledLocalContainer(configuration);
    }

    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return checker;
    }

    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return configuration.getHome() + "/conf/context.xml";
    }
    
    @Override
    protected void setUpResourceFile() throws Exception
    {
        Dom4JUtil xmlUtil = new Dom4JUtil(getFileHandler());
        String file = getResourceConfigurationFile(null);
        Document document = DocumentHelper.createDocument();
        document.addElement("Context");
        xmlUtil.saveXml(document, file);
    }
    
    public void testConfigure() throws Exception
    {
        configuration.configure(container);
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/catalina.properties"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/context.xml"));
    }

    @Override
    protected void setUpManager()
    {
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps");
        configuration.getFileHandler().mkdirs(container.getHome() + "/server/lib");
        configuration.getFileHandler().mkdirs(container.getHome() + "/server/webapps/manager");
        configuration.getFileHandler().createFile(
            container.getHome() + "/conf/Catalina/localhost/manager.xml");
        configuration.getFileHandler().createFile(
            container.getHome() + "/server/lib/catalina.jar");
    }

    public void testConfigureManager()
    {
        configuration.configure(container);
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/context.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/catalina.properties"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/Catalina/localhost/manager.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/server/lib/catalina.jar"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/server/webapps/manager"));
    }

    public void testConfigureSetsCorrectAJPConnectorIdentifier() throws Exception
    {
        // check protocol instead of classname, as class is not required.
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo("AJP/1.3", "//Connector[@port='8009']/@protocol", config);
    }

    public void testConfigureSetsDefaultAJPPort() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(TomcatPropertySet.AJP_PORT), "//Connector[@protocol='AJP/1.3']/@port", config);

    }

    public void testConfigureSetsAJPPort() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.AJP_PORT, "1001");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml");
        XMLAssert.assertXpathEvaluatesTo("1001", "//Connector[@protocol='AJP/1.3']/@port", config);
    }
    
    public void testCreateMultipleResourceTokenValues() throws XpathException, SAXException,
        IOException
    {
        // TODO
    }

    public void testCreateResourceTokenValue() throws XpathException, SAXException, IOException
    {
        // TODO
    }


}
