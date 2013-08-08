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
package org.codehaus.cargo.container.tomcat;

import java.io.IOException;
import org.codehaus.cargo.container.ContainerException;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.tomcat.internal.AbstractTomcatRemoteDeployer;
import org.codehaus.cargo.container.tomcat.internal.TomcatManagerException;

/**
 * A special Tomcat 7.x manager-based deployer to perform deployment to a remote container.
 * 
 * @version $Id$
 */
public class Tomcat7xRemoteDeployer extends AbstractTomcatRemoteDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractTomcatRemoteDeployer#AbstractTomcatRemoteDeployer(org.codehaus.cargo.container.RemoteContainer)
     */
    public Tomcat7xRemoteDeployer(RemoteContainer container)
    {
        super(container);
        this.managerContext += "/text";
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This is a special implementation of undeploy command for Tomcat 7.x
     * </p>
     * 
     * @see org.codehaus.cargo.container.tomcat.internal.AbstractTomcatManagerDeployer#performUndeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    protected void performUndeploy(Deployable deployable) throws TomcatManagerException,
            IOException
    {
        getTomcatManager().undeploy(getPath(deployable), getVersion(deployable));
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This is a special implementation of undeploy command for Tomcat 7.x with version support.
     * </p>
     * 
     * @see org.codehaus.cargo.container.tomcat.internal.AbstractTomcatManagerDeployer#getPath(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    protected String getPath(Deployable deployable)
    {
        if (deployable.getType() != DeployableType.WAR)
        {
            throw new ContainerException("Only WAR archives are supported for deployment in "
                + "Tomcat. Got [" + deployable.getFile() + "]");
        }

        String path = "/" + ((WAR) deployable).getContext();
        int doubleHash = path.indexOf("##");
        if (doubleHash > 0)
        {
            path = path.substring(0, doubleHash);
        }

        return path;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This is a special implementation of undeploy command for Tomcat 7.x with version support.
     * </p>
     * 
     * @see org.codehaus.cargo.container.tomcat.internal.AbstractTomcatManagerDeployer#getVersion(org.codehaus.cargo.container.deployable.Deployable)
     */
    @Override
    protected String getVersion(Deployable deployable)
    {
        if (deployable.getType() != DeployableType.WAR)
        {
            throw new ContainerException("Only WAR archives are supported for deployment in "
                + "Tomcat. Got [" + deployable.getFile() + "]");
        }

        String path = "/" + ((WAR) deployable).getContext();
        int doubleHash = path.indexOf("##");
        if (doubleHash > 0)
        {
            String version = path.substring(doubleHash + 2);
            return version;
        }
        else
        {
            return null;
        }
    }
}
