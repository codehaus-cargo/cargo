/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2024 Ali Tokmen.
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
package org.codehaus.cargo.container.tomee.internal;

import org.codehaus.cargo.container.ContainerException;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.tomcat.Tomcat7xRemoteDeployer;

/**
 * A special TomEE manager-based deployer to perform deployment to a remote container.
 */
public class AbstractTomeeRemoteDeployer extends Tomcat7xRemoteDeployer
{
    /**
     * {@inheritDoc}
     * @see Tomcat7xRemoteDeployer#Tomcat7xRemoteDeployer(org.codehaus.cargo.container.RemoteContainer)
     */
    public AbstractTomeeRemoteDeployer(RemoteContainer container)
    {
        super(container);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This is a special implementation of undeploy command for TomEE with version support.
     * </p>
     */
    @Override
    protected String getPath(Deployable deployable)
    {
        if (deployable.getType() != DeployableType.WAR)
        {
            throw new ContainerException("Only WAR archives are supported for remote deployment "
                + "in TomEE. Got [" + deployable.getFile() + "]");
        }

        return super.getPath(deployable);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This is a special implementation of undeploy command for TomEE with version support.
     * </p>
     */
    @Override
    protected String getVersion(Deployable deployable)
    {
        if (deployable.getType() != DeployableType.WAR)
        {
            throw new ContainerException("Only WAR archives are supported for remote deployment "
                + "in TomEE. Got [" + deployable.getFile() + "]");
        }

        return super.getVersion(deployable);
    }
}
