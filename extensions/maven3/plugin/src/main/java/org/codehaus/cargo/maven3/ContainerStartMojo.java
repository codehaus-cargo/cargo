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
package org.codehaus.cargo.maven3;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.LocalConfiguration;

/**
 * Start a container using Cargo.
 */
@Mojo(name = "start", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class ContainerStartMojo extends AbstractCargoMojo
{
    /**
     * Local container.
     */
    protected LocalContainer localContainer;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        Container container = createContainer();

        if (!container.getType().isLocal())
        {
            throw new MojoExecutionException("Only local containers can be started");
        }

        this.localContainer = (LocalContainer) container;
        addAutoDeployDeployable(this.localContainer);
        try
        {
            executeLocalContainerAction();
            waitDeployableMonitor(localContainer, true);
        }
        catch (Throwable t1)
        {
            getLog().error(
                "Starting container [" + this.localContainer + "] failed, now stopping container");
            try
            {
                this.localContainer.stop();
            }
            catch (Throwable t2)
            {
                getLog().debug("Stopping container [" + this.localContainer + "] failed", t2);
            }

            throw new MojoExecutionException(
                "Cannot start container [" + this.localContainer + "]", t1);
        }
    }

    /**
     * Executes the local container action.
     */
    protected void executeLocalContainerAction()
    {
        this.localContainer.start();
    }

    /**
     * If the project's packaging is war, ear or ejb and there is no deployer specified using the
     * <code>&lt;deployables&gt;</code> element, then add the generated artifact to the list of
     * deployables to deploy statically.<br>
     * <br>
     * Note that the reason we check that a deployer element has not been specified is because if it
     * has then the auto deployable will be deployed by the specified deployer.
     * 
     * @param container the local container to which to add the project's artifact
     * @throws MojoExecutionException if an error occurs
     */
    protected void addAutoDeployDeployable(LocalContainer container)
        throws MojoExecutionException
    {
        if (getDeployerElement() == null && getCargoProject().getPackaging() != null
            && getCargoProject().isJ2EEPackaging())
        {
            // Has no deployable been specified as part of the <deployables> config
            // element?
            if (getDeployablesElement() == null || getDeployablesElement().length == 0)
            {
                LocalConfiguration configuration = container.getConfiguration();
                configuration.addDeployable(createAutoDeployDeployable(container));
            }
        }
    }
}
