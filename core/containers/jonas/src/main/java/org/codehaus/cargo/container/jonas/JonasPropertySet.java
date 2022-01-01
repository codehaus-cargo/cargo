/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

/**
 * JOnAS specific properties.
 */
public class JonasPropertySet
{
    /**
     * The JOnAS target server name, defaults to "jonas".
     */
    public static final String JONAS_SERVER_NAME = "cargo.jonas.server.name";

    /**
     * The JOnAS target domain name, defaults to "jonas".
     */
    public static final String JONAS_DOMAIN_NAME = "cargo.jonas.domain.name";

    /**
     * The JOnAS target cluster name to be used for remote cluster deployment, defaults to null. If
     * null, we consider deployment is done on the target server only (not a cluster). If not null,
     * the target server must be set to the Domain Master's.
     */
    public static final String JONAS_CLUSTER_NAME = "cargo.jonas.cluster.name";

    /**
     * For MEJB remote deployment only, defines the path in the JNDI tree of the MEJB remote object,
     * defaults to "ejb/mgmt/MEJB".
     */
    public static final String JONAS_MEJB_JNDI_PATH = "cargo.jonas.jndi.mejb.path";

    /**
     * For MEJB remote deployment only, defines the path where the JAAS configuration file is.
     * Defaults to "jaas.config", ignored if {@link JonasPropertySet#JONAS_MEJB_JAAS_ENTRY} is not
     * set.
     */
    public static final String JONAS_MEJB_JAAS_FILE = "cargo.jonas.mejb.jaas.file";

    /**
     * For MEJB remote deployment only, defines the JAAS entry to use when connecting. Defaults to
     * null, in which case no authentication is used.
     */
    public static final String JONAS_MEJB_JAAS_ENTRY = "cargo.jonas.mejb.jaas.entry";

    /**
     * For MEJB remote deployment only, the jndi initial context factory, defaults to
     * "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory".
     */
    public static final String JONAS_MEJB_JNDI_INIT_CTX_FACT =
        "cargo.jonas.jndi.initial.context.factory";

    /**
     * Defines a deployable identifier for remote deployment, this identifier will be used within
     * JOnAS to identify a deployed application. When not specified, the file name of the deployable
     * is used as the identifier.
     */
    public static final String JONAS_DEPLOYABLE_IDENTIFIER = "cargo.jonas.deployable.identifier";

    /**
     * The list of JOnAS services to launch. Used in a standalone local configuration. Note that
     * this list is highly dependent on JOnAS versions. Check out your JOnAS documentation for
     * details.
     */
    public static final String JONAS_SERVICES_LIST = "cargo.jonas.services.list";

    /**
     * JMS port used by JOnAS.
     */
    public static final String JONAS_JMS_PORT = "cargo.jonas.jms.port";

    /**
     * Tells whether to ignore the version of the deployable when undeploying deployables. When
     * this is set to <code>true</code>, trying to undeploy <code>example-1.0.1.war</code> would
     * undeploy <code>JONAS_BASE/deploy/example.war</code>,
     * <code>JONAS_BASE/deploy/example-1.0.0.war</code>, etc. as long as the prefix and the
     * extension match.
     */
    public static final String JONAS_UNDEPLOY_IGNORE_VERSION = "cargo.jonas.undeploy.ignoreVersion";

    /**
     * Prefix used for configuration properties that will set values on the JOnAS configurator.
     */
    public static final String CONFIGURATOR_PREFIX = "cargo.jonas.configurator.";

    /**
     * Utility classes don't have a public constructor.
     */
    protected JonasPropertySet()
    {
    }
}
