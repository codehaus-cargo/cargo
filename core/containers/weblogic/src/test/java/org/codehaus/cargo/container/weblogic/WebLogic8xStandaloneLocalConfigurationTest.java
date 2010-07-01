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

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.DataSourceFixture;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.weblogic.internal.WebLogic8xConfigurationChecker;
import org.codehaus.cargo.util.Dom4JUtil;
import org.custommonkey.xmlunit.XMLAssert;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

public class WebLogic8xStandaloneLocalConfigurationTest extends
    AbstractWeblogicStandaloneConfigurationTest
{
    @Override
    public LocalConfiguration createLocalConfiguration(String home)
    {
        return new WebLogicStandaloneLocalConfiguration(home);
    }

    @Override
    public InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new WebLogic8xInstalledLocalContainer(configuration);
    }

    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new WebLogic8xConfigurationChecker("server");
    }

    @Override
    protected String getDataSourceConfigurationFile(DataSourceFixture fixture)
    {
        return configuration.getHome() + "/config.xml";
    }

    public void testConfigure() throws Exception
    {
        configuration.configure(container);

        assertTrue(configuration.getFileHandler().exists(configuration.getHome() + "/config.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/DefaultAuthenticatorInit.ldift"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/applications/cargocpc.war"));

    }

    public void testDoConfigureSetsDefaultPort() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(configuration.getHome() + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration.getPropertyValue(ServletPropertySet.PORT),
            "//Server/@ListenPort", config);

    }

    public void testDoConfigureSetsPort() throws Exception
    {
        configuration.setProperty(ServletPropertySet.PORT, "123");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(configuration.getHome() + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo("123", "//Server/@ListenPort", config);

    }

    public void testDoConfigureCreatesWar() throws Exception
    {
        configuration.addDeployable(new WAR("my.war"));
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(configuration.getHome() + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo("my.war", "//WebAppComponent/@URI", config);
    }

    public void testDoConfigureSetsDefaultAddress() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(configuration.getHome() + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo(configuration
            .getPropertyValue(GeneralPropertySet.HOSTNAME), "//Server/@ListenAddress", config);

    }

    public void testDoConfigureSetsAddress() throws Exception
    {
        configuration.setProperty(GeneralPropertySet.HOSTNAME, "localhost");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(configuration.getHome() + "/config.xml");
        XMLAssert.assertXpathEvaluatesTo("localhost", "//Server/@ListenAddress", config);

    }

    @Override
    protected void setUpDataSourceFile() throws Exception
    {
        Dom4JUtil xmlUtil = new Dom4JUtil(getFileHandler());
        String file = configuration.getHome() + "/config.xml";
        Document document = DocumentHelper.createDocument();
        document.addElement("Domain");
        xmlUtil.saveXml(document, file);
    }

}
