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
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;

/**
 * Installs a container into a given directory. If the container is already installed nothing
 * happens. The container is defined using the <code>&lt;container&gt;</code> element as described
 * in the <a href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">Cargo
 * Maven 3 plugin reference guide</a>.
 */
@Mojo(name = "install", requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class ContainerInstallMojo extends AbstractCargoMojo
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        // Creating the container not only instantiate the container and its configuration but it
        // also installs it if required.
        Container container = createContainer();

        if (container.getType() == ContainerType.INSTALLED)
        {
            getLog().info("Container is installed at ["
                + ((InstalledLocalContainer) container).getHome());
        }
        else
        {
            getLog().warn("Only installed containers types can be installed. You have specified a "
                + "[" + container.getType().getType() + "] one.");
        }
    }
}
