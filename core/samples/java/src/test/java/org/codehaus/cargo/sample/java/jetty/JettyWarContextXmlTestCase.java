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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.jetty.JettyPropertySet;
import org.codehaus.cargo.sample.java.AbstractWarTestCase;
import org.codehaus.cargo.sample.java.CargoTestCase;
import org.codehaus.cargo.sample.java.validator.IsInstalledLocalContainerValidator;
import org.codehaus.cargo.sample.java.validator.StartsWithContainerValidator;
import org.codehaus.cargo.sample.java.validator.SupportsPropertyValidator;

/**
 * Test for Jetty Installed Local container deploying WARs using the <code>context.xml</code> file.
 */
public class JettyWarContextXmlTestCase extends AbstractWarTestCase
{
    /**
     * Add the required validators.
     * @see #addValidator(org.codehaus.cargo.sample.java.validator.Validator)
     */
    public JettyWarContextXmlTestCase()
    {
        this.addValidator(new IsInstalledLocalContainerValidator());
        this.addValidator(new StartsWithContainerValidator("jetty"));
        this.addValidator(new SupportsPropertyValidator(
            ConfigurationType.STANDALONE, JettyPropertySet.DEPLOYER_CREATE_CONTEXT_XML));
    }

    /**
     * Test Jetty Standalone Local container deploying the sample WAR using the
     * <code>context.xml</code> file and various EE versions.
     * @param jettyEeVersion Jetty EE version.
     * @throws Exception If anything goes wrong.
     */
    protected void testWarContextXml(String jettyEeVersion) throws Exception
    {
        if (!getLocalContainer().getConfiguration().getCapability().supportsProperty(
            JettyPropertySet.DEPLOYER_EE_VERSION))
        {
            setContainer(null);
            Assumptions.abort("Provided Jetty container doesn't support multiple EE versions");
        }
        if ("ee11".equals(jettyEeVersion))
        {
            if (getContainer().getName().startsWith("Jetty 12.0"))
            {
                setContainer(null);
                Assumptions.abort("Jetty 12.0 doesn't support EE11");
            }
        }
        if ("ee8".equals(jettyEeVersion) || "ee9".equals(jettyEeVersion))
        {
            for (Map.Entry<String, String> testData
                : new HashMap<String, String>(getTestData().testDataArtifacts).entrySet())
            {
                getTestData().testDataArtifacts.put(testData.getKey(),
                    testData.getValue().replace("/deployables-jakarta-ee/", "/deployables/"));
            }
        }

        File contextXml = new File(getLocalContainer().getConfiguration().getHome());
        contextXml = new File(contextXml, "webapps/simple-war.xml");

        Assertions.assertFalse(contextXml.exists(), "Context XML already exists");
        LocalConfiguration configuration = getLocalContainer().getConfiguration();
        configuration.setProperty(JettyPropertySet.MODULES,
            configuration.getPropertyValue(JettyPropertySet.MODULES).replace(
                configuration.getPropertyValue(JettyPropertySet.DEPLOYER_EE_VERSION) + "-",
                    jettyEeVersion + "-"));
        configuration.setProperty(JettyPropertySet.DEPLOYER_EE_VERSION, jettyEeVersion);
        configuration.setProperty(JettyPropertySet.DEPLOYER_CREATE_CONTEXT_XML, "true");
        testWar("simple", "Sample page for testing");
        Assertions.assertTrue(contextXml.exists(), "Context XML not created");
    }

    /**
     * Test Jetty Standalone Local container deploying the sample WAR using the
     * <code>context.xml</code> file.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testWarContextXml() throws Exception
    {
        File contextXml = new File(getLocalContainer().getConfiguration().getHome());
        if ("jetty6x".equals(getContainer().getId())
            || "jetty7x".equals(getContainer().getId())
            || "jetty8x".equals(getContainer().getId()))
        {
            contextXml = new File(contextXml, "contexts");
        }
        else
        {
            contextXml = new File(contextXml, "webapps");
        }
        contextXml = new File(contextXml, "simple-war.xml");

        Assertions.assertFalse(contextXml.exists(), "Context XML already exists");
        LocalConfiguration configuration = getLocalContainer().getConfiguration();
        configuration.setProperty(JettyPropertySet.DEPLOYER_CREATE_CONTEXT_XML, "true");
        testWar("simple", "Sample page for testing");
        Assertions.assertTrue(contextXml.exists(), "Context XML not created");
    }

    /**
     * Test Jetty Standalone Local container deploying the sample WAR using the
     * <code>context.xml</code> file with EE 8 profile.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testWarContextXmlEE8Profile() throws Exception
    {
        testWarContextXml("ee8");
    }

    /**
     * Test Jetty Standalone Local container deploying the sample WAR using the
     * <code>context.xml</code> file with EE 9 profile.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testWarContextXmlEE9Profile() throws Exception
    {
        testWarContextXml("ee9");
    }

    /**
     * Test Jetty Standalone Local container deploying the sample WAR using the
     * <code>context.xml</code> file with EE 10 profile.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testWarContextXmlEE10Profile() throws Exception
    {
        testWarContextXml("ee10");
    }

    /**
     * Test Jetty Standalone Local container deploying the sample WAR using the
     * <code>context.xml</code> file with EE 11 profile.
     * @throws Exception If anything goes wrong.
     */
    @CargoTestCase
    public void testWarContextXmlEE11Profile() throws Exception
    {
        testWarContextXml("ee11");
    }

}
