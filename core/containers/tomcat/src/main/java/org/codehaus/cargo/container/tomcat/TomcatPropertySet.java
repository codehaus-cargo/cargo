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
package org.codehaus.cargo.container.tomcat;

/**
 * Gathers all Tomcat properties.
 * 
 * @version $Id$
 */
public interface TomcatPropertySet
{
    /**
     * The URL for calling the Tomcat Manager app.
     * @deprecated Use {@link org.codehaus.cargo.container.property.RemotePropertySet#URI} instead.
     */
    @Deprecated
    String MANAGER_URL = "cargo.tomcat.manager.url";

    /**
     * The boolean value for the empty session path connector property.
     */
    String CONNECTOR_EMPTY_SESSION_PATH = "cargo.tomcat.connector.emptySessionPath";

    /**
     * Port tomcat will listen on for AJP 1.3 requests.
     */
    String AJP_PORT = "cargo.tomcat.ajp.port";

    /**
     * Whether the contexts for deployed Web applications should be set as reloadable.
     */
    String CONTEXT_RELOADABLE = "cargo.tomcat.context.reloadable";

    /**
     * Whether WAR deployables should be copied or referenced.
     */
    String COPY_WARS = "cargo.tomcat.copywars";
}
