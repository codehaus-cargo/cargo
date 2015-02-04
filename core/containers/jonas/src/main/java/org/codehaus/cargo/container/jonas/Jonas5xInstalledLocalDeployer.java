/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Static deployer that deploys WAR, EAR, EJB, RAR, File and Bundle to JOnAS.
 * 
 * @version $Id$
 */
public class Jonas5xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * Whether calls to methods of this deployer should generate warnings.
     */
    private boolean warn;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public Jonas5xInstalledLocalDeployer(LocalContainer container)
    {
        super(container);

        // By default, warn the user about this deployer's limitations
        this.warn = true;
    }

    /**
     * @param warn Whether calls to methods of this deployer should generate warnings.
     */
    public void setWarn(boolean warn)
    {
        this.warn = warn;
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractCopyingInstalledLocalDeployer#deploy(Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        // If necessary, warn the user about this deployer's limitations
        warn();

        super.deploy(deployable);
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractCopyingInstalledLocalDeployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        // If necessary, warn the user about this deployer's limitations
        warn();

        String deployableFilename =
            getDeployableDir(deployable) + "/" + getDeployableName(deployable);
        getFileHandler().delete(deployableFilename);
    }

    /**
     * {@inheritDoc}. For JOnAS 5.x this is the <code>deploy</code> directory.
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        return getContainer().getConfiguration().getHome() + "/deploy";
    }

    /**
     * If necessary, warn the user about this deployer's limitations.
     */
    protected void warn()
    {
        if (this.warn)
        {
            getLogger().warn("The jonas5x local deployer requires the target JOnAS server to be in"
                + " development mode.", getClass().getName());
            getLogger().warn("", getClass().getName());
            getLogger().warn("If this is not the case, please use the jonas5x remote deployer.",
                getClass().getName());
            getLogger().warn("Note that the jonas5x remote deployer can be used on a local server"
                + " by setting the server name to localhost.", getClass().getName());
        }
    }
}
