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

import org.codehaus.cargo.container.internal.util.PropertyUtils;
import org.codehaus.cargo.container.property.DataSourceConverter;
import org.codehaus.cargo.container.property.DatasourcePropertySet;
import org.codehaus.cargo.container.property.TransactionSupport;

/**
 * Fixture used to provide inputs for DataSource testing.
 * 
 * @version $Id$
 */
public class DataSourceFixture
{
    /**
     * Id.
     */
    public String id;

    /**
     * Connection type.
     */
    public String connectionType;

    /**
     * Transaction support.
     */
    public TransactionSupport transactionSupport;

    /**
     * Driver class name.
     */
    public String driverClass;

    /**
     * URL.
     */
    public String url;

    /**
     * JNDI name.
     */
    public String jndiLocation;

    /**
     * Username.
     */
    public String username;

    /**
     * Password.
     */
    public String password;

    /**
     * Driver properties.
     */
    public String driverProperties;

    /**
     * Saves all parameters.
     * @param id Id.
     * @param connectionType Connection type.
     * @param transactionSupport Transaction support.
     * @param driverClass Driver class.
     * @param url URL.
     * @param jndiLocation JNDI location.
     * @param username Username.
     * @param password Password.
     * @param driverProperties Driver properties.
     */
    public DataSourceFixture(String id, String connectionType,
        TransactionSupport transactionSupport, String driverClass, String url,
        String jndiLocation, String username, String password, String driverProperties)
    {
        super();
        this.id = id;
        this.connectionType = connectionType;
        this.transactionSupport = transactionSupport;
        this.driverClass = driverClass;
        this.url = url;
        this.jndiLocation = jndiLocation;
        this.username = username;
        this.password = password;
        this.driverProperties = driverProperties;
    }

    /**
     * @return {@link Properties} corresponding to this {@link DataSourceFixture}'s attributes.
     */
    public Properties buildDataSourceProperties()
    {
        Properties properties = new Properties();
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.ID, id);
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.CONNECTION_TYPE,
            connectionType);
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.TRANSACTION_SUPPORT,
            transactionSupport);
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.DRIVER_CLASS,
            driverClass);
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.URL, url);
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.JNDI_LOCATION,
            jndiLocation);
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.USERNAME, username);
        PropertyUtils.setPropertyIfNotNull(properties, DatasourcePropertySet.PASSWORD, password);
        PropertyUtils.setPropertyIfNotNull(properties,
            DatasourcePropertySet.CONNECTION_PROPERTIES, driverProperties);
        return properties;
    }

    /**
     * @return {@link DataSource} corresponding to this {@link DataSourceFixture}'s attributes.
     */
    public DataSource buildDataSource()
    {
        return new DataSourceConverter().fromProperties(buildDataSourceProperties());
    }

    /**
     * @return String corresponding to this {@link DataSourceFixture}'s attributes.
     */
    public String buildDataSourcePropertyString()
    {
        DataSourceConverter converter = new DataSourceConverter();
        return converter.toPropertyString(buildDataSource());
    }

}
