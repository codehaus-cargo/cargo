/* 
 * ========================================================================
 * 
 * Copyright 2007-2008 OW2.
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
package org.codehaus.cargo.container.jonas;

import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.EAR;
import org.codehaus.cargo.container.deployable.EJB;
import org.codehaus.cargo.container.deployable.RAR;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.jonas.internal.Jonas4xAdmin;
import org.codehaus.cargo.container.jonas.internal.JonasAdmin;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;

/**
 * Static deployer that deploys WAR, EAR, EJB and RAR to JOnAS.
 * 
 * @version $Id$
 */
public class Jonas4xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * JOnAS admin used for hot deployment.
     */
    private JonasAdmin admin;

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */
    public Jonas4xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        this(container, new Jonas4xAdmin((Jonas4xInstalledLocalContainer) container), null);
    }

    /**
     * Creation of a local depoyer with a given Jonas4xAdmin object and file handler.
     * 
     * @param container the container to be used
     * @param admin the JOnAS admin to use for deployment
     * @param fileHandler the file handler to use, can be null to use the default file handler
     *            implementation
     */
    public Jonas4xInstalledLocalDeployer(InstalledLocalContainer container, JonasAdmin admin,
        FileHandler fileHandler)
    {
        super(container);
        this.admin = admin;
        if (fileHandler != null)
        {
            super.setFileHandler(fileHandler);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployEar(String,
     *      org.codehaus.cargo.container.deployable.EAR)
     */
    protected void deployEar(String deployableDir, EAR ear) throws CargoException
    {
        deploy(deployableDir + "/apps", ear, getFileHandler().getName(ear.getFile()),
            new GenericCopyingDeployable());
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployEjb(String,
     *      org.codehaus.cargo.container.deployable.EJB)
     */
    protected void deployEjb(String deployableDir, EJB ejb) throws CargoException
    {
        deploy(deployableDir + "/ejbjars", ejb, getFileHandler().getName(ejb.getFile()),
            new GenericCopyingDeployable());
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployRar(String,
     *      org.codehaus.cargo.container.deployable.RAR)
     */
    protected void deployRar(String deployableDir, RAR rar) throws CargoException
    {
        deploy(deployableDir + "/rars", rar, getFileHandler().getName(rar.getFile()),
            new GenericCopyingDeployable());
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployExpandedWar(String,
     *      org.codehaus.cargo.container.deployable.WAR)
     */
    protected void deployExpandedWar(String deployableDir, WAR war) throws CargoException
    {
        if (admin.isServerRunning())
        {
            getLogger()
                .warn("Hot deployment of expanded war impossible", this.getClass().getName());
            return;
        }
        super.deployExpandedWar(deployableDir + "/webapps/autoload", war);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deployWar(String,
     *      org.codehaus.cargo.container.deployable.WAR)
     */
    protected void deployWar(String deployableDir, WAR war) throws CargoException
    {
        deploy(deployableDir + "/webapps", war, war.getContext() + ".war", new CopyingDeployable()
        {

            public void copyDeployable(String deployableDir, Deployable deployable)
            {
                getFileHandler().copyFile(
                    deployable.getFile(),
                    getFileHandler()
                        .append(deployableDir, ((WAR) deployable).getContext() + ".war"));
            }
        });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(Deployable)
     */
    public void redeploy(Deployable deployable) throws CargoException
    {
        undeploy(deployable);
        deploy(deployable);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(Deployable)
     */
    public void undeploy(Deployable deployable) throws CargoException
    {
        String fileName = getFileHandler().getName(deployable.getFile());
        if (deployable instanceof WAR)
        {
            fileName = ((WAR) deployable).getContext() + ".war";
        }
        boolean isRunning = false;

        isRunning = admin.isServerRunning();
        if (isRunning)
        {
            boolean undeployed = admin.unDeploy(fileName);
            if (!undeployed)
            {
                throw new CargoException("Unable to undeploy file " + fileName
                    + " through JOnAS admin");
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#deploy(String, Deployable, String, CopyingDeployable)
     */
    private void deploy(String targetDir, Deployable deployable, String fileName,
        CopyingDeployable copying) throws CargoException
    {
        boolean isRunning = false;
        String targetDirectory = targetDir;

        isRunning = admin.isServerRunning();
        if (!isRunning)
        {
            targetDirectory += "/autoload";
        }

        copying.copyDeployable(targetDirectory, deployable);
        if (isRunning)
        {
            // hot deployment through JOnAS admin
            boolean deployed = admin.deploy(fileName);
            if (!deployed)
            {
                throw new CargoException("Unable to deploy file " + fileName
                    + " through JOnAS admin");
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#getDeployableDir()
     */
    public String getDeployableDir()
    {
        // not the real exact deployment dir since under JOnAS they depends on the
        // deployable type and this information is not provided as method input parameter,
        // returned string is used as a base for overriden deployXXX methods
        return getContainer().getConfiguration().getHome();
    }

    /**
     * This Interface allows copying the deployable archive file to the JOnAS directory.
     */
    private interface CopyingDeployable
    {
        /**
         * Copy the Deployable archive file to the deployable directory.
         * 
         * @param deployableDir the deployable directory
         * @param deployable deployable to deploy
         */
        void copyDeployable(String deployableDir, Deployable deployable);
    }

    /**
     * Generic class to allow copying the deployable archive file.
     */
    private class GenericCopyingDeployable implements CopyingDeployable
    {
        /**
         * {@inheritDoc}
         * 
         * @see org.codehaus.cargo.container.jonas.Deployer.GenericCopyingDeployable#copyDeployable(String,
         *      Deployable)
         */
        public void copyDeployable(String deployableDir, Deployable deployable)
        {
            getFileHandler().copyFile(
                deployable.getFile(),
                getFileHandler().append(deployableDir,
                    getFileHandler().getName(deployable.getFile())));
        }
    }
}
