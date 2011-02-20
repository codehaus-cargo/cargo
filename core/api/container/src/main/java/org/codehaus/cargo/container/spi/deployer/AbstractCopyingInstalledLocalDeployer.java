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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Bundle;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.EJB;
import org.codehaus.cargo.container.deployable.File;
import org.codehaus.cargo.container.deployable.RAR;
import org.codehaus.cargo.container.deployable.SAR;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Local deployer that deploys WARs, EJBs and EARs to a <code>deployable</code> directory of the
 * given installed container. Note that this deployer supports expanded WARs by copying the expanded
 * WAR to the <code>deployable</code> directory. In other words it does not support in-place
 * expanded WARs (i.e. expanded WARs located in a different directory).
 * 
 * @version $Id$
 */
public abstract class AbstractCopyingInstalledLocalDeployer extends AbstractInstalledLocalDeployer
{
    /**
     * @see #setShouldDeployExpandedWARs(boolean)
     */
    private boolean shouldDeployExpandedWARs;

    /**
     * @see #setShouldDeployExpandedSARs(boolean)
     */
    private boolean shouldDeployExpandedSARs;

    /**
     * @see #setShouldDeployExpandedRARs(boolean)
     */
    private boolean shouldDeployExpandedRARs;

    /**
     * Deployed Deployables.
     */
    private List<Deployable> deployedDeployables;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalDeployer#AbstractInstalledLocalDeployer(org.codehaus.cargo.container.InstalledLocalContainer)
     */
    public AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);

        this.shouldDeployExpandedWARs = true;
        this.shouldDeployExpandedSARs = true;
        this.shouldDeployExpandedRARs = true;
        this.deployedDeployables = new ArrayList<Deployable>();
    }

    /**
     * Decide whether expanded WARs should be deployed. Some classes using this deployer may not
     * want to deploy expanded WARs as they may want to deploy them in-situ by modifying the
     * container's configuration file to point to the location of the expanded WAR. This saves some
     * copying time and make it easier for development round-trips.
     * 
     * @param flag if true expanded WARs will be deployed
     */
    public void setShouldDeployExpandedWARs(boolean flag)
    {
        this.shouldDeployExpandedWARs = flag;
    }

    /**
     * Decide whether expanded SARs should be deployed. Some classes using this deployer may not
     * want to deploy expanded SARs as they may want to deploy them in-situ by modifying the
     * container's configuration file to point to the location of the expanded SAR. This saves some
     * copying time and make it easier for development round-trips.
     * 
     * @param flag if true expanded SARs will be deployed
     */
    public void setShouldDeployExpandedSARs(boolean flag)
    {
        this.shouldDeployExpandedSARs = flag;
    }

    /**
     * Decide whether expanded RARs should be deployed. Some classes using this deployer may not
     * want to deploy expanded RARs as they may want to deploy them in-situ by modifying the
     * container's configuration file to point to the location of the expanded RAR. This saves some
     * copying time and make it easier for development round-trips.
     * 
     * @param flag if true expanded RARs will be deployed
     */
    public void setShouldDeployExpandedRARs(boolean flag)
    {
        this.shouldDeployExpandedRARs = flag;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(Deployable)
     */
    @Override
    public synchronized void deploy(Deployable deployable)
    {
        if (!canBeDeployed(deployable))
        {
            throw new ContainerException("Failed to deploy [" + deployable.getFile() + "] to ["
                + getDeployableDir() + "]. The required web context is already in use"
                + " by another application.");
        }

        String deployableDir = getDeployableDir();
        getLogger().info("Deploying [" + deployable.getFile() + "] to [" + deployableDir + "]...",
            this.getClass().getName());

        // Check that the container supports the deployable type to deploy
        if (!getContainer().getCapability().supportsDeployableType(deployable.getType()))
        {
            throw new ContainerException(deployable.getType().getType().toUpperCase()
                + " archives are not supported for deployment in [" + getContainer().getId()
                + "]. Got [" + deployable.getFile() + "]");
        }

        try
        {
            if (deployable.getType() == DeployableType.WAR)
            {
                if (!((WAR) deployable).isExpandedWar())
                {
                    deployWar(deployableDir, (WAR) deployable);
                }
                else if (this.shouldDeployExpandedWARs)
                {
                    deployExpandedWar(deployableDir, (WAR) deployable);
                }
            }
            else if (deployable.getType() == DeployableType.EAR)
            {
                deployEar(deployableDir, (EAR) deployable);
            }
            else if (deployable.getType() == DeployableType.EJB)
            {
                deployEjb(deployableDir, (EJB) deployable);
            }
            else if (deployable.getType() == DeployableType.SAR)
            {
                if (deployable.isExpanded() && this.shouldDeployExpandedSARs)
                {
                    deployExpandedSar(deployableDir, (SAR) deployable);
                }
                else
                {
                    deploySar(deployableDir, (SAR) deployable);
                }
            }
            else if (deployable.getType() == DeployableType.RAR)
            {
                if (deployable.isExpanded() && this.shouldDeployExpandedRARs)
                {
                    deployExpandedRar(deployableDir, (RAR) deployable);
                }
                else
                {
                    deployRar(deployableDir, (RAR) deployable);
                }
            }
            else if (deployable.getType() == DeployableType.FILE)
            {
                deployFile(deployableDir, (File) deployable);
            }
            else if (deployable.getType() == DeployableType.BUNDLE)
            {
                deployBundle(deployableDir, (Bundle) deployable);
            }
            else
            {
                throw new ContainerException(
                        "Only WAR, EJB, EAR, RAR and SAR are currently supported");
            }
        }
        catch (Exception e)
        {
            throw new ContainerException("Failed to deploy [" + deployable.getFile() + "] to ["
                + deployableDir + "]", e);
        }

        this.deployedDeployables.add(deployable);
    }

    /**
     * Checks whether the given Deployable can actually be deployed and whether a deployable has a
     * web context path that already exist in another previously deployed Deployable.
     * 
     * @param newDeployable deployable
     * @return true, if the deployable can be deployed
     */
    protected boolean canBeDeployed(final Deployable newDeployable)
    {
        final Set<String> newDeployableContextSet = getWebContextsSet(newDeployable);
        for (Deployable deployedDeployable : this.deployedDeployables)
        {
            for (String webContext : getWebContexts(deployedDeployable))
            {
                if (newDeployableContextSet.contains(webContext))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Adapter method - to hide the fact that there is no unified API to retrieve web contexts from
     * a deployable.
     * 
     * @param deployable deployable
     * @return a list of all web contexts this deployable uses
     */
    private static List<String> getWebContexts(final Deployable deployable)
    {
        List<String> webContexts;
        if (deployable.getType() == DeployableType.EAR)
        {
            webContexts = ((EAR) deployable).getWebContexts();
        }
        else if (deployable.getType() == DeployableType.WAR)
        {
            webContexts = Arrays.asList(new String[] {((WAR) deployable).getContext()});
        }
        else
        {
            webContexts = Collections.emptyList();
        }
        return webContexts;
    }

    /**
     * Create a set of all web contexts for a Deployable.
     * 
     * @param deployable Deployable
     * @return a set of all web contexts contained in this deployable.
     */
    private static Set<String> getWebContextsSet(final Deployable deployable)
    {
        final Set<String> webContextSet = new HashSet<String>();
        for (String webContext : getWebContexts(deployable))
        {
            webContextSet.add(webContext);
        }
        return webContextSet;
    }

    /**
     * Specifies the directory {@link org.codehaus.cargo.container.deployable.Deployable}s should be
     * copied to.
     * 
     * @return Deployable directory
     */
    public abstract String getDeployableDir();

    /**
     * Copy the EAR file to the deployable directory.
     * 
     * @param deployableDir the directory where the container is expecting deployables to be dropped
     * for deployments
     * @param ear the EAR deployable
     */
    protected void deployEar(String deployableDir, EAR ear)
    {
        // CARGO-598: If the EAR has a name property, use that one (instead of the EAR file name)
        String earName = ear.getName();
        if (earName == null)
        {
            earName = getFileHandler().getName(ear.getFile());
        }
        if (!earName.toLowerCase().contains(".ear"))
        {
            earName = earName + ".ear";
        }

        getFileHandler().copyFile(ear.getFile(), getFileHandler().append(deployableDir, earName));
    }

    /**
     * Copy the SAR file to the deployable directory.
     * @param deployableDir The directory to copy it too
     * @param sar the sar to copy
     */
    protected void deploySar(String deployableDir, SAR sar)
    {
        getFileHandler().copyFile(sar.getFile(),
            getFileHandler().append(deployableDir, getFileHandler().getName(sar.getFile())));
    }

    /**
     * Copy the RAR file to the deployable directory.
     * @param deployableDir The directory to copy it too
     * @param rar the rar to copy
     */
    protected void deployRar(String deployableDir, RAR rar)
    {
        String rarName = rar.getName();
        if (rarName == null)
        {
            rarName = getFileHandler().getName(rar.getName());
        }
        if (!rarName.toLowerCase().contains(".rar"))
        {
            rarName = rarName + ".rar";
        }
        getFileHandler().copyFile(rar.getFile(),
            getFileHandler().append(deployableDir, rarName));
    }

    /**
     * Copy the EJB file to the deployable directory.
     * 
     * @param deployableDir the container's deployable directory
     * @param ejb the EJB deployable
     */
    protected void deployEjb(String deployableDir, EJB ejb)
    {
        getFileHandler().copyFile(ejb.getFile(),
            getFileHandler().append(deployableDir, getFileHandler().getName(ejb.getFile())));
    }

    /**
     * Copy the WAR file to the deployable directory, renaming it if the user has specified a custom
     * context for this WAR.
     * 
     * @param deployableDir the directory where the container is expecting deployables to be dropped
     * for deployments
     * @param war the WAR war
     */
    protected void deployWar(String deployableDir, WAR war)
    {
        String context = war.getContext();
        if ("".equals(context) || "/".equals(context))
        {
            getLogger().info("The WAR file has its context set to / and will therefore be "
                + "deployed as ROOT.war", this.getClass().getName());
            context = "ROOT";
        }

        getFileHandler().copyFile(
            war.getFile(), getFileHandler().append(deployableDir, context + ".war"));
    }

    /**
     * Copy the full expanded WAR directory to the deployable directory, renaming it if the user has
     * specified a custom context for this expanded WAR.
     * 
     * @param deployableDir the directory where the container is expecting deployables to be dropped
     * for deployments
     * @param war the expanded WAR war
     */
    protected void deployExpandedWar(String deployableDir, WAR war)
    {
        String context = war.getContext();
        if ("".equals(context) || "/".equals(context))
        {
            getLogger().info("The expanded WAR has its context set to / and will therefore be "
                + "deployed as ROOT", this.getClass().getName());
            context = "ROOT";
        }

        getFileHandler().copyDirectory(
            war.getFile(), getFileHandler().append(deployableDir, context));
    }

    /**
     * Copy the full expanded SAR directory to the deployable directory, renaming it if the user has
     * specified a custom context for this expanded SAR.
     * @param deployableDir the directory to deploy the expanded SAR
     * @param sar the expanded SAR sar
     */
    protected void deployExpandedSar(String deployableDir, SAR sar)
    {
        getFileHandler().copyDirectory(sar.getFile(),
            getFileHandler().append(deployableDir, getFileHandler().getName(sar.getFile())));
    }

    /**
     * Copy the full expanded RAR directory to the deployable directory, renaming it if the user has
     * specified a custom context for this expanded RAR.
     * @param deployableDir the directory to deploy the expanded RAR
     * @param rar the expanded RAR rar
     */
    protected void deployExpandedRar(String deployableDir, RAR rar)
    {
        getFileHandler().copyDirectory(rar.getFile(),
            getFileHandler().append(deployableDir, getFileHandler().getName(rar.getFile())));
    }

    /**
     * Copy the file to the deployable directory.
     * @param deployableDir the directory to hold the file
     * @param file The file to copy
     */
    protected void deployFile(String deployableDir, File file)
    {
        if (getFileHandler().isDirectory(file.getFile()))
        {
            getFileHandler().copyDirectory(
                    file.getFile(),
                    getFileHandler()
                            .append(deployableDir, getFileHandler().getName(file.getFile())));
        }
        else
        {
            getFileHandler().copyFile(
                    file.getFile(),
                    getFileHandler()
                            .append(deployableDir, getFileHandler().getName(file.getFile())));
        }
    }

    /**
     * Copy the OSGi bundle file to the deployable directory.
     * 
     * @param deployableDir the container's deployable directory
     * @param bundle the OSGi bundle deployable
     */
    protected void deployBundle(String deployableDir, Bundle bundle)
    {
        getFileHandler().copyFile(bundle.getFile(),
            getFileHandler().append(deployableDir, getFileHandler().getName(bundle.getFile())));
    }
}
