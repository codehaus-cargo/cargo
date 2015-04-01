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

import java.io.File;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Static deployer that deploys WARs and EARs to the JBoss <code>deploy</code> directory.
 * 
 */
public class JBossInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(org.codehaus.cargo.container.LocalContainer)
     */
    public JBossInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}. For JBoss container the target is the <code>deploy</code> directory.
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        String clustered = getContainer().getConfiguration().
                                 getPropertyValue(JBossPropertySet.CLUSTERED);

        if (Boolean.valueOf(clustered).booleanValue())
        {
            return getFileHandler().append(getContainer().getConfiguration().getHome(), "farm");
        }
        else
        {
            return getFileHandler().append(getContainer().getConfiguration().getHome(), "deploy");
        }
    }

    /**
     * {@inheritDoc}. We override the base implementation because JBoss requires that expanded WAR
     * directories to end with <code>.war</code> so we have to rename the expanded WAR directory.
     * See <a href="http://docs.jboss.org/jbossas/jboss4guide/r4/html/ch2.chapter.html#d0e5347">
     * the JBoss documentation for AbstractWebDeployer</a>.
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
        if (deployable.getType() == DeployableType.WAR)
        {
            WAR war = (WAR) deployable;
            undeployFile(getDeployableDir(deployable), war.getContext() + ".war");
        }

        else if (deployable.getType() == DeployableType.EAR)
        {
            EAR ear = (EAR) deployable;
            undeployFile(getDeployableDir(deployable), ear.getName() + ".ear");
        }

        else if (deployable.getType() == DeployableType.FILE
                || deployable.getType() == DeployableType.SAR)
        {
            String fileName = getFileHandler().getName(deployable.getFile());
            undeployFile(getDeployableDir(deployable), fileName);
        }

        else
        {
            super.undeploy(deployable);
            return;
        }
    }

    /**
     * Checks whether file or dir represented by string exists.
     * 
     * @param fileName path to check
     * @return true if file/dir exists
     */
    private boolean fileExists(String fileName)
    {
        return new File(fileName).exists();
    }

    /**
     * Undeploy the file in specified directory with the specified file name.
     * 
     * @param directory The directory name
     * @param file The file name
     */
    private void undeployFile(String directory, String file)
    {
        String fileName = getFileHandler().append(directory, file);
        if (fileExists(fileName))
        {
            getLogger().info("Undeploying [" + fileName + "]...", this.getClass().getName());
            getFileHandler().delete(fileName);
        }
        else
        {
            getLogger().info(
                    "Couldn't not find file to undeploy [" + fileName + "]",
                    this.getClass().getName());
        }
    }

}
