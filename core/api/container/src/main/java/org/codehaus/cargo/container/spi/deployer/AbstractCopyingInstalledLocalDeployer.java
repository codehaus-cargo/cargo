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
package org.codehaus.cargo.container.spi.deployer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.RAR;
import org.codehaus.cargo.container.deployable.WAR;

/**
 * Local deployer that deploys deployables to a <code>deployable</code> directory of the given
 * installed container. Note that this deployer supports some expanded deployables by copying the
 * expanded deployable to the <code>deployable</code> directory. In other words it does not
 * support in-place expanded deployables (e.g. expanded WARs located in a different directory).
 * 
 * @version $Id$
 */
public abstract class AbstractCopyingInstalledLocalDeployer extends
    AbstractInstalledLocalDeployer
{
    /**
     * Contains those DeployableTypes that should not be deployed expanded. Default is to allow
     * expanded deployment and the exceptions to that rule are set here.
     */
    private Set<DeployableType> doNotDeployExpanded = new HashSet<DeployableType>();

    /**
     * Deployed Deployables.
     */
    private List<Deployable> deployedDeployables;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractInstalledLocalDeployer#AbstractInstalledLocalDeployer(org.codehaus.cargo.container.InstalledLocalContainer)
     */
    public AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);

        this.deployedDeployables = new ArrayList<Deployable>();
    }

    /**
     * Decide whether some expanded deployables of the specified type should be deployed or not.
     * Some classes using this deployer may not want to deploy some expanded deployables, as they
     * may want to deploy them in-situ by modifying the container's configuration file to point
     * to the location of the expanded deployable. This saves some copying time and make it easier
     * for development round-trips.
     * 
     * @param type the deployable type
     * @param flag whether expanded deployment of the specified deployment type should be allowed
     *            or not
     */
    public void setShouldDeployExpanded(DeployableType type, boolean flag)
    {
        if (flag)
        {
            this.doNotDeployExpanded.remove(type);
        }
        else
        {
            this.doNotDeployExpanded.add(type);
        }
    }

    /**
     * @param type the deployable type
     * @return whether expanded deployment of the specified deployment type should be done
     */
    protected boolean shouldDeployExpanded(DeployableType type)
    {
        return !this.doNotDeployExpanded.contains(type);
    }

    /**
     * Decide whether expanded WARs should be deployed. Some classes using this deployer may not
     * want to deploy expanded WARs as they may want to deploy them in-situ by modifying the
     * container's configuration file to point to the location of the expanded WAR. This saves some
     * copying time and make it easier for development round-trips.
     * 
     * @param flag if true expanded WARs will be deployed
     * @deprecated Use {@link #setShouldDeployExpanded(DeployableType, boolean)} instead
     */
    @Deprecated
    public void setShouldDeployExpandedWARs(boolean flag)
    {
        setShouldDeployExpanded(DeployableType.WAR, flag);
    }

    /**
     * Decide whether expanded SARs should be deployed. Some classes using this deployer may not
     * want to deploy expanded SARs as they may want to deploy them in-situ by modifying the
     * container's configuration file to point to the location of the expanded SAR. This saves some
     * copying time and make it easier for development round-trips.
     * 
     * @param flag if true expanded SARs will be deployed
     * @deprecated Use {@link #setShouldDeployExpanded(DeployableType, boolean)} instead
     */
    @Deprecated
    public void setShouldDeployExpandedSARs(boolean flag)
    {
        setShouldDeployExpanded(DeployableType.SAR, flag);
    }

    /**
     * Decide whether expanded RARs should be deployed. Some classes using this deployer may not
     * want to deploy expanded RARs as they may want to deploy them in-situ by modifying the
     * container's configuration file to point to the location of the expanded RAR. This saves some
     * copying time and make it easier for development round-trips.
     * 
     * @param flag if true expanded RARs will be deployed
     * @deprecated Use {@link #setShouldDeployExpanded(DeployableType, boolean)} instead
     */
    @Deprecated
    public void setShouldDeployExpandedRARs(boolean flag)
    {
        setShouldDeployExpanded(DeployableType.RAR, flag);
    }

    /**
     * {@inheritDoc}
     * 
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
        getLogger().info(
            "Deploying [" + deployable.getFile() + "] to [" + deployableDir + "]...",
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
            if (deployable.isExpanded())
            {
                if (!shouldDeployExpanded(deployable.getType()))
                {
                    throw new ContainerException("Container " + getContainer().getName()
                        + " cannot deploy expanded " + deployable.getType() + " deployables");
                }

                if (!getFileHandler().isDirectory(deployable.getFile()))
                {
                    throw new ContainerException("The deployable's file " + deployable.getFile()
                        + " is not a directory, hence cannot be deployed as expanded");
                }
            }

            doDeploy(deployableDir, deployable);
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
     * Do the actual deployment. This can be overriden.
     * @param deployableDir Directory in which to deploy.
     * @param deployable Deployable to deploy.
     */
    protected void doDeploy(String deployableDir, Deployable deployable)
    {
        String deployableName = getDeployableName(deployable);
        if (deployable.isExpanded())
        {
            getFileHandler().copyDirectory(deployable.getFile(),
                getFileHandler().append(deployableDir, deployableName));
        }
        else
        {
            getFileHandler().copyFile(deployable.getFile(),
                getFileHandler().append(deployableDir, deployableName),
                true);
        }
    }

    /**
     * Gets the deployable name for the given <code>deployable</code>.
     * @param deployable Deployable to get the name for.
     * @return Deployable name.
     */
    protected String getDeployableName(Deployable deployable)
    {
        String deployableName;
        if (DeployableType.WAR.equals(deployable.getType()))
        {
            WAR war = (WAR) deployable;
            String context = war.getContext();
            if ("".equals(context) || "/".equals(context))
            {
                getLogger().info(
                    "The WAR file has its context set to / and will therefore be "
                        + "deployed as ROOT.war", this.getClass().getName());
                context = "ROOT";
            }
            if (war.isExpanded())
            {
                deployableName = context;
            }
            else
            {
                deployableName = context + ".war";
            }
        }
        else if (DeployableType.EAR.equals(deployable.getType()))
        {
            // CARGO-598: If the EAR has a name property, use that one instead of file name
            EAR ear = (EAR) deployable;
            String earName = ear.getName();
            if (earName == null || "".equals(earName))
            {
                earName = getFileHandler().getName(ear.getFile());
            }
            if (!earName.toLowerCase().endsWith(".ear"))
            {
                earName = earName + ".ear";
            }
            deployableName = earName;
        }
        else if (DeployableType.RAR.equals(deployable.getType()))
        {
            RAR rar = (RAR) deployable;
            String rarName = rar.getName();
            if (rarName == null || "".equals(rarName))
            {
                rarName = getFileHandler().getName(rar.getFile());
            }
            if (!rarName.toLowerCase().endsWith(".rar"))
            {
                rarName = rarName + ".rar";
            }
            deployableName = rarName;
        }
        else
        {
            deployableName = getFileHandler().getName(deployable.getFile());
        }
        return deployableName;
    }
}
