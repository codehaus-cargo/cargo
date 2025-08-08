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
package org.codehaus.cargo.sample.java.jetty;

import java.util.List;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.sample.java.CargoTestCase;

/**
 * Jetty Embedded EE 10 profile test.
 */
public class JettyEmbeddedEE10ProfileTest extends AbstractJettyEmbeddedEEProfileTest
{
    /**
     * Add the required validators and initializes the Jetty Embedded specific embedded container
     * classpath resolver.
     * @see AbstractJettyEmbeddedEEProfileTest#AbstractJettyEmbeddedEEProfileTest(String)
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public JettyEmbeddedEE10ProfileTest()
    {
        super("ee10");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> filterDependencies(List<String> dependencies)
    {
        return dependencies;
    }

    /**
     * Test Jetty Embedded with EE 10 profile.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testEE10Profile() throws Exception
    {
        LocalConfiguration configuration = getLocalContainer().getConfiguration();
        configuration.setProperty(JettyPropertySet.DEPLOYER_EE_VERSION, "ee10");
        testWar("simple", "Sample page for testing");
    }
}
