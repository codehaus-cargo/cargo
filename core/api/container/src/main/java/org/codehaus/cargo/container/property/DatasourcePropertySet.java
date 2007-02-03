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

/**
 * Gathers all data source properties valid for all types of containers.
 * 
 * @version $Id$
 */
public interface DatasourcePropertySet
{
    /**
     * A property to encapsulate all the other datasource properties.  This is to get around
     * cargo only passing strings around, instead of objects.
     */
    String DATASOURCE = "cargo.datasource.datasource";

    /**
     * The JNDI location that this datasource should be bound do (in the config file).
     * Note that many application servers may prepend a context (typically
     * <code>java:comp/env</code>) to this context.
     */
    String JNDI_LOCATION = "cargo.datasource.jndi";

    /**
     * The type of the data source (typically <code>javax.sql.XADataSource</code> or
     * <code>javax.sql.DataSource</code>).
     */
    String DATASOURCE_TYPE = "cargo.datasource.type";

    /**
     * The class name of the JDBC driver.  Example: <code>org.hsqldb.jdbcDriver</code>.
     */
    String DRIVER_CLASS = "cargo.datasource.driver";

    /**
     * The url to connect to the database.  Example: <code>jdbc:hsqldb:database/jiradb</code>.
     */
    String URL = "cargo.datasource.url";

    /**
     * The username to use when connecting to the database.
     */
    String USERNAME = "cargo.datasource.username";

    /**
     * The password to use when connecting to the database.
     */
    String PASSWORD = "cargo.datasource.password";
}
