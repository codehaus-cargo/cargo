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
package org.codehaus.cargo.maven2;

import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.maven2.configuration.Deployable;

/**
 * Common code used by Cargo MOJOs that start a container.
 * 
 * @version $Id$
 */
public class AbstractContainerStartMojo extends AbstractCargoMojo
{
    /**
     * Local container.
     */
    protected LocalContainer localContainer;

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#doExecute()
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        Container container = createContainer();

        if (!container.getType().isLocal())
        {
            throw new MojoExecutionException("Only local containers can be started");
        }

        this.localContainer = (LocalContainer) container;
        addAutoDeployDeployable(this.localContainer);
        this.localContainer.start();

        if (getDeployablesElement() != null)
        {
            for (Deployable deployable : getDeployablesElement())
            {
                URL pingURL = deployable.getPingURL();
                if (pingURL != null)
                {
                    DeployableMonitor monitor;
                    Long pingTimeout = deployable.getPingTimeout();
                    if (pingTimeout == null)
                    {
                        monitor = new URLDeployableMonitor(pingURL);
                    }
                    else
                    {
                        monitor = new URLDeployableMonitor(pingURL, pingTimeout.longValue());
                    }
                    DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
                    watchdog.setLogger(container.getLogger());
                    monitor.setLogger(container.getLogger());
                    watchdog.watchForAvailability();
                }
            }
        }
    }

    /**
     * If the project's packaging is war, ear or ejb and there is no deployer specified and the user
     * has not defined the auto-deployable inside the <code>&lt;deployables&gt;</code> element, then
     * add the generated artifact to the list of deployables to deploy statically.
     * 
     * Note that the reason we check that a deployer element has not been specified is because if it
     * has then the auto deployable will be deployed by the specified deployer.
     * 
     * @param container the local container to which to add the project's artifact
     * @throws MojoExecutionException if an error occurs
     */
    private void addAutoDeployDeployable(LocalContainer container)
        throws MojoExecutionException
    {
        if (getDeployerElement() == null && getCargoProject().getPackaging() != null
            && getCargoProject().isJ2EEPackaging())
        {
            // Has the auto-deployable already been specified as part of the <deployables> config
            // element?
            if (getDeployablesElement() == null
                || !containsAutoDeployable(getDeployablesElement()))
            {
                LocalConfiguration configuration = container.getConfiguration();
                configuration.addDeployable(createAutoDeployDeployable(container));
            }
        }
    }
}
