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
package org.codehaus.cargo.container.tomcat.internal;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractRemoteDeployer;
import org.codehaus.cargo.container.spi.deployer.DeployerWatchdog;
import org.codehaus.cargo.container.tomcat.TomcatPropertySet;

/**
 * Common code to perform both local or remote deployments using a Tomcat manager-based deployer.
 * 
 * @version $Id$
 */
public abstract class AbstractTomcatManagerDeployer extends AbstractRemoteDeployer
{
    /**
     * The name of the user agent when communicating with Tomcat manager.
     */
    private static final String NAME = "Codehaus Cargo";

    /**
     * Context where the Tomcat manager lives.
     */
    protected String managerContext = "/manager";

    /**
     * The Tomcat manager wrapper.
     */
    private TomcatManager manager;

    /**
     * Default initialization.
     * 
     * @param container the container to which to deploy to
     */
    public AbstractTomcatManagerDeployer(Container container)
    {
        super(container);
    }

    /**
     * @return the configuration to use for deployment
     */
    protected abstract Configuration getConfiguration();

    /**
     * @return the tomcat manager instance
     */
    protected TomcatManager getTomcatManager()
    {
        if (this.manager == null)
        {
            this.manager = createManager(getConfiguration());
        }

        return this.manager;
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#deploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void deploy(Deployable deployable)
    {
        String file = deployable.getFile();
        getLogger().info("Deploying [" + file + "]", this.getClass().getName());

        try
        {
            getTomcatManager().deploy(getPath(deployable), new FileInputStream(file));
        }
        catch (TomcatManagerException exception)
        {
            throw new ContainerException("Failed to deploy [" + file + "]", exception);
        }
        catch (IOException exception)
        {
            throw new ContainerException("Failed to deploy [" + file + "]", exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#undeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void undeploy(Deployable deployable)
    {
        String file = deployable.getFile();
        getLogger().info("Undeploying [" + file + "]", this.getClass().getName());

        try
        {
            TomcatDeployableStatus status = getTomcatManager().getStatus(getPath(deployable));
            if (!status.equals(TomcatDeployableStatus.NOT_FOUND))
            {
                performUndeploy(deployable);
            }
            else
            {
                getLogger().debug("Deployable [" + ((WAR) deployable).getContext()
                    + "] already undeployed", this.getClass().getName());
            }
        }
        catch (TomcatManagerException exception)
        {
            throw new ContainerException("Failed to undeploy [" + file + "]", exception);
        }
        catch (IOException exception)
        {
            throw new ContainerException("Failed to undeploy [" + file + "]", exception);
        }
    }

    /**
     * Performs undeployment of deployable.
     * 
     * <p>
     * Note: This is done differently by the different versions of Tomcat which is why we're using
     * an Abstract method here.
     * </p>
     * 
     * @param deployable the {@link Deployable} to undeploy
     * @throws TomcatManagerException If TomcatManagerException error occured perfoming the command
     * @throws IOException If I/O error occured getting the path of deployable
     */
    protected abstract void performUndeploy(Deployable deployable)
        throws TomcatManagerException, IOException;

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void redeploy(Deployable deployable)
    {
        String file = deployable.getFile();

        try
        {
            TomcatDeployableStatus status = getTomcatManager().getStatus(getPath(deployable));
            if (!status.equals(TomcatDeployableStatus.NOT_FOUND))
            {
                getLogger().info("Redeploying [" + file + "]", this.getClass().getName());
                undeploy(deployable);
            }
            else
            {
                getLogger().info("[" + file + "] is not deployed. Doing a fresh deployment.",
                    this.getClass().getName());
            }
            deploy(deployable);
        }
        catch (TomcatManagerException exception)
        {
            throw new ContainerException("Failed to redeploy [" + file + "]", exception);
        }
        catch (IOException exception)
        {
            throw new ContainerException("Failed to redeploy [" + file + "]", exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#redeploy(org.codehaus.cargo.container.deployable.Deployable, org.codehaus.cargo.container.deployer.DeployableMonitor)
     */
    @Override
    public void redeploy(Deployable deployable, DeployableMonitor monitor)
    {
        this.redeploy(deployable);

        // Wait for the Deployable to be redeployed
        DeployerWatchdog watchdog = new DeployerWatchdog(monitor);
        watchdog.setLogger(getLogger());
        watchdog.watchForAvailability();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#start(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void start(Deployable deployable)
    {
        String file = deployable.getFile();
        getLogger().info("Starting [" + file + "]", this.getClass().getName());

        try
        {
            TomcatDeployableStatus status = getTomcatManager().getStatus(getPath(deployable));
            if (status.equals(TomcatDeployableStatus.STOPPED))
            {
                getTomcatManager().start(getPath(deployable));
            }
            else
            {
                getLogger().debug("Deployable [" + ((WAR) deployable).getContext()
                    + "] already started or doesn't exists", this.getClass().getName());
            }
        }
        catch (TomcatManagerException exception)
        {
            throw new ContainerException("Failed to start [" + file + "]", exception);
        }
        catch (IOException exception)
        {
            throw new ContainerException("Failed to start [" + file + "]", exception);
        }
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.deployer.Deployer#stop(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    public void stop(Deployable deployable)
    {
        String file = deployable.getFile();
        getLogger().info("Stopping [" + file + "]", this.getClass().getName());

        try
        {
            TomcatDeployableStatus status = getTomcatManager().getStatus(getPath(deployable));
            if (status.equals(TomcatDeployableStatus.RUNNING))
            {
                getTomcatManager().stop(getPath(deployable));
            }
            else
            {
                getLogger().debug("Deployable [" + ((WAR) deployable).getContext()
                    + "] already stopped or doesn't exists", this.getClass().getName());
            }
        }
        catch (TomcatManagerException exception)
        {
            throw new ContainerException("Failed to stop [" + file + "]", exception);
        }
        catch (IOException exception)
        {
            throw new ContainerException("Failed to stop [" + file + "]", exception);
        }
    }

    /**
     * @return the list of applications available in Tomcat and their statuses.
     */
    public String list()
    {
        getLogger().debug("Getting the list of applications and their statuses",
            this.getClass().getName());
        try
        {
            return getTomcatManager().list();
        }
        catch (TomcatManagerException exception)
        {
            throw new ContainerException("Failed to get the list of applications", exception);
        }
        catch (IOException exception)
        {
            throw new ContainerException("Failed to get the list of applications", exception);
        }
    }

    /**
     * Creates a Tomcat manager wrapper from the specified configuration.
     * 
     * @param configuration the configuration to construct the Tomcat manager wrapper from
     * @return the Tomcat manager wrapper
     */
    protected TomcatManager createManager(Configuration configuration)
    {
        TomcatManager manager;

        URL managerURL = getManagerURL(configuration);

        String username = configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = configuration.getPropertyValue(RemotePropertySet.PASSWORD);

        // The user has to be specify the usrname and password properties as there are no
        // defaults for Tomcat (By default the Manager app is not deployed and the user not
        // defined).
        if (username == null || password == null)
        {
            throw new ContainerException("The [" + RemotePropertySet.USERNAME + "] and ["
                + RemotePropertySet.PASSWORD
                + "] properties are mandatory and need to be defined " + "in your configuration.");
        }

        StringBuilder userAgent = new StringBuilder(NAME);
        String version = this.getClass().getPackage().getImplementationVersion();
        if (version != null && version.length() > 0)
        {
            userAgent.append('/');
            userAgent.append(version);
        }
        manager = new TomcatManager(managerURL, username, password);
        manager.setLogger(getLogger());
        manager.setUserAgent(userAgent.toString());

        return manager;
    }

    /**
     * @param configuration the configuration to construct the Tomcat manager URL from
     * @return the URL to use to connect to the Tomcat manager
     */
    private URL getManagerURL(Configuration configuration)
    {
        URL url;

        String managerURL = configuration.getPropertyValue(RemotePropertySet.URI);

        String oldManagerURL = configuration.getPropertyValue(TomcatPropertySet.MANAGER_URL);
        if (oldManagerURL != null)
        {
            getLogger().warn("The property " + TomcatPropertySet.MANAGER_URL
                + " has been deprecated, please use " + RemotePropertySet.URI + " instead.",
                this.getClass().getName());
            if (managerURL == null)
            {
                managerURL = oldManagerURL;
            }
        }

        // If not defined by the user use a default URL
        if (managerURL == null)
        {
            managerURL = configuration.getPropertyValue(GeneralPropertySet.PROTOCOL) + "://"
                + configuration.getPropertyValue(GeneralPropertySet.HOSTNAME) + ":"
                + configuration.getPropertyValue(ServletPropertySet.PORT) + this.managerContext;

            getLogger().debug("Setting Tomcat Manager URL to " + managerURL,
                this.getClass().getName());
        }

        getLogger().debug("Tomcat Manager URL is " + managerURL, this.getClass().getName());

        try
        {
            url = new URL(managerURL);
        }
        catch (MalformedURLException e)
        {
            throw new ContainerException("Invalid Tomcat Manager URL [" + managerURL + "]", e);
        }

        return url;
    }

    /**
     * Gets the webapp path for the specified deployable.
     * 
     * @param deployable the deployable
     * @return the webapp path for the specified deployable
     */
    protected String getPath(Deployable deployable)
    {
        if (deployable.getType() != DeployableType.WAR)
        {
            throw new ContainerException("Only WAR archives are supported for deployment in "
                + "Tomcat. Got [" + deployable.getFile() + "]");
        }

        return "/" + ((WAR) deployable).getContext();
    }
}
