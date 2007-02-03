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
package org.codehaus.cargo.container.tomcat;

import java.io.IOException;

import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.tomcat.internal.AbstractTomcatRemoteDeployer;
import org.codehaus.cargo.container.tomcat.internal.TomcatManagerException;

/**
 * A special Tomcat4x manager-based deployer to perform deployment to a remote container.
 * 
 * @version $Id$
 */
public class Tomcat4xRemoteDeployer extends AbstractTomcatRemoteDeployer
{
    /**
     * {@inheritDoc}
     * @see AbstractTomcatRemoteDeployer#AbstractTomcatRemoteDeployer(org.codehaus.cargo.container.RemoteContainer)
     */
    public Tomcat4xRemoteDeployer(RemoteContainer container)
    {
        super(container); 
    }

    /**
     * {@inheritDoc}
     *
     * <p>This is a special implementation of undeploy command for Tomcat 4.x due the
     * http://issues.apache.org/bugzilla/show_bug.cgi?id=28851 issue. The issue has
     * been fixed in Tomcat 5.x and performUndeploy implementation for Tomcat5x differs from
     * Tomcat4.x.</p>
     *
     * @see org.codehaus.cargo.container.tomcat.internal.AbstractTomcatManagerDeployer#performUndeploy(org.codehaus.cargo.container.deployable.Deployable)
     */
    protected void performUndeploy(Deployable deployable) throws TomcatManagerException, IOException
    {
        stop(deployable);
        getTomcatManager().remove(getPath(deployable));
    }
}
