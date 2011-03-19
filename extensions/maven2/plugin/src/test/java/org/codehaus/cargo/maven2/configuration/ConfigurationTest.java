/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol.
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
package org.codehaus.cargo.maven2.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.maven2.util.CargoProject;

/**
 * Unit tests for {@link Configuration}.
 * 
 * @version $Id$
 */
public class ConfigurationTest extends TestCase
{
    /**
     * Create an configuration with no properties.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateConfigurationWithNoProperties() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setImplementation(StandaloneLocalConfigurationStub.class.getName());
        configuration.setHome("/some/path");

        configuration.createConfiguration("testcontainer", ContainerType.INSTALLED, null);
    }

    /**
     * Setting a Null property is the way Maven2 operates when the user specifies an empty element.
     * We need to verify that the Cargo plugin intercepts that and replaces the Null with an empty
     * String.
     * @throws Exception If anything goes wrong.
     */
    public void testCreateConfigurationWithAPropertyWithNullValue() throws Exception
    {
        Configuration configurationElement = new Configuration();
        configurationElement.setImplementation(StandaloneLocalConfigurationStub.class.getName());

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("someName", null);
        configurationElement.setProperties(properties);

        org.codehaus.cargo.container.configuration.Configuration configuration =
            configurationElement.createConfiguration("testcontainer", ContainerType.INSTALLED,
                new CargoProject(null, null, null, null, null, null, null));

        assertEquals("", configuration.getPropertyValue("someName"));
    }

    /**
     * Test adding resources to the configuration.
     * @throws Exception If anything goes wrong.
     */
    public void testAddResources() throws Exception
    {
        Configuration configurationElement = new Configuration();
        configurationElement.setImplementation(StandaloneLocalConfigurationStub.class.getName());

        Resource resource = new Resource();
        resource.setName("name");
        resource.setType("someType");
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        resource.setParameters(parameters);
        configurationElement.setResources(new Resource[] {resource});

        org.codehaus.cargo.container.configuration.Configuration configuration =
            configurationElement.createConfiguration("testContainer", ContainerType.INSTALLED,
                new CargoProject(null, null, null, null, null, null, null));

        StandaloneLocalConfigurationStub conf = (StandaloneLocalConfigurationStub) configuration;
        List<org.codehaus.cargo.container.configuration.entry.Resource> resources = conf
            .getResources();
        assertEquals("resources not of correct size", 1, resources.size());
        org.codehaus.cargo.container.configuration.entry.Resource r =
            (org.codehaus.cargo.container.configuration.entry.Resource) resources.get(0);
        assertEquals("name not correct", "name", r.getName());
        assertEquals("type not correct", "someType", r.getType());
    }

}
