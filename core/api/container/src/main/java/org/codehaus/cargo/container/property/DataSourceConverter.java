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

import java.util.Arrays;
import java.util.Properties;

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.configuration.entry.DataSource;
import org.codehaus.cargo.container.configuration.entry.Resource;
import org.codehaus.cargo.container.internal.util.PropertyUtils;

/**
 * A DataSource is a representation of an database pool bound to JNDI. This converter will take a
 * property and convert it to a DataSource and visa versa.
 * 
 * @version $Id$
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
        DataSource data = new DataSource();
        data.setJndiLocation(properties.getProperty(DatasourcePropertySet.JNDI_LOCATION));
        String connectionType = properties.getProperty(DatasourcePropertySet.CONNECTION_TYPE);
        if (ConfigurationEntryType.XA_DATASOURCE.equals(connectionType))
        {
            data.setConnectionType(connectionType);
        }
        else
        {
            data.setConnectionType(ConfigurationEntryType.JDBC_DRIVER);
        }
        String transactionSupportProperty =
            properties.getProperty(DatasourcePropertySet.TRANSACTION_SUPPORT);

        if (TransactionSupport.XA_TRANSACTION.toString().equals(transactionSupportProperty)
            || ConfigurationEntryType.XA_DATASOURCE.equals(connectionType))
        {
            data.setTransactionSupport(TransactionSupport.XA_TRANSACTION);
        }
        else if (TransactionSupport.LOCAL_TRANSACTION.toString().equals(
            transactionSupportProperty))
        {
            data.setTransactionSupport(TransactionSupport.LOCAL_TRANSACTION);
        }
        else
        {
            data.setTransactionSupport(TransactionSupport.NO_TRANSACTION);
        }
        data.setDriverClass(properties.getProperty(DatasourcePropertySet.DRIVER_CLASS));
        data.setUrl(properties.getProperty(DatasourcePropertySet.URL));
        data.setUsername(properties.getProperty(DatasourcePropertySet.USERNAME));
        data.setPassword(properties.getProperty(DatasourcePropertySet.PASSWORD));
        data.setId(properties.getProperty(DatasourcePropertySet.ID));
        String driverPropertiesAsASemicolonDelimitedString =
            properties.getProperty(DatasourcePropertySet.CONNECTION_PROPERTIES);
        data
            .setConnectionProperties(getDriverPropertiesFromString(
                driverPropertiesAsASemicolonDelimitedString));
        if (data.getId() == null)
        {
            data.setId(createIdFromJndiLocationIfNotNull(data.getJndiLocation()));
        }
        setCredentialsIfInsideDriverProperties(data);
        return data;
    }

    /**
     * if the enclosed driver properties object is set, and also contains the user and password
     * properties, set the corresponding member values.
     * 
     * @param data DataSource to serialize into a string.
     */
    private void setCredentialsIfInsideDriverProperties(DataSource data)
    {
        if (data.getConnectionProperties() != null)
        {
            if (data.getConnectionProperties().containsKey("user"))
            {
                data.setUsername(data.getConnectionProperties().getProperty("user"));
            }

            if (data.getConnectionProperties().containsKey("password"))
            {
                data.setPassword(data.getConnectionProperties().getProperty("password"));
            }
        }
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
        if (property != null && !property.trim().equals(""))
        {
            return PropertyUtils.splitPropertiesOnSemicolon(property);
        }
        else
        {
            return new Properties();
        }
    }

    /**
     * return a string that can be used to name this configuration or null, if jndiLocation was not
     * specified.
     * 
     * @param jndiLocation used to construct the id
     * @return a string that can be used to name this configuration or null, if jndiLocation was not
     * specified.
     * @see org.codehaus.cargo.container.configuration.entry.DataSource#createIdFromJndiLocation(String)
     */
    private static String createIdFromJndiLocationIfNotNull(String jndiLocation)
    {
        String id = null;
        if (jndiLocation != null)
        {
            id = createIdFromJndiLocation(jndiLocation);
        }
        return id;
    }

    /**
     * Get a string name for the configuration of this datasource. This should be XML and filesystem
     * friendly. For example, the String returned will have no slashes or punctuation, and be as
     * short as possible.
     * 
     * @param jndiLocation used to construct the id
     * @return a string that can be used to name this configuration
     */
    protected static String createIdFromJndiLocation(String jndiLocation)
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
     * Get a string representation of this datasource.
     * 
     * @param data DataSource to serialize into a string.
     * @return a string representation
     * @see PropertyUtils#joinPropertiesOnPipe(java.util.Properties)
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
        if (data.getConnectionProperties() != null && data.getConnectionProperties().size() != 0)
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
