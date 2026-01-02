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
package org.codehaus.cargo.sample.java.jetty;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.sample.java.CargoTestCase;

/**
 * Jetty Embedded EE 11 profile test.
 */
public class JettyEmbeddedEE11ProfileTest extends AbstractJettyEmbeddedEEProfileTest
{
    /**
     * Add the required validators and initializes the Jetty Embedded specific embedded container
     * classpath resolver.
     * @see AbstractJettyEmbeddedEEProfileTest#AbstractJettyEmbeddedEEProfileTest(String)
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public JettyEmbeddedEE11ProfileTest()
    {
        super("ee11");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp(
        CargoTestCase.CargoTestcaseInvocationContext cargoContext, ExtensionContext testContext)
        throws Exception
    {
        String containerUrl =
            System.getProperty("cargo." + cargoContext.getContainerId() + ".url");
        if (containerUrl != null && containerUrl.contains("jetty-home-12.0."))
        {
            Assumptions.abort("Jetty 12.0 doesn't support EE11");
        }
        super.setUp(cargoContext, testContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> filterDependencies(List<String> dependencies)
    {
        List<String> result = new ArrayList<String>(dependencies.size());
        for (String dependency : dependencies)
        {
            if ("lib/jakarta.servlet-api-6.0.*.jar".equals(dependency))
            {
                result.add("lib/jakarta.servlet-api-6.1.*.jar");
            }
            else
            {
                result.add(dependency);
            }
        }
        return result;
    }

    /**
     * Test Jetty Embedded with EE 11 profile.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testEE11Profile() throws Exception
    {
        LocalConfiguration configuration = getLocalContainer().getConfiguration();
        configuration.setProperty(JettyPropertySet.DEPLOYER_EE_VERSION, "ee11");
        testWar("simple", "Sample page for testing");
    }
}
