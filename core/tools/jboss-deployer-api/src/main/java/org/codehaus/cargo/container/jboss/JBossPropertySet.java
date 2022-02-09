/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2022 Ali Tokmen.
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
package org.codehaus.cargo.container.jboss;

/**
 * JBoss-specific properties.
 */
public interface JBossPropertySet
{
    /**
     * The JBoss configuration selected. Examples of valid values: "default", "all", "minimal".
     */
    String CONFIGURATION = "cargo.jboss.configuration";

    /**
     * The JBoss profile. Examples of valid values: "default", "farm".
     */
    String PROFILE = "cargo.jboss.profile";

    /**
     * Whether the JBoss Configuration should be clustered.
     */
    String CLUSTERED = "cargo.jboss.clustered";

    /**
     * The port used for AJP.
     */
    String JBOSS_AJP_PORT = "cargo.jboss.ajp.port";

    /**
     * The port used for HTTPS.
     */
    String JBOSS_HTTPS_PORT = "cargo.jboss.https.port";

    /**
     * The port used for the JRMP service.
     */
    String JBOSS_JRMP_PORT = "cargo.jboss.jrmp.port";

    /**
     * The port used for JBoss naming service.
     */
    String JBOSS_NAMING_PORT = "cargo.jboss.naming.port";

    /**
     * The port used for the JMX server.
     */
    String JBOSS_JMX_PORT = "cargo.jboss.jmx.port";

    /**
     * The port used for the remote native management interface.
     */
    String JBOSS_MANAGEMENT_NATIVE_PORT = "cargo.jboss.management-native.port";

    /**
     * The port used for the remote native management interface using the HTTP protocol.
     */
    String JBOSS_MANAGEMENT_HTTP_PORT = "cargo.jboss.management-http.port";

    /**
     * The port used for the remote native management interface using the HTTPS protocol.
     */
    String JBOSS_MANAGEMENT_HTTPS_PORT = "cargo.jboss.management-https.port";

    /**
     * The port used for the OSGi HTTP server.
     */
    String JBOSS_OSGI_HTTP_PORT = "cargo.jboss.osgi.http.port";

    /**
     * The port used for the mini webserver used for dynamic and class and resource loading.
     */
    String JBOSS_CLASSLOADING_WEBSERVICE_PORT = "cargo.jboss.classloading.webservice.port";

    /**
     * The port used for the JRMP invoker.
     */
    String JBOSS_JRMP_INVOKER_PORT = "cargo.jboss.jrmp.invoker.port";

    /**
     * The port used for the invoker pool.
     */
    String JBOSS_INVOKER_POOL_PORT = "cargo.jboss.invoker.pool.port";

    /**
     * The port used for the JBoss remoting transport connector.
     */
    String JBOSS_REMOTING_TRANSPORT_PORT = "cargo.jboss.remoting.transport.port";

    /**
     * The port used for the JBoss EJB3 remoting.
     */
    String JBOSS_EJB3_REMOTING_PORT = "cargo.jboss.ejb3.remoting.port";

    /**
     * The port used for the JBossTS Recovery Manager.
     */
    String JBOSS_TRANSACTION_RECOVERY_MANAGER_PORT =
        "cargo.jboss.transaction.recoveryManager.port";

    /**
     * The port used for the JBossTS Transaction Status Manager.
     */
    String JBOSS_TRANSACTION_STATUS_MANAGER_PORT = "cargo.jboss.transaction.statusManager.port";

    /**
     * Port number to serve deployable through. Default is <code>1 +
     * {@link org.codehaus.cargo.container.property.ServletPropertySet#PORT}</code> (for example,
     * <code>18080</code> if the JBoss servlet port was <code>8080</code>). This will be used by
     * remote deployers who do not share filesystem with cargo.
     */
    String REMOTEDEPLOY_PORT = "cargo.jboss.remotedeploy.port";

    /**
     * Address to serve deployable through. Default is
     * <code>InetAddress.getLocalHost().getCanonicalHostName()</code>. This will be used by remote
     * deployers who do not share filesystem with cargo.
     */
    String REMOTEDEPLOY_HOSTNAME = "cargo.jboss.remotedeploy.hostname";

    /**
     * Deployment target directory to use instead of the default <code>deployments</code> directory.
     */
    String ALTERNATIVE_DEPLOYMENT_DIR = "cargo.jboss.deployment.dir";

    /**
     * By default, Codehaus Cargo renames the WAR files to match the Web application context,
     * nevertheless this is not relevant when the WAR file has the JBoss-specific context defined
     * in it.<br>
     * <br>
     * This property defines whether to keep the original WAR filename if the WAR file has a
     * <code>&lt;context-root&gt;</code> in the <code>WEB-INF/jboss-web.xml</code> file.
     */
    String KEEP_ORIGINAL_WAR_FILENAME = "cargo.jboss.deployer.keepOriginalWarFilename";

    /**
     * Modules directory to use instead of the default <code>modules</code> directory.
     */
    String ALTERNATIVE_MODULES_DIR = "cargo.jboss.modules.dir";

    /**
     * External CLI script file paths.<br>
     * Used for custom configuration of JBoss container in online mode.<br>
     * <br>
     * Example usage:<br>
     * <code>setProperty("cargo.jboss.script.cli.online.journal",
     *                       "target/jms-journal.cli")</code>
     */
    String CLI_ONLINE_SCRIPT = "cargo.jboss.script.cli.online";
}
