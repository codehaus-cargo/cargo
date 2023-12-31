/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
import java.util.Map;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.configuration.builder.ConfigurationChecker;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.tomcat.internal.AbstractCatalinaStandaloneLocalConfigurationTest;
import org.codehaus.cargo.container.tomcat.internal.Tomcat5x6x7xConfigurationChecker;
import org.custommonkey.xmlunit.XMLAssert;

/**
 * Tests for the Tomcat 5 implementation of StandaloneLocalConfigurationTest
 */
public class Tomcat5xStandaloneLocalConfigurationTest extends
    AbstractCatalinaStandaloneLocalConfigurationTest
{
    /**
     * Creates a {@link Tomcat5xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat5xStandaloneLocalConfiguration(home)
        {
            @Override
            protected void setupConfFiles(String confDir)
            {
                setupManager(container);
            }

            @Override
            protected void configureFiles(
                Map<String, String> replacements, LocalContainer container)
            {
                createServerXml();
                super.configureFiles(replacements, container);
            }
        };
    }

    /**
     * Create a template server.xml needed for performXmlReplacements.
     */
    void createServerXml()
    {
        String file = configuration.getHome() + "/conf/server.xml";
        getFileHandler().writeTextFile(file,
            "<Server><Service>"
                + "<Connector/>"
                + "<Engine><Host>"
                + "<Valve className='org.apache.catalina.valves.AccessLogValve'/>"
                + "</Host></Engine>"
                + "</Service></Server>", StandardCharsets.UTF_8);
    }

    /**
     * Creates a {@link Tomcat5xInstalledLocalContainer}. {@inheritDoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat5xInstalledLocalContainer(configuration);
    }

    /**
     * @return {@link Tomcat5x6x7xConfigurationChecker}.
     */
    @Override
    protected ConfigurationChecker createConfigurationChecker()
    {
        return new Tomcat5x6x7xConfigurationChecker();
    }

    /**
     * {@inheritDoc}
     * @param fixture Resource fixture.
     * @return <code>conf/context.xml</code> in the configuration's home.
     */
    @Override
    protected String getResourceConfigurationFile(ResourceFixture fixture)
    {
        return configuration.getHome() + "/conf/context.xml";
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
            file, "<Engine><Context/></Engine>", StandardCharsets.UTF_8);
    }

    /**
     * Test {@link
     * Tomcat5xStandaloneLocalConfiguration#configure(org.codehaus.cargo.container.LocalContainer)}
     * @throws Exception If anything goes wrong.
     */
    public void testConfigure() throws Exception
    {
        configuration.configure(container);
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/context.xml"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUpManager()
    {
        configuration.getFileHandler().mkdirs(container.getHome() + "/webapps");
        configuration.getFileHandler().mkdirs(container.getHome() + "/server/webapps/manager");
        configuration.getFileHandler().createFile(
            container.getHome() + "/conf/Catalina/localhost/manager.xml");
    }

    /**
     * Test {@link
     * Tomcat5xStandaloneLocalConfiguration#configure(org.codehaus.cargo.container.LocalContainer)}
     * and check manager.
     */
    public void testConfigureManager()
    {
        configuration.configure(container);
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/context.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/conf/Catalina/localhost/manager.xml"));
        assertTrue(configuration.getFileHandler().exists(
            configuration.getHome() + "/server/webapps/manager"));
    }

    /**
     * Assert that the attribute 'startStopThreads' isn't added if the property isn't set.
     * @throws Exception If anything does wrong.
     */
    public void testConfigureSetsHostStartStopThreads() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.HOST_START_STOP_THREADS, "42");
        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathNotExists("//Host/@startStopThreads", config);
    }
}
