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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;

/**
 * Static deployer that deploys WARs to the Tomcat <code>webapps</code> directory.
 *  
 * @version $Id$
 */
public class TomcatCopyingInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * @see #setShouldCopyWars(boolean)
     */
    private boolean shouldCopyWars = true;
    
    /**
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */
    public TomcatCopyingInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);
    }

    /**
     * Specifies the directory {@link org.codehaus.cargo.container.deployable.Deployable}s should
     * be copied to. For Tomcat this is the <code>webapps</code> directory.
     *
     * @return Deployable the directory to deploy to
     */
    @Override
    public String getDeployableDir()
    {
        return getFileHandler().append(getContainer().getConfiguration().getHome(), "webapps");
    }

    /**
     * Whether the local deployer should copy the wars to the Tomcat webapps directory. This is
     * because Tomcat standalone configuration may not want to copy wars and instead configure
     * server.xml to point to where the wars are located instead of copying them.
     * 
     * @param shouldCopyWars true if the wars should be copied
     */
    public void setShouldCopyWars(boolean shouldCopyWars)
    {
        this.shouldCopyWars = shouldCopyWars;
    }
   
    /**
     * We override the default implementation from {@link AbstractCopyingInstalledLocalDeployer} in
     * order to handle the special Tomcat scenarios: if the deployable is a {@link TomcatWAR}
     * instance and it containts a <code>context.xml</code> file that we need to manually copy.
     * 
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#deployWar(String, org.codehaus.cargo.container.deployable.WAR)
     */
    @Override
    protected void deployWar(String deployableDir, WAR war)
    {
        if (war instanceof TomcatWAR)
        {
            TomcatWAR tomcatWar = (TomcatWAR) war;

            // If the WAR contains a META-INF/context.xml then it means the user is 
            // defining how to deploy it.
            if (tomcatWar.containsContextFile())
            {
                // Drop WAR files into the webapps dir, Tomcat will read META-INF/context.xml itself
                super.deployWar(deployableDir, war);
            }
            else if (this.shouldCopyWars)
            {
                super.deployWar(deployableDir, war);
            }
        }
        else if (this.shouldCopyWars)
        {
            super.deployWar(deployableDir, war);
        }
    }

    /**
     * We override the default implementation from {@link AbstractCopyingInstalledLocalDeployer} in
     * order to handle the special Tomcat scenarios: if the deployable is a {@link TomcatWAR}
     * instance and it contains a <code>context.xml</code> file that we need to manually copy.
     * 
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#deployExpandedWar(String, org.codehaus.cargo.container.deployable.WAR)
     */
    @Override
    protected void deployExpandedWar(String deployableDir, WAR war)
    {
        if (war instanceof TomcatWAR)
        {
            TomcatWAR tomcatWar = (TomcatWAR) war;

            // If the WAR contains a META-INF/context.xml then it means the user is 
            // defining how to deploy it.
            if (tomcatWar.containsContextFile())
            {
                // Note: We know here that the war is an expanded war as this method is only called 
                // for expanded wars...

                String contextDir = getFileHandler().createDirectory(
                    getContainer().getConfiguration().getHome(),
                    "conf/Catalina/" + getContainer().getConfiguration().getPropertyValue(
                        GeneralPropertySet.HOSTNAME));

                // Copy context.xml to <config>/Catalina/<hostname>/<context-path>.xml
                getFileHandler().copyFile(
                    getFileHandler().append(tomcatWar.getFile(), "META-INF/context.xml"),
                    getFileHandler().append(contextDir, tomcatWar.getContext() + ".xml"));
            }
            else if (this.shouldCopyWars)
            {
                super.deployExpandedWar(deployableDir, war);
            }
        }
        else
        {
            if (this.shouldCopyWars)
            {
                super.deployExpandedWar(deployableDir, war);
            }
        }
    }

    /**
     * Undeploy WAR deployables by deleting the local file from the Tomcat webapps directory.
     *
     * {@inheritDoc}
     * @see AbstractCopyingInstalledLocalDeployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        // Check that the container supports the deployable type to undeploy
        if (!getContainer().getCapability().supportsDeployableType(deployable.getType()))
        {
            throw new ContainerException(getContainer().getName() + " doesn't support ["
                + deployable.getType().getType().toUpperCase() + "] archives. Got ["
                + deployable.getFile() + "]");
        }

        String deployableDir = getDeployableDir();
        try
        {
            if (deployable.getType() == DeployableType.WAR)
            {
                WAR war = (WAR) deployable;
                String context = war.getContext();
                getLogger().info("Undeploying context [" + context + "] from [" + deployableDir
                    + "]...", this.getClass().getName());

                // Delete either the WAR file or the expanded WAR directory.
                String warLocation;
                if (war.isExpandedWar())
                {
                    warLocation = getFileHandler().append(deployableDir, context);
                }
                else
                {
                    warLocation = getFileHandler().append(deployableDir, context + ".war");
                }

                if (getFileHandler().exists(warLocation))
                {
                    getLogger().info("Trying to delete WAR from [" + warLocation + "]...",
                        this.getClass().getName());
                    getFileHandler().delete(warLocation);
                }
                else
                {
                    throw new ContainerException("Failed to undeploy as there is no WAR at ["
                        + warLocation + "]");
                }
            }
            else
            {
                throw new ContainerException("Only WAR undeployment is currently supported");
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to undeploy [" + deployable.getFile()
                + "] from [" + deployableDir + "]", e);
        }
    }
}
