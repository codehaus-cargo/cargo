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

/**
 * Gathers all data source properties valid for all types of containers.
 * 
 * @version $Id$
 */
public interface DatasourcePropertySet
{
    /**
     * A property to encapsulate all the other datasource properties. This is to get around cargo
     * only passing strings around, instead of objects. <br>
     */
    String DATASOURCE = "cargo.datasource.datasource";

    /**
     * The JNDI location that this datasource should be bound do (in the config file). Note that
     * many application servers may prepend a context (typically <code>java:comp/env</code>) to this
     * context. <br>
     */
    String JNDI_LOCATION = "cargo.datasource.jndi";

    /**
     * The type of the data source (typically <code>javax.sql.XADataSource</code>,
     * <code>javax.sql.ConnectionPoolDataSource</code> or <code>javax.sql.DataSource</code>). <br>
     */
    String CONNECTION_TYPE = "cargo.datasource.type";

    /**
     * The transaction support of the data source. One of <code>NO_TRANSACTION</code>,
     * <code>LOCAL_TRANSACTION</code> or <code>XA_TRANSACTION</code> <br>
     */
    String TRANSACTION_SUPPORT = "cargo.datasource.transactionsupport";

    /**
     * The class name of the Driver or XADataSource implementation clas. Example:
     * <code>org.hsqldb.jdbcDriver</code>. <br>
     */
    String DRIVER_CLASS = "cargo.datasource.driver";

    /**
     * The url to connect to the database. Example: <code>jdbc:hsqldb:database/jiradb</code>. The
     */
    String URL = "cargo.datasource.url";

    /**
     * The username to use when connecting to the database. <br>
     */
    String USERNAME = "cargo.datasource.username";

    /**
     * The password to use when connecting to the database. <br>
     */
    String PASSWORD = "cargo.datasource.password";

    /**
     * Unique id to use in configuration files. <br>
     */
    String ID = "cargo.datasource.id";

    /**
     * Extra properties passed to the JDBC driver or datasource implementation. <br>
     */
    String CONNECTION_PROPERTIES = "cargo.datasource.properties";

}
