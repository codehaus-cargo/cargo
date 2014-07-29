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
import org.codehaus.cargo.container.configuration.entry.DataSource;

/**
 * Unit tests for {@link DataSourceConverter}.
 * 
 * @version $Id$
 */
public class DataSourceConverterTest extends TestCase
{

    /**
     * DataSource converter.
     */
    private DataSourceConverter dataSourceConverter;

    /**
     * Creates the test datasource converter. {@inheritDoc}
     * @throws Exception If anything goes wrong.
     */
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        dataSourceConverter = new DataSourceConverter();
    }

    /**
     * Test the {@link Properties} constructor.
     */
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
        assertEquals(0, ds.getConnectionProperties().size());
        assertEquals("", ds.getPassword());
        assertEquals(props, dataSourceConverter.toProperties(ds));
    }

    /**
     * Test that the default value is driver.
     */
    public void testDefaultIsDriver()
    {
        Properties props = new Properties();
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals("java.sql.Driver", ds.getConnectionType());
    }

    /**
     * Test that the {@link ConfigurationEntryType#JDBC_DRIVER} property is the driver.
     */
    public void testDriverIsDriver()
    {
        Properties props = new Properties();
        props
            .setProperty(DatasourcePropertySet.CONNECTION_TYPE, ConfigurationEntryType.JDBC_DRIVER);
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals("java.sql.Driver", ds.getConnectionType());
    }

    /**
     * Test that the {@link DatasourcePropertySet#CONNECTION_TYPE} property can define an XA
     * datasource.
     */
    public void testXADataSourceIsXADataSource()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.CONNECTION_TYPE,
            ConfigurationEntryType.XA_DATASOURCE);
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals("javax.sql.XADataSource", ds.getConnectionType());
        assertEquals(TransactionSupport.XA_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the default mode is {@link TransactionSupport#NO_TRANSACTION}.
     */
    public void testDefaultIsNoTransaction()
    {
        Properties props = new Properties();
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals(TransactionSupport.NO_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the {@link TransactionSupport#NO_TRANSACTION} mode is
     * {@link TransactionSupport#NO_TRANSACTION}.
     */
    public void testNoTransactionIsNoTransaction()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.NO_TRANSACTION.toString());
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals(TransactionSupport.NO_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the {@link TransactionSupport#LOCAL_TRANSACTION} mode is
     * {@link TransactionSupport#LOCAL_TRANSACTION}.
     */
    public void testLocalTransactionIsLocalTransaction()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.LOCAL_TRANSACTION.toString());
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals(TransactionSupport.LOCAL_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the {@link TransactionSupport#XA_TRANSACTION} mode is
     * {@link TransactionSupport#XA_TRANSACTION}.
     */
    public void testXATransactionIsXATransaction()
    {
        Properties props = new Properties();
        props.setProperty(DatasourcePropertySet.TRANSACTION_SUPPORT,
            TransactionSupport.XA_TRANSACTION.toString());
        DataSource ds = dataSourceConverter.fromProperties(props);
        assertEquals(TransactionSupport.XA_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that an empty property string generates a <code>null</code>.
     */
    public void testIdIsNullWhenPropertyStringIsBlank()
    {
        String propertyString = "";
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals(null, ds.getId());
    }

    /**
     * Test an id with a slash.
     */
    public void testIdWithSlash()
    {
        String jndiName = "jdbc/DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }

    /**
     * Test the driver properties to string getter.
     */
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

    /**
     * Test the empty driver properties getter.
     */
    public void testGetEmptyDriverProperties()
    {
        String propertyString = "";
        String driverPropertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + propertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(driverPropertyString);
        assertEquals(0, ds.getConnectionProperties().size());
    }

    /**
     * Test the setting of multiple properties delimited by a semicolon.
     */
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

    /**
     * Test the driver properties when the username is set on datasource.
     */
    public void testDatabaseDriverPropertiesUsernamePropertySetsUserOnDataSource()
    {
        String driverPropertyString = "user=APP;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("APP", ds.getUsername());
    }

    /**
     * Test the driver properties when the password is set on datasource.
     */
    public void testDatabaseDriverPropertiesPasswordPropertySetsPasswordOnDataSource()
    {
        String driverPropertyString = "password=egg;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("egg", ds.getPassword());
    }

    /**
     * Test the driver properties when an empty password is set on datasource.
     */
    public void testDatabaseDriverPropertiesEmptyPasswordPropertySetsPasswordOnDataSource()
    {
        String driverPropertyString = "password=;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("", ds.getPassword());
    }

    /**
     * Test the driver properties when the username and the password is set on datasource.
     */
    public void testDatabaseDriverPropertiesUserAndPasswordPropertySetsDataSourceUserAndPassword()
    {
        String driverPropertyString = "user=APP;password=egg;CreateDatabase=create";
        String propertyString =
            DatasourcePropertySet.CONNECTION_PROPERTIES + "=" + driverPropertyString;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("APP", ds.getUsername());
        assertEquals("egg", ds.getPassword());
    }

    /**
     * Test the driver properties when the username and the password is set on datasource.
     */
    public void testDatabaseDriverPropertiesUserAndPasswordPropertyOverrideDSUserAndPassword()
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

    /**
     * Test an id with two slashes.
     */
    public void testIdWithTwoSlashes()
    {
        String jndiName = "jdbc/app1/DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }

    /**
     * Test an id with a dot.
     */
    public void testIdWithADot()
    {
        String jndiName = "jdbc.DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }

    /**
     * Test an id with a dot and a slash.
     */
    public void testIdWithADotAndASlash()
    {
        String jndiName = "jdbc.app1/DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }

    /**
     * Test an id without dots nor slashes.
     */
    public void testIdWithoutDotsOrSlashes()
    {
        String jndiName = "DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals(jndiName, ds.getId());
    }

    /**
     * Test an id with a Java column.
     */
    public void testIdWithJavaColon()
    {
        String jndiName = "java:DataSource";
        String propertyString = DatasourcePropertySet.JNDI_LOCATION + "=" + jndiName;
        DataSource ds = dataSourceConverter.fromPropertyString(propertyString);
        assertEquals("DataSource", ds.getId());
    }
}
