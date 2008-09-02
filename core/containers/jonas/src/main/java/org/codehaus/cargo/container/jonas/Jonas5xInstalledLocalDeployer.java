/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.EJB;
import org.codehaus.cargo.container.deployable.RAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;
import org.codehaus.cargo.util.FileHandler;

/**
 * Static deployer that deploys WAR, EAR, EJB and RAR to JOnAS.
 * 
 * @version $Id$
 */
public class Jonas5xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */
    public Jonas5xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        this(container, null);
    }

    /**
     * Creation of a local deployer with a given file handler.
     * 
     * @param container the container to be used
     * @param fileHandler the file handler to use, can be null to use the default file handler
     *            implementation
     */
    public Jonas5xInstalledLocalDeployer(InstalledLocalContainer container, FileHandler fileHandler)
    {
        super(container);
        if (fileHandler != null)
        {
            super.setFileHandler(fileHandler);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#getDeployableDir()
     */
    public String getDeployableDir()
    {
        return getContainer().getConfiguration().getHome() + "/deploy";
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployRar(String, RAR)
     */
    public void deployRar(String deployableDir, RAR rar)
    {
        super.deployRar(deployableDir, rar);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployWar(String, WAR)
     */
    public void deployWar(String deployableDir, WAR war)
    {
        super.deployWar(deployableDir, war);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployEjb(String, EJB)
     */
    public void deployEjb(String deployableDir, EJB ejb)
    {
        super.deployEjb(deployableDir, ejb);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployEar(String, EAR)
     */
    public void deployEar(String deployableDir, EAR ear)
    {
        super.deployEar(deployableDir, ear);
    }

    /**
     * Copy the full expanded WAR directory to the deployable directory, renaming it if the user
     * has specified a custom context for this expanded WAR.
     *
     * @param deployableDir the directory where the container is expecting deployables to be dropped
     *        for deployments
     * @param war the expanded WAR war
     */
    public void deployExpandedWar(String deployableDir, WAR war)
    {
        getFileHandler().copyDirectory(
            war.getFile(), getFileHandler().append(deployableDir, war.getContext()));
    }
}
