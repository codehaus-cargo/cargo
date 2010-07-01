/* 
 * ========================================================================
 * 
 * Codehaus CARGO, copyright 2004-2010 Vincent Massol.
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

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.State;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployer.Deployer;

/**
 * Implementation decides how to deploy or undeploy based on the running state of the server.
 * 
 * @version $Id$
 */
public abstract class AbstractSwitchableLocalDeployer extends AbstractInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalDeployer#AbstractInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public AbstractSwitchableLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }
    
    /**
     * this deployer will be used when the server is running.
     * 
     * @return deployer used when server is up
     */
    protected abstract Deployer getHotDeployer();

    /**
     * this deployer will be used when the server is not running.
     * 
     * @return deployer used when server is down
     */
    protected abstract Deployer getColdDeployer();

    /**
     * {@inheritDoc} deploys via hotDeployer, if the server is started or starting. Otherwise, it
     * uses the coldDeployer.
     * 
     * @see org.codehaus.cargo.container.spi.deployer.AbstractDeployer#deploy(org.codehaus.cargo.container.deployable.Deployable)
     * @param deployable - what to deploy
     */
    @Override
    public void deploy(Deployable deployable)
    {
        if (isRunning())
        {
            getHotDeployer().deploy(deployable);
        }
        else
        {
            getColdDeployer().deploy(deployable);
        }
    }

    /**
     * return whether or not the container is running.
     * 
     * @return true, if the container is running
     */
    private boolean isRunning()
    {
        return getContainer().getState().equals(State.STARTED)
            || getContainer().getState().equals(State.STARTING);
    }

    /**
     * {@inheritDoc} undeploys via hotDeployer, if the server is started or starting. Otherwise, it
     * uses the coldDeployer.
     * 
     * @see org.codehaus.cargo.container.spi.deployer.AbstractDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     * @param deployable - what to undeploy
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        if (isRunning())

        {
            getHotDeployer().undeploy(deployable);
        }
        else
        {
            getColdDeployer().undeploy(deployable);
        }
    }

}
