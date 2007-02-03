/* 
 * ========================================================================
 * 
 * Copyright 2006 Vincent Massol.
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

import java.io.IOException;

import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.deployable.Deployable;

/**
 * Common code to perform local deployments using a Tomcat manager-based deployer.
 * 
 * @version $Id$
 */
public abstract class AbstractTomcatManagerInstalledLocalDeployer extends
    AbstractTomcatManagerDeployer
{
    /**
     * The local container to deploy to.
     */
    protected LocalContainer container;

    /**
     * {@inheritDoc}
     * @see AbstractTomcatManagerDeployer#AbstractTomcatManagerDeployer(org.codehaus.cargo.container.Container)
     */
    public AbstractTomcatManagerInstalledLocalDeployer(LocalContainer container)
    {
        super(container);
        this.container = container; 
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.tomcat.internal.AbstractTomcatManagerDeployer#getConfiguration()
     */
    protected Configuration getConfiguration()
    {
        return this.container.getConfiguration();
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.container.tomcat.internal.AbstractTomcatManagerDeployer#performUndeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    protected void performUndeploy(Deployable deployable) throws TomcatManagerException, IOException
    {
        getTomcatManager().undeploy(getPath(deployable));
    }
}
