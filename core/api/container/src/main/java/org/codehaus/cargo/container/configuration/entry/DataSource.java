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
package org.codehaus.cargo.container.configuration.entry;

import java.util.Arrays;
import java.util.Properties;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.property.TransactionSupport;

/**
 * A Datasource is a representation of a JDBC datasource. If supported by the container, this
 * property is used to setup a datasource.
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
     * Constructor containing default DataSource construction rules.
     * 
     * @param jndiLocation Where to bind this DataSource (typically <code>java:comp/env</code>).
     * @param connectionType What to use to get a connection from the database.
     *      <code>javax.sql.XADataSource</code> or <code>javax.sql.Driver</code>.
     * @param transactionSupport Transaction support of the datasource ex.
     *      <code>XA_TRANSACTION</code>
     * @param driverClass The class name of the Driver or XADataSource. Example:
     *      <code>org.hsqldb.jdbcDriver</code>.
     * @param url The url of the driver.
     * @param username The user to connect to the database with.
     * @param password The password to {@link #username}.
     * @param id Id used in configuration files.
     * @param connectionProperties Extra properties passed to the jdbc driver.
     */
    public DataSource(String jndiLocation, String connectionType,
            TransactionSupport transactionSupport, String driverClass, String url,
            String username, String password, String id, Properties connectionProperties)
    {
        // Can also be implemented as Builder pattern to avoid null parameters.
        this.jndiLocation = jndiLocation;
        this.connectionType = createConnectionType(connectionType);
        this.transactionSupport =
            createTransactionSupport(transactionSupport, this.connectionType);
        this.driverClass = driverClass;
        this.url = url;
        this.username = username;
        this.password = password;

        if (id == null && jndiLocation != null)
        {
            this.id = createIdFromJndiLocation(jndiLocation);
        }
        else
        {
            this.id = id;
        }

        this.connectionProperties = connectionProperties;
        setCredentialsIfInsideDriverProperties();
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

    /**
     * Create valid connection type based on input value.
     * 
     * @param connectionType Connection type value to be converted.
     * @return Valid connection type.
     */
    private String createConnectionType(String connectionType)
    {
        if (null == connectionType)
        {
            return ConfigurationEntryType.JDBC_DRIVER;
        }
        else
        {
            switch (connectionType)
            {
                case ConfigurationEntryType.XA_DATASOURCE:
                    return ConfigurationEntryType.XA_DATASOURCE;
                case ConfigurationEntryType.DATASOURCE:
                    return ConfigurationEntryType.DATASOURCE;
                default:
                    return ConfigurationEntryType.JDBC_DRIVER;
            }
        }
    }

    /**
     * Create valid transaction support value based on input values.
     * 
     * @param transactionSupport Configured transaction support.
     * @param connectionType Valid connection type.
     * @return Valid transaction support.
     */
    private TransactionSupport createTransactionSupport(TransactionSupport transactionSupport,
            String connectionType)
    {
        if (TransactionSupport.XA_TRANSACTION.equals(transactionSupport)
            || ConfigurationEntryType.XA_DATASOURCE.equals(connectionType))
        {
            return TransactionSupport.XA_TRANSACTION;
        }
        else if (TransactionSupport.LOCAL_TRANSACTION.equals(transactionSupport))
        {
            return TransactionSupport.LOCAL_TRANSACTION;
        }
        else
        {
            return TransactionSupport.NO_TRANSACTION;
        }
    }

    /**
     * Get a string name for the configuration of this datasource. This should be XML and filesystem
     * friendly. For example, the String returned will have no slashes or punctuation, and be as
     * short as possible.
     * 
     * @param jndiLocation used to construct the id
     * @return a string that can be used to name this configuration
     */
    private String createIdFromJndiLocation(String jndiLocation)
    {
        // using indexOf to avoid introducing a regex package dependency. when we move
        // to jdk 5+, this can be more easily performed with regex.

        int[] delimeters =
            new int[] {
                // jndi locations are organized by dots or slashes. In JBoss, it could have a colon
                jndiLocation.lastIndexOf('/'), jndiLocation.lastIndexOf('.'),
                jndiLocation.lastIndexOf(':')};
        Arrays.sort(delimeters);

        int highestIndex = delimeters[2];

        // highestIndex could be -1, or a location of a character we don't want. In either case, we
        // want to increase it by one
        return jndiLocation.substring(highestIndex + 1);
    }

    /**
     * if the enclosed driver properties object is set, and also contains the user and password
     * properties, set the corresponding member values.
     */
    private void setCredentialsIfInsideDriverProperties()
    {
        if (getConnectionProperties() != null)
        {
            if (getConnectionProperties().containsKey("user"))
            {
                setUsername(getConnectionProperties().getProperty("user"));
            }

            if (getConnectionProperties().containsKey("password"))
            {
                setPassword(getConnectionProperties().getProperty("password"));
            }
        }
    }
}
