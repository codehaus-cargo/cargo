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

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.spi.util.ContainerUtils;
import org.codehaus.cargo.maven2.configuration.Container;
import org.codehaus.cargo.maven2.configuration.ZipUrlInstaller;

/**
 * Start a container using Cargo and wait until user pressed CTRL + C to stop.
 * 
 * @version $Id$
 * @goal run
 * @requiresDependencyResolution test
 */
public class ContainerRunMojo extends AbstractContainerStartMojo
{
    /**
     * Sets the container id.
     * 
     * @parameter expression="${containerId}"
     */
    private String containerId;

    /**
     * Sets the container download URL.
     * 
     * @parameter expression="${containerUrl}"
     */
    private String containerUrl;

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#doExecute()
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        super.doExecute();

        getLog().info("Press Ctrl-C to stop the container...");
        ContainerUtils.waitTillContainerIsStopped(this.localContainer);
    }

    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#createNewContainer()
     */
    @Override
    protected org.codehaus.cargo.container.Container createNewContainer()
        throws MojoExecutionException
    {
        if ((containerId != null && containerUrl == null))
        {
            throw new MojoExecutionException(
                "If you specify a containerId, you also need to specify a containerUrl.");
        }

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
