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
package org.codehaus.cargo.container.deployer;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Common properties and configuration for deployable monitors.
 */
public abstract class AbstractDeployableMonitor extends LoggedObject implements DeployableMonitor
{
    /**
     * List of {@link DeployableMonitorListener} that we will notify when the
     * {@link org.codehaus.cargo.container.deployable.Deployable} is deployed or undeployed.
     */
    private List<DeployableMonitorListener> listeners;

    /**
     * The timeout after which we stop waiting for deployment.
     */
    private long timeout;

    /**
     * Constructor with default 20s timeout.
     */
    public AbstractDeployableMonitor()
    {
        this(20000L);
    }

    /**
     * @param timeout the timeout after which we stop monitoring the deployment
     */
    public AbstractDeployableMonitor(long timeout)
    {
        this.listeners = new ArrayList<DeployableMonitorListener>();
        this.timeout = timeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerListener(DeployableMonitorListener listener)
    {
        this.listeners.add(listener);
    }

    /**
     * Notify listeners that deployable is deployed/undeployed.
     * @param isDeployed True is deployable is deployed, false otherwise.
     */
    protected void notifyListeners(boolean isDeployed)
    {
        for (DeployableMonitorListener listener : listeners)
        {
            getLogger().debug("Notifying monitor listener [" + listener + "]",
                this.getClass().getName());

            if (isDeployed)
            {
                listener.deployed();
            }
            else
            {
                listener.undeployed();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimeout()
    {
        return this.timeout;
    }
}
