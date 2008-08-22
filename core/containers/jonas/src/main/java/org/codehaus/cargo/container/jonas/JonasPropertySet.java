/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2.
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
 * 
 * @version $Id$
 */
public class JonasPropertySet
{
    /**
     * Defines a deployable identifier for remote deployment, this identifier will be used within
     * JOnAS to identify a deployed application When not specified, the file name of the deployable
     * is used as the identifier.
     */
    public static final String JONAS_DEPLOYABLE_IDENTIFIER = "cargo.jonas.deployable.identifier";

    /**
     * The JOnAS target server name to be used for remote deployment, defaults to "jonas".
     */
    public static final String JONAS_SERVER_NAME = "cargo.jonas.server.name";

    /**
     * The JOnAS target domain name to be used for remote deployment, defaults to "jonas".
     */
    public static final String JONAS_DOMAIN_NAME = "cargo.jonas.domain.name";

    /**
     * The JOnAS target cluster name to be used for remote cluster deployment, defaults to null.
     * If null, we consider deployment is done on the target server only (not a cluster).
     * If not null, the target server must be set to the Domain Master's.
     */
    public static final String JONAS_CLUSTER_NAME = "cargo.jonas.cluster.name";

    /**
     * The JOnAS realm name for a standalone configuration, defaults to "memrlm_1", useful setting
     * for webapps using a custom authentication realm name.
     */
    public static final String JONAS_REALM_NAME = "cargo.jonas.realm.name";

    /**
     * The JOnAS web container class name implementation to be used for standalone configuration,
     * will be autodetected when no setting provided.
     */
    public static final String JONAS_WEBCONTAINER_CLASS_NAME =
        "cargo.jonas.webcontainer.class.name";

    /**
     * The JOnAS available datasources configs names(comma delimited) for standalone configuration,
     * defaults to "HSQL1". You will still have to configure manually in your JOnAS install home the
     * datasource.properties files. HSLQ1 should always be provided.
     */
    public static final String JONAS_AVAILABLES_DATASOURCES = "cargo.jonas.datasources.name";

    /**
     * For MEJB remote deployment only, defines the path in the JNDI tree of the MEJB remote object,
     * defaults to "ejb/mgmt/MEJB".
     */
    public static final String JONAS_MEJB_JNDI_PATH = "cargo.jonas.jndi.mejb.path";

    /**
     * For MEJB remote deployment only, defines the path where the JAAS configuration file is.
     * Defaults to "jaas.config", ignored if {@link JonasPropertySet#JONAS_MEJB_JAAS_ROLE} is not
     * set.
     */
    public static final String JONAS_MEJB_JAAS_FILE = "cargo.jonas.jndi.jaas.file";

    /**
     * For MEJB remote deployment only, defines the role to use when connecting. Defaults to null,
     * in which case no authentication is used.
     */
    public static final String JONAS_MEJB_JAAS_ROLE = "cargo.jonas.jndi.jaas.role";

    /**
     * For MEJB remote deployment only, the jndi initial context factory, defaults to
     * "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory".
     */
    public static final String JONAS_MEJB_JNDI_INIT_CTX_FACT =
        "cargo.jonas.jndi.initial.context.factory";

    /**
     * Utility classes don't have a public constructor.
     */
    protected JonasPropertySet()
    {
    }
}
