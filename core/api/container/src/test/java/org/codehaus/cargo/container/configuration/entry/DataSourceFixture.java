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
    public String id;

    public String connectionType;

    public TransactionSupport transactionSupport;

    public String driverClass;

    public String url;

    public String jndiLocation;

    public String username;

    public String password;

    public String driverProperties;

    /**
     * @param id
     * @param connectionType
     * @param transactionSupport
     * @param driverClass
     * @param url
     * @param jndiLocation
     * @param username
     * @param password
     * @param driverProperties
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

    public DataSource buildDataSource()
    {
        return new DataSourceConverter().fromProperties(buildDataSourceProperties());
    }

    public String buildDataSourcePropertyString()
    {
        DataSourceConverter converter = new DataSourceConverter();
        return converter.toPropertyString(buildDataSource());
    }

}
