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
package org.codehaus.cargo.maven2;

import java.net.URL;

/**
 * Redeploy a deployable (i.e. undeploy and deploy it again) in a container.
 * 
 * @goal deployer-redeploy
 * @requiresDependencyResolution test
 */
public class DeployerRedeployMojo extends AbstractDeployerMojo
{
    @Override
    protected void performDeployerActionOnSingleDeployable(
        org.codehaus.cargo.container.deployer.Deployer deployer,
        org.codehaus.cargo.container.deployable.Deployable deployable,
        URL pingURL, Long pingTimeout)
    {
        getLog().debug("Redeploying [" + deployable.getFile() + "]"
            + (pingURL == null ? " ..." : " using ping URL [" + pingURL + "]"
                + (pingTimeout == null ? "" : " and ping timeout [" + pingTimeout + "]")));

        if (pingURL != null)
        {
            deployer.redeploy(deployable, createDeployableMonitor(pingURL, pingTimeout,
                deployable));
        }
        else
        {
            deployer.redeploy(deployable);
        }
    }
}
