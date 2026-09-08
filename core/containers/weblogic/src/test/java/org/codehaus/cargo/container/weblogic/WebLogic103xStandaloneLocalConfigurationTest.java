/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link WebLogic103xStandaloneLocalConfiguration}.
 */
public class WebLogic103xStandaloneLocalConfigurationTest
    extends WebLogic10xStandaloneLocalConfigurationTest
{
    /**
     * WL_HOME
     */
    private static final String WL_HOME = BEA_HOME + "/weblogic103";

    /**
     * Configuration version.
     */
    private static final String CONFIGURATION_VERSION = "10.3.0.2";

    /**
     * Domain version.
     */
    private static final String DOMAIN_VERSION = "10.3.0.1";

    /**
     * {@inheritDoc}
     */
    @BeforeEach
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.configuration = new WebLogic103xStandaloneLocalConfiguration(DOMAIN_HOME);
        this.configuration.setFileHandler(this.fileHandler);

        this.container = new WebLogic103xInstalledLocalContainer(configuration);
        this.container.setHome(WL_HOME);
        this.container.setFileHandler(this.fileHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testConstructorSetsPropertyDefaults() throws Exception
    {
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.ADMIN_USER), "weblogic");
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.ADMIN_PWD), "weblogic");
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.SERVER), "server");
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.CONFIGURATION_VERSION), "10.3.0.0");
        Assertions.assertEquals(
            configuration.getPropertyValue(WebLogicPropertySet.DOMAIN_VERSION), "10.3.0.0");
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testDoConfigureSetsDomainVersion() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.DOMAIN_VERSION, DOMAIN_VERSION);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(DOMAIN_VERSION, xpathEngine.evaluate(
            "//weblogic:domain-version", toSource(config)));
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testDoConfigureSetsConfigurationVersion() throws Exception
    {
        configuration.setProperty(WebLogicPropertySet.CONFIGURATION_VERSION,
            CONFIGURATION_VERSION);
        configuration.doConfigure(container);
        String config = configuration.getFileHandler().readTextFile(
            DOMAIN_HOME + "/config/config.xml", StandardCharsets.UTF_8);
        Assertions.assertEquals(CONFIGURATION_VERSION, xpathEngine.evaluate(
            "//weblogic:configuration-version", toSource(config)));
    }

}
