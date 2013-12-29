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
package org.codehaus.cargo.container.glassfish;

/**
 * Interface for Glassfish-specific properties.
 * 
 * @version $Id$
 */
public interface GlassFishPropertySet
{
    /**
     * The admin HTTP port that Glassfish will use. Defaults to 4848.
     */
    String ADMIN_PORT = "cargo.glassfish.admin.port";

    /**
     * The value for --portbase parameter. No default.
     */
    String PORT_BASE = "cargo.glassfish.portbase";

    /**
     * By default, GlassFish deploys a datasource called <code>DerbyPool</code> which attempts to
     * connect to a locally running Derby database on port <code>1527</code>. On the other hand, it
     * will not by default start such a server -as a result, it is safer to remove this datasource
     * in order to avoid confusing the Glassfish persistence manager.
     */
    String REMOVE_DEFAULT_DATASOURCE = "cargo.glassfish.removeDefaultDatasource";

    //
    // these names are named to match asadmin --domainproperties option
    //

    /**
     * JMS port. Defaults to 7676.
     */
    String JMS_PORT = "cargo.glassfish.jms.port";

    /**
     * IIOP port. Defaults to 3700.
     */
    String IIOP_PORT = "cargo.glassfish.orb.listener.port";

    /**
     * HTTPS port. Defaults to 8181.
     */
    String HTTPS_PORT = "cargo.glassfish.http.ssl.port";

    /**
     * IIOP+SSL port. Defaults to 3820.
     */
    String IIOPS_PORT = "cargo.glassfish.orb.ssl.port";

    /**
     * IIOP mutual authentication port. Defaults to 3920.
     */
    String IIOP_MUTUAL_AUTH_PORT = "cargo.glassfish.orb.mutualauth.port";

    /**
     * JMX admin port. Defaults to 8686.
     */
    String JMX_ADMIN_PORT = "cargo.glassfish.domain.jmxPort";

    /**
     * Debugger port. Defaults to 9009.
     *
     * GlassFish 3 only.
     */
    String DEBUGGER_PORT = "cargo.glassfish.java.debugger.port";

    /**
     * Felix shell service port. Defaults to 6666.
     *
     * GlassFish 3 only.
     */
    String OSGI_SHELL_PORT = "cargo.glassfish.osgi.shell.telnet.port";

    /**
     * Glassfish domain name.
     */
    String DOMAIN_NAME = "cargo.glassfish.domain.name";

    /**
     * Glassfish domain debug mode.
     */
    String DEBUG_MODE = "cargo.glassfish.domain.debug";
    
    /**
     * Prefix for extra arguments for "asadmin deploy"
     */
    String DEPLOY_ARG_PREFIX = "cargo.glassfish.deploy.arg.";
    
    /**
     * Prefix for extra arguments for "asadmin undeploy"
     */
    String UNDEPLOY_ARG_PREFIX = "cargo.glassfish.undeploy.arg.";
}
