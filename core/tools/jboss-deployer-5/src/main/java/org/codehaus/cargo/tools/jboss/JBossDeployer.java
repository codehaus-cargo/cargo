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
package org.codehaus.cargo.tools.jboss;

import java.io.File;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.codehaus.cargo.container.jboss.internal.IJBossProfileManagerDeployer;
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
     * RMI provider URL, for example <code>jnp://localhost:1099</code>.
     */
    private final String providerURL;

    /**
     * @param providerURL Provider URL to use.
     */
    public JBossDeployer(final String providerURL)
    {
        this.providerURL = providerURL;
    }

    /**
     * {@inheritDoc}
     * @see IJBossProfileManagerDeployer#deploy(File, String)
     */
    public void deploy(File deploymentFile, String deploymentName) throws Exception
    {
        DeploymentManager deployMgr = getDeploymentManager();

        deployMgr.loadProfile(new ProfileKey(ProfileKey.DEFAULT), true);
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
        deployMgr.loadProfile(new ProfileKey(ProfileKey.DEFAULT), true);
        String[] repositoryNames = deployMgr.getRepositoryNames(deploymentNameArray,
            ManagedDeployment.DeploymentPhase.APPLICATION);
        DeploymentProgress stop = deployMgr.stop(ManagedDeployment.DeploymentPhase.APPLICATION,
            repositoryNames);
        stop.run();
        checkFailed(stop);
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
        Properties props = new Properties();
        props.setProperty(
            Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.PROVIDER_URL, this.providerURL);
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        Context ctx = new InitialContext(props);

        Object profileService = ctx.lookupLink("ProfileService");
        ProfileService ps = (ProfileService) ctx.lookup("ProfileService");

        return ps.getDeploymentManager();
    }

}
