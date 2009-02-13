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
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.property.DatasourcePropertySet;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

public class DataSourceConverterTest extends TestCase
{

    private DataSourceConverter dataSourceConverter;

    public void setUp() throws Exception
    {
        super.setUp();
        dataSourceConverter = new DataSourceConverter();
    }

    public void testPropertiesConstructor()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.ID, "rudolf");
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.NO_TRANSACTION.toString());
        props.setProperty(DatasourcePropertySet.JNDI_LOCATION, "jdbc/JiraDS");
        props.setProperty(DatasourcePropertySet.CONNECTION_TYPE, ConfigurationEntryType.JDBC_DRIVER);
        props.setProperty(DatasourcePropertySet.DRIVER_CLASS, "org.hsqldb.jdbcDriver");
        props.setProperty(DatasourcePropertySet.URL, "postresql:localhost:jirads");
        props.setProperty(DatasourcePropertySet.USERNAME, "sa");
        props.setProperty(DatasourcePropertySet.PASSWORD, "");
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals(0, ds.getConnectionProperties().size());
        assertEquals("", ds.getPassword());
        assertEquals(props, dataSourceConverter.toProperties(ds));
    }
    
    public void testDefalutIsDriver()
    {
        Properties props = new Properties();
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals("java.sql.Driver", ds.getConnectionType());
    }
    
    public void testDriverIsDriver()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.CONNECTION_TYPE, ConfigurationEntryType.JDBC_DRIVER);
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals("java.sql.Driver", ds.getConnectionType());
    }

    public void testXADataSourceIsXADataSource()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.CONNECTION_TYPE, ConfigurationEntryType.XA_DATASOURCE);
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals("javax.sql.XADataSource", ds.getConnectionType());
        assertEquals(TransactionSupport.XA_TRANSACTION, ds.getTransactionSupport());

    }

    public void testDefaultIsNO_TRANSACTION()
    {
        Properties props = new Properties();
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals(TransactionSupport.NO_TRANSACTION, ds.getTransactionSupport());
    }

    public void testNO_TRANSACTIONIsNO_TRANSACTION()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.NO_TRANSACTION.toString());
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals(TransactionSupport.NO_TRANSACTION, ds.getTransactionSupport());
    }

    public void testLOCAL_TRANSACTIONIsLOCAL_TRANSACTION()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.LOCAL_TRANSACTION.toString());
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals(TransactionSupport.LOCAL_TRANSACTION, ds.getTransactionSupport());
    }

    public void testXA_TRANSACTIONIsXA_TRANSACTION()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.XA_TRANSACTION.toString());
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals(TransactionSupport.XA_TRANSACTION, ds.getTransactionSupport());
    }

    public void testIdIsNullWhenPropertyStringIsBlank()
    {
        String propertyString = "";
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals(null, ds.getId());
    }

    public void testIdWithSlash()
    {
        String jndiName = "jdbc/DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }

    public void testGetDriverPropertiesAsString()
    {
        String propertyString = "user=APP;CreateDatabase=create";
        String driverPropertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + propertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(driverPropertyString);
        try
        {
            assertEquals(propertyString, dataSourceConverter
                .getConnectionPropertiesAsASemicolonDelimitedString(ds));
        }
        catch (ComparisonFailure e)
        {
            assertEquals("CreateDatabase=create;user=APP", dataSourceConverter
                .getConnectionPropertiesAsASemicolonDelimitedString(ds));

        }
    }

    public void testGetEmptyDriverProperties()
    {
        String propertyString = "";
        String driverPropertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + propertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(driverPropertyString);
        assertEquals(0, ds.getConnectionProperties().size());
    }

    public void testMultipleDriverPropertiesDelimitedBySemiColon()
    {
        Properties driverProperties = new Properties();
        driverProperties.setProperty("user", "APP");
        driverProperties.setProperty("CreateDatabase", "create");

        String driverPropertyString = "user=APP;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals(driverProperties, ds.getConnectionProperties());
    }

    public void testDatabaseDriverPropertiesUsernamePropertySetsUserOnDataSource()
    {
        String driverPropertyString = "user=APP;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("APP", ds.getUsername());
    }

    public void testDatabaseDriverPropertiesPasswordPropertySetsPasswordOnDataSource()
    {
        String driverPropertyString = "password=egg;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("egg", ds.getPassword());
    }

    public void testDatabaseDriverPropertiesEmptyPasswordPropertySetsPasswordOnDataSource()
    {
        String driverPropertyString = "password=;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("", ds.getPassword());
    }

    public void testDatabaseDriverPropertiesUsernameAndPasswordPropertySetsUserAndPasswordOnDataSource()
    {
        String driverPropertyString = "user=APP;password=egg;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("APP", ds.getUsername());
        assertEquals("egg", ds.getPassword());
    }

    public void testDatabaseDriverPropertiesUsernameAndPasswordPropertyOverrideUserAndPasswordOnDataSource()
    {
        String driverPropertyString = "user=APP;password=egg;CreateDatabase=create";
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.USERNAME, "sa");
        props.setProperty(DatasourcePropertySet.PASSWORD, "");
        props.setProperty(DatasourcePropertySet.CONNECTION_PROPERTIES, driverPropertyString);
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals("APP", ds.getUsername());
        assertEquals("egg", ds.getPassword());
    }

    public void testIdWithTwoSlashes()
    {
        String jndiName = "jdbc/app1/DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }

    public void testIdWithADot()
    {
        String jndiName = "jdbc.DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }

    public void testIdWithADotAndASlash()
    {
        String jndiName = "jdbc.app1/DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }

    public void testIdWithoutDotsOrSlashes()
    {
        String jndiName = "DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals(jndiName, ds.getId());
    }

    public void testIdWithJavaColon()
    {
        String jndiName = "java:DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }
}
