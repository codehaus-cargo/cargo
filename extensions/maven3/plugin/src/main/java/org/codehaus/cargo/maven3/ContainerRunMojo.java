/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2023 Ali Tokmen.
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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.spi.jvm.DefaultJvmLauncher;
import org.codehaus.cargo.container.spi.util.ContainerUtils;
import org.codehaus.cargo.maven3.configuration.Container;
import org.codehaus.cargo.maven3.configuration.ZipUrlInstaller;

/**
 * Start a container using Cargo and wait until user pressed CTRL + C to stop.
 */
@Mojo(name = "run", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = false)
public class ContainerRunMojo extends ContainerStartMojo
{
    /**
     * Sets the container id.
     */
    @Parameter(property = "cargo.maven.containerId")
    private String containerId;

    /**
     * Sets the container download URL.
     */
    @Parameter(property = "cargo.maven.containerUrl")
    private String containerUrl;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        org.codehaus.cargo.container.Container container = createContainer();

        if (!container.getType().isLocal())
        {
            throw new MojoExecutionException("Only local containers can be started");
        }

        // When Ctrl-C is pressed, stop the container
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                DefaultJvmLauncher.shutdownInProgress = true;
                try
                {
                    stopContainer();
                }
                catch (NumberFormatException ignored)
                {
                    try
                    {
                        stopContainer();
                    }
                    catch (Exception e)
                    {
                        ContainerRunMojo.this.getLog().warn("Failed stopping the container", e);
                    }
                }
                catch (Exception e)
                {
                    ContainerRunMojo.this.getLog().warn("Failed stopping the container", e);
                }
            }

            /**
             * Stop the container
             * @throws Exception if anything goes wrong
             */
            private void stopContainer() throws Exception
            {
                if (ContainerRunMojo.this.localContainer != null
                    && (org.codehaus.cargo.container.State.STARTED
                        == ContainerRunMojo.this.localContainer.getState()
                    ||
                        org.codehaus.cargo.container.State.STARTING
                        == ContainerRunMojo.this.localContainer.getState()))
                {
                    ContainerRunMojo.this.localContainer.stop();
                }
            }
        });

        this.localContainer = (LocalContainer) container;
        addAutoDeployDeployable(this.localContainer);
        try
        {
            executeLocalContainerAction();
            waitDeployableMonitor(localContainer, true);
        }
        catch (Throwable t)
        {
            getLog().error("Starting container [" + this.localContainer + "] failed", t);
        }

        getLog().info("Press Ctrl-C to stop the container...");
        ContainerUtils.waitTillContainerIsStopped(this.localContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected org.codehaus.cargo.container.Container createNewContainer()
        throws MojoExecutionException
    {
        Container containerElement = getContainerElement();
        if (containerId != null)
        {
            if (containerElement == null)
            {
                containerElement = new Container();
                setContainerElement(containerElement);
            }

            containerElement.setContainerId(containerId);
            containerElement.setType(ContainerType.INSTALLED);
            containerElement.setHome(null);
        }

        if (containerUrl != null)
        {
            if (containerElement == null)
            {
                throw new MojoExecutionException("If containerUrl is specified alone, an "
                    + "associated <container> element must also be defined in the configuration. "
                    + "Alternatively, you can also define a containerId.");
            }
            containerElement.setType(ContainerType.INSTALLED);
            containerElement.setHome(null);

            ZipUrlInstaller zipUrlInstaller = containerElement.getZipUrlInstaller();
            if (zipUrlInstaller == null)
            {
                zipUrlInstaller = new ZipUrlInstaller();
                containerElement.setZipUrlInstaller(zipUrlInstaller);
            }

            try
            {
                URL url = new URL(containerUrl);
                zipUrlInstaller.setUrl(url);
            }
            catch (MalformedURLException e)
            {
                throw new MojoExecutionException("Invalid containerUrl", e);
            }
            zipUrlInstaller.setExtractDir(null);
        }

        return super.createNewContainer();
    }
}
