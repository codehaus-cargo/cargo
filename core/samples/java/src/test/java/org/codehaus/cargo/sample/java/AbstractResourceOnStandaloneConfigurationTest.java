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
package org.codehaus.cargo.sample.java;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.entry.ResourceFixture;
import org.codehaus.cargo.container.property.ResourcePropertySet;

/**
 * Test for resource capabilities.
 */
public abstract class AbstractResourceOnStandaloneConfigurationTest extends AbstractWarTestCase
{
    /**
     * Initializes the test case.
     * @param testName Test name.
     * @param testData Test environment data.
     * @throws Exception If anything goes wrong.
     */
    public AbstractResourceOnStandaloneConfigurationTest(String testName,
        EnvironmentTestData testData) throws Exception
    {
        super(testName, testData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setContainer(createContainer(createConfiguration(ConfigurationType.STANDALONE)));
    }

    /**
     * Add resource to configuration using properties.
     * @param fixture Container.
     */
    protected void addResourceToConfigurationViaProperty(ResourceFixture fixture)
    {
        Configuration config = getLocalContainer().getConfiguration();
        String resourcePropertyString = fixture.buildResourcePropertyString();
        if (EnvironmentTestData.jakartaEeContainers.contains(getContainer().getId()))
        {
            resourcePropertyString = resourcePropertyString.replace("javax.jms.", "jakarta.jms.");
            resourcePropertyString =
                resourcePropertyString.replace("javax.mail.", "jakarta.mail.");
        }
        config.setProperty(ResourcePropertySet.RESOURCE, resourcePropertyString);
    }
}
