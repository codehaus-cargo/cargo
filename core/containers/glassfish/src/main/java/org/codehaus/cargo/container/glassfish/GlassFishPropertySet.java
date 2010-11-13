/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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
    String ADMIN_PORT = "cargo.glassfish.adminPort";

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
     * Glassfish domain name.
     */
    String DOMAIN_NAME = "cargo.glassfish.domain.name";
}
