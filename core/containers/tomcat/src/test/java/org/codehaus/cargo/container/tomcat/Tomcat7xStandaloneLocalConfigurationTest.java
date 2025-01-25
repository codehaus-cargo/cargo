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
import java.util.Map;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Tests for the Tomcat 7 implementation of StandaloneLocalConfigurationTest
 */
public class Tomcat7xStandaloneLocalConfigurationTest extends
    Tomcat6xStandaloneLocalConfigurationTest
{

    /**
     * Creates a {@link Tomcat7xStandaloneLocalConfiguration}. {@inheritDoc}
     * @param home Configuration home.
     * @return Local configuration for <code>home</code>.
     */
    @Override
    protected LocalConfiguration createLocalConfiguration(String home)
    {
        return new Tomcat7xStandaloneLocalConfiguration(home)
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
     * Creates a {@link Tomcat7xInstalledLocalContainer}. {@inheritDoc}
     * @param configuration Container's configuration.
     * @return Local container for <code>configuration</code>.
     */
    @Override
    protected InstalledLocalContainer createLocalContainer(LocalConfiguration configuration)
    {
        return new Tomcat7xInstalledLocalContainer(configuration);
    }

    /**
     * Checks the activation of multipart parsing.
     */
    @Test
    public void testExtraContextAttributes()
    {
        Assertions.assertTrue(Boolean.parseBoolean(
            configuration.getProperties().get(TomcatPropertySet.CONTEXT_ALLOW_MULTIPART)));
        Assertions.assertTrue(Boolean.parseBoolean(
            configuration.getProperties().get(TomcatPropertySet.CONTEXT_ALLOW_WEB_JARS)));
    }

    /**
     * Assert that the attribute 'startStopThreads' isn't added if the property isn't set.
     * @throws Exception If anything does wrong.
     */
    @Test
    public void testConfigureWithoutHostStartStopThreads() throws Exception
    {
        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathNotExists("//Host/@startStopThreads", config);
    }

    /**
     * Assert that the attribute 'startStopThreads' is added to the property's value .
     * @throws Exception If anything does wrong.
     */
    @Override
    public void testConfigureSetsHostStartStopThreads() throws Exception
    {
        configuration.setProperty(TomcatPropertySet.HOST_START_STOP_THREADS, "42");
        configuration.configure(container);

        String config = configuration.getFileHandler().readTextFile(
                configuration.getHome() + "/conf/server.xml", StandardCharsets.UTF_8);
        XMLAssert.assertXpathEvaluatesTo("42", "//Host/@startStopThreads", config);
    }
}
