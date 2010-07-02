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
package org.codehaus.cargo.maven2;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.spi.util.ContainerUtils;

/**
 * Start a container using Cargo. 
 * 
 * @version $Id$
 * @goal start
 * @requiresDependencyResolution compile
 */
public class ContainerStartMojo extends AbstractCargoMojo
{
    /**
     * Decides whether to wait after the container is started or to return the execution
     * flow to the user.
     * 
     * @parameter default-value = "true"
     * @required
     */
    private boolean wait;

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#doExecute()
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        Container container = createContainer();

        if (!container.getType().isLocal())
        {
            throw new MojoExecutionException("Only local containers can be started");
        }

        LocalContainer localContainer = (LocalContainer) container;
        addAutoDeployDeployable(localContainer);
        localContainer.start();

        if (this.wait)
        {
            getLog().info("Press Ctrl-C to stop the container...");
            ContainerUtils.waitTillContainerIsStopped(localContainer);
        }
    }

    /**
     * If the project's packaging is war, ear or ejb and there is no deployer specified and
     * the user has not defined the auto-deployable inside the <code>&lt;deployables&gt;</code>
     * element, then add the generated artifact to the list of deployables to deploy statically.
     * 
     * Note that the reason we check that a deployer element has not been specified is because
     * if it has then the auto deployable will be deployed by the specified deployer.
     * 
     * @param container the local container to which to add the project's artifact
     * @throws MojoExecutionException if an error occurs
     */
    protected void addAutoDeployDeployable(LocalContainer container)
        throws MojoExecutionException
    {
        if ((getDeployerElement() == null) && (getCargoProject().getPackaging() != null)
            && getCargoProject().isJ2EEPackaging())
        {
            // Has the auto-deployable already been specified as part of the <deployables> config
            // element? 
            if ((getConfigurationElement() == null)
                || (getConfigurationElement().getDeployables() == null)
                || !containsAutoDeployable(getConfigurationElement().getDeployables()))
            {
                LocalConfiguration configuration = container.getConfiguration();
                configuration.addDeployable(createAutoDeployDeployable(container));
            }
        }
    }
}
