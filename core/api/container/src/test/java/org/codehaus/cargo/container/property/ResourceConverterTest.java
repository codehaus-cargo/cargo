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
package org.codehaus.cargo.container.property;

import java.util.Properties;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.Resource;

/**
 * Unit tests for {@link ResourceConverter}.
 * 
 * @version $Id$
 */
public class ResourceConverterTest extends TestCase
{

    /**
     * Resource converter.
     */
    private ResourceConverter resourceConverter;

    /**
     * Creates the test resource converter. {@inheritdoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        resourceConverter = new ResourceConverter();
    }

    /**
     * Test the {@link Properties} constructor.
     */
    public void testPropertiesConstructor()
    {
        Properties props = new Properties();

        props.setProperty(ResourcePropertySet.RESOURCE_NAME, "jdbc/JiraDS");
        props.setProperty(ResourcePropertySet.RESOURCE_TYPE, ConfigurationEntryType.XA_DATASOURCE);
        props.setProperty(ResourcePropertySet.RESOURCE_CLASS, "org.hsqldb.jdbcDriver");
        Resource ds = resourceConverter.fromProperties(props);
        assertEquals(0, ds.getParameters().size());
        assertEquals(props, resourceConverter.toProperties(ds));
    }

    /**
     * Test that {@link ResourcePropertySet#RESOURCE_TYPE} sets
     * {@link ResourcePropertySet#RESOURCE_TYPE}.
     */
    public void testXAResourceIsXAResource()
    {
        Properties props = new Properties();
        props.setProperty(ResourcePropertySet.RESOURCE_TYPE, ConfigurationEntryType.XA_DATASOURCE);
        Resource ds = resourceConverter.fromProperties(props);
        assertEquals("javax.sql.XADataSource", ds.getType());
    }

    /**
     * Test string parsing.
     */
    public void testGetParametersAsString()
    {
        String propertyString = "user=APP;CreateDatabase=create";
        String driverPropertyString = ResourcePropertySet.PARAMETERS + "=" + propertyString;
        Resource ds = resourceConverter.fromPropertyString(driverPropertyString);
        try
        {
            assertEquals(propertyString, resourceConverter
                .getParametersAsASemicolonDelimitedString(ds));
        }
        catch (ComparisonFailure e)
        {
            assertEquals("CreateDatabase=create;user=APP", resourceConverter
                .getParametersAsASemicolonDelimitedString(ds));

        }
    }

    /**
     * Test string parsing when the property has backslashes.
     */
    public void testGetParametersAsStringContainingBackslashes()
    {
        String propertyString = "user=APP;path=c:\\users\\me";
        String driverPropertyString = ResourcePropertySet.PARAMETERS + "=" + propertyString;
        Resource ds = resourceConverter.fromPropertyString(driverPropertyString);
        try
        {
            assertEquals(propertyString, resourceConverter
                .getParametersAsASemicolonDelimitedString(ds));
        }
        catch (ComparisonFailure e)
        {
            assertEquals("path=c:\\users\\me;user=APP", resourceConverter
                .getParametersAsASemicolonDelimitedString(ds));

        }
    }

    /**
     * Test get empty parameters.
     */
    public void testGetEmptyParameters()
    {
        String propertyString = "";
        String driverPropertyString = ResourcePropertySet.PARAMETERS + "=" + propertyString;
        Resource ds = resourceConverter.fromPropertyString(driverPropertyString);
        assertEquals(0, ds.getParameters().size());
    }

    /**
     * Test get multiple parameters delimited by a semicolon.
     */
    public void testMultipleParametersDelimitedBySemiColon()
    {
        Properties parameters = new Properties();
        parameters.setProperty("user", "APP");
        parameters.setProperty("CreateDatabase", "create");

        String driverPropertyString = "user=APP;CreateDatabase=create";
        String propertyString = ResourcePropertySet.PARAMETERS + "=" + driverPropertyString;
        Resource ds = resourceConverter.fromPropertyString(propertyString);
        assertEquals(parameters, ds.getParameters());
    }

}
