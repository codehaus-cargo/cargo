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
package org.codehaus.cargo.container.property;

import java.util.Properties;

import junit.framework.ComparisonFailure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;

/**
 * Unit tests for {@link DataSourceConverter}.
 */
public class DataSourceConverterTest
{

    /**
     * DataSource converter.
     */
    private DataSourceConverter dataSourceConverter;

    /**
     * Creates the test datasource converter.
     */
    @BeforeEach
    protected void setUp()
    {
        dataSourceConverter = new DataSourceConverter();
    }

    /**
     * Test the {@link Properties} constructor.
     */
    @Test
    public void testPropertiesConstructor()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.ID, "rudolf");
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.NO_TRANSACTION.toString());
        props.setProperty(DatasourcePropertySet.JNDI_LOCATION, "jdbc/JiraDS");
        props
            .setProperty(DatasourcePropertySet.CONNECTION_TYPE, ConfigurationEntryType.JDBC_DRIVER);
        props.setProperty(DatasourcePropertySet.DRIVER_CLASS, "org.hsqldb.jdbcDriver");
        props.setProperty(DatasourcePropertySet.URL, "postresql:localhost:jirads");
        props.setProperty(DatasourcePropertySet.USERNAME, "sa");
        props.setProperty(DatasourcePropertySet.PASSWORD, "");
        DataSource ds = dataSourceConverter.fromProperties(props);
        Assertions.assertEquals(0, ds.getConnectionProperties().size());
        Assertions.assertEquals("", ds.getPassword());
        Assertions.assertEquals(props, dataSourceConverter.toProperties(ds));
    }

    /**
     * Test that the default value is driver.
     */
    @Test
    public void testDefaultIsDriver()
    {
        Properties props = new Properties();
        DataSource ds = dataSourceConverter.fromProperties(props);
        Assertions.assertEquals("java.sql.Driver", ds.getConnectionType());
    }

    /**
     * Test that the {@link ConfigurationEntryType#JDBC_DRIVER} property is the driver.
     */
    @Test
    public void testDriverIsDriver()
    {
        Properties props = new Properties();
        props
            .setProperty(DatasourcePropertySet.CONNECTION_TYPE, ConfigurationEntryType.JDBC_DRIVER);
        DataSource ds = dataSourceConverter.fromProperties(props);
        Assertions.assertEquals("java.sql.Driver", ds.getConnectionType());
    }

    /**
     * Test that the {@link DatasourcePropertySet#CONNECTION_TYPE} property can define an XA
     * datasource.
     */
    @Test
    public void testXADataSourceIsXADataSource()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.CONNECTION_TYPE,
            ConfigurationEntryType.XA_DATASOURCE);
        DataSource ds = dataSourceConverter.fromProperties(props);
        Assertions.assertEquals("javax.sql.XADataSource", ds.getConnectionType());
        Assertions.assertEquals(TransactionSupport.XA_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the default mode is {@link TransactionSupport#NO_TRANSACTION}.
     */
    @Test
    public void testDefaultIsNoTransaction()
    {
        Properties props = new Properties();
        DataSource ds = dataSourceConverter.fromProperties(props);
        Assertions.assertEquals(TransactionSupport.NO_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the {@link TransactionSupport#NO_TRANSACTION} mode is
     * {@link TransactionSupport#NO_TRANSACTION}.
     */
    @Test
    public void testNoTransactionIsNoTransaction()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.NO_TRANSACTION.toString());
        DataSource ds = dataSourceConverter.fromProperties(props);
        Assertions.assertEquals(TransactionSupport.NO_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the {@link TransactionSupport#LOCAL_TRANSACTION} mode is
     * {@link TransactionSupport#LOCAL_TRANSACTION}.
     */
    @Test
    public void testLocalTransactionIsLocalTransaction()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.LOCAL_TRANSACTION.toString());
        DataSource ds = dataSourceConverter.fromProperties(props);
        Assertions.assertEquals(TransactionSupport.LOCAL_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the {@link TransactionSupport#XA_TRANSACTION} mode is
     * {@link TransactionSupport#XA_TRANSACTION}.
     */
    @Test
    public void testXATransactionIsXATransaction()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.XA_TRANSACTION.toString());
        DataSource ds = dataSourceConverter.fromProperties(props);
        Assertions.assertEquals(TransactionSupport.XA_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that an empty property string generates a <code>null</code>.
     */
    @Test
    public void testIdIsNullWhenPropertyStringIsBlank()
    {
        String propertyString = "";
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals(null, ds.getId());
    }

    /**
     * Test an id with a slash.
     */
    @Test
    public void testIdWithSlash()
    {
        String jndiName = "jdbc/DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals("DataSource", ds.getId());
    }

    /**
     * Test the driver properties to string getter.
     */
    @Test
    public void testGetDriverPropertiesAsString()
    {
        String propertyString = "user=APP;CreateDatabase=create";
        String driverPropertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + propertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(driverPropertyString);
        try
        {
            Assertions.assertEquals(propertyString, dataSourceConverter
                .getConnectionPropertiesAsASemicolonDelimitedString(ds));
        }
        catch (ComparisonFailure e)
        {
            Assertions.assertEquals("CreateDatabase=create;user=APP", dataSourceConverter
                .getConnectionPropertiesAsASemicolonDelimitedString(ds));
        }
    }

    /**
     * Test the empty driver properties getter.
     */
    @Test
    public void testGetEmptyDriverProperties()
    {
        String propertyString = "";
        String driverPropertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + propertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(driverPropertyString);
        Assertions.assertEquals(0, ds.getConnectionProperties().size());
    }

    /**
     * Test the setting of multiple properties delimited by a semicolon.
     */
    @Test
    public void testMultipleDriverPropertiesDelimitedBySemiColon()
    {
        Properties driverProperties = new Properties();
        driverProperties.setProperty("user", "APP");
        driverProperties.setProperty("CreateDatabase", "create");

        String driverPropertyString = "user=APP;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals(driverProperties, ds.getConnectionProperties());
    }

    /**
     * Test the driver properties when the username is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesUsernamePropertySetsUserOnDataSource()
    {
        String driverPropertyString = "user=APP;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals("APP", ds.getUsername());
    }

    /**
     * Test the driver properties when the password is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesPasswordPropertySetsPasswordOnDataSource()
    {
        String driverPropertyString = "password=egg;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals("egg", ds.getPassword());
    }

    /**
     * Test the driver properties when an empty password is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesEmptyPasswordPropertySetsPasswordOnDataSource()
    {
        String driverPropertyString = "password=;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals("", ds.getPassword());
    }

    /**
     * Test the driver properties when the username and the password is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesUserAndPasswordPropertySetsDataSourceUserAndPassword()
    {
        String driverPropertyString = "user=APP;password=egg;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals("APP", ds.getUsername());
        Assertions.assertEquals("egg", ds.getPassword());
    }

    /**
     * Test the driver properties when the username and the password is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesUserAndPasswordPropertyOverrideDSUserAndPassword()
    {
        String driverPropertyString = "user=APP;password=egg;CreateDatabase=create";
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.USERNAME, "sa");
        props.setProperty(DatasourcePropertySet.PASSWORD, "");
        props.setProperty(DatasourcePropertySet.CONNECTION_PROPERTIES, driverPropertyString);
        DataSource ds = dataSourceConverter.fromProperties(props);
        Assertions.assertEquals("APP", ds.getUsername());
        Assertions.assertEquals("egg", ds.getPassword());
    }

    /**
     * Test an id with two slashes.
     */
    @Test
    public void testIdWithTwoSlashes()
    {
        String jndiName = "jdbc/app1/DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals("DataSource", ds.getId());
    }

    /**
     * Test an id with a dot.
     */
    @Test
    public void testIdWithADot()
    {
        String jndiName = "jdbc.DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals("DataSource", ds.getId());
    }

    /**
     * Test an id with a dot and a slash.
     */
    @Test
    public void testIdWithADotAndASlash()
    {
        String jndiName = "jdbc.app1/DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals("DataSource", ds.getId());
    }

    /**
     * Test an id without dots nor slashes.
     */
    @Test
    public void testIdWithoutDotsOrSlashes()
    {
        String jndiName = "DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals(jndiName, ds.getId());
    }

    /**
     * Test an id with a Java column.
     */
    @Test
    public void testIdWithJavaColon()
    {
        String jndiName = "java:DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        Assertions.assertEquals("DataSource", ds.getId());
    }
}
