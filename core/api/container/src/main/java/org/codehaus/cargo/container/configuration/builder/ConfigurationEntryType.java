/*
 * ========================================================================
 *
 * Copyright 2005 Vincent Massol.
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
 * @version $Id: $
 */
public interface ConfigurationEntryType
{
    /**
     * {@value}
     */
    String DATASOURCE = "javax.sql.DataSource";

    /**
     * {@value}
     */
    String CONNECTIONPOOL_DATASOURCE = "javax.sql.ConnectionPoolDataSource";

    /**
     * {@value}
     */
    String XA_DATASOURCE = "javax.sql.XADataSource";
   
    /**
     * {@value}
     */
    String JDBC_DRIVER = "java.sql.Driver";

}
