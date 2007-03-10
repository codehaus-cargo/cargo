/*
 * ========================================================================
 *
 * Copyright 2005 Vincent Massol.
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

import java.util.Map;
import java.util.HashMap;

import junit.framework.TestCase;
import org.codehaus.cargo.container.stub.StandaloneLocalConfigurationStub;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.maven2.configuration.Configuration;

public class ConfigurationTest extends TestCase
{
    public void testCreateConfigurationWithNoProperties() throws Exception
    {
        Configuration configuration = new Configuration();
        configuration.setImplementation(StandaloneLocalConfigurationStub.class.getName());
        configuration.setHome("/some/path");

        configuration.createConfiguration("testcontainer", ContainerType.INSTALLED, null);
    }

    /**
     * Setting a Null property is the way Maven2 operates when the user specifies an empty element.
     * We need to verify that the Cargo plugin intercepts that and replaces the Null with an
     * empty String.
     */
    public void testCreateConfigurationWithAPropertyWithNullValue() throws Exception
    {
        Configuration configurationElement = new Configuration();
        configurationElement.setImplementation(StandaloneLocalConfigurationStub.class.getName());

        Map properties = new HashMap();
        properties.put("someName", null);
        configurationElement.setProperties(properties);

        org.codehaus.cargo.container.configuration.Configuration configuration =
            configurationElement.createConfiguration("testcontainer", ContainerType.INSTALLED,
                null);

        assertEquals("", configuration.getPropertyValue("someName"));
    }

}
