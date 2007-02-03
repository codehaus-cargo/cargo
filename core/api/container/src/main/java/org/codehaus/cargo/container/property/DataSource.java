/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
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

import org.codehaus.cargo.container.internal.util.PropertyUtils;

import java.util.Properties;

/**
 * A Datasource is a representation of a JDBC datasource.  If supported by the container,
 * this property is used to setup a datasource.
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
     * (typically <code>javax.sql.XADataSource</code> or <code>javax.sql.DataSource</code>).
     */
    private String dataSourceType;

    /**
     * The class name of the driver.
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
     * Contruct a DataSource object.
     *
     * @param jndiLocation (typically <code>java:comp/env</code>)
     * @param dataSourceType (typically <code>javax.sql.XADataSource</code> or
     *        <code>javax.sql.DataSource</code>)
     * @param driverClass The class name of the JDBC driver.
     *        Example: <code>org.hsqldb.jdbcDriver</code>
     * @param url The url to connect to the database.
     *        Example: <code>jdbc:hsqldb:database/jiradb</code>
     * @param username The username to use when connecting to the database.
     * @param password The password to use when connecting to the database.
     */
    public DataSource(String jndiLocation, String dataSourceType, String driverClass, String url,
        String username, String password)
    {
        this.jndiLocation = jndiLocation;
        this.dataSourceType = dataSourceType;
        this.driverClass = driverClass;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Contruct a DataSource from a single String.
     *
     * @param datasourceInformation A string, really a list of properties, representing a datasource
     * @see org.codehaus.cargo.container.internal.util.PropertyUtils#getDataSourceProperties(String)
     */
    public DataSource(String datasourceInformation)
    {
        this(PropertyUtils.getDataSourceProperties(datasourceInformation));
    }

    /**
     * Contruct a DataSource from a list of properties.
     *
     * @param properties A list of properties representing this datasource
     * @see PropertyUtils#getDataSourceProperties(String)
     */
    public DataSource(Properties properties)
    {
        this.jndiLocation = properties.getProperty(DatasourcePropertySet.JNDI_LOCATION);
        this.dataSourceType = properties.getProperty(DatasourcePropertySet.DATASOURCE_TYPE);
        this.driverClass = properties.getProperty(DatasourcePropertySet.DRIVER_CLASS);
        this.url = properties.getProperty(DatasourcePropertySet.URL);
        this.username = properties.getProperty(DatasourcePropertySet.USERNAME);
        this.password = properties.getProperty(DatasourcePropertySet.PASSWORD);
    }

    /**
     * Get a string representation of this datasource.
     *
     * @return a string representation
     * @see PropertyUtils#getDataSourceString(java.util.Properties)
     */
    public String getStringRepresentation()
    {
        Properties properties = new Properties();
        setPropertyIfNotNull(
            properties, DatasourcePropertySet.JNDI_LOCATION, this.jndiLocation);
        setPropertyIfNotNull(
            properties, DatasourcePropertySet.DATASOURCE_TYPE, this.dataSourceType);
        setPropertyIfNotNull(
            properties, DatasourcePropertySet.DRIVER_CLASS, this.driverClass);
        setPropertyIfNotNull(
            properties, DatasourcePropertySet.URL, this.url);
        setPropertyIfNotNull(
            properties, DatasourcePropertySet.USERNAME, this.username);
        setPropertyIfNotNull(
            properties, DatasourcePropertySet.PASSWORD, this.password);

        return PropertyUtils.getDataSourceString(properties);
    }

    /**
     * Sets a property value if the property is not null.
     *
     * @param properties the properties object to store the property into
     * @param property the property to set
     * @param value the value to set
     */
    private void setPropertyIfNotNull(Properties properties, String property, String value)
    {
        if (value != null)
        {
            properties.setProperty(property, value);
        }
    }

    /**
     * The JNDI location that this datasource should be bound do (in the config file). Note that
     * many application servers may prepend a context (typically <code>java:comp/env</code>) to
     * this context.
     *
     * @return the JDNI location
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
    public String getDataSourceType()
    {
        return this.dataSourceType;
    }

    /**
     * The class name of the JDBC driver. Example: <code>org.hsqldb.jdbcDriver</code>.
     *
     * @return the class name of the JDBC driver
     */
    public String getDriverClass()
    {
        return this.driverClass;
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
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getStringRepresentation();
    }
}
