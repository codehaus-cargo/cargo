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
package org.codehaus.cargo.maven2;

import java.net.URL;

import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployer.DeployableMonitorListener;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;

/**
 * Deploy a deployable to a container.
 *
 * @goal deployer-deploy
 * @requiresDependencyResolution compile
 * @version $Id$
 */
public class DeployerDeployMojo extends AbstractDeployerMojo
{
    public class DeployerListener implements DeployableMonitorListener
    {
        private Deployable deployable;

        public DeployerListener(Deployable deployable)
        {
            this.deployable = deployable;
        }

        /*
        * @see org.codehaus.cargo.container.deployer.DeployableMonitorListener#deployed()
        */
        public void deployed()
        {
            getLog().debug("Watchdog finds [" + this.deployable.getFile() + "] deployed.");
        }

        /*
         * @see org.codehaus.cargo.container.deployer.DeployableMonitorListener#undeployed()
         */
        public void undeployed()
        {
            getLog().debug("Watchdog finds [" + this.deployable.getFile() + "] not deployed yet.");
        }
    }

    protected void performDeployerActionOnSingleDeployable(
        org.codehaus.cargo.container.deployer.Deployer deployer,
        org.codehaus.cargo.container.deployable.Deployable deployable, URL pingURL,
        Long pingTimeout)
    {
        getLog().debug("Deploying [" + deployable.getFile() + "]"
            + ((pingURL == null) ? " ..." : " using ping URL [" + pingURL + "]"
            + ((pingTimeout == null) ? "" : " and ping timeout [" + pingTimeout + "]")));

        if (pingURL != null)
        {
            deployer.deploy(deployable, createDeployableMonitor(pingURL, pingTimeout, deployable));
        }
        else
        {
            deployer.deploy(deployable);
        }
    }

    private DeployableMonitor createDeployableMonitor(URL pingURL, Long pingTimeout,
        Deployable deployable)
    {
        DeployableMonitor monitor;
        if (pingTimeout == null)
        {
            monitor = new URLDeployableMonitor(pingURL);
        }
        else
        {
            monitor = new URLDeployableMonitor(pingURL, pingTimeout.longValue());
        }
        DeployerListener listener = new DeployerListener(deployable);
        monitor.registerListener(listener);
        return monitor;
    }
}
