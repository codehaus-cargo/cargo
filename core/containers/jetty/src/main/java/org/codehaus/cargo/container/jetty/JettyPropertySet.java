/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
     * This is useful for various purposes:
     * <ul>
     * <li>Ability to perform hot deployments</li>
     * <li>Ability to tell Jetty to install the WAR under a given context name</li>
     * <li>Ability to customise the <a
     * href="https://codehaus-cargo.github.io/cargo/Application+Classpath.html">application
     * classpath</a></li>
     * <li>Accelerated deployment by avoiding an actual copy of the WAR</li>
     * </ul>
     */
    String DEPLOYER_CREATE_CONTEXT_XML = "cargo.jetty.deployer.createContextXml";

    /**
     * Name of the security realm for Jetty.
     */
    String REALM_NAME = "cargo.jetty.realm.name";

    /**
     * Comma-separated list of Jetty modules to activate.
     */
    String MODULES = "cargo.jetty.modules";

    /**
     * EE version to use when deploying to Jetty 12.x onwards using <code>context.xml</code> files.
     * <br><br>
     * The requirement for specifying the EE version can be understood by reading <a
     * href="https://eclipse.dev/jetty/documentation/jetty-12/operations-guide/#og-deploy-jetty">
     * the <i>Deploying Jetty Context XML Files</i> chapter of the Jetty Operations Guide</a>.
     * <br><br>
     * This parameter is used if {@link #DEPLOYER_CREATE_CONTEXT_XML} is set to <code>false</code>.
     */
    String DEPLOYER_EE_VERSION = "cargo.jetty.deployer.ee.version";

    /**
     * The port for HTTPS.
     */
    String CONNECTOR_HTTPS_PORT = "cargo.jetty.connector.https.port";

    /**
     * The file path for the key store file.
     */
    String CONNECTOR_KEY_STORE_FILE = "cargo.jetty.connector.keystoreFile";

    /**
     * The type of the key store file.
     */
    String CONNECTOR_KEY_STORE_TYPE = "cargo.jetty.connector.keystoreType";

    /**
     * The password for the server key store.
     */
    String CONNECTOR_KEY_STORE_PASSWORD = "cargo.jetty.connector.keystorePass";

}
