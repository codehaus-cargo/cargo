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
package org.codehaus.cargo.container.configuration.entry;

import java.util.Properties;

import org.codehaus.cargo.container.property.TransactionSupport;

/**
 * A Datasource is a representation of a JDBC datasource. If supported by the container, this
 * property is used to setup a datasource.
 * 
 * @version $Id$
 */
public class DataSource
{
    /**
     * (typically <code>java:comp/env</code>).
     */
    private String jndiLocation;

    /**
     * <code>javax.sql.XADataSource</code> or <code>javax.sql.Driver</code>.
     * 
     * @see org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType
     */
    private String connectionType;

    /**
     * transaction support of the datasource ex. <code>XA_TRANSACTION</code>
     */
    private TransactionSupport transactionSupport;

    /**
     * The class name of the Driver. Example: <code>org.hsqldb.jdbcDriver</code>.
     */
    private String driverClass;

    /**
     * The url of the driver.
     */
    private String url;

    /**
     * The username.
     */
    private String username;

    /**
     * The password.
     */
    private String password;

    /**
     * id used in configuration files.
     */
    private String id;

    /**
     * extra properties passed to the jdbc driver.
     */
    private Properties connectionProperties;

    /**
     * initializes connectionProperties to a new object.
     */
    public DataSource()
    {
        connectionProperties = new Properties();
    }

    /**
     * @param jndiLocation where to bind this DataSource (typically <code>java:comp/env</code>).
     */
    public void setJndiLocation(String jndiLocation)
    {
        this.jndiLocation = jndiLocation;
    }

    /**
     * @param connectionType what to use to get a connection from the database.
     * <code>javax.sql.XADataSource</code> or <code>javax.sql.Driver</code>.
     */
    public void setConnectionType(String connectionType)
    {
        this.connectionType = connectionType;
    }

    /**
     * @param transactionSupport transaction support of the datasource ex.
     * <code>XA_TRANSACTION</code>
     */
    public void setTransactionSupport(TransactionSupport transactionSupport)
    {
        this.transactionSupport = transactionSupport;
    }

    /**
     * @param driverClass The class name of the Driver or XADataSource. Example:
     * <code>org.hsqldb.jdbcDriver</code>.
     */
    public void setDriverClass(String driverClass)
    {
        this.driverClass = driverClass;
    }

    /**
     * @param url The url of the driver.
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * @param username the user to connect to the database with
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * @param password the password to {@link #username}
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * @param id id used in configuration files.
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @param connectionProperties extra properties passed to the jdbc driver.
     */
    public void setConnectionProperties(Properties connectionProperties)
    {
        this.connectionProperties = connectionProperties;
    }

    /**
     * The String used to identify this datasource in configuration files.
     * 
     * @return the datasource id
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * The JNDI location that this datasource should be bound do (in the config file). Note that
     * many application servers may prepend a context (typically <code>java:comp/env</code>) to this
     * context.
     * 
     * @return the JNDI location
     */
    public String getJndiLocation()
    {
        return this.jndiLocation;
    }

    /**
     * The type of the data source (typically <code>javax.sql.XADataSource</code> or
     * <code>javax.sql.DataSource</code>).
     * 
     * @return the datasource type.
     */
    public String getConnectionType()
    {
        return this.connectionType;
    }

    /**
     * The transaction support of the underlying connections, if <code>javax.xml.DataSource</code>.
     * 
     * @return transactional support of the DataSource
     */
    public TransactionSupport getTransactionSupport()
    {
        return transactionSupport;
    }

    /**
     * The class name of the Driver or XADataSource. Example: <code>org.hsqldb.jdbcDriver</code>.
     * 
     * @return the class name of the JDBC driver
     */
    public String getDriverClass()
    {
        return this.driverClass;
    }

    /**
     * Extra properties passed to the JDBC Driver.
     * 
     * @return Extra properties passed to the JDBC Driver.
     */
    public Properties getConnectionProperties()
    {
        return this.connectionProperties;
    }

    /**
     * The url to connect to the database. Example: <code>jdbc:hsqldb:database/jiradb</code>.
     * 
     * @return the url to connect to the database
     */
    public String getUrl()
    {
        return this.url;
    }

    /**
     * The username to use when connecting to the database.
     * 
     * @return the username (eg 'sa')
     */
    public String getUsername()
    {
        return this.username;
    }

    /**
     * The password to use when connecting to the database.
     * 
     * @return the password to use to connect to the database
     */
    public String getPassword()
    {
        return this.password;
    }

}
