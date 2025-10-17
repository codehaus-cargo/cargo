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
package org.codehaus.cargo.container.jetty;

import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Special container support for the Jetty 12.x servlet container.
 */
public class Jetty12xInstalledLocalContainer extends Jetty11xInstalledLocalContainer
{
    /**
     * Unique container id.
     */
    public static final String ID = "jetty12x";

    /**
     * Default EE version for the Jetty deployer.
     * @see JettyPropertySet#DEPLOYER_EE_VERSION
     */
    public static final String DEFAULT_DEPLOYER_EE_VERSION = "ee10";

    /**
     * Default list of Jetty modules to activate.
     * @see JettyPropertySet#MODULES
     */
    public static final String DEFAULT_MODULES = "server,ext,http,"
        + DEFAULT_DEPLOYER_EE_VERSION + "-annotations,"
            + DEFAULT_DEPLOYER_EE_VERSION + "-plus,"
                + DEFAULT_DEPLOYER_EE_VERSION + "-jsp,"
                    + DEFAULT_DEPLOYER_EE_VERSION + "-deploy";

    /**
     * Jetty12xInstalledLocalContainer Constructor.
     * @param configuration The configuration associated with the container
     */
    public Jetty12xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return ID;
    }
}
