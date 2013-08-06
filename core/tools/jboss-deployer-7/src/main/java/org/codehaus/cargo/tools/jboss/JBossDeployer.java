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
import java.io.FileInputStream;
import java.io.IOException;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.DeploymentAction;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlan;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.as.controller.client.helpers.standalone.ServerUpdateActionResult;

import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.jboss.JBossPropertySet;
import org.codehaus.cargo.container.jboss.internal.IJBossProfileManagerDeployer;
import org.codehaus.cargo.container.jboss.internal.UsernamePasswordCallbackHandler;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.util.CargoException;

/**
 * JBoss deployer implementation.
 * 
 * @version $Id$
 */
public class JBossDeployer implements IJBossProfileManagerDeployer
{

    /**
     * Actions.
     */
    private enum Action
    {
        /**
         * Deploy.
         */
        DEPLOY,

        /**
         * Undeploy.
         */
        UNDEPLOY
    }
    
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
        executeAction(Action.DEPLOY, deploymentFile, deploymentName);
    }

    /**
     * {@inheritDoc}
     * @see IJBossProfileManagerDeployer#undeploy(String)
     */
    public void undeploy(final String deploymentName) throws Exception
    {
        executeAction(Action.UNDEPLOY, null, deploymentName);
    }

    /**
     * Execute deployment action.
     * @param type Action type.
     * @param deploymentFile Deployment file, can be <code>null</code>.
     * @param deploymentName Deployment name.
     * @throws Exception If anything fails.
     */
    private void executeAction(Action type, File deploymentFile, String deploymentName)
        throws Exception
    {
        String hostname = this.configuration.getPropertyValue(GeneralPropertySet.HOSTNAME);
        String portname;
        if (this.configuration.getCapability().supportsProperty(
            JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT))
        {
            portname =
                this.configuration.getPropertyValue(JBossPropertySet.JBOSS_MANAGEMENT_NATIVE_PORT);
        }
        else
        {
            portname =
                this.configuration.getPropertyValue(JBossPropertySet.JBOSS_MANAGEMENT_HTTP_PORT);
        }
        int portnumber = Integer.parseInt(portname);

        ModelControllerClient client = ModelControllerClient.Factory.create(hostname, portnumber,
            new UsernamePasswordCallbackHandler(this.configuration));
        try
        {
            ServerDeploymentManager manager = ServerDeploymentManager.Factory.create(client);
            DeploymentPlanBuilder builder = manager.newDeploymentPlan();
            DeploymentPlan plan;

            switch (type)
            {
                case DEPLOY:
                    plan = builder.add(deploymentName,
                        new FileInputStream(deploymentFile)).deploy(deploymentName).build();
                    break;
                case UNDEPLOY:
                    plan = builder.undeploy(deploymentName).remove(deploymentName).build();
                    break;
                default:
                    throw new IllegalStateException("Invalid action: " + type);
            }

            if (plan.getDeploymentActions().size() > 0)
            {
                ServerDeploymentPlanResult planResult = manager.execute(plan).get();
                // Check the results
                for (DeploymentAction action : plan.getDeploymentActions())
                {
                    ServerDeploymentActionResult actionResult =
                        planResult.getDeploymentActionResult(action.getId());
                    ServerUpdateActionResult.Result result = actionResult.getResult();

                    switch (result)
                    {
                        case FAILED:
                        case NOT_EXECUTED:
                        case ROLLED_BACK:
                            throw new CargoException("Deployment action " + action.getType()
                                + " failed", actionResult.getDeploymentException());
                        case CONFIGURATION_MODIFIED_REQUIRES_RESTART:
                            // Should show warning
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        finally
        {
            try
            {
                client.close();
            }
            catch (IOException e)
            {
                this.configuration.getLogger().warn(
                    "Failed closing the JBoss deployment client: " + e.toString(),
                        this.getClass().getName());
            }
        }
    }

}
