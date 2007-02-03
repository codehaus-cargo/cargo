/*
 * ========================================================================
 *
 * Copyright 2005-2006 Vincent Massol.
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
package org.codehaus.cargo.container.spi.deployer;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.util.log.LoggedObject;

import java.util.Iterator;
import java.util.List;

/**
 * Base deployer for local and remote deployments.
 *
 * @version $Id$
 */
public abstract class AbstractDeployer extends LoggedObject implements Deployer
{
    /**
     * {@inheritDoc}
     * @see #deploy(Deployable)
     */
    public void deploy(List deployables)
    {
        Iterator it = deployables.iterator();
        while (it.hasNext())
        {
            Deployable deployable = (Deployable) it.next();
            deploy(deployable);
        }
    }

    /**
     * {@inheritDoc}
     * @see Deployer#deploy(Deployable, DeployableMonitor)
     */
    public void deploy(Deployable deployable, DeployableMonitor monitor)
    {
        deploy(deployable);

        // Wait for the Deployable to be deployed
        DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
        watchdog.setLogger(getLogger());
        watchdog.watchForAvailability();
    }

    /**
     * {@inheritDoc}
     * @see Deployer#deploy(Deployable)
     */
    public void deploy(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }

    /**
     * {@inheritDoc}
     * @see Deployer#start(org.codehaus.cargo.container.deployable.Deployable)
     */
    public void start(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }

    /**
     * {@inheritDoc}
     * @see Deployer#stop(org.codehaus.cargo.container.deployable.Deployable)
     */
    public void stop(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }

    /**
     * {@inheritDoc}
     * @see Deployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    public void undeploy(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }

    /**
     * {@inheritDoc}
     * @see Deployer#redeploy(Deployable)
     */
    public void redeploy(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }
}
