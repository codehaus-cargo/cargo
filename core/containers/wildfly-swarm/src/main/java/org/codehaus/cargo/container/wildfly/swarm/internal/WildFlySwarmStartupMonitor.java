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
package org.codehaus.cargo.container.wildfly.swarm.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.spi.startup.AbstractPingContainerMonitor;
import org.codehaus.cargo.container.wildfly.swarm.WildFlySwarmPropertySet;

/**
 * Monitors URL that is provided as a mandatory configuration property. WildFly Swarm does not
 * accept deployments - container is bundled together with application, thus defining the ping URL
 * is user's responsibility.
 */
public class WildFlySwarmStartupMonitor extends AbstractPingContainerMonitor
{

    /**
     * {@inheritDoc}
     * @see AbstractPingContainerMonitor#AbstractPingContainerMonitor(Container)
     */
    public WildFlySwarmStartupMonitor(final Container container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URL getPingUrl()
    {
        String pingUrl =
                getConfiguration().getPropertyValue(WildFlySwarmPropertySet.SWARM_APPLICATION_URL);
        try
        {
            return new URL(pingUrl);
        }
        catch (MalformedURLException ex)
        {
            throw new ContainerException("The WildFly Swarm ping URL [" + pingUrl
                    + "] is not a valid URL. ", ex);
        }
    }
}
