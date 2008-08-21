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
import org.codehaus.cargo.container.jonas.internal.JonasAdmin;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;
import org.codehaus.cargo.util.CargoException;
import org.codehaus.cargo.util.FileHandler;

/**
 * Static deployer that deploys WAR, EAR, EJB and RAR to JOnAS.
 * 
 * @version $Id: Jonas4xInstalledLocalDeployer.java 14641 2008-07-25 11:46:29Z alitokmen $
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
     * @param fileHandler the file handler to use, can be null to use the defaut file handler imple
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
                    + " trough JOnAS admin");
            }
        }
    }

    /**
     * Deploy a {@link Deployable} to the running container.
     * 
     * @param targetDir the target Directory directory where the container is expecting deployables
     *            to be dropped for deployments
     * @param deployable the deployable object(the deployable file archive )
     * @param fileName the archive file name to deploy
     * @param copying Copying Deployable
     * @throws CargoException if the deplyable can not be deployted
     */
    private void deploy(String targetDir, Deployable deployable, String fileName,
        CopyingDeployable copying) throws CargoException
    {
        boolean isRunning = false;
        String targetDiroctory = targetDir;

        isRunning = admin.isServerRunning();
        if (!isRunning)
        {
            targetDiroctory += "/autoload";
        }
        copying.copyDeployable(targetDiroctory, deployable);
        if (isRunning)
        {
            // hot deployment trough JOnAS admin
            boolean deployed = admin.deploy(fileName);
            if (!deployed)
            {
                throw new CargoException("Unable to deploy file " + fileName
                    + " trough JOnAS admin");
            }
        }

    }

    /**
     * Specifies the directory {@link org.codehaus.cargo.container.deployable.Deployable}s should
     * be copied to. For Tomcat this is the <code>webapps</code> directory.
     * 
     * @return Deployable the directory to deploy to
     */
    public String getDeployableDir()
    {
        // not the real exact deployment dir since under JOnAS they depends on the
        // deployable type and this information is not provided as method input parameter,
        // returned string is used as a base for overriden deployXXX methods
        return getContainer().getConfiguration().getHome();
    }

    /**
     * 
     * this Interface allows copying the deployble archive file to the JOnAS directory. where will
     * be deployed
     * 
     */
    private interface CopyingDeployable
    {
        /**
         * copy the Deployable archive file to the deployable directory.
         * 
         * @param deployableDir the deployable directory
         * @param deployable deployable to deploy
         */
        void copyDeployable(String deployableDir, Deployable deployable);
    }

    /**
     * 
     * Generic class to allow copying the deployble archive file. to the JOnAS directory where will
     * be deployed
     * 
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
