/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
 */
public interface TomcatPropertySet
{
    /**
     * The boolean value for the empty session path connector property.
     */
    String CONNECTOR_EMPTY_SESSION_PATH = "cargo.tomcat.connector.emptySessionPath";

    /**
     * URI Encoding to set.
     */
    String URI_ENCODING = "cargo.tomcat.uriencoding";

    /**
     * Port tomcat will listen on for AJP 1.3 requests.
     */
    String AJP_PORT = "cargo.tomcat.ajp.port";

    /**
     * Whether the contexts for deployed Web applications should be set as reloadable.
     */
    String CONTEXT_RELOADABLE = "cargo.tomcat.context.reloadable";

    /**
     * Whether the contexts for deployed Web applications should allow multipart parsing.
     */
    String CONTEXT_ALLOW_MULTIPART = "cargo.tomcat.context.allowCasualMultipartParsing";

    /**
     * Whether the contexts for deployed webapplications should allow webjars support
     */
    String CONTEXT_ALLOW_WEB_JARS = "cargo.tomcat.context.addWebinfClassesResources";

    /**
     * Whether the contexts for deployed web applications should map JARs to
     * <code>WEB-INF/classes</code>. Refer to the <i>Ordering</i> chapter of
     * <a href="https://tomcat.apache.org/tomcat-8.0-doc/config/resources.html">the Tomcat 8.0
     * Configuration Guide</a> for more details.
     */
    String CONTEXT_MAP_JARS_TO_WEBINF_CLASSES = "cargo.tomcat.context.mapJarToWebinfClasses";

    /**
     * Whether WAR deployables should be copied or referenced.
     */
    String COPY_WARS = "cargo.tomcat.copywars";

    /**
     * The <code>webapps</code> directory of Tomcat.
     */
    String WEBAPPS_DIRECTORY = "cargo.tomcat.webappsDirectory";

    /**
     * The <code>webapps-javaee</code> directory of Tomcat, which with Toncat 10.x onwards is used
     * for storing legacy J2EE and Java EE applications which Tomcat transforms to Jakarta EE
     * before deployment.
     */
    String WEBAPPS_LEGACY_DIRECTORY = "cargo.tomcat.webappsLegacyDirectory";

    /**
     * Number of threads the host will use to start context elements in parallel.
     */
    String HOST_START_STOP_THREADS = "cargo.tomcat.host.startStopThreads";

    /**
     * Whether to use use http only.
     */
    String USE_HTTP_ONLY = "cargo.tomcat.useHttpOnly";

    /**
     * The HTTP protocol class.
     */
    String CONNECTOR_PROTOCOL_CLASS = "cargo.tomcat.connector.protocolClass";

    /**
     * The file path for the key store file.
     */
    String CONNECTOR_KEY_STORE_FILE = "cargo.tomcat.connector.keystoreFile";

    /**
     * The password for the server key store.
     */
    String CONNECTOR_KEY_STORE_PASSWORD = "cargo.tomcat.connector.keystorePass";

    /**
     * The type of the key store file.
     */
    String CONNECTOR_KEY_STORE_TYPE = "cargo.tomcat.connector.keystoreType";

    /**
     * To enable HTTP/2 support for an HTTP connector.
     */
    String CONNECTOR_HTTP_UPGRADE_PROTOCOL = "cargo.tomcat.connector.httpUpgradeProtocol";

    /**
     * The maximum HTTP header size.
     */
    String CONNECTOR_MAX_HTTP_HEADER_SIZE = "cargo.tomcat.connector.maxHttpHeaderSize";

    /**
     * The maximum Part count.
     */
    String CONNECTOR_MAX_PART_COUNT = "cargo.tomcat.connector.maxPartCount";

    /**
     * Custom valves defined as properties separated by <code>|</code><br>
     * Maven example:<br>
     * <code>
     * &lt;cargo.tomcat.valve.stuckthread&gt;<br>
     * &nbsp; &nbsp; className=org.apache.catalina.valves.StuckThreadDetectionValve|<br>
     * &nbsp; &nbsp; threshold=60<br>
     * &lt;/cargo.tomcat.valve.stuckthread&gt;
     * </code>
     */
    String CUSTOM_VALVE = "cargo.tomcat.valve";

    /**
     * Tomcat Manager update parameter when doing a deploy.
     * Setting to true allows deploy without doing an explicit undeploy of existing web-app first.
     */
    String DEPLOY_UPDATE = "cargo.tomcat.deploy.update";

    /**
     * Tomcat Manager update parameter when doing an undeploy.
     * Setting to true removes all versions when undeploying.
     */
    String UNDEPLOY_ALL_VERSIONS = "cargo.tomcat.undeploy.allVersions";

    /**
     * Whether to override the Java logging in the embedded container.
     */
    String EMBEDDED_OVERRIDE_JAVA_LOGGING = "cargo.tomcat.embedded.overrideJavaLogging";
}
