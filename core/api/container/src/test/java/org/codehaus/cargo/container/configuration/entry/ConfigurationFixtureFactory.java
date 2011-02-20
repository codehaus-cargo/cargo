/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
 * @version $Id$
 */
public final class ConfigurationFixtureFactory
{

    /**
     * Utility classes should not have a public or default constructor.
     */
    private ConfigurationFixtureFactory()
    {
        // Nothing
    }

    /**
     * @return {@link DataSourceFixture} for Apache Derby.
     */
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

    /**
     * @return Another {@link DataSourceFixture} for Apache Derby.
     */
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

    /**
     * @return {@link DataSourceFixture} for Apache Derby with driver-configured local transaction
     * support.
     */
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

    /**
     * @return {@link DataSourceFixture} for Apache Derby with driver-configured XA transaction
     * support.
     */
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

    /**
     * @return XA {@link DataSourceFixture} for Apache Derby.
     */
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

    /**
     * @return {@link ResourceFixture} representing a connection pool.
     */
    public static ResourceFixture createConnectionPoolDataSourceAsResource()
    {
        return new ResourceFixture("resource/ConnectionPoolDataSource",
            ConfigurationEntryType.CONNECTIONPOOL_DATASOURCE,
            "org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource",
            "createDatabase=create;databaseName=derbyDB");
    }

    /**
     * @return {@link ResourceFixture} representing an XA datasource.
     */
    public static ResourceFixture createXADataSourceAsResource()
    {
        return new ResourceFixture("resource/XADataSource",
            ConfigurationEntryType.XA_DATASOURCE,
            "org.apache.derby.jdbc.EmbeddedXADataSource",
            "createDatabase=create;databaseName=derbyDB");
    }

    /**
     * @return {@link ResourceFixture} representing a mail session.
     */
    public static ResourceFixture createMailSessionAsResource()
    {
        return new ResourceFixture("mail/Session",
            ConfigurationEntryType.MAIL_SESSION,
            null,
            "mail.smtp.host=localhost");
    }

    /**
     * @return {@link DataSourceFixture} with a Windows-style path.
     */
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
