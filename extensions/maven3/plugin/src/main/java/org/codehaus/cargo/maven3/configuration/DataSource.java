/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.maven3.configuration;

import java.util.Properties;

import org.codehaus.cargo.container.property.TransactionSupport;

/**
 * Holds configuration data for the <code>&lt;datasource&gt;</code> tag used to configure
 * the plugin in the <code>pom.xml</code> file.
 */
public class DataSource
{
    /**
     * JNDI name where to find this DataSource (typically <code>java:comp/env</code>).
     */
    private String jndiName;

    /**
     * Type of this DataSource, for example
     * <code>javax.sql.XADataSource</code> or <code>javax.sql.Driver</code>.
     * 
     * @see org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType
     */
    private String connectionType;

    /**
     * Transaction support of the datasource ex. <code>XA_TRANSACTION</code>
     * 
     * @see org.codehaus.cargo.container.property.TransactionSupport
     */
    private String transactionSupport;

    /**
     * The class name of the Driver. Example: <code>org.hsqldb.jdbcDriver</code>.
     */
    private String driverClass;

    /**
     * DataSource connection URL.
     */
    private String url;

    /**
     * DataSource username.
     */
    private String username;

    /**
     * DataSource password.
     * Default value is empty password.
     */
    private String password = "";

    /**
     * Id used in configuration files.
     */
    private String id;

    /**
     * Extra properties passed to the DataSource.
     */
    private Properties connectionProperties = new Properties();

    /**
     * @return Extra properties passed to the DataSource.
     */
    public Properties getConnectionProperties()
    {
        return connectionProperties;
    }

    /**
     * @param connectionProperties Extra properties passed to the DataSource.
     */
    public void setConnectionProperties(Properties connectionProperties)
    {
        this.connectionProperties = connectionProperties;
    }

    /**
     * @return Type of this DataSource, for example
     * <code>javax.sql.XADataSource</code> or <code>javax.sql.Driver</code>.
     */
    public String getConnectionType()
    {
        return connectionType;
    }

    /**
     * @param connectionType Type of this DataSource, for example
     * <code>javax.sql.XADataSource</code> or <code>javax.sql.Driver</code>.
     */
    public void setConnectionType(String connectionType)
    {
        this.connectionType = connectionType;
    }

    /**
     * @return The class name of the Driver.
     */
    public String getDriverClass()
    {
        return driverClass;
    }

    /**
     * @param driverClass The class name of the Driver.
     */
    public void setDriverClass(String driverClass)
    {
        this.driverClass = driverClass;
    }

    /**
     * @return Id used in configuration files.
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id Id used in configuration files.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return JNDI name where to find this DataSource.
     */
    public String getJndiName()
    {
        return jndiName;
    }

    /**
     * @param jndiName JNDI name where to find this DataSource.
     */
    public void setJndiName(String jndiName)
    {
        this.jndiName = jndiName;
    }

    /**
     * @return DataSource password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * @param password DataSource password.
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @return Transaction support of the datasource ex. <code>XA_TRANSACTION</code>
     */
    public String getTransactionSupport()
    {
        return transactionSupport;
    }

    /**
     * @param transactionSupport Transaction support of the datasource ex.
     * <code>XA_TRANSACTION</code>
     */
    public void setTransactionSupport(String transactionSupport)
    {
        this.transactionSupport = transactionSupport;
    }

    /**
     * @return DataSource connection URL.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * @param url DataSource connection URL.
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @return DataSource username.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @param username DataSource username.
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Create the datasource object.
     * @return Cargo resource object.
     */
    public org.codehaus.cargo.container.configuration.entry.DataSource createDataSource()
    {
        TransactionSupport tSupport = TransactionSupport.valueOf(transactionSupport);
        org.codehaus.cargo.container.configuration.entry.DataSource dataSource =
                new org.codehaus.cargo.container.configuration.entry.DataSource(
                        jndiName, connectionType, tSupport, driverClass, url, username,
                        password, id, connectionProperties);
        return dataSource;
    }
}
