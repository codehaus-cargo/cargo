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
package org.codehaus.cargo.container.tomee;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.tomcat.TomcatCopyingInstalledLocalDeployer;

/**
 * Static deployer that deploys EARs, EJBs and WARs to the TomEE directories.
 * 
 */
public class Tomee1xCopyingInstalledLocalDeployer extends TomcatCopyingInstalledLocalDeployer
{
    /**
     * @see #setShouldCopyWars(boolean)
     */
    private boolean shouldCopyWars = true;

    /**
     * {@inheritDoc}
     * @see TomcatCopyingInstalledLocalDeployer#TomcatCopyingInstalledLocalDeployer(LocalContainer)
     */
    public Tomee1xCopyingInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}. For TomEE this is the <code>apps</code> directory for EARs as well as EJBs
     * and the <code>webapps</code> directory for WARs.
     */
    @Override
    public String getDeployableDir(Deployable deployable)
    {
        if (DeployableType.EAR.equals(deployable.getType())
            || DeployableType.EJB.equals(deployable.getType()))
        {
            String apps = getContainer().getConfiguration().getPropertyValue(
                TomeePropertySet.APPS_DIRECTORY);
            String appsDirectory =
                getFileHandler().append(getContainer().getConfiguration().getHome(), apps);
            if (!getFileHandler().isDirectory(appsDirectory))
            {
                appsDirectory = getFileHandler().createDirectory(
                    getContainer().getConfiguration().getHome(), apps);
            }
            return appsDirectory;
        }
        else
        {
            return super.getDeployableDir(deployable);
        }
    }
}
