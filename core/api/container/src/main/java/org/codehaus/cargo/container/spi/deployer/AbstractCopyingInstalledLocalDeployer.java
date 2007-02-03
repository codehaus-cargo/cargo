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
package org.codehaus.cargo.container.spi.deployer;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.EJB;
import org.codehaus.cargo.container.deployable.WAR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Local deployer that deploys WARs, EJBs and EARs to a <code>deployable</code> directory of the 
 * given installed container. Note that this deployer supports expanded WARs by copying the
 * expanded WAR to the <code>deployable</code> directory. In other words it does not support
 * in-place expanded WARs (i.e. expanded WARs located in a different directory).
 *
 * @version $Id: $
 */
public abstract class AbstractCopyingInstalledLocalDeployer extends AbstractInstalledLocalDeployer
{
    /**
     * Empty iterator.
     */
    private static final Iterator EMPTY_ITERATOR = Collections.EMPTY_LIST.iterator();

    /**
     * @see #setShouldDeployExpandedWARs(boolean)
     */
    private boolean shouldDeployExpandedWARs;

    /**
     * Deployed Deployables.
     */
    private List deployedDeployables;

    /**
     * {@inheritDoc}
     * @see AbstractInstalledLocalDeployer#AbstractInstalledLocalDeployer(org.codehaus.cargo.container.InstalledLocalContainer)
     */
    public AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer container)
    {
        super(container);

        this.shouldDeployExpandedWARs = true;
        this.deployedDeployables = new ArrayList();
    }

    /**
     * Decide whether expanded WARs should be deployed. Some classes using this deployer may not
     * want to deploy expanded WARs as they may want to deploy them in-situ by modifying the
     * container's configuration file to point to the location of the expanded WAR. This saves
     * some copying time and make it easier for development round-trips.
     *
     * @param flag if true expanded WARs will be deployed
     */
    public void setShouldDeployExpandedWARs(boolean flag)
    {
        this.shouldDeployExpandedWARs = flag;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(Deployable)
     */
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
            else
            {
                throw new ContainerException("Only WAR, EJB and EAR are currently supported");
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
        final Set newDeployableContextSet = getWebContextsSet(newDeployable);
        final int size = this.deployedDeployables.size();
        for (int i = 0; i < size; i++)
        {
            final Deployable deployedDeployable = (Deployable) this.deployedDeployables.get(i);
            for (Iterator contextIterator = getWebContextsIterator(deployedDeployable);
                 contextIterator.hasNext();)
            {
                final Object context = contextIterator.next();
                if (newDeployableContextSet.contains(context))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Adapter method - to hide the fact that there is no unified API to retrieve
     * web contexts from a deployable.
     *
     * @param deployable deployable
     * @return an iterator over all web contexts this deployble uses
     */
    private static Iterator getWebContextsIterator(final Deployable deployable)
    {
        Iterator webContexts;
        if (deployable.getType() == DeployableType.EAR)
        {
            webContexts = ((EAR) deployable).getWebContexts();
        }
        else if (deployable.getType() == DeployableType.WAR)
        {
            webContexts = Arrays.asList(new String[]{((WAR) deployable).getContext()}).iterator();
        }
        else
        {
            webContexts = EMPTY_ITERATOR;
        }
        return webContexts;
    }

    /**
     * Create a set of all web contexts for a Deployable.
     *
     * @param deployable Deployable
     * @return a set of all web contexts contained in this deployable.
     */
    private static Set getWebContextsSet(final Deployable deployable)
    {
        final Set webContextSet = new HashSet();
        for (Iterator contextIterator = getWebContextsIterator(deployable);
             contextIterator.hasNext();)
        {
            webContextSet.add(contextIterator.next());
        }
        return webContextSet;
    }

    /**
     * Specifies the directory {@link org.codehaus.cargo.container.deployable.Deployable}s
     * should be copied to.
     *
     * @return Deployable directory
     */
    public abstract String getDeployableDir();

    /**
     * Copy the EAR file to the deployable directory.
     *
     * @param deployableDir the directory where the container is expecting deployables to be dropped
     *        for deployments
     * @param ear the EAR deployable
     */
    protected void deployEar(String deployableDir, EAR ear)
    {
        getFileHandler().copyFile(ear.getFile(),
            getFileHandler().append(deployableDir, getFileHandler().getName(ear.getFile())));
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
     * Copy the WAR file to the deployable directory, renaming it if the user has specified a
     * custom context for this WAR.
     *
     * @param deployableDir the directory where the container is expecting deployables to be dropped
     *        for deployments
     * @param war the WAR war
     */
    protected void deployWar(String deployableDir, WAR war)
    {
        getFileHandler().copyFile(
            war.getFile(), getFileHandler().append(deployableDir, war.getContext() + ".war"));
    }

    /**
     * Copy the full expanded WAR directory to the deployable directory, renaming it if the user
     * has specified a custom context for this expanded WAR.
     *
     * @param deployableDir the directory where the container is expecting deployables to be dropped
     *        for deployments
     * @param war the expanded WAR war
     */
    protected void deployExpandedWar(String deployableDir, WAR war)
    {
        getFileHandler().copyDirectory(
            war.getFile(), getFileHandler().append(deployableDir, war.getContext()));
    }
}
