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
package org.codehaus.cargo.container.jboss;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Static deployer that deploys WARs and EARs to the JBoss <code>deploy</code> directory.
 * 
 * @version $Id$
 */
public class JBossInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */
    public JBossInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * Specifies the directory where {@link org.codehaus.cargo.container.deployable.Deployable}s
     * should be copied to. For JBoss container the target is the <code>deploy</code> directory.
     * 
     * @return Deployable directory for the container
     */
    public String getDeployableDir()
    {  
        String clustered = getContainer().getConfiguration().
                getPropertyValue(JBossPropertySet.CLUSTERED);
        
        if (Boolean.parseBoolean(clustered))
        {
            return getFileHandler().append(getContainer().getConfiguration().getHome(), "farm");
        }
        else
        {
            return getFileHandler().append(getContainer().getConfiguration().getHome(), "deploy");
        }
    }

    /**
     * Copy the full expanded WAR directory to the deployable directory, renaming it if the user
     * has specified a custom context for this expanded WAR.
     *
     * @param deployableDir the directory where the container is expecting deployables to be dropped
     *        for deployments
     * @param war the expanded WAR war
     *
     * <p>JBoss requires that expanded WAR directories end with <code>.war</code> so we have to
     * rename the expanded WAR directory. See the
     * <a href="http://docs.jboss.org/jbossas/jboss4guide/r4/html/ch2.chapter.html#d0e5347">
     * JBoss documentation for AbstractWebDeployer</a>.</p>
     *
     */
    protected void deployExpandedWar(String deployableDir, WAR war)
    {
        getFileHandler().copyDirectory(
                war.getFile(), getFileHandler().append(deployableDir, war.getContext() + ".war"));
    }
}
