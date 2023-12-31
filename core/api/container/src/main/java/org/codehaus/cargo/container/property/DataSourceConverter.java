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
package org.codehaus.cargo.container.property;

import java.util.Properties;

import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.internal.util.PropertyUtils;

/**
 * A DataSource is a representation of an database pool bound to JNDI. This converter will take a
 * property and convert it to a DataSource and visa versa.
 */
public class DataSourceConverter
{

    /**
     * Construct a DataSource from a single String. Note that database driver properties can be
     * nested as long as they are semicolon delimited Example:
     * <code>CreateDatabase=create;DatabaseName=TEST</code>.
     * 
     * @param datasourceInformation A string, really a list of properties, representing a datasource
     * @return DataSource representing the string
     * @see org.codehaus.cargo.container.internal.util.PropertyUtils#splitPropertiesOnPipe(String)
     */
    public DataSource fromPropertyString(String datasourceInformation)
    {
        return fromProperties(PropertyUtils.splitPropertiesOnPipe(PropertyUtils
            .escapeBackSlashesIfNotNull(datasourceInformation)));
    }

    /**
     * Construct a DataSource from a list of properties.
     * 
     * @param properties A list of properties representing this datasource
     * @return DataSource representing the properties
     * @see PropertyUtils#splitPropertiesOnPipe(String)
     */
    public DataSource fromProperties(Properties properties)
    {
        String jndiLocation = properties.getProperty(DatasourcePropertySet.JNDI_LOCATION);
        String connectionType = properties.getProperty(DatasourcePropertySet.CONNECTION_TYPE);
        TransactionSupport transactionSupport = TransactionSupport.valueOf(
                properties.getProperty(DatasourcePropertySet.TRANSACTION_SUPPORT));
        String driverClass = properties.getProperty(DatasourcePropertySet.DRIVER_CLASS);
        String url = properties.getProperty(DatasourcePropertySet.URL);
        String username = properties.getProperty(DatasourcePropertySet.USERNAME);
        String password = properties.getProperty(DatasourcePropertySet.PASSWORD);
        String id = properties.getProperty(DatasourcePropertySet.ID);

        String driverPropertiesAsASemicolonDelimitedString =
            properties.getProperty(DatasourcePropertySet.CONNECTION_PROPERTIES);
        Properties connectionProperties = getDriverPropertiesFromString(
                driverPropertiesAsASemicolonDelimitedString);

        DataSource data = new DataSource(jndiLocation, connectionType, transactionSupport,
                driverClass, url, username, password, id, connectionProperties);
        return data;
    }

    /**
     * tests to see if the value is null before attempting to extract the database properties from
     * it.
     * 
     * @param property to parse, semicolon delimited
     * @return parsed or empty properties.
     */
    private Properties getDriverPropertiesFromString(String property)
    {
        if (property != null && !property.trim().isEmpty())
        {
            return PropertyUtils.splitPropertiesOnSemicolon(property);
        }
        else
        {
            return new Properties();
        }
    }

    /**
     * Get a string representation of this datasource.
     * 
     * @param data DataSource to serialize into a string.
     * @return a string representation
     */
    public String toPropertyString(DataSource data)
    {
        Properties properties = toProperties(data);
        return PropertyUtils.joinOnPipe(PropertyUtils.toMap(properties));
    }

    /**
     * Get a properties object containing all of the members of this datasource object. Note that
     * driver properties will be nested and delimited by a semicolon.
     * 
     * @param data DataSource to serialize into properties.
     * @return a properties object corresponding to this datasource
     */
    public Properties toProperties(DataSource data)
    {
        Properties properties = new Properties();
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.JNDI_LOCATION, data
            .getJndiLocation());
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.CONNECTION_TYPE,
            data.getConnectionType());
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.TRANSACTION_SUPPORT,
            data.getTransactionSupport());
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.DRIVER_CLASS, data
            .getDriverClass());
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.URL, data.getUrl());
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.USERNAME, data
            .getUsername());
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.PASSWORD, data
            .getPassword());
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.ID, data.getId());
        PropertyUtils.setPropertyIfNotNull(properties,
            DatasourcePropertySet.CONNECTION_PROPERTIES,
            getConnectionPropertiesAsASemicolonDelimitedString(data));
        return properties;
    }

    /**
     * tests to see if the value is null before attempting to join the database properties on a
     * semicolon.
     * 
     * @param data DataSource to parse connection properties from.
     * @return property string delimited by semicolon, or null, if they cannot be parsed because the
     * input properties weren't set or empty
     */
    public String getConnectionPropertiesAsASemicolonDelimitedString(DataSource data)
    {
        if (data.getConnectionProperties() != null && !data.getConnectionProperties().isEmpty())
        {
            return PropertyUtils.joinOnSemicolon(PropertyUtils.toMap(
                data.getConnectionProperties()));
        }
        else
        {
            return null;
        }
    }

    /**
     * This method converts the DataSource to a Resource.
     * 
     * @param ds DataSource to convert to a resource.
     * @param resourceType the type of the Resource to convert to. ex.
     * <code>javax.sql.DataSource</code>
     * @param driverParameter the name of the parameter to store {@link DataSource#getDriverClass()
     * driverClass}.
     * @return a Resource representing the assignable fields of the DataSource.
     */
    public Resource convertToResource(DataSource ds, String resourceType, String driverParameter)
    {
        Properties parameters = new Properties();
        if (ds.getUrl() != null)
        {
            PropertyUtils.setPropertyIfNotNull(parameters, "url", ds.getUrl());
        }
        PropertyUtils.setPropertyIfNotNull(parameters, "user", ds.getUsername());
        PropertyUtils.setPropertyIfNotNull(parameters, "password", ds.getPassword());
        PropertyUtils.setPropertyIfNotNull(parameters, driverParameter, ds.getDriverClass());

        parameters.putAll(ds.getConnectionProperties());

        Resource resource = new Resource(ds.getJndiLocation(), resourceType);
        resource.setParameters(PropertyUtils.toMap(parameters));
        return resource;
    }

}
