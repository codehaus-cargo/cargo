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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;

/**
 * Static deployer that deploys WARs and EARs to the JBoss <code>deployments</code> directory.
 * 
 * @version $Id$
 */
public class JBoss7xInstalledLocalDeployer extends JBossInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */
    public JBoss7xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * Specifies the directory where {@link Deployable}s
     * should be copied to. For JBoss container the target is the <code>deployments</code>
     * directory.
     * 
     * @return Deployable directory for the container
     */
    @Override
    public String getDeployableDir()
    {
        String altDeployDir = getContainer().getConfiguration().
        getPropertyValue(JBossPropertySet.ALTERNATIVE_DEPLOYMENT_DIR);
        if (altDeployDir != null && !"".equals(altDeployDir))
        {
            getContainer().getLogger().info("Using "
                + "non-default deployment target directory "
                + altDeployDir,
                JBoss7xInstalledLocalDeployer.class.getName());
            return getFileHandler().append(getContainer().
                getConfiguration().getHome(),
                altDeployDir);
        }
        else
        {
            return getFileHandler().append(getContainer().
                getConfiguration().getHome()
                , "deployments");
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer#deploy(Deployable)
     */
    @Override
    protected void doDeploy(String deployableDir, Deployable deployable)
    {
        super.doDeploy(deployableDir, deployable);

        if (deployable.isExpanded())
        {
            String deployableName = getDeployableName(deployable);
            getFileHandler().createFile(getFileHandler().append(deployableDir, deployableName
                + ".dodeploy"));
        }
    }

}
