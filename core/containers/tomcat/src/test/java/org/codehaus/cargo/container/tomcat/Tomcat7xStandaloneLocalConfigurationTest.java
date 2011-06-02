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
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Tests for the Tomcat 7 implementation of StandaloneLocalConfigurationTest
 * 
 * @version $Id$
 */
public class Tomcat7xStandaloneLocalConfigurationTest extends
    Tomcat6xStandaloneLocalConfigurationTest
{

    /**
     * Creates a {@link Tomcat7xStandaloneLocalConfiguration}. {@inheritdoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat7xStandaloneLocalConfiguration(home);
    }

    /**
     * Creates a {@link Tomcat7xInstalledLocalContainer}. {@inheritdoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat7xInstalledLocalContainer(configuration);
    }

    @Override
    protected void setUpManager()
    {
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps/manager");
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps/host-manager");
    }

    /**
     * note that manager is under webapps, not server/webapps in 5x.
     */
    @Override
    public void testConfigureManager()
    {
        configuration.configure(container);
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/webapps/manager"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/webapps/host-manager"));
    }

    /**
     * Test AJP configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureSetsCorrectAJPConnectorIdentifier() throws Exception
    {
        // check protocol instead of classname, as class is not required.
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("AJP/1.3", "//Connector[@port='8009']/@protocol", config);
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
            "//Connector[@protocol='AJP/1.3']/@port", config);
    }

    /**
     * Test AJP configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureSetsAJPPort() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.AJP_PORT, "1001");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("1001", "//Connector[@protocol='AJP/1.3']/@port", config);
    }

    /**
     * Test webapps directory configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureDefaultWebappsDirectory() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("webapps", "//Host/@appBase", config);
    }

    /**
     * Test webapps directory configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureSetsWebappsDirectory() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.WEBAPPS_DIRECTORY, "some_directory");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", "UTF-8");
        XMLAssert.assertXpathEvaluatesTo("some_directory", "//Host/@appBase", config);
    }

}
