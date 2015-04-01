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

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.LocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;

/**
 * Mojo to create a local container standalone configuration at a specified directory.
 * 
 * @goal configure
 * @requiresDependencyResolution test
 * @description Create a local container standalone configuration at a specified directory
 */
public class ConfigureMojo extends AbstractCargoMojo
{
    /**
     * {@inheritDoc}
     * @see org.codehaus.cargo.maven2.AbstractCargoMojo#doExecute()
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        Container container = createContainer();

        // Check if the container is a local container
        if (!container.getType().isLocal())
        {
            throw new MojoExecutionException("The configure goal can only be called on "
                + "a local container. Your container [" + container.getId() + "] is a ["
                + container.getType().getType() + "] container.");
        }

        LocalContainer localContainer = (LocalContainer) container;

        // Check if the configuration is a standalone local configuration
        if (localContainer.getConfiguration().getType() != ConfigurationType.STANDALONE)
        {
            throw new MojoExecutionException("The configure goal can only be called on "
                + "a standalone local configuration. Your configuration ["
                + localContainer.getConfiguration().toString() + "] is a ["
                + localContainer.getConfiguration().getType().getType() + "] configuration.");
        }

        localContainer.getConfiguration().configure(localContainer);

        getLog().info("Configuration created at [" + localContainer.getConfiguration().getHome()
            + "]");
    }
}
