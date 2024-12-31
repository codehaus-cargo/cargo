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
package org.codehaus.cargo.container.spi.deployer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.util.log.LogLevel;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Base deployer for local and remote deployments.
 */
public abstract class AbstractDeployer extends LoggedObject implements Deployer
{
    /**
     * @param container the container into which to perform deployment operations
     */
    public AbstractDeployer(Container container)
    {
        setLogger(container.getLogger());
    }

    /**
     * Helper method to deploy multiple deployables at a time.
     * @see Deployer#deploy(Deployable)
     * @param deployables Deployables to deploy.
     */
    public void deploy(List<Deployable> deployables)
    {
        for (Deployable deployable : deployables)
        {
            deploy(deployable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(Deployable deployable, DeployableMonitor monitor)
    {
        try
        {
            deploy(deployable);
        }
        catch (Throwable t)
        {
            // CARGO-1100: When the deployment action has failed, log the failure and then wait for
            // the watchdog to return. If deployment was indeed complete, the watchdog will detect
            // it; else it will make the method fail.
            getLogger().info("The deployment has failed: " + t.toString(),
                this.getClass().getName());
            if (getLogger().getLevel() == LogLevel.DEBUG)
            {
                Writer stackTrace = new StringWriter();
                t.printStackTrace(new PrintWriter(stackTrace));
                getLogger().debug(stackTrace.toString(), this.getClass().getName());
            }
        }

        // Wait for the Deployable to be deployed
        DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
        watchdog.setLogger(getLogger());
        watchdog.watchForAvailability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable, DeployableMonitor monitor)
    {
        try
        {
            undeploy(deployable);
        }
        catch (Throwable t)
        {
            // CARGO-1100: When the undeployment action has failed, log the failure and then wait
            // for the watchdog to return. If undeployment was indeed complete, the watchdog will
            // detect it; else it will make the method fail.
            getLogger().info("The undeployment has failed: " + t.toString(),
                this.getClass().getName());
            if (getLogger().getLevel() == LogLevel.DEBUG)
            {
                Writer stackTrace = new StringWriter();
                t.printStackTrace(new PrintWriter(stackTrace));
                getLogger().debug(stackTrace.toString(), this.getClass().getName());
            }
        }

        // Wait for the Deployable to be undeployed
        DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
        watchdog.setLogger(getLogger());
        watchdog.watchForUnavailability();
    }

    /**
     * Helper method to redeploy multiple deployables at a time.
     * @see Deployer#redeploy(Deployable)
     * @param deployables Deployables to redeploy.
     */
    public void redeploy(List<Deployable> deployables)
    {
        for (Deployable deployable : deployables)
        {
            redeploy(deployable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void redeploy(Deployable deployable, DeployableMonitor monitor)
    {
        try
        {
            undeploy(deployable);
        }
        catch (Throwable t)
        {
            getLogger().info("The undeployment phase of the redeploy action has failed: "
                + t.toString(), this.getClass().getName());
            if (getLogger().getLevel() == LogLevel.DEBUG)
            {
                Writer stackTrace = new StringWriter();
                t.printStackTrace(new PrintWriter(stackTrace));
                getLogger().debug(stackTrace.toString(), this.getClass().getName());
            }
        }

        // Wait for the Deployable to be undeployed
        DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
        watchdog.setLogger(getLogger());
        watchdog.watchForUnavailability();

        deploy(deployable, monitor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Deployable deployable, DeployableMonitor monitor)
    {
        start(deployable);

        // Wait for the Deployable to be started
        DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
        watchdog.setLogger(getLogger());
        watchdog.watchForAvailability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(Deployable deployable, DeployableMonitor monitor)
    {
        stop(deployable);

        // Wait for the Deployable to be stopped
        DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
        watchdog.setLogger(getLogger());
        watchdog.watchForUnavailability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        throw new ContainerException("Not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        try
        {
            undeploy(deployable);
        }
        catch (Throwable t)
        {
            getLogger().info("The undeployment phase of the redeploy action has failed: "
                + t.toString(), this.getClass().getName());
        }

        deploy(deployable);
    }
}
