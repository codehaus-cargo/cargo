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
package org.codehaus.cargo.sample.java.tomcat;

import java.io.File;

import org.junit.jupiter.api.Assertions;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;
import org.codehaus.cargo.sample.java.AbstractWarTestCase;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.SupportsPropertyValidator;

/**
 * Test for Tomcat TLS configuration options.
 */
public class TomcatTLSTest extends AbstractWarTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public TomcatTLSTest()
    {
        this.addValidator(new IsInstalledLocalContainerValidator());
        this.addValidator(new StartsWithContainerValidator("tomcat", "tomee"));
        this.addValidator(new SupportsPropertyValidator(
            ConfigurationType.STANDALONE, TomcatPropertySet.CONNECTOR_KEY_STORE_FILE));
    }

    /**
     * Create an package Tomcat container.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testTlsConfigContainer() throws Exception
    {
        File localhostJksFile = new File("target/test-classes/localhost.jks");
        Assertions.assertTrue(localhostJksFile.isFile());

        LocalConfiguration configuration = this.getLocalContainer().getConfiguration();

        // First, create a configuration using the SSL configuration options,
        // then put a WAR on it, finally start it and test for it to be running.
        configuration.setProperty(GeneralPropertySet.PROTOCOL, "https");
        configuration.setProperty(TomcatPropertySet.CONNECTOR_KEY_STORE_FILE,
            localhostJksFile.getAbsolutePath());
        configuration.setProperty(TomcatPropertySet.CONNECTOR_KEY_STORE_PASSWORD, "password");

        testWar("simple", "Sample page for testing");
    }
}
