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
package org.codehaus.cargo.container.spi.deployer;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.DeployableMonitorListener;
import org.codehaus.cargo.util.log.LoggedObject;

/**
 * Wait for a deployable to be deployed.
 * 
 * @version $Id$
 */
public class DeployerWatchdog extends LoggedObject implements DeployableMonitorListener
{
    /**
     * The monitor to use to monitor the {@link org.codehaus.cargo.container.deployable.Deployable}
     * being deployed.
     */
    private DeployableMonitor monitor;

    /**
     * Status flag set to true when the {@link org.codehaus.cargo.container.deployable.Deployable}
     * is deployed.
     */
    private boolean isDeployed;

    /**
     * @param monitor the monitor to use to monitor the
     * {@link org.codehaus.cargo.container.deployable.Deployable} being deployed
     */
    public DeployerWatchdog(DeployableMonitor monitor)
    {
        this.monitor = monitor;
        monitor.registerListener(this);
    }

    /**
     * @see DeployableMonitorListener#deployed()
     */
    public void deployed()
    {
        this.isDeployed = true;
    }

    /**
     * @see DeployableMonitorListener#undeployed()
     */
    public void undeployed()
    {
        this.isDeployed = false;
    }

    /**
     * Wait till the monitored Deployable is made available or throw an exception if the timeout
     * period is reached. Equivalent to <code>watch(true)</code>.
     */
    public void watchForAvailability()
    {
        watch(true);
    }

    /**
     * Wait till the monitored Deployable is made unavailable or throw an exception if the timeout
     * period is reached. Equivalent to <code>watch(false)</code>.
     */
    public void watchForUnavailability()
    {
        watch(false);
    }

    /**
     * @param shouldWatchForAvailability if true then wait till Deployable is made available, if
     * false wait till the Deployable is made unavailable
     */
    public void watch(boolean shouldWatchForAvailability)
    {
        boolean exitCondition;

        try
        {
            long startTime = System.currentTimeMillis();
            do
            {
                if (System.currentTimeMillis() - startTime > this.monitor.getTimeout())
                {
                    String message = "Deployable [" + this.monitor.getDeployableName()
                        + "] failed to finish "
                        + (shouldWatchForAvailability ? "deploying" : "undeploying")
                        + " within the timeout period [" + this.monitor.getTimeout()
                        + "]. The Deployable state is thus unknown.";
                    getLogger().info(message, this.getClass().getName());
                    throw new ContainerException(message);
                }

                Thread.sleep(100);

                this.monitor.monitor();

                exitCondition = shouldWatchForAvailability ? !this.isDeployed : this.isDeployed;

            }
            while (exitCondition);
        }
        catch (InterruptedException e)
        {
            throw new ContainerException("Failed to monitor deployment", e);
        }
    }
}
