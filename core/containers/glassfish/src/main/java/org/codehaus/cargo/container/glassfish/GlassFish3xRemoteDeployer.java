/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2025 Ali Tokmen.
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
package org.codehaus.cargo.container.glassfish;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;

import org.codehaus.cargo.container.ContainerException;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.property.GeneralPropertySet;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.spi.deployer.AbstractJsr88Deployer;

/**
 * GlassFish 3.x remote deployer, which uses the JSR-88 to deploy and undeploy applications.
 */
public class GlassFish3xRemoteDeployer extends AbstractJsr88Deployer
{

    /**
     * Constructor.
     * 
     * @param container the remote container
     */
    public GlassFish3xRemoteDeployer(RemoteContainer container)
    {
        super(container);
    }

    /**
     * @return The class name of the JSR-88 deployment factory.
     */
    @Override
    protected String getDeploymentFactoryClassName()
    {
        return "org.glassfish.deployapi.SunDeploymentFactory";
    }

    /**
     * @param dfm JSR-88 deployment factory manager with the target deployer factory registered.
     * @return The JSR-88 deployment manager for the target server.
     * @throws DeploymentManagerCreationException If deployment manager creation fails.
     */
    @Override
    protected DeploymentManager getDeploymentManager(DeploymentFactoryManager dfm)
        throws DeploymentManagerCreationException
    {
        String hostname = this.getRuntimeConfiguration().getPropertyValue(
            GeneralPropertySet.HOSTNAME);
        String port = this.getRuntimeConfiguration().getPropertyValue(
            GlassFishPropertySet.ADMIN_PORT);
        String username = this.getRuntimeConfiguration().getPropertyValue(
            RemotePropertySet.USERNAME);
        String password = this.getRuntimeConfiguration().getPropertyValue(
            RemotePropertySet.PASSWORD);

        return dfm.getDeploymentManager("deployer:Sun:AppServer::" + hostname + ":" + port,
            username, password);
    }

    /**
     * @param targets All available targets in the container instance.
     * @return Targets set up in the runtime configuration.
     */
    @Override
    protected Target[] filterTargets(Target[] targets)
    {
        String prop = this.getRuntimeConfiguration().getPropertyValue(
            GlassFishPropertySet.TARGET);

        if (prop != null && !prop.isEmpty())
        {
            Set<String> cfgTargets = new HashSet<String>(Arrays.asList(prop.split(",")));
            List<Target> result = new ArrayList<Target>();

            for (Target target : targets)
            {
                if (cfgTargets.contains(target.getName()))
                {
                    result.add(target);
                }
            }

            if (result.size() != cfgTargets.size())
            {
                String allTargets = "";

                for (Target t : targets)
                {
                    allTargets += t.getName() + " ";
                }

                throw new ContainerException("No such target(s), available targets are: "
                    + allTargets);
            }

            return result.toArray(new Target[result.size()]);
        }

        return super.filterTargets(targets);
    }

    /**
     * @param targetModuleIDs List with all available target module IDs for the target module.
     * @return Target module IDs set up in the runtime configuration.
     */
    @Override
    protected TargetModuleID[] filterTargetModuleIDs(List<TargetModuleID> targetModuleIDs)
    {
        String prop = this.getRuntimeConfiguration().getPropertyValue(
            GlassFishPropertySet.TARGET);

        if (prop != null && !prop.isEmpty())
        {
            Set<String> cfgTargets = new HashSet<String>(Arrays.asList(prop.split(",")));
            List<TargetModuleID> result = new ArrayList<TargetModuleID>();

            for (TargetModuleID targetModule : targetModuleIDs)
            {
                for (String target : cfgTargets)
                {
                    if (targetModule.getTarget().getName().equals(target))
                    {
                        result.add(targetModule);
                    }
                }
            }

            return result.toArray(new TargetModuleID[result.size()]);
        }

        return super.filterTargetModuleIDs(targetModuleIDs);
    }
}
