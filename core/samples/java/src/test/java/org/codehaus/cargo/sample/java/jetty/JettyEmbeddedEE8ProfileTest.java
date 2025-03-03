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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.sample.java.CargoTestCase;

/**
 * Jetty Embedded EE 8 profile test.
 */
public class JettyEmbeddedEE8ProfileTest extends AbstractJettyEmbeddedEEProfileTest
{
    /**
     * Add the required validators and initializes the Jetty Embedded specific embedded container
     * classpath resolver.
     * @see AbstractJettyEmbeddedEEProfileTest#AbstractJettyEmbeddedEEProfileTest(String)
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public JettyEmbeddedEE8ProfileTest()
    {
        super("ee8");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSupported(String containerId, ContainerType containerType, Method testMethod)
    {
        if (!super.isSupported(containerId, containerType, testMethod))
        {
            return false;
        }

        // Jetty 12.x Embedded has an issue with the ContextHandlerCollection.addHandler
        // method called with an EE8 WebAppContext
        if ("jetty12x".equals(containerId))
        {
            return false;
        }

        return true;
    }

    /**
     * Test Jetty Embedded with EE 8 profile.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testEE8Profile() throws Exception
    {
        for (Map.Entry<String, String> testData
            : new HashMap<String, String>(getTestData().testDataArtifacts).entrySet())
        {
            getTestData().testDataArtifacts.put(testData.getKey(),
                testData.getValue().replace("/deployables-jakarta-ee/", "/deployables/"));
        }
        LocalConfiguration configuration = getLocalContainer().getConfiguration();
        configuration.setProperty(JettyPropertySet.DEPLOYER_EE_VERSION, "ee8");
        testWar("simple");
    }
}
