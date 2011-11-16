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
package org.codehaus.cargo.tools.jboss;

import java.io.File;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.jboss.internal.IJBossProfileManagerDeployer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;

/**
 * JBoss deployer implementation.
 * 
 * @version $Id$
 */
public class JBossDeployer implements IJBossProfileManagerDeployer
{
    
    /**
     * Container configuration.
     */
    private Configuration configuration;

    /**
     * @param configuration Configuration of the container.
     */
    public JBossDeployer(Configuration configuration)
    {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     * @see IJBossProfileManagerDeployer#deploy(File, String)
     */
    public void deploy(File deploymentFile, String deploymentName) throws Exception
    {
        DeploymentManager deployMgr = getDeploymentManager();

        deployMgr.loadProfile(getProfile(), true);
        DeploymentProgress distribute = deployMgr.distribute(deploymentName,
            ManagedDeployment.DeploymentPhase.APPLICATION, deploymentFile.toURI().toURL(), true);
        distribute.run();
        checkFailed(distribute);

        String[] repositoryNames = distribute.getDeploymentID().getRepositoryNames();
        DeploymentProgress start = deployMgr.start(ManagedDeployment.DeploymentPhase.APPLICATION,
            repositoryNames);
        start.run();
        checkFailed(start);
    }

    /**
     * {@inheritDoc}
     * @see IJBossProfileManagerDeployer#undeploy(String)
     */
    public void undeploy(final String deploymentName) throws Exception
    {
        DeploymentManager deployMgr = getDeploymentManager();

        String[] deploymentNameArray = new String[1];
        deploymentNameArray[0] = deploymentName;
        deployMgr.loadProfile(getProfile(), true);
        String[] repositoryNames = deployMgr.getRepositoryNames(deploymentNameArray,
            ManagedDeployment.DeploymentPhase.APPLICATION);
        DeploymentProgress stop = deployMgr.stop(ManagedDeployment.DeploymentPhase.APPLICATION,
            repositoryNames);
        stop.run();
        checkFailed(stop);
    }

    /**
     * @return JBoss profile for the {@link Configuration}.
     */
    private ProfileKey getProfile()
    {
        String server = this.configuration.getPropertyValue(JBossPropertySet.CONFIGURATION);
        if (server == null || server.trim().length() == 0)
        {
            server = ProfileKey.DEFAULT;
        }

        String profile = this.configuration.getPropertyValue(JBossPropertySet.PROFILE);
        Boolean isClustered = Boolean.valueOf(this.configuration.getPropertyValue(
            JBossPropertySet.CLUSTERED));
        String name = isClustered ? "farm" : profile;
        if (name == null || name.trim().length() == 0)
        {
            name = ProfileKey.DEFAULT;
        }

        return new ProfileKey(ProfileKey.DEFAULT, server, name);
    }

    /**
     * @param progress DP to check for failure.
     * @throws Exception If progress has failed.
     */
    private void checkFailed(DeploymentProgress progress) throws Exception
    {
        final int timeout = 30;
        DeploymentStatus status = progress.getDeploymentStatus();
        for (int i = 0; i < 30; i++)
        {
            Thread.sleep(1000);
            if (status.isCompleted() || status.isFailed())
            {
                break;
            }
            if (i == timeout - 1)
            {
                throw new Exception("Operation timed out");
            }
        }
        if (status.isFailed())
        {
            Exception cause = status.getFailure();
            throw new Exception("Remote action failed: " + status.getMessage()
                + " (" + cause.getMessage() + ")", cause);
        }
    }

    /**
     * @return The JBoss deployment manager.
     * @throws Exception If anything fails.
     */
    private DeploymentManager getDeploymentManager() throws Exception
    {
        StringBuilder providerURL = new StringBuilder();
        providerURL.append("jnp://");
        providerURL.append(this.configuration.getPropertyValue(GeneralPropertySet.HOSTNAME));
        providerURL.append(':');
        providerURL.append(this.configuration.getPropertyValue(GeneralPropertySet.RMI_PORT));

        Properties properties = new Properties();
        properties.setProperty(
            Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        properties.setProperty(Context.PROVIDER_URL, providerURL.toString());
        properties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        String username = this.configuration.getPropertyValue(RemotePropertySet.USERNAME);
        String password = this.configuration.getPropertyValue(RemotePropertySet.PASSWORD);
        if (username != null && password != null)
        {
            properties.setProperty(Context.SECURITY_PRINCIPAL, username);
            properties.setProperty(Context.SECURITY_CREDENTIALS, password);
        }

        Context ctx = new InitialContext(properties);

        Object profileService = ctx.lookupLink("ProfileService");
        ProfileService ps = (ProfileService) ctx.lookup("ProfileService");

        return ps.getDeploymentManager();
    }

}
