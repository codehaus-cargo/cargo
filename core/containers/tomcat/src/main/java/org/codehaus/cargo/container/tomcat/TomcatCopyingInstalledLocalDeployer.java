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
package org.codehaus.cargo.container.tomcat;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.WAR;
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
    public String getDeployableDir()
    {
        return getFileHandler().append(getContainer().getConfiguration().getHome(), "webapps");
    }

    /**
     * Whether the local deployer should copy the wars to tbe Tomcat webapps directory. This is 
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
}
