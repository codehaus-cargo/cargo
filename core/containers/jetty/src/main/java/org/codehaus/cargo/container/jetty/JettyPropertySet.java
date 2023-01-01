/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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
package org.codehaus.cargo.container.jetty;

/**
 * Gathers all Jetty properties.
 */
public interface JettyPropertySet
{
    /**
     * The URL for calling the Jetty Deployer webapp.
     */
    String DEPLOYER_URL = "cargo.jetty.deployer.url";

    /**
     * The default session path to use for all session cookies.
     */
    String SESSION_PATH = "cargo.jetty.session.path";

    /**
     * The boolean flag controlling the use of memory mapped buffers for serving static content.
     */
    String USE_FILE_MAPPED_BUFFER = "cargo.jetty.servlet.default.useFileMappedBuffer";

    /**
     * Whether to create <code>context.xml</code> files to deploy WARs.
     */
    String CREATE_CONTEXT_XML = "cargo.jetty.createContextXml";

    /**
     * Name of the security realm for Jetty.
     */
    String REALM_NAME = "cargo.jetty.realm.name";

    /**
     * Comma-separated list of Jetty modules to activate.
     */
    String MODULES = "cargo.jetty.modules";

}
