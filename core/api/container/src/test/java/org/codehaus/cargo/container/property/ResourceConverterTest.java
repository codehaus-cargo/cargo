/* 
 * ========================================================================
 * 
 * Copyright 2004-2006 Vincent Massol.
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

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.property.ResourcePropertySet;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

public class ResourceConverterTest extends TestCase
{

    private ResourceConverter ResourceConverter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        ResourceConverter = new ResourceConverter();
    }

    public void testPropertiesConstructor()
    {
        Properties props = new Properties();

        props.setProperty(ResourcePropertySet.RESOURCE_NAME, "jdbc/JiraDS");
        props.setProperty(ResourcePropertySet.RESOURCE_TYPE, ConfigurationEntryType.XA_DATASOURCE);
        props.setProperty(ResourcePropertySet.RESOURCE_CLASS, "org.hsqldb.jdbcDriver");
        Resource ds = ResourceConverter.fromProperties(props);
        assertEquals(0, ds.getParameters().size());
        assertEquals(props, ResourceConverter.toProperties(ds));
    }

    public void testXAResourceIsXAResource()
    {
        Properties props = new Properties();
        props.setProperty(ResourcePropertySet.RESOURCE_TYPE, ConfigurationEntryType.XA_DATASOURCE);
        Resource ds = ResourceConverter.fromProperties(props);
        assertEquals("javax.sql.XADataSource", ds.getType());
    }

    public void testGetParametersAsString()
    {
        String propertyString = "user=APP;CreateDatabase=create";
        String driverPropertyString = ResourcePropertySet.PARAMETERS + "=" + propertyString;
        Resource ds = ResourceConverter.fromPropertyString(driverPropertyString);
        try
        {
            assertEquals(propertyString, ResourceConverter
                .getParametersAsASemicolonDelimitedString(ds));
        }
        catch (ComparisonFailure e)
        {
            assertEquals("CreateDatabase=create;user=APP", ResourceConverter
                .getParametersAsASemicolonDelimitedString(ds));

        }
    }
    
    public void testGetParametersAsStringContainingBackslashes() {
        String propertyString = "user=APP;path=c:\\users\\me";
        String driverPropertyString = ResourcePropertySet.PARAMETERS + "=" + propertyString;
        Resource ds = ResourceConverter.fromPropertyString(driverPropertyString);
        try
        {
            assertEquals(propertyString, ResourceConverter
                .getParametersAsASemicolonDelimitedString(ds));
        }
        catch (ComparisonFailure e)
        {
            assertEquals("path=c:\\users\\me;user=APP", ResourceConverter
                .getParametersAsASemicolonDelimitedString(ds));

        }
    }

    public void testGetEmptyParameters()
    {
        String propertyString = "";
        String driverPropertyString = ResourcePropertySet.PARAMETERS + "=" + propertyString;
        Resource ds = ResourceConverter.fromPropertyString(driverPropertyString);
        assertEquals(0, ds.getParameters().size());
    }

    public void testMultipleParametersDelimitedBySemiColon()
    {
        Properties parameters = new Properties();
        parameters.setProperty("user", "APP");
        parameters.setProperty("CreateDatabase", "create");

        String driverPropertyString = "user=APP;CreateDatabase=create";
        String propertyString = ResourcePropertySet.PARAMETERS + "=" + driverPropertyString;
        Resource ds = ResourceConverter.fromPropertyString(propertyString);
        assertEquals(parameters, ds.getParameters());
    }

}
