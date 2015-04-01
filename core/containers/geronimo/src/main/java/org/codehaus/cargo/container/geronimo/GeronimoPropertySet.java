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
package org.codehaus.cargo.container.geronimo;

/**
 * Interface for Geronimo-specific properties.
 * 
 */
public interface GeronimoPropertySet
{
    /**
     * The id for the servlet container. Ex. tomcat or jetty
     */
    String GERONIMO_SERVLET_CONTAINER_ID = "cargo.geronimo.servlet.containerId";

    /**
     * Geronimo list of user credentials. Ex.
     * name1:pwd1:role11,...,role1N|name2:pwd2:role21,...,role2N|...
     */
    String GERONIMO_USERS = "cargo.geronimo.users";

    /**
     * Log level for file log appender.
     */
    String GERONIMO_FILE_LOGLEVEL = "cargo.geronimo.log.file";

    /**
     * Log level for console log appender.
     */
    String GERONIMO_CONSOLE_LOGLEVEL = "cargo.geronimo.log.console";
}
