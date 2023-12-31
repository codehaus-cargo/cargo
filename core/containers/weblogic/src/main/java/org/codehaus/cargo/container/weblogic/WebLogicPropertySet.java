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
package org.codehaus.cargo.container.weblogic;

/**
 * Gathers all WebLogic properties.
 */
public interface WebLogicPropertySet
{
    /**
     * User with administrator rights.
     */
    String ADMIN_USER = "cargo.weblogic.administrator.user";

    /**
     * Password for user with administrator rights.
     */
    String ADMIN_PWD = "cargo.weblogic.administrator.password";

    /**
     * WebLogic server name.
     */
    String SERVER = "cargo.weblogic.server";

    /**
     * The auto-deploy folder, if different then default.
     */
    String DEPLOYABLE_FOLDER = "cargo.weblogic.deployable.folder";

    /**
     * BEA Home. This is where bea products are installed.
     */
    String BEA_HOME = "cargo.weblogic.bea.home";

    /**
     * Version of the domain configuration. Used in WebLogic 9x+. format: 9.2.3.0.
     */
    String CONFIGURATION_VERSION = "cargo.weblogic.configuration.version";

    /**
     * Lowest common denominator of the servers in the domain. Used in WebLogic 9x+. format:
     * 9.2.3.0.
     */
    String DOMAIN_VERSION = "cargo.weblogic.domain.version";

    /**
     * Log level used in the server log.
     */
    String LOGGING = "cargo.weblogic.logging";

    /**
     * Which way is log file rotated.<br>
     * Possible values are: "none", "byTime", "bySize".
     */
    String LOG_ROTATION_TYPE = "cargo.weblogic.logging.rotation.type";

    /**
     * WebLogic JMS server name.
     */
    String JMS_SERVER = "cargo.weblogic.jms.server";

    /**
     * WebLogic JMS module name.
     */
    String JMS_MODULE = "cargo.weblogic.jms.module";

    /**
     * WebLogic JMS subdeployment name.
     */
    String JMS_SUBDEPLOYMENT = "cargo.weblogic.jms.subdeployment";

    /**
     * External offline jython script file paths.<br>
     * Used for custom configuration of WebLogic container.<br>
     * <br>
     * Example:<br>
     * /home/me/script1.py|<br>
     * /home/me/script2.py<br>
     */
    String JYTHON_SCRIPT_OFFLINE = "cargo.weblogic.script.jython.offline";

    /**
     * External online jython script file paths.<br>
     * Used for custom configuration of WebLogic container.<br>
     * <br>
     * Example:<br>
     * /home/me/script1.py|<br>
     * /home/me/script2.py<br>
     */
    String JYTHON_SCRIPT_ONLINE = "cargo.weblogic.script.jython.online";

    /**
     * Whether to replace properties, surrounded by <code>@</code> symbols, when reading the files
     * {@link WebLogicPropertySet#JYTHON_SCRIPT_ONLINE} and/or
     * {@link WebLogicPropertySet#JYTHON_SCRIPT_OFFLINE}.
     */
    String JYTHON_SCRIPT_REPLACE_PROPERTIES = "cargo.weblogic.script.jython.replaceProperties";

    /**
     * Specifies whether to ignore the installed implementation of the
     * weblogic.security.SSL.HostnameVerifier interface
     * (when this server is acting as a client to another application server).<br>
     * Possible values: true or false.<br>
     * Default set to true for test purposes.
     */
    String SSL_HOSTNAME_VERIFICATION_IGNORED = "cargo.weblogic.ssl.verification.hostname.ignored";

    /**
     * The name of the class that implements the weblogic.security.SSL.HostnameVerifier interface.
     * Used for setting Custom Hostname Verifier.<br>
     * Default set to None.
     */
    String SSL_HOSTNAME_VERIFIER_CLASS = "cargo.weblogic.ssl.verification.hostname.class";

    /**
     * Timeout for JTA transactions.
     * If the transaction is still in the "active" state after this time (counting from begin()),
     * it is automatically rolled back.
     */
    String JTA_TRANSACTION_TIMEOUT = "cargo.weblogic.jta.timeout";

    /**
     * Path to local WebLogic home. This property is used in Remote container
     * to gather WLST dependencies needed for remote communication with WebLogic instance.
     *
     * WebLogic needs to be installed locally even for remote calls.
     */
    String LOCAL_WEBLOGIC_HOME = "cargo.weblogic.installation.home";

    /**
     * Minimal length of user password - number.
     * Used when configuring new users.<br>
     * <b>Note</b>: Please be aware that WebLogic 1.2.1.4 onwards doesn't have a configurable
     * password validator.
     */
    String PASSWORD_LENGTH_MIN = "cargo.weblogic.password.length.min";

    /**
     * Minimal number of numeric or special characters in password - number.
     * Used when configuring new users.<br>
     * <b>Note</b>: Please be aware that WebLogic 1.2.1.4 onwards doesn't have a configurable
     * password validator.
     */
    String PASSWORD_SPNUM_MIN = "cargo.weblogic.password.spnum.min";

    /**
     * If set to false then deployments are deployed to WebLogic in stopped state.
     * If set to true then deployments are deployed while WebLogic is running.
     *
     * Online deployment is usable for deploying larger applications with roles defined in web.xml.
     *
     * Possible values: true or false. Default value false.
     */
    String ONLINE_DEPLOYMENT = "cargo.weblogic.deployment.online";
}
