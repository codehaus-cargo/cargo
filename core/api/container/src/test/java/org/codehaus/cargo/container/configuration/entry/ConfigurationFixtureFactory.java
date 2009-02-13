/*
 * ========================================================================
 *
 * Copyright 2006 Vincent Massol.
 *
 * Licensed under the Apache License, Version 2.0 (the "License",true);
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

import org.codehaus.cargo.container.configuration.builder.ConfigurationEntryType;
import org.codehaus.cargo.container.property.TransactionSupport;

/**
 * Creates fixtures used to provide inputs for DataSource or Resourcetesting.
 * 
 * @version $Id: $
 */
public class ConfigurationFixtureFactory
{

    public static DataSourceFixture createDataSource()
    {
        return new DataSourceFixture(null,
            null,
            null,
            "org.apache.derby.jdbc.EmbeddedDriver",
            "jdbc:derby:derbyDB;create=true",
            "jdbc/CargoDS",
            "APP",
            "nonemptypassword",
            null);
    }
    
    public static DataSourceFixture createAnotherDataSource()
    {
        return new DataSourceFixture(null,
            ConfigurationEntryType.JDBC_DRIVER,
            TransactionSupport.NO_TRANSACTION,
            "org.apache.derby.jdbc.EmbeddedDriver",
            "jdbc:derby:derbyDB2;create=true",
            "jdbc/CargoDS2",
            "APP",
            "nonemptypassword",
            null);
    }
    
    public static DataSourceFixture createDriverConfiguredDataSourceWithLocalTransactionSupport()
    {
        return new DataSourceFixture(null,
            null,
            TransactionSupport.LOCAL_TRANSACTION,
            "org.apache.derby.jdbc.EmbeddedDriver",
            "jdbc:derby:derbyDB;create=true",
            "jdbc/CargoDS",
            "APP",
            "",
            null);
    }

    public static DataSourceFixture createDriverConfiguredDataSourceWithXaTransactionSupport()
    {
        return new DataSourceFixture(null,
            null,
            TransactionSupport.XA_TRANSACTION,
            "org.apache.derby.jdbc.EmbeddedDriver",
            "jdbc:derby:derbyDB;create=true",
            "jdbc/CargoDS",
            "APP",
            "",
            null);
    }


    public static DataSourceFixture createXADataSourceConfiguredDataSource()
    {
        return new DataSourceFixture(null,
            ConfigurationEntryType.XA_DATASOURCE,
            null,
            "org.apache.derby.jdbc.EmbeddedXADataSource",
            null,
            "jdbc/CargoDS",
            "APP",
            "nonemptypassword",
            "createDatabase=create;databaseName=derbyDB");
    }

    public static ResourceFixture createConnectionPoolDataSourceAsResource()
    {
        return new ResourceFixture("resource/ConnectionPoolDataSource",
            ConfigurationEntryType.CONNECTIONPOOL_DATASOURCE,
            "org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource",
            "createDatabase=create;databaseName=derbyDB");
    }

    public static ResourceFixture createXADataSourceAsResource()
    {
        return new ResourceFixture("resource/XADataSource",
            ConfigurationEntryType.XA_DATASOURCE,
            "org.apache.derby.jdbc.EmbeddedXADataSource",
            "createDatabase=create;databaseName=derbyDB");
    }

    public static DataSourceFixture createDataSourceWithWindowsPath()
    {
        return new DataSourceFixture(null,
            null,
            TransactionSupport.NO_TRANSACTION,
            "org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:c:\\temp\\db/jira-home/database",
            "jdbc/JiraDS",
            "sa",
            "",
            null);
    }

}
