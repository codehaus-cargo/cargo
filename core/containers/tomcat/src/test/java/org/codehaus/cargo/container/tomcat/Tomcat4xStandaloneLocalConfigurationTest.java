/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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

import java.nio.charset.StandardCharsets;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfigurationTest;
import org.codehaus.cargo.container.tomcat.internal.Tomcat4xConfigurationChecker;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Tests for the Tomcat 4 implementation of StandaloneLocalConfigurationTest
 */
public class Tomcat4xStandaloneLocalConfigurationTest extends
    AbstractCatalinaStandaloneLocalConfigurationTest
{

    /**
     * Default AJP port.
     */
    protected static final String AJP_PORT = "8001";

    /**
     * Creates a {@link Tomcat4xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat4xStandaloneLocalConfiguration(home);
    }

    /**
     * Creates a {@link Tomcat4xInstalledLocalContainer}. {@inheritDoc}
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
     * {@inheritDoc}
     * @param fixture Resource fixture.
     * @return <code>conf/server.xml</code> in the configuration's home.
     */
    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return configuration.getHome() + "/conf/server.xml";
    }

    /**
     * {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    protected void setUpResourceFile() throws Exception
    {
        String file = getResourceConfigurationFile(null);
        getFileHandler().writeTextFile(
            file, "<Engine><DefaultContext/></Engine>", StandardCharsets.UTF_8);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUpManager()
    {
        configuration.getFileHandler().mkdirs(container.getHome() + "/conf");
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps");
        configuration.getFileHandler().mkdirs(container.getHome() + "/server/webapps/manager");
        configuration.getFileHandler().createFile(container.getHome() + "/webapps/manager.xml");
        // seems copy needs to have a file present
        configuration.getFileHandler().createFile(
            container.getHome() + "/server/webapps/manager/touch.txt");
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
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
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
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo(AJP_PORT,
            "//Connector[@className='org.apache.ajp.tomcat4.Ajp13Connector']/@port", config);
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
        String xml = configuration.getFileHandler().readTextFile(
            getDataSourceConfigurationFile(null), StandardCharsets.UTF_8);

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

    /**
     * Test webapps directory configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testConfigureDefaultWebappsDirectory() throws Exception
    {
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
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
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo("some_directory", "//Host/@appBase", config);
    }

    /**
     * Assert that the attribute 'startStopThreads' isn't added if the property isn't set.
     * @throws Exception If anything does wrong.
     */
    public void testConfigureSetsHostStartStopThreads() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.HOST_START_STOP_THREADS, "42");
        configuration.configure(container);
        String config =
            configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathNotExists("//Host/@startStopThreads", config);
    }
}
