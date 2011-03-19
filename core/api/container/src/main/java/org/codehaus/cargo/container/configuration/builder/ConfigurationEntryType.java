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
package org.codehaus.cargo.container.configuration.builder;

/**
 * Represents the type of a resource, such as a <code>javax.sql.DataSource</code>.
 * 
 * @version $Id$
 */
public interface ConfigurationEntryType
{
    /**
     * Standard DataSource, most often used by application code directly.
     */
    String DATASOURCE = "javax.sql.DataSource";

    /**
     * ConnectionPool DataSource, used by frameworks who manage their own database connections.
     */
    String CONNECTIONPOOL_DATASOURCE = "javax.sql.ConnectionPoolDataSource";

    /**
     * XADataSource, typically used to configure DataSource objects.
     */
    String XA_DATASOURCE = "javax.sql.XADataSource";

    /**
     * Driver, typically used to configure DataSource objects.
     */
    String JDBC_DRIVER = "java.sql.Driver";

    /**
     * Used to obtain connections to outgoing and incoming mail resources.
     */
    String MAIL_SESSION = "javax.mail.Session";

    /**
     * object which can be used for sending email using SMTP.
     */
    String MIMETYPE_DATASOURCE = "javax.mail.internet.MimePartDataSource";

}
