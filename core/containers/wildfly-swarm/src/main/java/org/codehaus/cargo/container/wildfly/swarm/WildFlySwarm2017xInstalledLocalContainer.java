/*
 * ========================================================================
 *
 *  Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  ========================================================================
 */
package org.codehaus.cargo.container.wildfly.swarm;

import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.wildfly.swarm.internal.AbstractWildFlySwarmInstalledLocalContainer;

/**
 * WildFly Swarm 2017.x series container implementation.
 */
public class WildFlySwarm2017xInstalledLocalContainer extends
    AbstractWildFlySwarmInstalledLocalContainer
{
    /**
     * WildFly Swarm 2017.x series unique id.
     */
    static final String CONTAINER_ID = "wildfly-swarm2017x";

    /**
     * Version String.
     */
    static final String VERSION = "2017.x";

    /**
     * {@inheritDoc}
     * @see AbstractWildFlySwarmInstalledLocalContainer#AbstractWildFlySwarmInstalledLocalContainer(LocalConfiguration)
     */
    public WildFlySwarm2017xInstalledLocalContainer(LocalConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId()
    {
        return CONTAINER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getVersion()
    {
        return VERSION;
    }
}
