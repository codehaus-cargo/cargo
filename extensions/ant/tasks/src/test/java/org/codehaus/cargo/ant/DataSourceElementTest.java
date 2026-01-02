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
package org.codehaus.cargo.ant;

import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.property.TransactionSupport;

/**
 * Unit tests for {@link DataSourceElement}.
 */
public class DataSourceElementTest
{

    /**
     * DataSource.
     */
    private DataSourceElement dataSource;

    /**
     * Creates the test datasource.
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        dataSource = new DataSourceElement();
    }

    /**
     * Test that the default value is driver.
     */
    @Test
    public void testDefaultIsDriver()
    {
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("java.sql.Driver", ds.getConnectionType());
    }

    /**
     * Test that the {@link ConfigurationEntryType#JDBC_DRIVER} property is the driver.
     */
    @Test
    public void testDriverIsDriver()
    {
        dataSource.setConnectionType(ConfigurationEntryType.JDBC_DRIVER);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("java.sql.Driver", ds.getConnectionType());
    }

    /**
     * Test that the {@link DatasourcePropertySet#CONNECTION_TYPE} property can define an XA
     * datasource.
     */
    @Test
    public void testXADataSourceIsXADataSource()
    {
        dataSource.setConnectionType(ConfigurationEntryType.XA_DATASOURCE);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("javax.sql.XADataSource", ds.getConnectionType());
        Assertions.assertEquals(TransactionSupport.XA_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the default mode is {@link TransactionSupport#NO_TRANSACTION}.
     */
    @Test
    public void testDefaultIsNoTransaction()
    {
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals(TransactionSupport.NO_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the {@link TransactionSupport#NO_TRANSACTION} mode is
     * {@link TransactionSupport#NO_TRANSACTION}.
     */
    @Test
    public void testNoTransactionIsNoTransaction()
    {
        dataSource.setTransactionSupport(TransactionSupport.NO_TRANSACTION.toString());
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals(TransactionSupport.NO_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the {@link TransactionSupport#LOCAL_TRANSACTION} mode is
     * {@link TransactionSupport#LOCAL_TRANSACTION}.
     */
    @Test
    public void testLocalTransactionIsLocalTransaction()
    {
        dataSource.setTransactionSupport(TransactionSupport.LOCAL_TRANSACTION.toString());
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals(TransactionSupport.LOCAL_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that the {@link TransactionSupport#XA_TRANSACTION} mode is
     * {@link TransactionSupport#XA_TRANSACTION}.
     */
    @Test
    public void testXATransactionIsXATransaction()
    {
        dataSource.setTransactionSupport(TransactionSupport.XA_TRANSACTION.toString());
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals(TransactionSupport.XA_TRANSACTION, ds.getTransactionSupport());
    }

    /**
     * Test that an empty property string generates a <code>null</code>.
     */
    @Test
    public void testIdIsNullWhenPropertyStringIsBlank()
    {
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals(null, ds.getId());
    }

    /**
     * Test an id with a slash.
     */
    @Test
    public void testIdWithSlash()
    {
        String jndiName = "jdbc/DataSource";
        dataSource.setJndiName(jndiName);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("DataSource", ds.getId());
    }

    /**
     * Test the empty driver properties getter.
     */
    @Test
    public void testGetEmptyDriverProperties()
    {
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals(0, ds.getConnectionProperties().size());
    }

    /**
     * Test the driver properties when the username is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesUsernamePropertySetsUserOnDataSource()
    {
        Properties properties = new Properties();
        properties.setProperty("user", "APP");
        properties.setProperty("CreateDatabase", "create");
        dataSource.setConnectionProperties(properties);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("APP", ds.getUsername());
    }

    /**
     * Test the driver properties when the password is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesPasswordPropertySetsPasswordOnDataSource()
    {
        Properties properties = new Properties();
        properties.setProperty("password", "egg");
        properties.setProperty("CreateDatabase", "create");
        dataSource.setConnectionProperties(properties);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("egg", ds.getPassword());
    }

    /**
     * Test the driver properties when an empty password is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesEmptyPasswordPropertySetsPasswordOnDataSource()
    {
        Properties properties = new Properties();
        properties.setProperty("password", "");
        properties.setProperty("CreateDatabase", "create");
        dataSource.setConnectionProperties(properties);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("", ds.getPassword());
    }

    /**
     * Test the driver properties when the username and the password is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesUserAndPasswordPropertySetsDataSourceUserAndPassword()
    {
        Properties properties = new Properties();
        properties.setProperty("user", "APP");
        properties.setProperty("password", "egg");
        properties.setProperty("CreateDatabase", "create");
        dataSource.setConnectionProperties(properties);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("APP", ds.getUsername());
        Assertions.assertEquals("egg", ds.getPassword());
    }

    /**
     * Test the driver properties when the username and the password is set on datasource.
     */
    @Test
    public void testDatabaseDriverPropertiesUserAndPasswordPropertyOverrideDSUserAndPassword()
    {
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        Properties properties = new Properties();
        properties.setProperty("user", "APP");
        properties.setProperty("password", "egg");
        properties.setProperty("CreateDatabase", "create");
        dataSource.setConnectionProperties(properties);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
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
        dataSource.setJndiName(jndiName);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("DataSource", ds.getId());
    }

    /**
     * Test an id with a dot.
     */
    @Test
    public void testIdWithADot()
    {
        String jndiName = "jdbc.DataSource";
        dataSource.setJndiName(jndiName);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("DataSource", ds.getId());
    }

    /**
     * Test an id with a dot and a slash.
     */
    @Test
    public void testIdWithADotAndASlash()
    {
        String jndiName = "jdbc.app1/DataSource";
        dataSource.setJndiName(jndiName);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("DataSource", ds.getId());
    }

    /**
     * Test an id without dots nor slashes.
     */
    @Test
    public void testIdWithoutDotsOrSlashes()
    {
        String jndiName = "DataSource";
        dataSource.setJndiName(jndiName);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals(jndiName, ds.getId());
    }

    /**
     * Test an id with a Java column.
     */
    @Test
    public void testIdWithJavaColon()
    {
        String jndiName = "java:DataSource";
        dataSource.setJndiName(jndiName);
        org.codehaus.cargo.container.configuration.entry.DataSource ds =
                dataSource.createDataSource();
        Assertions.assertEquals("DataSource", ds.getId());
    }
}
