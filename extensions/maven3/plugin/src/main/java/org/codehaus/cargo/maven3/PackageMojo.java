/*
 * ========================================================================
 *
 * Codehaus Cargo, copyright 2004-2011 Vincent Massol, 2012-2026 Ali Tokmen.
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
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.maven3.configuration.Packager;
import org.codehaus.cargo.util.DefaultFileHandler;
import org.codehaus.cargo.util.FileHandler;

/**
 * Package a container distribution, a Configuration and deployed deployables. See
 * {@link org.codehaus.cargo.container.packager.Packager}.
 */
@Mojo(name = "package", requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class PackageMojo extends AbstractCargoMojo
{
    /**
     * @see #getPackagerElement()
     */
    @Parameter
    private Packager packager;

    /**
     * @return the user configuration of a Cargo
     * {@link org.codehaus.cargo.container.packager.Packager}. See the <a
     * href="https://codehaus-cargo.github.io/cargo/Maven3+Plugin+Reference+Guide.html">Cargo
     * Maven 3 plugin reference guide</a> and
     * {@link org.codehaus.cargo.maven3.configuration.Packager} for more details.
     */
    protected Packager getPackagerElement()
    {
        return this.packager;
    }

    /**
     * @param packagerElement the {@link org.codehaus.cargo.container.packager.Packager}
     * configuration defined by the user
     * @see #getPackagerElement()
     */
    protected void setPackagerElement(Packager packagerElement)
    {
        this.packager = packagerElement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute() throws MojoExecutionException
    {
        Container container = createContainer();

        if (container.getType() != ContainerType.INSTALLED)
        {
            throw new MojoExecutionException("Only installed local containers can be packaged");
        }

        InstalledLocalContainer installedContainer = (InstalledLocalContainer) container;

        // Note: The configuration must have been generated at this point. In the future we might
        // want to raise a warning if it hasn't been.

        createPackager(installedContainer).packageContainer(installedContainer);
    }

    /**
     * Create the packager.
     * @param container Container to use.
     * @return Packager for the container and the Maven 3 packager element.
     * @throws MojoExecutionException If anything goes wrong.
     */
    protected org.codehaus.cargo.container.packager.Packager createPackager(
        org.codehaus.cargo.container.Container container) throws MojoExecutionException
    {
        // If no packager output location has been specified, set up a default location in the
        // build directory.
        if (getPackagerElement() == null)
        {
            setPackagerElement(new Packager());
        }

        if (getPackagerElement().getOutputLocation() == null)
        {
            FileHandler fileHandler = new DefaultFileHandler();
            fileHandler.setLogger(container.getLogger());
            String outputLocation = fileHandler.append(
                getCargoProject().getBuildDirectory(), "package");
            getPackagerElement().setOutputLocation(outputLocation);
        }

        return getPackagerElement().createPackager(container);
    }
}
