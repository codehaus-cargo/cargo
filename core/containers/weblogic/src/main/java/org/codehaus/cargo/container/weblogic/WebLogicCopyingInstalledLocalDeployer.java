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
package org.codehaus.cargo.container.weblogic;

import java.io.File;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Static deployer that deploys WARs to the WebLogic auto-deploy directory. EARs and RARs are not
 * supported, yet.
 * 
 * @version $Id$
 */
public class WebLogicCopyingInstalledLocalDeployer extends
        AbstractCopyingInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */
    public WebLogicCopyingInstalledLocalDeployer(
            InstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * Specifies the directory where {@link org.codehaus.cargo.container.deployable.Deployable}s
     * should be copied to. For WebLogic container the target is the auto-deploy directory.
     * 
     * @return Deployable directory for the container
     */
    @Override
    public String getDeployableDir()
    {
        WebLogicLocalContainer container = (WebLogicLocalContainer) getContainer();
        return new File(container.getConfiguration().getHome(), container
                .getAutoDeployDirectory()).toString();
    }

    /**
     * {@inheritDoc}. We override the base implementation because WebLogic requires that expanded
     * WAR directories to end with <code>.war</code> so we have to rename the expanded WAR
     * directory. See <a href="http://e-docs.bea.com/wls/docs81/deployment/overview.html#1036349">
     * the WebLogic documentation for Exploded Archive Directories</a>.
     */
    @Override
    protected String getDeployableName(Deployable deployable)
    {
        String deployableName = super.getDeployableName(deployable);
        if (DeployableType.WAR.equals(deployable.getType()) && deployable.isExpanded())
        {
            deployableName += ".war";
        }
        return deployableName;
    }

    /**
     * Removes previously deployed artifact.
     * 
     * @param deployable artifact to undeploy
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        if (deployable.getType() != DeployableType.WAR)
        {
            super.undeploy(deployable);
            return;
        }
        WAR war = (WAR) deployable;
        String fileName = getFileHandler().append(getDeployableDir(),
                war.getContext() + ".war");
        if (getFileHandler().exists(fileName))
        {
            getLogger().info("Undeploying [" + fileName + "]...",
                    this.getClass().getName());
            getFileHandler().delete(fileName);
        }
    }

}
