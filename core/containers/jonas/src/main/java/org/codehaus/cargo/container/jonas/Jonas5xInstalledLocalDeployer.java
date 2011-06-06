/*
 * ========================================================================
 *
 * Copyright 2007-2008 OW2. Code from this file
 * was originally imported from the OW2 JOnAS project.
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
package org.codehaus.cargo.container.jonas;

import java.io.IOException;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.RuntimeConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.jonas.internal.MBeanServerConnectionFactory;
import org.codehaus.cargo.container.spi.deployer.AbstractCopyingInstalledLocalDeployer;
import org.codehaus.cargo.util.FileHandler;

/**
 * Static deployer that deploys WAR, EAR, EJB, RAR, File and Bundle to JOnAS.
 * 
 * @version $Id$
 */
public class Jonas5xInstalledLocalDeployer extends AbstractCopyingInstalledLocalDeployer
{
    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#AbstractCopyingInstalledLocalDeployer(InstalledLocalContainer)
     */
    public Jonas5xInstalledLocalDeployer(InstalledLocalContainer container)
    {
        this(container, null);
    }

    /**
     * Creation of a local deployer with a given file handler.
     * 
     * @param container the container to be used
     * @param fileHandler the file handler to use, can be null to use the default file handler
     * implementation
     */
    public Jonas5xInstalledLocalDeployer(InstalledLocalContainer container, FileHandler fileHandler)
    {
        super(container);
        if (fileHandler != null)
        {
            super.setFileHandler(fileHandler);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractCopyingInstalledLocalDeployer#deploy(Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        Deployer hotDeployer;
        try
        {
            hotDeployer = getRemoteDeployer();
        }
        catch (IOException e)
        {
            // Cannot get remote deployer, therefore cold deploy
            super.deploy(deployable);
            return;
        }

        hotDeployer.deploy(deployable);
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractCopyingInstalledLocalDeployer#undeploy(Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        Deployer hotDeployer;
        try
        {
            hotDeployer = getRemoteDeployer();
        }
        catch (IOException e)
        {
            // Cannot get remote deployer, therefore cold undeploy
            String deployableFilename = getDeployableDir() + "/" + getDeployableName(deployable);
            getFileHandler().delete(deployableFilename);
            return;
        }

        hotDeployer.undeploy(deployable);
    }

    /**
     * {@inheritDoc}
     *
     * @see AbstractCopyingInstalledLocalDeployer#redeploy(Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        this.undeploy(deployable);
        this.deploy(deployable);
    }

    /**
     * {@inheritDoc}
     * 
     * @see AbstractCopyingInstalledLocalDeployer#getDeployableDir()
     */
    @Override
    public String getDeployableDir()
    {
        return getContainer().getConfiguration().getHome() + "/deploy";
    }

    /**
     * @return A remote {@link Deployer} implementation that resembles the current one.
     * @throws IOException If connection has failed because the remote server is not accessible.
     * Note that {@link SecurityException}s are catched and transformed into
     * {@link ContainerException}s.
     */
    protected Deployer getRemoteDeployer() throws IOException
    {
        RuntimeConfiguration configuration = new JonasRuntimeConfiguration();
        configuration.setLogger(this.getContainer().getConfiguration().getLogger());
        configuration.getProperties().putAll(
            this.getContainer().getConfiguration().getProperties());
        RemoteContainer container = new Jonas5xRemoteContainer(configuration);
        Jonas5xJsr160RemoteDeployer remoteDeployer = new Jonas5xJsr160RemoteDeployer(container);

        MBeanServerConnectionFactory connectionFactory =
            remoteDeployer.getMBeanServerConnectionFactory();
        try
        {
            connectionFactory.getServerConnection(configuration);
            connectionFactory.destroy();
        }
        catch (SecurityException e)
        {
            throw new ContainerException(
                "Cannot connect to the target JOnAS server due to a security issue. "
                + "Please check the username and passeword.", e);
        }
        catch (IOException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ContainerException("Cannot instantiate JSR160 JMX connector", e);
        }
        finally
        {
            connectionFactory = null;
            System.gc();
        }

        return remoteDeployer;
    }
}
