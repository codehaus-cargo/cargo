/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
import org.codehaus.cargo.container.tomcat.internal.TomcatManagerException;

/**
 * A special Tomcat 7.x manager-based deployer to perform deployment to a remote container.
 */
public class Tomcat7xRemoteDeployer extends Tomcat6xRemoteDeployer
{
    /**
     * {@inheritDoc}
     * @see Tomcat6xRemoteDeployer#Tomcat6xRemoteDeployer(org.codehaus.cargo.container.RemoteContainer)
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
     * This is a special implementation of undeploy command for Tomcat 7.x onwards, which supports
     * deployable versions.
     * </p>
     */
    @Override
    protected void performUndeploy(Deployable deployable) throws TomcatManagerException,
            IOException
    {
        boolean undeployAllVersions = Boolean.parseBoolean(
                getConfiguration().getPropertyValue(TomcatPropertySet.UNDEPLOY_ALL_VERSIONS));
        if (undeployAllVersions)
        {
            getTomcatManager().undeploy(getPath(deployable));
        }
        else
        {
            getTomcatManager().undeploy(getPath(deployable), getVersion(deployable));
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This is a special implementation of getPath command for Tomcat 7.x with version support.
     * </p>
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
     * This is a special implementation of getVersion command for Tomcat 7.x with version support.
     * </p>
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
