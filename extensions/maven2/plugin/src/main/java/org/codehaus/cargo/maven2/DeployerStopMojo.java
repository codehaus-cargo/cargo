/*
 * ========================================================================
 *
 * Codehaus CARGO, copyright 2004-2011 Vincent Massol, 2012-2021 Ali Tokmen.
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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Stop a deployable which is already deployed in a container.
 */
@Mojo(
    name = "deployer-stop", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class DeployerStopMojo extends AbstractDeployerMojo
{
    @Override
    protected void performDeployerActionOnSingleDeployable(
        org.codehaus.cargo.container.deployer.Deployer deployer,
        org.codehaus.cargo.container.deployable.Deployable deployable,
        org.codehaus.cargo.container.deployer.DeployableMonitor monitor)
    {
        getLog().debug("Stopping [" + deployable.getFile() + "]"
            + (monitor == null ? " ..." : " with deployable Id [" + monitor.getDeployableName()
                + "] and timeout [" + monitor.getTimeout() + "]"));

        if (monitor != null)
        {
            deployer.stop(deployable, monitor);
        }
        else
        {
            deployer.stop(deployable);
        }
    }
}
